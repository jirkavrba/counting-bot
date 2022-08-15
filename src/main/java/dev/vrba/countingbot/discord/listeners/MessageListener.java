package dev.vrba.countingbot.discord.listeners;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import dev.vrba.countingbot.discord.DiscordEventListener;
import dev.vrba.countingbot.repository.ChannelsRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@AllArgsConstructor
public class MessageListener implements DiscordEventListener {

    private final ChannelsRepository repository;

    @NonNull
    @Override
    public Mono<Void> register(@NonNull GatewayDiscordClient client) {
        return client
                .on(MessageCreateEvent.class)
                // Message was sent in a guild
                .filter(event -> event.getGuildId().isPresent())
                // Message was sent inside a tracked channel
                .filterWhen(event -> repository.isTrackedChannel(event.getMessage().getChannelId().asLong()))
                // Replace this with checking for numbers, evaluation expressions and reacting with appropriate emoji
                .flatMap(event ->
                        this.evaluate(event.getMessage().getContent())
                                .map(evaluation -> this.processNumber(event, evaluation))
                                .orElseGet(Mono::empty))
                .then();
    }

    /**
     * Evaluate the provided input (or return an empty optional if the input is not valid)
     *
     * @param input Input obtained from a
     * @return Parsed integer after evaluation or empty optional
     */
    private Optional<Long> evaluate(@NonNull String input) {
        if (!input.matches("[\\d \\-+/*()]+")) {
            return Optional.empty();
        }

        try {
            final var evaluation = new DoubleEvaluator().evaluate(input);

            if (evaluation.isInfinite() || evaluation.isNaN()) {
                return Optional.empty();
            }

            return Optional.of(evaluation.longValue());
        }
        // An invalid expression was provided
        catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Mono<Void> processNumber(@NonNull MessageCreateEvent event, long number) {
        final var channel = event.getMessage().getChannelId().asLong();

        return this.repository.getCurrentCount(channel)
                .filter(count -> count + 1 == number)
                // The number was correct
                .flatMap(count -> this.handleCorrectNumber(event, channel))
                // The number was not correct
                // TODO: Method is called even after the correct number handler is invoked for some reason
                .switchIfEmpty(this.handleIncorrectNumber(event, channel));
    }

    private Mono<Void> handleCorrectNumber(@NonNull MessageCreateEvent event, long channel) {
        return this.repository.incrementCount(channel)
                .and(event.getMessage().addReaction(ReactionEmoji.unicode("✅")));
    }

    private Mono<Void> handleIncorrectNumber(@NonNull MessageCreateEvent event, long channel) {
        return this.repository.resetCount(channel)
                .and(event.getMessage().addReaction(ReactionEmoji.unicode("❌")));
    }
}

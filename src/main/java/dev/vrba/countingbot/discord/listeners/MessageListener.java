package dev.vrba.countingbot.discord.listeners;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import dev.vrba.countingbot.discord.DiscordEventListener;
import dev.vrba.countingbot.repository.ChannelsRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@AllArgsConstructor
public class MessageListener implements DiscordEventListener {

    private final ChannelsRepository repository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        final var user = event.getMessage().getUserData().id().asLong();

        return this.repository.getCurrentCount(channel)
                // If the same user did not send the last counted message
                .filterWhen(count -> this.repository.getLastUser(channel).map(last -> last != user))
                // The number was correct
                .flatMap(count -> {
                    // If the number is a valid successor to the last tracked count
                    final var valid = count + 1 == number;

                    return valid
                            ? this.handleCorrectNumber(event, channel, user)
                            : this.handleIncorrectNumber(event, channel, user, count);
                })
                .onErrorResume(exception -> {
                    this.logger.error("Exception was thrown in the message event listener", exception);
                    return Mono.empty();
                });
    }

    private Mono<Void> handleCorrectNumber(@NonNull MessageCreateEvent event, long channel, long user) {
        return this.repository.incrementCount(channel)
                .and(this.repository.setLastUser(channel, user))
                .and(event.getMessage().addReaction(ReactionEmoji.unicode("✔")));
    }

    private Mono<Void> handleIncorrectNumber(@NonNull MessageCreateEvent event, long channel, long user, long number) {
        return this.repository.resetCount(channel)
                .and(event.getMessage().addReaction(ReactionEmoji.unicode("❌")))
                .and(event.getMessage().getChannel()
                        .cast(TextChannel.class)
                        .flatMap(textChannel -> textChannel.createMessage(
                            "<@" + user + "> fucked it up at **" + number + "** lmao \uD83E\uDD21"
                        ))
                );
    }
}

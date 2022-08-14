package dev.vrba.countingbot.discord.listeners;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import dev.vrba.countingbot.discord.DiscordEventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class MessageListener implements DiscordEventListener {

    @NonNull
    @Override
    public Mono<Void> register(@NonNull GatewayDiscordClient client) {
        return client
                .on(MessageCreateEvent.class)
                // Message was sent in a guild
                .filter(event -> event.getGuildId().isPresent())
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
     * @return Parsed integer after evaluation or empty optiona
     */
    private Optional<Integer> evaluate(@NonNull String input) {
        if (!input.matches("[\\d \\-+/*()]+")) {
            return Optional.empty();
        }

        try {
            return Optional.of(new DoubleEvaluator().evaluate(input).intValue());
        }
        // An invalid expression was provided
        catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private Mono<Void> processNumber(@NonNull MessageCreateEvent event, int number) {
        return event.getMessage().addReaction(ReactionEmoji.unicode("\uD83D\uDDFF"));
    }
}

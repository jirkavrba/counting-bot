package dev.vrba.countingbot.discord.listeners;

import dev.vrba.countingbot.discord.DiscordEventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
                .flatMap(event -> event.getMessage().addReaction(ReactionEmoji.unicode("\uD83D\uDDFF")))
                .then();
    }

}

package dev.vrba.countingbot.discord;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface DiscordSlashCommand {

    @NonNull
    ApplicationCommandRequest define();

    @NonNull
    Mono<Void> handle(@NonNull ApplicationCommandInteractionEvent event);

}
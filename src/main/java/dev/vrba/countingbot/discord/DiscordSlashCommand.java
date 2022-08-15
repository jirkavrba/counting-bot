package dev.vrba.countingbot.discord;

import discord4j.core.event.domain.command.ApplicationCommandEvent;
import discord4j.discordjson.json.gateway.ApplicationCommandCreate;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface DiscordSlashCommand {

    @NonNull
    ApplicationCommandCreate define();

    @NonNull
    Mono<Void> handle(@NonNull ApplicationCommandEvent event);

}
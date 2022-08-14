package dev.vrba.countingbot.discord;

import discord4j.core.GatewayDiscordClient;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface DiscordEventListener {

    @NonNull
    Mono<Void> register(@NonNull GatewayDiscordClient client);

}

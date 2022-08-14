package dev.vrba.countingbot.discord;

import dev.vrba.countingbot.configuration.DiscordConfiguration;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.util.AllowedMentions;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class DiscordBot implements CommandLineRunner {

    private final DiscordConfiguration configuration;

    @Override
    public void run(String... args) throws Exception {
        // TODO: Check MESSAGE_CONTENT intent
        DiscordClientBuilder.create(configuration.token())
                .setDefaultAllowedMentions(AllowedMentions.suppressAll())
                .build()
                .gateway()
                .setEnabledIntents(IntentSet.of(Intent.GUILD_MESSAGES))
                .login()
                .flatMap(client ->
                    updatePresence(client)
                        .and(registerEventListeners(client))
                        .and(registerSlashCommands(client))
                )
                .block();

    }

    @NonNull
    private Mono<Void> updatePresence(@NonNull GatewayDiscordClient client) {
        return client.updatePresence(ClientPresence.online(ClientActivity.playing("with numbers")));
    }

    private Mono<Void> registerSlashCommands(@NonNull GatewayDiscordClient client) {
        return Mono.empty();
    }

    private Mono<Void> registerEventListeners(@NonNull GatewayDiscordClient client) {
        return Mono.empty();
    }
}

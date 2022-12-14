package dev.vrba.countingbot.discord.commands;

import dev.vrba.countingbot.discord.DiscordSlashCommand;
import dev.vrba.countingbot.repository.ChannelsRepository;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class StartCommand implements DiscordSlashCommand {

    private final ChannelsRepository repository;

    @NonNull
    @Override
    public ApplicationCommandRequest define() {
        return ApplicationCommandRequest.builder()
                .name("start-counting")
                .description("Makes the bot track this channel for count messages")
                .defaultPermission(true)
                .build();
    }

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull ApplicationCommandInteractionEvent event) {
        return event.getInteraction()
                .getChannel()
                .flatMap(channel ->
                        this.repository.resetCount(channel.getId().asLong()).and(
                                event.reply().withEmbeds(
                                        EmbedCreateSpec.builder()
                                                .title("Let's go!")
                                                .description("you can start counting in this channel!")
                                                .color(Color.MEDIUM_SEA_GREEN)
                                                .build()
                                )
                        )
                )
                .then();
    }
}

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
public class CurrentCommand implements DiscordSlashCommand {

    private final ChannelsRepository repository;

    @NonNull
    @Override
    public ApplicationCommandRequest define() {
        return ApplicationCommandRequest.builder()
                .name("current-count")
                .description("Provides information about the count state in this channel")
                .defaultPermission(true)
                .build();
    }

    @NonNull
    @Override
    public Mono<Void> handle(@NonNull ApplicationCommandInteractionEvent event) {
        return event.getInteraction()
                .getChannel()
                .flatMap(channel ->
                        this.repository.getCurrentCount(channel.getId().asLong())
                                .zipWith(this.repository.getLastUser(channel.getId().asLong()))
                                .flatMap(tuple -> {
                                    final var count = tuple.getT1();
                                    final var user = tuple.getT2();

                                    return event.reply().withEmbeds(EmbedCreateSpec.builder()
                                            .color(Color.ENDEAVOUR)
                                            .addField("Current count value", count.toString(), false)
                                            .addField("Last message was sent by", user == 0L ? "Nobody" : "<@" + user + ">", false)
                                            .build());
                                })
                )
                .then();
    }
}

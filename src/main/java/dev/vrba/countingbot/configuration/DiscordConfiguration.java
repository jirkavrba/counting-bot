package dev.vrba.countingbot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.lang.NonNull;

@ConstructorBinding
@ConfigurationProperties(prefix = "discord")
public record DiscordConfiguration(@NonNull String token) {}

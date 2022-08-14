package dev.vrba.countingbot;

import dev.vrba.countingbot.configuration.DiscordConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DiscordConfiguration.class)
public class CountingBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CountingBotApplication.class, args);
    }

}

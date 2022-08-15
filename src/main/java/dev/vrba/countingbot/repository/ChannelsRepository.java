package dev.vrba.countingbot.repository;

import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@AllArgsConstructor
public class ChannelsRepository {

    private final ReactiveRedisOperations<String, String> operations;

    /**
     * Check if the provided channel id belongs to a tracked channel
     *
     * @param channel ID of the channel to be checked
     * @return boolean mono indicating if the channel is tracked
     */
    public Mono<Boolean> isTrackedChannel(long channel) {
        // Check that the channel key is present in the redis key
        return this.operations.opsForValue()
                .get(countKey(channel))
                .map(value -> true)
                .defaultIfEmpty(false);
    }

    /**
     * Reset count for the provided channel
     *
     * @param channel ID of the channel that the count should be reset for
     * @return Current count of the guild (always zero)
     */
    public Mono<Long> resetCount(long channel) {
        final var ops = this.operations.opsForValue();

        return ops.set(countKey(channel), "0")
                .and(ops.set(userKey(channel), "0"))
                .map(output -> 0L);
    }

    /**
     * Increment the current count value for the provided channel
     *
     * @param channel ID of the channel that the count should be incremented for
     * @return Current count after the increment
     */
    public Mono<Long> incrementCount(long channel) {
        return this.operations.opsForValue()
                .increment(countKey(channel));
    }

    /**
     * @param channel ID of the channel that the count message has been sent to
     * @return Current number that should be counted withing the tracked channel
     */
    public Mono<Long> getCurrentCount(long channel) {
        return this.operations.opsForValue()
                .get(countKey(channel))
                .map(Long::valueOf);
    }

    /**
     * @param channel ID of the channel that the last user id should be returned
     * @return ID of the user that submitted the last number to the provided channel
     */
    public Mono<Long> getLastUser(long channel) {
        return this.operations.opsForValue()
                .get(userKey(channel))
                .map(Long::valueOf);
    }

    /**
     * @param channel ID of the channel that the last sender should be set to
     * @param user ID of the last sender that should be stored for the given channel
     */
    public Mono<Void> setLastUser(long channel, long user) {
        return this.operations.opsForValue()
                .set(userKey(channel), String.valueOf(user))
                .then();
    }

    @NonNull
    private String countKey(long channel) {
        return "channel:" + channel + ":count";
    }

    @NonNull
    private String userKey(long channel) {
        return "channel:"  + channel + ":user";
    }
}

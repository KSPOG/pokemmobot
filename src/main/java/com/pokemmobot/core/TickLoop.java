package com.pokemmobot.core;

import java.time.Duration;

public class TickLoop {
    private final Duration tickDuration;

    public TickLoop(Duration tickDuration) {
        this.tickDuration = tickDuration;
    }

    public void run(long maxTicks, TickHandler tickHandler) throws InterruptedException {
        for (long tick = 1; tick <= maxTicks; tick++) {
            tickHandler.onTick(tick);
            Thread.sleep(tickDuration.toMillis());
        }
    }

    @FunctionalInterface
    public interface TickHandler {
        void onTick(long tick);
    }
}

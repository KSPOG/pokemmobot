package com.pokemmobot.actions;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

public class ActionQueue {
    private final Queue<ActionType> queue = new ArrayDeque<>();
    private final Duration cooldown;
    private Instant lastDispatch = Instant.EPOCH;

    public ActionQueue(Duration cooldown) {
        this.cooldown = cooldown;
    }

    public void enqueue(ActionType actionType) {
        queue.offer(actionType);
    }

    public Optional<ActionType> pollReady() {
        Instant now = Instant.now();
        if (now.isBefore(lastDispatch.plus(cooldown))) {
            return Optional.empty();
        }

        ActionType action = queue.poll();
        if (action != null) {
            lastDispatch = now;
        }
        return Optional.ofNullable(action);
    }

    public int size() {
        return queue.size();
    }
}

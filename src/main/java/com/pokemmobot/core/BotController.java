package com.pokemmobot.core;

import com.pokemmobot.actions.ActionQueue;
import com.pokemmobot.actions.InputDriver;
import com.pokemmobot.microbots.Microbot;
import com.pokemmobot.model.GameSnapshot;

public class BotController {
    private final Microbot microbot;
    private final ActionQueue actionQueue;
    private final InputDriver inputDriver;
    private BotState state = BotState.IDLE;

    public BotController(Microbot microbot, ActionQueue actionQueue, InputDriver inputDriver) {
        this.microbot = microbot;
        this.actionQueue = actionQueue;
        this.inputDriver = inputDriver;
    }

    public void onTick(GameSnapshot snapshot) {
        state = resolveState(snapshot);

        if (microbot.canRun(snapshot)) {
            microbot.tick(snapshot, actionQueue);
        }

        actionQueue.pollReady().ifPresent(inputDriver::send);
        System.out.printf("[tick=%d] state=%s queue=%d%n", snapshot.tick(), state, actionQueue.size());
    }

    private BotState resolveState(GameSnapshot snapshot) {
        if (snapshot.lowHealth()) {
            return BotState.HEAL;
        }
        if (snapshot.battleActive()) {
            return BotState.BATTLE;
        }
        if (snapshot.movementAvailable()) {
            return BotState.MOVE;
        }
        return BotState.IDLE;
    }
}

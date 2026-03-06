package com.pokemmobot.microbots;

import com.pokemmobot.actions.ActionQueue;
import com.pokemmobot.actions.ActionType;
import com.pokemmobot.model.GameSnapshot;

public class BasicEncounterMicrobot implements Microbot {
    @Override
    public String name() {
        return "basic-encounter";
    }

    @Override
    public boolean canRun(GameSnapshot snapshot) {
        return true;
    }

    @Override
    public void tick(GameSnapshot snapshot, ActionQueue actionQueue) {
        if (snapshot.lowHealth()) {
            actionQueue.enqueue(ActionType.OPEN_MENU);
            actionQueue.enqueue(ActionType.USE_HEALING_ITEM);
            return;
        }

        if (snapshot.battleActive()) {
            actionQueue.enqueue(ActionType.RUN);
            return;
        }

        if (snapshot.movementAvailable()) {
            actionQueue.enqueue(ActionType.MOVE_UP);
        }
    }
}

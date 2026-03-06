package com.pokemmobot.microbots;

import com.pokemmobot.actions.ActionQueue;
import com.pokemmobot.model.GameSnapshot;

public interface Microbot {
    String name();

    boolean canRun(GameSnapshot snapshot);

    void tick(GameSnapshot snapshot, ActionQueue actionQueue);
}

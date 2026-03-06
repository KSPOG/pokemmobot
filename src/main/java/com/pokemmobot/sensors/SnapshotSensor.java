package com.pokemmobot.sensors;

import com.pokemmobot.model.GameSnapshot;

public interface SnapshotSensor {
    GameSnapshot readSnapshot(long tick);
}

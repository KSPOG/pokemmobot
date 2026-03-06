package com.pokemmobot.sensors;

import com.pokemmobot.model.GameSnapshot;

public class WindowCaptureSensor implements SnapshotSensor {
    @Override
    public GameSnapshot readSnapshot(long tick) {
        // TODO: Replace mock values with real frame/OCR/UI analysis.
        boolean battle = tick % 9 == 0;
        return new GameSnapshot(
                tick,
                !battle,
                battle,
                tick % 21 == 0,
                tick % 15 == 0
        );
    }
}

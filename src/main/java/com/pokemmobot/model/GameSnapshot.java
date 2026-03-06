package com.pokemmobot.model;

public record GameSnapshot(
        long tick,
        boolean movementAvailable,
        boolean battleActive,
        boolean lowHealth,
        boolean lowPowerPoints
) {
}

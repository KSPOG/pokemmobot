package com.pokemmobot;

import com.pokemmobot.actions.ActionQueue;
import com.pokemmobot.actions.InputDriver;
import com.pokemmobot.core.BotController;
import com.pokemmobot.core.TickLoop;
import com.pokemmobot.microbots.BasicEncounterMicrobot;
import com.pokemmobot.sensors.SnapshotSensor;
import com.pokemmobot.sensors.WindowCaptureSensor;

import java.time.Duration;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        SnapshotSensor sensor = new WindowCaptureSensor();
        ActionQueue actionQueue = new ActionQueue(Duration.ofMillis(250));
        InputDriver inputDriver = new InputDriver();

        BotController controller = new BotController(new BasicEncounterMicrobot(), actionQueue, inputDriver);
        TickLoop tickLoop = new TickLoop(Duration.ofMillis(200));

        // Demo run; replace with continuous loop and emergency hotkey handling.
        tickLoop.run(30, tick -> controller.onTick(sensor.readSnapshot(tick)));
    }
}

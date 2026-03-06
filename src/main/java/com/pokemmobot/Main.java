package com.pokemmobot;

import com.pokemmobot.actions.ActionQueue;
import com.pokemmobot.actions.InputDriver;

import com.pokemmobot.client.PokeMMOClientLauncher;

import com.pokemmobot.client.PokeMMOClientLauncher;


import com.pokemmobot.core.BotController;
import com.pokemmobot.core.TickLoop;
import com.pokemmobot.microbots.BasicEncounterMicrobot;
import com.pokemmobot.sensors.SnapshotSensor;
import com.pokemmobot.sensors.WindowCaptureSensor;


import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        AppConfig config = AppConfig.fromArgs(args);

        if (config.launchClient()) {
            PokeMMOClientLauncher launcher = new PokeMMOClientLauncher();
            launcher.launch(config.clientWorkingDirectory(), config.clientPath());
            Thread.sleep(config.clientWaitMs());
        }

        SnapshotSensor sensor = new WindowCaptureSensor();
        ActionQueue actionQueue = new ActionQueue(Duration.ofMillis(config.actionCooldownMs()));
        InputDriver inputDriver = new InputDriver();

        BotController controller = new BotController(new BasicEncounterMicrobot(), actionQueue, inputDriver);
        TickLoop tickLoop = new TickLoop(Duration.ofMillis(config.tickMs()));

        System.out.printf(
                "Starting bot: ticks=%d tickMs=%d cooldownMs=%d launchClient=%s%n",
                config.maxTicks(),
                config.tickMs(),
                config.actionCooldownMs(),
                config.launchClient()
        );

        tickLoop.run(config.maxTicks(), tick -> controller.onTick(sensor.readSnapshot(tick)));
    }

    private record AppConfig(
            long maxTicks,
            long tickMs,
            long actionCooldownMs,
            boolean launchClient,
            String clientPath,
            Path clientWorkingDirectory,
            long clientWaitMs
    ) {
        static AppConfig fromArgs(String[] args) {
            long maxTicks = 30;
            long tickMs = 200;
            long actionCooldownMs = 250;
            boolean launchClient = false;
            String clientPath = defaultClientPath();
            Path clientWorkingDirectory = Path.of(".").toAbsolutePath().normalize();
            long clientWaitMs = 10_000;

            for (String arg : args) {
                if (arg.startsWith("--ticks=")) {
                    maxTicks = parsePositiveLong(arg, "--ticks=");
                } else if (arg.startsWith("--tick-ms=")) {
                    tickMs = parsePositiveLong(arg, "--tick-ms=");
                } else if (arg.startsWith("--cooldown-ms=")) {
                    actionCooldownMs = parsePositiveLong(arg, "--cooldown-ms=");
                } else if (arg.equals("--launch-client")) {
                    launchClient = true;
                } else if (arg.startsWith("--client-path=")) {
                    clientPath = parseNonEmpty(arg, "--client-path=");
                } else if (arg.startsWith("--client-workdir=")) {
                    clientWorkingDirectory = Path.of(parseNonEmpty(arg, "--client-workdir="));
                } else if (arg.startsWith("--client-wait-ms=")) {
                    clientWaitMs = parsePositiveLong(arg, "--client-wait-ms=");
                } else {
                    throw new IllegalArgumentException("Unknown argument: " + arg);
                }
            }

            return new AppConfig(
                    maxTicks,
                    tickMs,
                    actionCooldownMs,
                    launchClient,
                    clientPath,
                    clientWorkingDirectory,
                    clientWaitMs
            );
        }

        private static String defaultClientPath() {
            String os = System.getProperty("os.name", "").toLowerCase();
            return os.contains("win") ? "PokeMMO.exe" : "./PokeMMO.sh";
        }

        private static String parseNonEmpty(String arg, String prefix) {
            String value = arg.substring(prefix.length()).trim();
            if (value.isEmpty()) {
                throw new IllegalArgumentException(prefix + " cannot be empty");
            }
            return value;
        }

        private static long parsePositiveLong(String arg, String prefix) {
            String value = arg.substring(prefix.length());
            long parsed = Long.parseLong(value);
            if (parsed <= 0) {
                throw new IllegalArgumentException(prefix + " must be > 0, got: " + parsed);
            }
            return parsed;
        }


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

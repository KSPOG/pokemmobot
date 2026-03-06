package com.pokemmobot.client.launcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PokeMMOClientLauncher {
    public Process launch(Path workingDirectory, String clientPath) throws IOException {
        return launch(workingDirectory, clientPath, null);
    }

    public Process launch(Path workingDirectory, String clientPath, Consumer<String> outputConsumer) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        List<String> command = buildCommand(os, clientPath);

        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true);

        System.out.printf("Launching PokeMMO client: command=%s cwd=%s%n", command, workingDirectory);
        Process process = builder.start();
        if (outputConsumer != null) {
            streamOutput(process.getInputStream(), outputConsumer);
        }
        return process;
    }

    private void streamOutput(InputStream stream, Consumer<String> outputConsumer) {
        Thread outputThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputConsumer.accept(line);
                }
            } catch (IOException ex) {
                outputConsumer.accept("[launcher] Failed reading client output: " + ex.getMessage());
            }
        }, "pokemmo-client-output");
        outputThread.setDaemon(true);
        outputThread.start();
    }

    private List<String> buildCommand(String os, String clientPath) {
        List<String> command = new ArrayList<>();
        if (os.contains("win")) {
            command.add(clientPath);
            return command;
        }

        if (os.contains("mac") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            File script = new File(clientPath);
            if (script.exists() && script.canExecute()) {
                command.add(script.getPath());
            } else {
                command.add("bash");
                command.add(clientPath);
            }
            return command;
        }

        throw new IllegalStateException("Unsupported operating system: " + os);
    }
}

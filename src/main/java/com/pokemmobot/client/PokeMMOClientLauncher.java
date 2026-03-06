package com.pokemmobot.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PokeMMOClientLauncher {
    public Process launch(Path workingDirectory, String clientPath) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase();
        List<String> command = buildCommand(os, clientPath);

        ProcessBuilder builder = new ProcessBuilder(command)
                .directory(workingDirectory.toFile())
                .redirectErrorStream(true)
                .inheritIO();

        System.out.printf("Launching PokeMMO client: command=%s cwd=%s%n", command, workingDirectory);
        return builder.start();
    }

    private List<String> buildCommand(String os, String clientPath) {
        List<String> command = new ArrayList<>();
        if (os.contains("win")) {
            command.add("cmd");
            command.add("/c");
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

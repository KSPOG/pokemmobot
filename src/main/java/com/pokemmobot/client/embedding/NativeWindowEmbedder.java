package com.pokemmobot.client.embedding;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Panel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Consumer;

public class NativeWindowEmbedder {

    public boolean tryEmbed(Process process, Panel targetPanel, Duration timeout, Consumer<String> logger) {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("linux")) {
            return tryEmbedLinux(process, targetPanel, timeout, logger);
        }
        if (os.contains("win")) {
            return tryEmbedWindows(process, targetPanel, timeout, logger);
        }

        logger.accept("Window embedding is currently implemented for Linux/Windows only.");
        return false;
    }

    private boolean tryEmbedLinux(Process process, Panel targetPanel, Duration timeout, Consumer<String> logger) {
        OptionalLong parentWindow = resolveNativeWindowHandle(targetPanel, "getWindow");
        if (parentWindow.isEmpty()) {
            logger.accept("Unable to read native host panel handle for embedding.");
            return false;
        }

        if (!commandExists("xdotool")) {
            logger.accept("Cannot embed on Linux: missing 'xdotool'. Install xdotool to enable in-frame embedding.");
            return false;
        }

        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            Set<Long> candidatePids = descendantPidsLinux(process.pid());
            for (long pid : candidatePids) {
                List<String> windows = runAndCollect(List.of("xdotool", "search", "--all", "--pid", String.valueOf(pid)));
                for (String childWindow : windows) {
                    int code = runAndExitCode(List.of(
                            "xdotool",
                            "windowreparent",
                            childWindow,
                            String.valueOf(parentWindow.getAsLong())
                    ));
                    if (code == 0) {
                        runAndExitCode(List.of("xdotool", "windowsize", childWindow, "100%", "100%"));
                        runAndExitCode(List.of("xdotool", "windowmove", childWindow, "0", "0"));
                        logger.accept("Embedded PokeMMO native window into custom client frame.");
                        return true;
                    }
                }
            }
            sleep(350);
        }

        logger.accept("PokeMMO window was not discovered before embedding timeout.");
        return false;
    }

    private boolean tryEmbedWindows(Process process, Panel targetPanel, Duration timeout, Consumer<String> logger) {
        OptionalLong hostHwnd = resolveNativeWindowHandle(targetPanel, "getHWnd");
        if (hostHwnd.isEmpty()) {
            logger.accept("Unable to read host HWND for embedding.");
            return false;
        }

        if (!commandExists("powershell")) {
            logger.accept("Cannot embed on Windows: powershell not available.");
            return false;
        }

        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            for (long pid : descendantPidsWindows(process.pid())) {
                String script = "$sig='[DllImport(\"user32.dll\")] public static extern IntPtr SetParent(IntPtr c, IntPtr p);';"
                        + "Add-Type -Name Win -Namespace Native -MemberDefinition $sig;"
                        + "$p=Get-Process -Id " + pid + " -ErrorAction SilentlyContinue;"
                        + "if($null -eq $p -or $p.MainWindowHandle -eq 0){ exit 2 };"
                        + "[Native.Win]::SetParent($p.MainWindowHandle, [IntPtr]" + hostHwnd.getAsLong() + ") | Out-Null;"
                        + "exit 0";

                int code = runAndExitCode(List.of("powershell", "-NoProfile", "-Command", script));
                if (code == 0) {
                    logger.accept("Embedded PokeMMO native window into custom client frame.");
                    return true;
                }
            }
            sleep(350);
        }

        logger.accept("PokeMMO window was not discovered before embedding timeout.");
        return false;
    }

    private Set<Long> descendantPidsLinux(long rootPid) {
        Set<Long> result = new LinkedHashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(rootPid);
        result.add(rootPid);

        while (!queue.isEmpty()) {
            long current = queue.removeFirst();
            for (String child : runAndCollect(List.of("bash", "-lc", "ps -o pid= --ppid " + current))) {
                try {
                    long childPid = Long.parseLong(child.trim());
                    if (result.add(childPid)) {
                        queue.addLast(childPid);
                    }
                } catch (NumberFormatException ignored) {
                    // Ignore malformed ps output lines.
                }
            }
        }
        return result;
    }

    private Set<Long> descendantPidsWindows(long rootPid) {
        Set<Long> pids = new LinkedHashSet<>();
        pids.add(rootPid);

        String script = "$root=" + rootPid + ";"
                + "$all=Get-CimInstance Win32_Process | Select-Object ProcessId,ParentProcessId;"
                + "$queue=New-Object System.Collections.Queue;"
                + "$queue.Enqueue($root);"
                + "$seen=New-Object 'System.Collections.Generic.HashSet[int]';"
                + "$seen.Add($root) | Out-Null;"
                + "while($queue.Count -gt 0){"
                + "  $current=$queue.Dequeue();"
                + "  foreach($p in $all){"
                + "    if($p.ParentProcessId -eq $current -and -not $seen.Contains([int]$p.ProcessId)){"
                + "      $seen.Add([int]$p.ProcessId) | Out-Null;"
                + "      $queue.Enqueue([int]$p.ProcessId);"
                + "    }"
                + "  }"
                + "}"
                + "$seen | ForEach-Object { Write-Output $_ }";

        for (String line : runAndCollect(List.of("powershell", "-NoProfile", "-Command", script))) {
            try {
                pids.add(Long.parseLong(line.trim()));
            } catch (NumberFormatException ignored) {
                // Ignore malformed lines.
            }
        }

        return pids;
    }

    private OptionalLong resolveNativeWindowHandle(Component component, String methodName) {
        try {
            SwingUtilities.invokeAndWait(component::addNotify);
            java.lang.reflect.Field peerField = Component.class.getDeclaredField("peer");
            peerField.setAccessible(true);
            Object peer = peerField.get(component);
            if (peer == null) {
                return OptionalLong.empty();
            }
            Method method = peer.getClass().getMethod(methodName);
            method.setAccessible(true);
            Object value = method.invoke(peer);
            if (value instanceof Number number) {
                return OptionalLong.of(number.longValue());
            }
        } catch (Exception ignored) {
            return OptionalLong.empty();
        }
        return OptionalLong.empty();
    }

    private boolean commandExists(String name) {
        int code = runAndExitCode(List.of("bash", "-lc", "command -v " + name + " >/dev/null 2>&1"));
        return code == 0;
    }

    private List<String> runAndCollect(List<String> command) {
        List<String> lines = new ArrayList<>();
        ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        lines.add(line.trim());
                    }
                }
            }
            process.waitFor();
        } catch (Exception ignored) {
            return List.of();
        }
        return lines;
    }

    private int runAndExitCode(List<String> command) {
        ProcessBuilder pb = new ProcessBuilder(command).redirectErrorStream(true);
        try {
            Process process = pb.start();
            process.getInputStream().transferTo(java.io.OutputStream.nullOutputStream());
            return process.waitFor();
        } catch (IOException ex) {
            return -1;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}

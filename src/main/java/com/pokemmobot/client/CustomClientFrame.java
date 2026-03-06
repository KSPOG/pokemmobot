package com.pokemmobot.client;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CustomClientFrame extends JFrame {
    private final PokeMMOClientLauncher launcher;
    private final Path clientWorkingDirectory;
    private final String clientPath;
    private final long clientWaitMs;
    private final JTextArea eventLog = new JTextArea();
    private final JButton launchButton = new JButton("Launch PokeMMO", null);
    private final JLabel statusBadge = new JLabel("IDLE");
    private final Panel nativeClientHost = new Panel(new BorderLayout());
    private final NativeWindowEmbedder windowEmbedder = new NativeWindowEmbedder();
    private volatile Process clientProcess;

    public CustomClientFrame(PokeMMOClientLauncher launcher, Path clientWorkingDirectory, String clientPath, long clientWaitMs) {
        super("PokeMMO Microbot Hub");
        this.launcher = launcher;
        this.clientWorkingDirectory = clientWorkingDirectory;
        this.clientPath = clientPath;
        this.clientWaitMs = clientWaitMs;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1360, 820));
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(22, 24, 28));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildBottomLog(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        top.setBackground(new Color(32, 35, 41));

        JLabel title = new JLabel("Microbot Hub  •  PokeMMO Client Host");
        title.setForeground(new Color(230, 232, 236));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        top.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        statusBadge.setForeground(new Color(133, 250, 170));
        statusBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(95, 150, 110)),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        launchButton.addActionListener(this::onLaunchClient);
        controls.add(statusBadge);
        controls.add(launchButton);
        top.add(controls, BorderLayout.EAST);
        return top;
    }

    private JSplitPane buildMainContent() {
        JSplitPane outer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildLeftRail(), buildCenterAndSidebar());
        outer.setResizeWeight(0.12);
        outer.setBorder(BorderFactory.createEmptyBorder());
        return outer;
    }

    private JPanel buildLeftRail() {
        JPanel rail = new JPanel();
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));
        rail.setBackground(new Color(27, 30, 35));
        rail.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        rail.setPreferredSize(new Dimension(180, 0));

        rail.add(navButton("Dashboard"));
        rail.add(Box.createVerticalStrut(8));
        rail.add(navButton("Plugins"));
        rail.add(Box.createVerticalStrut(8));
        rail.add(navButton("Scripts"));
        rail.add(Box.createVerticalStrut(8));
        rail.add(navButton("Profiles"));
        rail.add(Box.createVerticalStrut(8));
        rail.add(navButton("Live Console"));
        rail.add(Box.createVerticalGlue());

        JLabel foot = new JLabel("PokeMMO Target");
        foot.setForeground(new Color(140, 145, 155));
        rail.add(foot);
        return rail;
    }

    private Component navButton(String label) {
        JButton button = new JButton(label);
        button.setMaximumSize(new Dimension(160, 34));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        return button;
    }

    private JSplitPane buildCenterAndSidebar() {
        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(new Color(18, 20, 24));
        center.add(buildClientViewport(), BorderLayout.CENTER);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(370, 0));
        sidebar.setBackground(new Color(30, 33, 38));

        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
        tabs.addTab("Installed", createInstalledPanel());
        tabs.addTab("Plugin Hub", createPluginHubPanel());
        sidebar.add(tabs, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, center, sidebar);
        splitPane.setResizeWeight(0.76);
        return splitPane;
    }

    private JPanel buildClientViewport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Client Frame"));

        JTextArea hint = new JTextArea(
                "This frame hosts the live PokeMMO window.\n" +
                "Press Launch PokeMMO to start and embed the native client into this region."
        );
        hint.setEditable(false);
        hint.setBackground(new Color(18, 20, 24));
        hint.setForeground(new Color(180, 186, 198));
        hint.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        nativeClientHost.setBackground(Color.BLACK);
        nativeClientHost.add(hint, BorderLayout.NORTH);

        panel.add(nativeClientHost, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createInstalledPanel() {
        DefaultListModel<PluginScriptItem> model = new DefaultListModel<>();
        PluginScriptRegistry.defaults().forEach(model::addElement);

        JList<PluginScriptItem> list = new JList<>(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                PluginScriptItem item = (PluginScriptItem) value;
                String state = item.enabled() ? "ENABLED" : "DISABLED";
                String label = String.format("%s  •  %s\n%s\n[%s]", item.name(), item.category(), item.description(), state);
                JTextArea area = new JTextArea(label);
                area.setLineWrap(true);
                area.setWrapStyleWord(true);
                area.setEditable(false);
                area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
                area.setBackground(isSelected ? new Color(67, 74, 90) : new Color(42, 45, 52));
                area.setForeground(new Color(236, 238, 242));
                return area;
            }
        });
        list.setFixedCellHeight(74);
        return new JScrollPane(list);
    }

    private JScrollPane createPluginHubPanel() {
        JTextArea hub = new JTextArea();
        hub.setEditable(false);
        hub.setText(
                "Plugin Hub (Microbot-style)\n\n" +
                "- Route Walker / Profile Paths\n" +
                "- Encounter Filters / Species Targeting\n" +
                "- Heal + Return Templates\n" +
                "- Safety Break Profiles\n" +
                "- Runtime Metrics Overlay\n\n" +
                "Next: add install/update/version controls here with one-click enable/disable."
        );
        return new JScrollPane(hub);
    }

    private JScrollPane buildBottomLog() {
        eventLog.setEditable(false);
        eventLog.setRows(7);
        eventLog.setBorder(BorderFactory.createTitledBorder("Live Event Log"));
        return new JScrollPane(eventLog);
    }

    private void onLaunchClient(ActionEvent ignored) {
        if (clientProcess != null && clientProcess.isAlive()) {
            log("Client is already running in this session.");
            return;
        }

        new Thread(() -> {
            try {
                setLaunchButtonEnabled(false);
                setStatus("STARTING");
                log("Launching client: " + clientPath);

                clientProcess = launcher.launch(clientWorkingDirectory, clientPath, line -> log("[client] " + line));
                Thread.sleep(clientWaitMs);

                boolean embedded = windowEmbedder.tryEmbed(
                        clientProcess,
                        nativeClientHost,
                        Duration.ofSeconds(15),
                        this::log
                );

                if (embedded) {
                    setStatus("EMBEDDED");
                } else {
                    setStatus("DETACHED");
                    log("Client started but embedding failed on this environment.");
                }

                clientProcess.waitFor();
                log("Client exited with code: " + clientProcess.exitValue());
                setStatus("IDLE");
            } catch (Exception ex) {
                log("Launch failed: " + ex.getMessage());
                setStatus("ERROR");
            } finally {
                setLaunchButtonEnabled(true);
            }
        }, "client-launch-thread").start();
    }

    private void setLaunchButtonEnabled(boolean enabled) {
        SwingUtilities.invokeLater(() -> launchButton.setEnabled(enabled));
    }

    private void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusBadge.setText(status));
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            eventLog.append("[" + timestamp + "] " + message + System.lineSeparator());
            eventLog.setCaretPosition(eventLog.getDocument().getLength());
        });
    }
}

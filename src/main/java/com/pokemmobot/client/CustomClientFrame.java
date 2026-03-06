package com.pokemmobot.client;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CustomClientFrame extends JFrame {
    private final PokeMMOClientLauncher launcher;
    private final Path clientWorkingDirectory;
    private final String clientPath;
    private final long clientWaitMs;
    private final JTextArea eventLog = new JTextArea();

    public CustomClientFrame(PokeMMOClientLauncher launcher, Path clientWorkingDirectory, String clientPath, long clientWaitMs) {
        super("PokeMMO Microbot Client");
        this.launcher = launcher;
        this.clientWorkingDirectory = clientWorkingDirectory;
        this.clientPath = clientPath;
        this.clientWaitMs = clientWaitMs;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1280, 760));
        setLayout(new BorderLayout());

        add(buildCenterAndSidebar(), BorderLayout.CENTER);
        add(buildBottomLog(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    private JSplitPane buildCenterAndSidebar() {
        JPanel clientArea = new JPanel(new BorderLayout());
        clientArea.setBackground(Color.BLACK);

        JTextArea banner = new JTextArea(
                "PokeMMO client viewport\n\n" +
                "Use Launch Client to start the game process and keep this custom shell open for plugin/script control."
        );
        banner.setEditable(false);
        banner.setForeground(new Color(240, 240, 240));
        banner.setBackground(new Color(20, 20, 20));
        banner.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        banner.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        clientArea.add(banner, BorderLayout.CENTER);

        JButton launchButton = new JButton("Launch PokeMMO Client", null);
        launchButton.addActionListener(this::onLaunchClient);
        clientArea.add(launchButton, BorderLayout.NORTH);

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBackground(new Color(36, 38, 43));

        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
        tabs.addTab("Installed", createInstalledPanel());
        tabs.addTab("Plugin Hub", createPluginHubPanel());
        sidebar.add(tabs, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, clientArea, sidebar);
        splitPane.setResizeWeight(0.78);
        return splitPane;
    }

    private JScrollPane createInstalledPanel() {
        DefaultListModel<PluginScriptItem> model = new DefaultListModel<>();
        PluginScriptRegistry.defaults().forEach(model::addElement);

        JList<PluginScriptItem> list = new JList<>(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                PluginScriptItem item = (PluginScriptItem) value;
                String state = item.enabled() ? "ON" : "OFF";
                String label = String.format("[%s] %s (%s)", state, item.name(), item.category());
                return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
            }
        });
        return new JScrollPane(list);
    }

    private JScrollPane createPluginHubPanel() {
        JTextArea hub = new JTextArea();
        hub.setEditable(false);
        hub.setText(
                "Plugin Hub\n\n" +
                "- Auto Walk Route\n" +
                "- Single Area Farm\n" +
                "- Auto Heal and Return\n" +
                "- Stuck Recovery\n" +
                "- Metrics Overlay\n\n" +
                "Use this area as the future install/update surface for scripts."
        );
        return new JScrollPane(hub);
    }

    private JScrollPane buildBottomLog() {
        eventLog.setEditable(false);
        eventLog.setRows(6);
        eventLog.setBorder(BorderFactory.createTitledBorder("Event Log"));
        return new JScrollPane(eventLog);
    }

    private void onLaunchClient(ActionEvent ignored) {
        new Thread(() -> {
            try {
                log("Launching client: " + clientPath);
                launcher.launch(clientWorkingDirectory, clientPath);
                log("Client launch requested. Waiting " + clientWaitMs + "ms for startup.");
                Thread.sleep(clientWaitMs);
                log("Client startup wait complete.");
            } catch (Exception ex) {
                log("Launch failed: " + ex.getMessage());
            }
        }, "client-launch-thread").start();
    }

    private void log(String message) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        eventLog.append("[" + timestamp + "] " + message + System.lineSeparator());
    }
}

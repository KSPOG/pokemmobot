package com.pokemmobot.client;

import java.util.List;

public final class PluginScriptRegistry {
    private PluginScriptRegistry() {
    }

    public static List<PluginScriptItem> defaults() {
        return List.of(
                new PluginScriptItem("AutoLogin", "Utility", true),
                new PluginScriptItem("Inventory Setups", "Utility", false),
                new PluginScriptItem("Basic Encounter", "Microbot", true),
                new PluginScriptItem("Auto Walk Route", "Microbot", false),
                new PluginScriptItem("Heal and Return", "Microbot", false),
                new PluginScriptItem("Break Handler", "Safety", false),
                new PluginScriptItem("Mouse Macro Recorder", "Tool", false),
                new PluginScriptItem("Quest Helper", "Tool", false)
        );
    }
}

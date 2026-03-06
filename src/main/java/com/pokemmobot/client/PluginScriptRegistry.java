package com.pokemmobot.client;

import java.util.List;

public final class PluginScriptRegistry {
    private PluginScriptRegistry() {
    }

    public static List<PluginScriptItem> defaults() {
        return List.of(
                new PluginScriptItem("AutoLogin", "Utility", "Credential/profile boot helper", "Core", true),
                new PluginScriptItem("Inventory Setups", "Utility", "Preset items + restore", "PvE", false),
                new PluginScriptItem("Basic Encounter", "Microbot", "Run/Heal/Move encounter loop", "Farming", true),
                new PluginScriptItem("Auto Walk Route", "Microbot", "Waypoint route runner", "Farming", false),
                new PluginScriptItem("Heal and Return", "Microbot", "Return-to-nurse recovery", "Safety", false),
                new PluginScriptItem("Break Handler", "Safety", "Randomized anti-pattern breaks", "Safety", false),
                new PluginScriptItem("Mouse Macro Recorder", "Tool", "Record and replay clicks", "Tools", false),
                new PluginScriptItem("Quest Helper", "Tool", "Dialog + waypoint hints", "Utility", false)
        );
    }
}

package com.pokemmobot.plugins.model;

public record PluginScriptItem(
        String name,
        String category,
        String description,
        String profile,
        boolean enabled
) {
}

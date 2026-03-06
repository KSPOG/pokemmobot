package com.pokemmobot.client;

public record PluginScriptItem(
        String name,
        String category,
        String description,
        String profile,
        boolean enabled
) {
}

# Microbot-Style Plugin / Script List

This is a starter list of plugin-style modules/scripts to implement, similar to how Microbot projects split behavior into focused, swappable scripts.

## Core Runtime Plugins

| Plugin | Purpose | Status |
|---|---|---|
| `ClientLauncherPlugin` | Launch/attach to PokeMMO client process before bot loop | ✅ implemented (`PokeMMOClientLauncher`) |
| `TickLoopPlugin` | Stable fixed-rate execution loop | ✅ implemented (`TickLoop`) |
| `ActionDispatchPlugin` | Cooldown-aware action queue and dispatch | ✅ implemented (`ActionQueue` + `InputDriver` stub) |
| `StateMachinePlugin` | Resolve high-level bot state (`IDLE`, `MOVE`, `BATTLE`, `HEAL`) | ✅ implemented (`BotController` + `BotState`) |

## Sensor Plugins (Perception)

| Plugin | Purpose | Status |
|---|---|---|
| `WindowCaptureSensorPlugin` | Capture frames from the PokeMMO window | 🟡 mock only (`WindowCaptureSensor`) |
| `BattleDetectionPlugin` | Detect battle/overworld state from frame data | ⬜ planned |
| `HealthAndPPPlugin` | Detect low HP/PP from UI elements | ⬜ planned |
| `StuckDetectionPlugin` | Detect repeated no-progress movement patterns | ⬜ planned |

## Microbot Scripts (Behavior)

| Script | Purpose | Status |
|---|---|---|
| `BasicEncounterScript` | Run from battle, heal on low HP, move while overworld | ✅ implemented (`BasicEncounterMicrobot`) |
| `AutoWalkRouteScript` | Follow waypoint loop and recover from drift | ⬜ planned |
| `SingleAreaFarmScript` | Farm encounters in one area with reset rules | ⬜ planned |
| `AutoHealAndReturnScript` | Return to PokéCenter and route back | ⬜ planned |
| `PickupAndLootScript` | Item pickup/path interruption handler | ⬜ planned |

## Utility / Safety Plugins

| Plugin | Purpose | Status |
|---|---|---|
| `EmergencyStopPlugin` | Global hotkey to immediately stop loop/actions | ⬜ planned |
| `WatchdogPlugin` | Restart/abort behavior when no progress timeout triggers | ⬜ planned |
| `MetricsPlugin` | Encounters/hour, run success, heal count, stuck count | ⬜ planned |
| `StructuredLoggingPlugin` | JSON logs per tick and action dispatch | ⬜ planned |

## Recommended Build Order

1. `EmergencyStopPlugin`
2. `BattleDetectionPlugin` + `HealthAndPPPlugin`
3. `AutoWalkRouteScript`
4. `SingleAreaFarmScript`
5. `MetricsPlugin` + `StructuredLoggingPlugin`

## Notes

- Treat each script as a separate `Microbot` implementation.
- Keep each plugin/script small and testable.
- The current launcher supports `--launch-client` and related options so script testing can include client startup flow.

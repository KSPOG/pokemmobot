# PokeMMO Microbot Client (Java Starter)

This repo now includes a **Java starter scaffold** for a microbot-style PokeMMO helper client.

> ⚠️ Check PokeMMO ToS before automating gameplay.

## Project structure

- `src/main/java/com/pokemmobot/Main.java` — app entrypoint and wiring.
- `core/` — tick loop and high-level bot controller/state.
- `sensors/` — snapshot sensor abstraction and mock window sensor.
- `actions/` — queued actions and input driver stub.
- `microbots/` — microbot interface + basic encounter microbot.
- `model/` — immutable game snapshot model.

## Run

```bash
mvn -q compile
java -cp target/classes com.pokemmobot.Main
```

## What is implemented

- Fixed-rate tick loop.
- Basic FSM state resolution (`IDLE`, `MOVE`, `BATTLE`, `HEAL`).
- Single action queue with cooldown.
- Example microbot that:
  - runs from battle,
  - heals when low HP,
  - moves while in overworld.

## Next steps

1. Replace `WindowCaptureSensor` mock logic with real screen/OCR parsing.
2. Replace `InputDriver` prints with actual key/mouse dispatch.
3. Add emergency hotkey and watchdog timeouts.
4. Add structured logging + metrics.

# PokeMMO Microbot Client (Java Starter)

This repo now includes a **Java starter scaffold** for a microbot-style PokeMMO helper client.

> ⚠️ Check PokeMMO ToS before automating gameplay.

## Project structure

- `src/main/java/com/pokemmobot/Main.java` — app entrypoint, client launch options, and wiring.
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

Run and auto-launch the local PokeMMO client first:

```bash
java -cp target/classes com.pokemmobot.Main --launch-client --client-wait-ms=12000
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

## Debugging in IntelliJ IDEA

From your screenshot, the key issue is that **Main class is empty**.

### 1) Fix the Run/Debug configuration

In `Run | Edit Configurations...` for your `Pokemmo` Application config:

- **Main class**: `com.pokemmobot.Main`
- **Use classpath of module**: `pokemmobot`
- **JRE**: Java 17
- **Working directory**: project root (the folder containing `pom.xml`)

Then click **Apply**.

### 2) Set useful breakpoints

Start with breakpoints in:

- `Main.main(...)`
- `BotController.onTick(...)`
- `BasicEncounterMicrobot.tick(...)`
- `ActionQueue.pollReady()`
- `WindowCaptureSensor.readSnapshot(...)`

This lets you follow: snapshot -> decision -> queued action -> dispatched input.

### 3) Launch debugger

Use the bug icon (**Debug 'Pokemmo'**). In the debug tool window:

- Watch `snapshot`, `state`, and `actionQueue.size()`.
- Step into (`F7`) from `onTick` to microbot decisions.
- Step over (`F8`) to inspect queue growth and cooldown behavior.

### 4) Fast troubleshooting checklist

If IntelliJ still won’t run:

1. `File -> Project Structure -> SDKs`: ensure JDK 17 is configured.
2. `Project Structure -> Project`: Project SDK = 17.
3. Maven tool window -> click **Reload All Maven Projects**.
4. Build once with `mvn -q compile` or IntelliJ Build Project.

### 5) Optional: temporary debug logs

Add quick prints while iterating, for example in `BotController.onTick(...)`:

```java
System.out.printf("snapshot=%s state=%s queue=%d%n", snapshot, state, actionQueue.size());
```

Remove noisy logs once behavior is stable.

### 6) Program arguments vs VM options (what to put)

For this project:

- **Program arguments** = values passed to `Main.main(String[] args)`.
- **VM options** = JVM flags (memory, system properties, debug/JIT flags).

#### Program arguments (recommended)

You can now use:

- `--ticks=100` (how many ticks to run)
- `--tick-ms=200` (tick interval)
- `--cooldown-ms=250` (action dispatch cooldown)
- `--launch-client` (start PokeMMO client process before bot loop)
- `--client-path=./PokeMMO.sh` or `--client-path=PokeMMO.exe`
- `--client-workdir=/path/to/pokemmo`
- `--client-wait-ms=12000` (wait for client to open before loop starts)

Example:

```text
--launch-client --ticks=120 --tick-ms=150 --cooldown-ms=200 --client-wait-ms=12000
```

If you leave Program arguments empty, defaults are used (`30`, `200`, `250`), and client auto-launch stays disabled unless you pass `--launch-client`.

#### VM options (optional)

Usually leave this empty in IntelliJ debug.

Useful examples when needed:

- `-Dbot.profile=dev` (custom system property)
- `-Xms256m -Xmx1g` (heap sizing)

You do **not** need to add remote debug VM flags for normal IntelliJ debugging; IntelliJ handles that automatically when you click **Debug**.

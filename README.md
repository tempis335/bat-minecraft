# Bat Minecraft

Fabric mod for Minecraft `1.21.4` that makes sleeping bats aggressive toward nearby players.

When a player approaches a bat while it is roosting upside down under a block, the bat wakes up, targets that player, flies toward them, and attacks at close range. Creative mode players and spectators are ignored.

## Requirements

- Java 21
- Minecraft 1.21.4
- Fabric Loader 0.19.2 or newer

## Build

```bash
./gradlew build
```

The compiled `.jar` file will be created in `build/libs`.

On Windows:

```powershell
.\gradlew.bat build
```

## Run In Development

```bash
./gradlew runClient
```

On Windows:

```powershell
.\gradlew.bat runClient
```

## Project Layout

- `src/main/java/io/github/tempis335/batminecraft/mixin/BatEntityMixin.java` contains the aggressive bat behavior.
- `src/main/resources/fabric.mod.json` defines the mod metadata for Fabric Loader.
- `src/main/resources/bat_minecraft.mixins.json` registers the mixin configuration.

## Behavior

1. The mod runs its gameplay logic on the server side.
2. A roosting bat looks for the nearest valid player within 4 blocks.
3. When a player is found, the bat wakes up and stores that player as its target.
4. While the target is alive and within 24 blocks, the bat flies toward them.
5. At close range, the bat deals 1 heart of damage once per second.

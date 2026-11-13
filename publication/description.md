# WindCraft BR

WindCraft BR adds server-controlled wind and a craftable Wind Glider to Minecraft. Explore with changing winds, measure local conditions with an anemometer, and use air currents to travel from high places.

## Features

- Dynamic wind direction, speed and gusts influenced by biome, altitude, time and weather.
- Wind-driven clouds and nearby environmental particles. Leaf blocks keep their vanilla geometry.
- An anemometer that reports wind speed and direction.
- A craftable glider affected by headwind, tailwind, crosswind, turbulence and updrafts.
- Mouse-directed flight, a compact flight HUD and continuous wind audio.
- Your current first-person or third-person perspective is preserved; F5 works normally.
- Durability, drag and limited flare maneuvers keep the glider from providing unlimited powered flight.

## Requirements and installation

Minecraft Java 26.3, Forge 66.1.8 and Java 25 are required. Install the JAR in the instance's mods folder and remove older WindCraft JARs. Install version 0.5.0 on both the server and each client. Fabric and NeoForge are not supported by this file.

## Crafting the Wind Glider

Use a crafting table:

```text
W B W
W S W
T S T
```

W = wool of any color (4), B = bamboo (1), S = stick (2), T = string (2). The recipe produces one Wind Glider.

## Flight controls

Hold the glider in either hand and use it to ready or close it. You can ready it on the ground before jumping, or deploy immediately in midair without a minimum falling distance. Lift depends on airspeed and wind; the glider does not launch you from the ground.

- Mouse: steer toward where you look, with gradual turns and wind drift.
- Forward/backward: trade altitude for speed or slow down.
- Left/right: add a turning input.
- Jump: a brief flare when sufficient speed is available; it has a cooldown.
- Sneak: stop gliding.

Controls follow the game's key bindings. The flight HUD shows speed, world Y altitude, wind and vertical air current. Opening too late or colliding at high speed can still cause damage.

## Configuration and commands

World gameplay settings are in serverconfig/windcraft-glider-server.toml and windcraft-server.toml. Local visual and sound preferences are in config/windcraft-client.toml, including gliderWindSound and gliderWindVolume.

Commands: /wind info, /wind debug, /wind set speed, /wind set direction and /wind reset. Debug and wind changes require game-master permission.

## Alpha status

Version 0.5.0 is an early testing release. It passed 37 automated tests and was observed gliding in first person in a local world. Audio listening checks, two-client multiplayer, some landing/collision scenarios and compatibility with custom armor or camera mods are still pending. Back up existing worlds before testing. This release does not include turbines, sailing boats or destructive wind physics.

Interface and item text are available in Brazilian Portuguese and English. License: MIT.

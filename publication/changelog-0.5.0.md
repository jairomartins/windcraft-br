# WindCraft BR 0.5.0 — Alpha

- Redesigned the Wind Glider inventory icon.
- Removed the 1.5-block minimum fall requirement: ready the glider on the ground and jump, or activate immediately in the air.
- Preserved the current game perspective and FOV; removed the automatic flight camera.
- Flight follows the player's mouse look using the existing server-authoritative physics, with keyboard turning and pitch controls.
- Added a continuous local wind loop whose volume and pitch follow relative airspeed, with client sound settings.
- Added a ready-to-jump HUD state and updated Portuguese/English tooltips.
- Fixed out-of-bounds texture UVs on the compact held-item model.
- Passed 37 automated tests, including mouse-look heading convergence and zero-fall-distance physics.

Known limitations: audio listening checks, two-client multiplayer and the complete visual/landing/collision matrix are not finished. This is an Alpha release for testing.

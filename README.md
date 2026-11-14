# PotA - Numismatic KeepInventory Compat

A small compatibility mod for **Minecraft 1.20.1 / Fabric**, developed for **Path of the Ascended**.

## What it fixes

Numismatic Overhaul normally applies its `moneyDropPercentage` gamerule during Minecraft's standard death inventory-loss path. With `keepInventory=true`, that path is skipped, allowing a player's purse balance to escape the configured death penalty.

This mod restores that penalty **only on a genuine player death while keepInventory is enabled**. It calculates the percentage from Numismatic's existing gamerule, drops equivalent physical Numismatic coins at the actual death position and dimension, and then deducts the value from the purse.

The server-side true-death event avoids applying the penalty to Hardcore Revival knockouts and successful rescues. When `keepInventory=false`, Numismatic Overhaul continues handling death normally.

## Scope

- Uses Numismatic's own coin conversion; death penalty drops do **not** turn into Money Bags.
- Preserves native one-coin-per-item-entity death drops, including for large penalties. No additional drop limit or stacking adjustment.
- Commits the purse deduction only after the physical drops are created and attempts rollback on errors.
- Does not alter other items, existing Money Bags, Piggy Banks, loose currency, inventory retention, XP, stamina or unrelated death systems.
- No configuration or custom commands.

## Requirements

- **Minecraft 1.20.1**, Fabric Loader, Java 17
- **Fabric API**
- **Numismatic Overhaul 0.2.18+** (developed against `0.2.18+1.20`)

## Build

Requires Java 17 and Gradle 8.8:

```sh
gradle clean build
```

Gradle resolves dependencies from Fabric and Modrinth Maven; no third-party JARs are included in this repository. The compiled mod JAR appears in `build/libs/`.

## Current version

**1.0.2.1** removes Money Bag fallback from death penalty drops and replaces routine console output with restrained structured logging. True-death detection, gamerule calculation and purse transaction ordering are unchanged.

The previous 1.0.2 true-death behaviour was tested in the Path of the Ascended development instance with Hardcore Revival. An in-game regression test of the updated release is still recommended.

## License

**Copyright © 2026 Path of the Ascended. All Rights Reserved.**

This is **publicly viewable source, not an open-source licence**. See [LICENSE](LICENSE). Licence grants made under previous MIT-licensed distributions remain valid.

## AI-Assisted Development Disclosure

This mod was written with the assistance of AI. All code has been reviewed, tested, and refined by a human developer to ensure functionality, compatibility, and performance.

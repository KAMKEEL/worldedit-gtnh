# ![WorldEdit](http://static.sk89q.com/readme/worldedit.png)

[WorldEdit](https://github.com/EngineHub/WorldEdit/tree/forge-archive/1.7.10) fork at 1.7.10 for use in a GTNH instance. Non-forge
related code has been removed for ease of maintenance.
Patch AsyncWorldEdit
--------------------
This repository includes `asyncworldedit_patch.diff`, which modifies
AsyncWorldEdit v3.5.4 so it can run with this fork of WorldEdit.
To build AsyncWorldEdit:

1. Clone `https://github.com/SBPrime/AsyncWorldEdit` and checkout tag `v3.5.4`.
2. Apply `asyncworldedit_patch.diff` from this repo:
   ```bash
   git apply ../worldedit-gtnh/asyncworldedit_patch.diff
   ```
3. Add JitPack as a repository and depend on the prebuilt
   `worldedit-bukkit` and `fml` artifacts. Example for Gradle:
   ```groovy
   repositories { maven { url 'https://jitpack.io' } }
   dependencies {
       compileOnly 'com.github.GTNewHorizons:WorldEdit-GTNH:HEAD-SNAPSHOT:all'
       implementation 'com.github.GTNewHorizons:Forge:1.7.10-142.1104'
   }
   ```
4. Run `./gradlew build` to produce the Bukkit plugin and Forge injector jars.

The required jars are hosted on JitPack so the `asyncworldedit/libs` folder is no longer used.

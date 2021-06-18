# AltReality

What if things had been done differently... What else could have been added to the game?

## Setup
1. Edit build.gradle and mod.json to suit your needs.
    * The "mixins" object can be removed from mod.json if you do not need to use mixins.
    * Please remember to replace all occurrences of "modid" with your own mod ID.
2. Run the following command, replacing "eclipse" with the command your specific ide if you are not using eclipse:

```
./gradlew eclipse
```

NOTE: if you want sources (recommended), instead run

```
./gradlew rebuildLVT genSources eclipse
```

Once your mod is complete and ready for use it can be compiled and reobfuscated with:

```
./gradlew build
```

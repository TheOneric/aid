# AiD: Anime in Deutsch

## Build
Da es für Android quasi unmöglich macht ein anderes Buildsystem als Gradle zu 
benutzen, ist das Buildsystem leider Gradle.  
Da zusätzlich Standard Android-App-Abhängigkeiten angeben neueste Gradle-Version 
zu benötigen und natürlich kaum eine Stable-Distro diese pacakget, musste ich 
leider auch den Gradle-Binärblob ins VCS packen.
Wer eine Gradle-packagende Rolling-Release-Distros verwendet oder eine eigene 
Gradle-Version baut, kann `gradlew`, `gradlew.bat` und `gradle/` löschen und 
versuchen ob es passt.

### Android Studio
Einfach öffnen, alles sollte so funktionieren wie es ist.

### Ohne Android Studio
Android Sdk Tools werden benötigt.
Kopiere `local.properties.template` nach `local.properties` und gib den Pfad zu 
den Android-SDK Tools an.
Danach kann mit `./gradlew build` etc gebaut werden.

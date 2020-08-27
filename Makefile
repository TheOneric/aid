# Because wInDOwS just can't be normal …
ifeq ($(OS),Windows_NT)
    GRADLE_WRAPPER := gradlew.bat
else
    GRADLE_WRAPPER := ./gradlew
endif

GEN_FILES := ./app/src/main/assets/aod-touch.xpi


all: pregen android-app
	@echo Done.

pregen: aod-touch

aod-touch: app/src/main/assets/aod-touch.xpi

app/src/main/assets:
	mkdir -p $@

app/src/main/assets/aod-touch.xpi: app/src/main/assets
	@echo "Build bundeled web extensions …"
	cd webext && zip -r -FS ../$@ *

android-app:
	$(GRADLE_WRAPPER) build


clean:
	rm -fr $(GEN_FILES)
	$(GRADLE_WRAPPER) clean


help:
	@echo "This Makefile can either be used to pregenerate files not built by"
	@echo "Android Studio, or it can built the entire app (assuming README's"
	@echo "build instructions were followed)"
	@echo ""
	@printf "%-12s: Build everything\n" "all"
	@echo ""
	@printf "%-12s: Build only the app (Wrapper around gradle)\n" "android-app"
	@echo ""
	@printf "%-12s: Pregenerate all files not built by gradle\n" "pregen"
	@printf "  %-10s: Build webextension making AoD toch-friendly\n" "aod-touch"
	@echo ""
	@printf "%-12s: Clean all generated files\n" "clean"

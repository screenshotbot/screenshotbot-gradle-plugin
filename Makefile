
.PHONY:

OTHER=other
LOCAL_REPO=$(shell pwd)/localRepo
ESCAPED_LOCAL_REPO=$(shell echo $(LOCAL_REPO) | sed 's/\//\\\//g')
VERSION=$(shell grep '^version ' plugin/build.gradle | cut -d "'" -f 2)
# REMOTE_RECORDER_VERSION=$(shell curl https://screenshotbot.io/recorder-version/current)
REMOTE_RECORDER_VERSION=releases/2.11.0/
PLATFORMS=darwin linux linux-arm64
SHELL:=/bin/bash

# Gradle auto-detects JDKs in /usr/lib/jvm and friends, but not in
# /opt/software, so anything installed there is invisible to it. Roborazzi's
# included build (:include-build:roborazzi-gradle-plugin) requests a
# languageVersion=17 toolchain while the builders' detected JDK is 21, which
# fails with "Cannot find a Java installation on your machine ... matching
# {languageVersion=17}". Point Gradle at whatever 17 is installed rather than
# hardcoding a version, so a JDK upgrade under /opt/software doesn't break this.
JDK_PATHS=$(shell ls -d /opt/software/jdk-17* 2>/dev/null | paste -sd, -)
GRADLE_JDK_ARGS=$(if $(JDK_PATHS),-Porg.gradle.java.installations.paths=$(JDK_PATHS),)
GRADLEW=./gradlew $(GRADLE_JDK_ARGS)

paparazzi-integration: publish
	@echo
	@echo PAPARAZZI
	@echo

	rm -rf $(OTHER)
	git clone https://github.com/screenshotbot/paparazzi-example.git $(OTHER)

	$(MAKE) update-other-repo

	cd $(OTHER) && $(GRADLEW) --stacktrace recordAndVerifyPaparazziDebugScreenshotbotCI
	cd $(OTHER) && $(GRADLEW) --stacktrace  :sample:recordPaparazziDebugScreenshotbot
	cd $(OTHER) && $(GRADLEW) --stacktrace :sample:verifyPaparazziDebugScreenshotbot

roborazzi-integration: publish
	@echo
	@echo ROBORAZZI:
	@echo
	rm -rf $(OTHER)
	git clone ssh://git@phabricator.tdrhq.com:2222/diffusion/24/roborazzi.git $(OTHER)

	$(MAKE) update-other-repo


	cd $(OTHER) && $(GRADLEW) :sample-android:recordRoborazziDebugScreenshotbot
	cd $(OTHER) && $(GRADLEW) :sample-android:verifyRoborazziDebugScreenshotbot

cpst-integration: publish
	@echo
	@echo CPST:
	@echo
	rm -rf $(OTHER)
	git clone -b app-without-screenshots https://github.com/screenshotbot/compose-preview-example.git $(OTHER)

	$(MAKE) update-other-repo

	cd $(OTHER) && $(GRADLEW) --configuration-cache --stacktrace recordAndVerifyDebugScreenshotTest

fix-version:
	cd $(OTHER) && if test -f build.gradle.kts ; then \
        echo using kotlin gradle files ; \
		sed -i 's/id("io.screenshotbot.plugin") version '.*'/id("io.screenshotbot.plugin") version "$(VERSION)"/' */build.gradle.kts ; \
    else \
		echo using groovy gradle files ; \
		sed -i "s/id 'io.screenshotbot.plugin' version '.*'/id 'io.screenshotbot.plugin' version '$(VERSION)'/" *.gradle */build.gradle ; \
    fi
	shopt -s nullglob ; cd $(OTHER) && sed -i "s#/home/arnold/builds/screenshotbot-gradle/localRepo#$(shell pwd)/localRepo#g" *.gradle */build.gradle */build.gradle.kts

	cd $(OTHER) && ( cat *.gradle */build.gradle || true )

update-other-repo: fix-version update-maven-local

update-maven-local:
	echo $(ESCAPED_LOCAL_REPO)
	shopt -s nullglob ;	sed -i 's/home\/arnold\/myLocal/$(ESCAPED_LOCAL_REPO)/' $(OTHER)/settings.gradle*

publish: .PHONY
	./gradlew :plugin:publish

integration-tests-with-env: | publish paparazzi-integration roborazzi-integration cpst-integration

integration-tests:
	ANDROID_HOME=/opt/software/android-sdk $(MAKE) integration-tests-with-env

copy-binaries:
	cd plugin/src/main/resources/io/screenshotbot/gradle && \
    for platform in $(PLATFORMS) ; do \
        echo Downloading $$artifact ; \
		echo $(REMOTE_RECORDER_VERSION) > version.txt ; \
		curl https://screenshotbot.io/artifact/$(REMOTE_RECORDER_VERSION)recorder-$$platform -o recorder-$$platform ; \
		export SCREENSHOTBOT_DIR=$$PWD/$$platform/ ; \
		mkdir -p $SCREENSHOTBOT_DIR ; \
        sh recorder-$$platform ; \
		rm recorder-$$platform ; \
    done

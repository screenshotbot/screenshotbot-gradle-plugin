
.PHONY:

OTHER=other
LOCAL_REPO=$(shell pwd)/localRepo
ESCAPED_LOCAL_REPO=$(shell echo $(LOCAL_REPO) | sed 's/\//\\\//g')
VERSION=$(shell grep '^version ' plugin/build.gradle | cut -d "'" -f 2)
# REMOTE_RECORDER_VERSION=$(shell curl https://screenshotbot.io/recorder-version/current)
REMOTE_RECORDER_VERSION=releases/2.11.0/
PLATFORMS=darwin linux linux-arm64
SHELL:=/bin/bash

paparazzi-integration: publish
	@echo
	@echo PAPARAZZI
	@echo

	rm -rf $(OTHER)
	git clone https://github.com/screenshotbot/paparazzi-example.git $(OTHER)

	$(MAKE) update-other-repo

	cd $(OTHER) && ./gradlew --stacktrace recordAndVerifyPaparazziDebugScreenshotbotCI
	cd $(OTHER) && ./gradlew --stacktrace  :sample:recordPaparazziDebugScreenshotbot
	cd $(OTHER) && ./gradlew --stacktrace :sample:verifyPaparazziDebugScreenshotbot

roborazzi-integration: publish
	@echo
	@echo ROBORAZZI:
	@echo
	rm -rf $(OTHER)
	git clone ssh://git@phabricator.tdrhq.com:2222/diffusion/24/roborazzi.git $(OTHER)

	$(MAKE) update-other-repo


	cd $(OTHER) && ./gradlew :sample-android:recordRoborazziDebugScreenshotbot
	cd $(OTHER) && ./gradlew :sample-android:verifyRoborazziDebugScreenshotbot

cpst-integration: publish
	@echo
	@echo CPST:
	@echo
	rm -rf $(OTHER)
	git clone -b app-without-screenshots https://github.com/screenshotbot/compose-preview-example.git $(OTHER)

	$(MAKE) update-other-repo

	cd $(OTHER) && ./gradlew --configuration-cache --stacktrace recordAndVerifyDebugScreenshotTest

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

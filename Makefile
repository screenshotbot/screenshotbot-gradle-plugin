
.PHONY:

OTHER=other
LOCAL_REPO=$(shell pwd)/localRepo
ESCAPED_LOCAL_REPO=$(shell echo $(LOCAL_REPO) | sed 's/\//\\\//g')

paparazzi-integration:
	rm -rf $(OTHER)
	git clone ssh://git@phabricator.tdrhq.com:2222/diffusion/23/paparazzi-example.git $(OTHER)

	$(MAKE) update-maven-local
	cd $(OTHER) && ./gradlew :sample:recordPaparazziDebugScreenshotbot
	cd $(OTHER) && ./gradlew :sample:verifyPaparazziDebugScreenshotbot


update-maven-local:
	echo $(ESCAPED_LOCAL_REPO)
	sed -i 's/home\/arnold\/myLocal/$(ESCAPED_LOCAL_REPO)/' $(OTHER)/settings.gradle

integration-tests-with-env: | paparazzi-integration

integration-tests:
	ANDROID_HOME=/opt/software/android-sdk $(MAKE) integration-tests-with-env


# A gradle plugin for Screenshotbot

This is an Open source plugin that integrates popular Android
screenshot testing libraries with Screenshotbot.

Currently we support:

* Paparazzi
* Facebook's screenshot-tests-for-android

But we eventually plan to support:

* Shot
* Dropshots
* Roborazzi

# Getting started

Including the plugin is pretty straightforward. In your
`build.gradle`, add the following lines

```
plugins {
  id 'io.screenshotbot.plugin' version '1.1'
}
```

If you use Paparazzi this will automatically create three new tasks
for each of your Parazzi flavors, like so:

```
recordAndVerifyPaparazziDebugScreenshotbotCI
recordPaparazziDebugScreenshotbot
verifyPaparazziDebugScreenshotbot
```

The last two tasks are meant to be used by developers while working
locally, and does not affect your CI state. You might have to run
`~/screenshotbot/recorder dev install` and follow the instructions to
install a key locally.

The first task will be run in your CI, in place of simply `:verifyPaparazziDebug`.
Screenshotbot does not require any screenshots to be stored in your repository,
we'll run the record step and upload the screenshots to Screenshotbot, and also
process information from you CI environment to figure out things like which Pull Request to
send notifications on. On CI, you will have to set the `SCREENSHOTBOT_API_KEY` and `SCREENSHOTBOT_API_SECRET` environment variables.

# Configure Enterprise or OSS Installations

By default, the plugin assumes you are using the installation at https://screenshotbot.io.

If you are Enterprise customer, we give you a custom installation that looks like `https://<customer>.screenshotbot.io`. Similarly, you might have your own domain for OSS users.

In this case, you might want to set the default domain across all subprojects in your root
`build.gradle`

```groovy
subprojects {
    plugins.withId('io.screenshotbot.plugin') {
        screenshotbot {
            hostname "https://staging.screenshotbot.io"
        }
    }
}

```

# License

This library is licensed under the Mozilla Public License, v2.

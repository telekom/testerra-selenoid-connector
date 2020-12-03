# Testerra Selenoid Connector

This module for Testerra provides access to videos and vnc streams created by Selenoid Docker container. It will register
automatically by using Testera `ModuleHook`.

---- 

## Usage

The Testerra Selenoid Connector is published to a Bintray repository https://bintray.com/testerra-io.

Include the following dependency in your project.

Gradle:

````groovy
implementation 'eu.tsystems.mms.tic.testerra:selenoid-connector:1.0'
````

Maven:

````xml

<dependency>
    <groupId>eu.tsystems.mms.tic.testerra</groupId>
    <artifactId>selenoid-connector</artifactId>
    <version>1.0</version>
</dependency>
````

Set the properties according to your needs. Either provide the property values via `test.properties` file of Testerra or just pass
them via command line and build arguments `-Dtt.selenoid....`.

## How does it work?

Testerra Selenoid Connector will register a new `WebDriverFactory` passing in the `Capapbilities` needed by Selenoid,
e.g. `enableVNC`. Additional to that, Testerre Selenoid Connector will register some `Worker` implementations and a `VideoCollector`
implementation.

The `SelenoideEvidenceVideoCollector` will automatically add videos to your failed test methods, when video ws enabled by property
and a `WebDriver` session relates to the test method.

The `SelenoidExclusiveSessionVideoWorker` will look for exclusive WebDriver sessions and will then link the video afterwards to all
relating methods.

## Properties

|Property|Default|Description|
|---|---|---|
|tt.screencaster.active|true|All videos will be collected in failure case of test method and for exclusive sessions.|
|tt.selenoid.vnc.enabled|true|VNC Stream will be activated and logged to the console.|
|tt.selenoid.vnc.address|none|VNC Host address - Will be used to generate a unique url for accessing the VNC session. <br> For a hosted [noVNC server](https://github.com/novnc/noVNC) this should be `http://<host>:<port>/vnc.html`.|

## Set browser language and locale

Sometimes you want to start your browser with a specific language / locale setting to test your websites in different language /
locales.
`Selenoid` can handle environment variables passed via the `DesiredCapabilities`. This approach is implemented in this module. So
the `selenoid-connector` will use the JVM default locale to start the browser.

So let's see how this works in an example. If system default language is us-english, your JVM will have these settings applied as
default on startup. Without providing a specific property your browser will start with these settings as well. If you
pass `-Duser.language=de -Duser.country=DE` the `selenoid-connector` will inject these settings into your browser and therefore the
browser will have a different language/locale setting as your host system. With this in mind, you're able to do multi-language test
without affecting your host system language.

## Toubleshooting

### VNC url not displayed in my logfile

Please ensure that you setup up the `tt.selenoid.vnc.address` correctly, by using an url like
this `http://novnc-host:no-vnc-port/vnc.html`.

### Videos not show up in report

Please ensure that you setup `tt.screencaster.active` properly and the test method you're expected videos for failed. Testerra will
only append videos to the report for failing methods.

### I got the same video on multiple test methods

Congratulations. You're using exclusive WebDriver sessions. This is a feature from Testerra. One WebDriver session will be used
accross multiple test methods until you close it. Because the generated video is valid for multiple test methods, we linked it for
you to all of them.

---

## Publication

### ... to a Maven repo

```sh
gradle publishToMavenLocal
```

or pass then properties via. CLI

```sh
gradle publish -DdeployUrl=<repo-url> -DdeployUsername=<repo-user> -DdeployPassword=<repo-password>
```

Set a custom version

```shell script
gradle publish -DmoduleVersion=<version>
```

### ... to Bintray

Upload and publish this module to Bintray:

````sh
gradle bintrayUpload -DmoduleVersion=<version> -DBINTRAY_USER=<bintray-user> -DBINTRAY_API_KEY=<bintray-api-key>
```` 

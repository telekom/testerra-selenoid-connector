# Selenoid Connector

Using a Selenium Grid based on [Selenoid](https://github.com/aerokube/selenoid) this module provides access to videos and VNC
streams.

The module will register automatically by using `ModuleHook`.

---- 

## Usage

Include the following dependency in your project.

Gradle:

````groovy
implementation 'eu.tsystems.mms.tic.testerra:selenoid-connector:1-SNAPSHOT'
````

Maven:

````xml

<dependency>
    <groupId>eu.tsystems.mms.tic.testerra</groupId>
    <artifactId>selenoid-connector</artifactId>
    <version>1-SNAPSHOT</version>
</dependency>
````

## Video support

The Selenoid connector can collect the generated video stream and provide it for the Testerra report.

*Activate the video screencaster*

````
# test.properties
# activate generation of screencast in general, default = false
tt.screencaster.active=true
# activate appending of screencast to report for failed tests, default = true
tt.screencaster.active.on.failed=true
# activate appending of screencast to report for successful tests, default = false
tt.screencaster.active.on.success=true
````

After finishing the test run the connector collects the video files and adds them to the report.

*You will find all video files in the method detail view in a separate tab.*

![](doc/selenoid_connector_report_video_tab.png)

> IMPORTANT: By default only video files of failed methods will be added.

## VNC support

For debugging you tests in a Selenoid grid you can activate the support for linking the VNC streaming URL. As VNC client we are
using [noVNC](https://github.com/novnc/noVNC).

### Set up a noVNC server.

* Please not that your Grid must be available by your noVNC client.
* We prefer to use one the many Docker images available at https://hub.docker.com/.

### Configure your `test.properties` file

````
# test.properties
tt.selenoid.vnc.enabled=true
tt.selenoid.vnc.address=http://<your-no-vnc-client>:<port>/vnc.html
````

### Start your test locally

Starting your test in your local IDE you will find the VNC client URL in the log messages. The URL can only be generated if a
browser session was started successfully.

*Some IDEs mark the URL as clickable link.*
![](doc/selenoid_connector_vnc_url.png)

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

## Properties

|Property|Default|Description|
|---|---|---|
|tt.screencaster.active|true|All videos will be collected in failure case of test method and for exclusive sessions.|
|tt.screencaster.active.on.success|false|When true, generated video files will be attached the report for successful test methods|
|tt.screencaster.active.on.failed|true|Generated video files will be attached the report for failed test methods|
|tt.selenoid.vnc.enabled|true|VNC Stream will be activated and logged to the console.|
|tt.selenoid.vnc.address|none|VNC Host address - Will be used to generate a unique url for accessing the VNC session. <br> For a hosted [noVNC server](https://github.com/novnc/noVNC) this should be `http://<host>:<port>/vnc.html`.|

## Troubleshooting

### VNC url not displayed in my logfile

Please ensure that you setup up the `tt.selenoid.vnc.address` correctly, by using an url like
this `http://novnc-host:no-vnc-port/vnc.html`.

### Videos not show up in report

Please ensure that you setup `tt.screencaster.active` and its related sub-properties for successful/failed methods properly.

### I got the same video on multiple test methods

Congratulations. You're using exclusive WebDriver sessions. This is a feature from Testerra. One WebDriver session will be used
across multiple test methods until you close it. Because the generated video is valid for multiple test methods, we linked it for
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

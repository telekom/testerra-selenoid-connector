# Testerre Selenoid Connector

This module for Testerra provides access to videos and vnc streams created by Selenoid Docker container.
It will register automatically by using Testera `ModuleHook`.

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

Set the properties according to your needs. Either provide the property values via `test.properties` file of Testerra or just pass them via command line and build arguments `-Dtt.selenoid....`.

## How does it work?

Testerra Selenoid Connector will register a new `WebDriverFactory` passing in the `Capapbilities` needed by Selenoid, e.g. `enableVNC`. 
Additional to that, Testerre Selenoid Connector will register some `Worker` implementations and a `VideoCollector` implementation.

The `SelenoideEvidenceVideoCollector` will automatically add videos to your failed test methods, when video ws enabled by property and a `WebDriver` session relates to the test method.  

The `SelenoidExclusiveSessionVideoWorker` will look for exclusive WebDriver sessions and will then link the video afterwards to all relating methods.  

 
## Properties
|Property|Default|Description|
|---|---|---|
|tt.screencaster.active|true|All videos will be collected in failure case of test method and for exclusive sessions.|
|tt.selenoid.vnc.enabled|true|VNC Stream will be activated and logged to the console.|
|tt.selenoid.vnc.address|<none>|VNC Host address - Will be used to generate a unique url for accessing the VNC session.|
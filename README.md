Project Lighthouse
==================

The goal of this project is to develop a prototype system that will use
visible light communication to authentication an IoT device. An IoT device
will be equipped with a tri-color (RGB) LED. The light emitted by the LED will
be modulated with the fingerprint of the device's X.509 certificate. A
smartphone application will receive and decode the signal using the
smartphone's camera. The application will establish a TLS connection with the
IoT device via Wi-Fi P2P and will use the received fingerprint to authenticate
the certificate presented by the device.

This repository contains the source code for the tri-color LED transmitter,
the Android application (receiver), and supporting libraries. The transmitter
has been designed for Raspberry Pi and the smartphone application runs
on an Android phone.

The software is implemented in Java and invokes a couple of native libraries
(included) via JNI. The build system is based on Gradle. A Makefile is
provided for convenience.

To build the software, make sure you have, gcc, make, Java (including JNI) and
the pigpio library installed on your Raspberry Pi (all are installed by
default in Raspbian). If the native libraries do not build, you may need to
adjust the path to JNI headers in Makefile. During the first build, Gradle
will download and cache missing dependencies.

To run the LED transmitter on a Raspberry Pi, invoke "make run". The software
will be re-built automatically if necessary. The application is run under root
via sudo (required by the native pigpio library).

Directory Structure
-------------------
```
├── build.gradle      : The main build script for Gradle
├── gradle            : A Gradle wrapper that downloads all packages and dependencies
├── gradle.properties : Settings for the Gradle build system
├── gradlew           : A wrapper script to invoke Gradle on Linux/MacOS
├── gradlew.bat       : A wrapper script to invoke Gradle on Windows
├── Makefile          : A convenience Makefile with commont targets (e.g., run)
├── pigpio            : A Java (JNI) wrapper library for pigpio
│   └── src
│       ├── main      : The Java portion of the library
│       └── pigpio    : The native (C) portion of the library
├── README            : This file
├── settings.gradle   : Settings for the Gradle build system
├── trx               : The LED transmitter application
│   └── build.gradle  : Main Gradle build file for the LED transmitter application
└── unix              : A Java (JNI) wrapper library for UNIX APIs (e.g., unix domain sockets)
    └── src
        ├── main      : The Java portion of the library
        └── unix      : The native (C) portion of the library
```

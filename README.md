# Project Lighthouse

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

## Build Instructions

### LED Transmitter

The transmitter application must be built on Linux. To build it, you will need
`make`, Java, and the pigpio library installed. To build the transmitter
application, run `make build-trx` in the top-level directory of the git clone.

To build just the Java portion of the transmitter (without the JNI part), run
`make build-trx-java`.

### Android Receiver App

The Android receiver application requires OpenCV-Android-SDK version 3.4.1 or
newer. Download the SDK from

https://sourceforge.net/projects/opencvlibrary/files/opencv-android/3.4.1/opencv-3.4.1-android-sdk.zip/download

and unpack it into your home directory. If you unpack the SDK in some other
location, you may need to also update settings.gradle.

To build the Android application, you will also need Android SDK and Android
NDK. Make sure you have both libraries installed and working.

To build the debugging version of the application, run either `make
app-debug`, or `./gradlew assembleDebug`.

The application can also be imported into Android Studio and built there. You
may need to disable "On Demand Configuration" in Android Studio settings.

## Namespace Structure
```
io.sece.vlc      : Java library shared by trx and rcvr
io.sece.vlc.trx  : LED transmitter for Raspberry Pi
io.sece.vlc.rcvr : Receiver Android application
io.sece.pigpio   : Java wrapper for pigpio
io.sece.unix     : Assorted utility classes (UNIX domain sockets)
```

## Directory Structure
```
├── Makefile          : A convenience Makefile with common targets (e.g., run)
├── README.md         : This file
├── build.gradle      : The main build script for Gradle
├── shared            : A shared Java library used by both transmitter and receiver
├── trx               : The LED transmitter application
├── app               : Receiver application for Android
├── pigpio            : A Java (JNI) wrapper library for pigpio
│   └── src
│       ├── main      : The Java portion of the library
│       └── pigpio    : The native (C) portion of the library
├── settings.gradle   : Settings for the Gradle build system
├── scripts           : Helper shell scripts
├── gradle            : A Gradle wrapper that downloads all packages and dependencies
├── gradle.properties : Settings for the Gradle build system
├── gradlew           : A wrapper script to invoke Gradle on Linux/MacOS
├── gradlew.bat       : A wrapper script to invoke Gradle on Windows
└── unix              : A Java (JNI) wrapper library for UNIX APIs (e.g., unix domain sockets)
    └── src
        ├── main      : The Java portion of the library
        └── unix      : The native (C) portion of the library
```

pkg:=io.sece.vlc
trx_pkg:=$(pkg).trx
app_pkg:=$(pkg).rcvr
apk_dir:=app/build/outputs/apk
native_dir:=build/native
jvm:=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt

alldep:=Makefile
gradle:=./gradlew
CFLAGS:=-I$(jvm)/include -I$(jvm)/include/linux

all: build-trx

##### Android package targets #####

app-debug: $(apk_dir)/debug/app-debug.apk $(alldep)

app-release: $(apk_dir)/app-release-unsigned.apk $(alldep)

uninstall-app: $(alldep)
	adb uninstall $(app_pkg)

install-app: $(apk_dir)/debug/app-debug.apk $(alldep)
	adb uninstall $(app_pkg) >/dev/null 2>&1 || true
	adb install $(apk_dir)/debug/app-debug.apk

.PHONY: $(apk_dir)/debug/app-debug.apk
$(apk_dir)/debug/app-debug.apk: $(alldep)
	$(gradle) assembleDebug

.PHONY: $(apk_dir)/app-release-unsigned.apk
$(apk_dir)/app-release-unsigned.apk: $(alldep)
	$(gradle) assembleRelease

.PHONY:
logcat: $(alldep)
	scripts/pidlog.sh $(app_pkg) -v color -b all

.PHONY:
clean-app: $(alldep)
	$(gradle) :app:clean :opencv:clean :shared:clean


##### JNI native libraries targets #####

build-native: $(native_dir)/libunix-java.so $(native_dir)/libpigpio-java.so $(alldep)

$(native_dir)/libunix-java.so: unix/src/unix/c/sock.c $(alldep)
	mkdir -p $(native_dir)
	$(CC) $(CFLAGS) -shared -o $@ $<

$(native_dir)/libpigpio-java.so: pigpio/src/pigpio/c/pigpio.c $(alldep)
	mkdir -p $(native_dir)
	$(CC) $(CFLAGS) -shared -o $@ $< -lpigpio


##### LED transmitter targets #####

.PHONY: build-trx
build-trx: build-native build-trx-java

.PHONY: build-trx-java
build-trx-java:
	$(gradle) --configure-on-demand :trx:assemble

.PHONY: run
run: build-trx $(alldep)
	sudo java -Dled.driver=PiRgbLED -Dled.params="22,27,17" -Djava.library.path=$(native_dir) -cp pigpio/build/libs/pigpio.jar:shared/build/libs/shared.jar:trx/build/libs/trx.jar:/home/pi/gson-2.8.4.jar:/home/pi/openrq-3.3.2.jar $(trx_pkg).Main

.PHONY: clean-trx
clean-trx: $(alldep)
	$(gradle) --configure-on-demand :trx:clean :pigpio:clean :unix:clean :shared:clean
	rm -rf $(native_dir)

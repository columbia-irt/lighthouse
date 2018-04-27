pkg:=io.sece.vlc
apk_dir:=app/build/outputs/apk
native_dir:=build/native
jvm:=/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt

alldep:=Makefile
CFLAGS:=-I$(jvm)/include -I$(jvm)/include/linux

all: build-trx

##### Android package targets #####

app-debug: $(apk_dir)/debug/app-debug.apk $(alldep)

app-release: $(apk_dir)/app-release-unsigned.apk $(alldep)

uninstall: $(alldep)
	adb uninstall $(pkg)

install: $(apk_dir)/debug/app-debug.apk $(alldep)
	adb uninstall $(pkg) >/dev/null 2>&1 || true
	adb install $(apk_dir)/debug/app-debug.apk

.PHONY: $(apk_dir)/debug/app-debug.apk
$(apk_dir)/debug/app-debug.apk: $(alldep)
	./gradlew assembleDebug

.PHONY: $(apk_dir)/app-release-unsigned.apk
$(apk_dir)/app-release-unsigned.apk: $(alldep)
	./gradlew assembleRelease

.PHONY:
log logcat: $(alldep)
	scripts/pidlog.sh $(pkg) -v color -b all


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
build-trx: build-native
	./gradlew :trx:assemble

.PHONY: run
run: build-trx $(alldep)
	sudo java -Djava.library.path=$(native_dir) -cp ./pigpio/build/libs/pigpio.jar:trx/build/libs/trx.jar $(pkg).trx.Main


##### Other #####

.PHONY: clean
clean: $(alldep)
	./gradlew clean

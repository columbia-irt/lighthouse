#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <string.h>
#include <stdarg.h>
#include <unistd.h>
#include <jni.h>
#include <pigpio.h>

#define PACKAGE          "io/sece/pigpio/"
#define NATIVE_LIBRARY   PACKAGE "PiGPIO"
#define PIGPIO_EXCEPTION PACKAGE "PiGPIOException"


/* Raise an exception "name" with the string built from the formatting string
 * and additional parameters (if any). Return 0 if an exception was raised and
 * -1 if no exception was raised (internal error or exception class not
 * found). Prints a message stderr if the exception cannot be raised. */
static int
throw(JNIEnv *e, const char *name, const char *fmt, ...)
{
    int rc;
    va_list ap;
    jthrowable ex;
    static char msg[1024];

    /* If there's an exception already do nothing */
    if ((*e)->ExceptionOccurred(e)) return 0;

    ex = (*e)->FindClass(e, name);
    if ((*e)->ExceptionOccurred(e)) return 0;

    va_start(ap, fmt);
    vsnprintf(msg, sizeof(msg), fmt, ap);
    rc = 0;
    if (ex) {
        rc = (*e)->ThrowNew(e, ex, msg);
    }
    if (!ex || rc < 0) {
        fprintf(stderr, "ERROR: Couldn't raise the following exception\n");
        fprintf(stderr, "%s: %s\n", name, msg);
        return -1;
    }
    va_end(ap);
    return 0;
}


static void
jni_gpioInitialise(JNIEnv *e, jclass lib) {
    int rc = gpioInitialise();
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Can't initialize pigpio: %d", rc);
    }
}


static void
jni_gpioTerminate(JNIEnv *e, jclass lib) {
    gpioTerminate();
}


static jint
jni_gpioHardwareRevision(JNIEnv *e, jclass lib) {
    int rc = gpioHardwareRevision();
    if (rc == 0) {
        throw(e, PIGPIO_EXCEPTION, "Can't obtain hardware revision");
    }
    return rc;
}


static jint
jni_gpioVersion(JNIEnv *e, jclass lib) {
    return gpioVersion();
}


static void
jni_gpioSetMode(JNIEnv *e, jclass lib, jint gpio, jint mode)
{
    int rc = gpioSetMode(gpio, mode);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
}


static jint
jni_gpioGetMode(JNIEnv *e, jclass lib, jint gpio)
{
    int rc = gpioGetMode(gpio);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
    return rc;
}


static void
jni_gpioSetPullUpDown(JNIEnv *e, jclass lib, jint gpio, jint pud)
{
    int rc = gpioSetPullUpDown(gpio, pud);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
}


static jint
jni_gpioRead(JNIEnv *e, jclass lib, jint gpio)
{
    int rc = gpioRead(gpio);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
    return rc;
}


static void
jni_gpioWrite(JNIEnv *e, jclass lib, jint gpio, jint level)
{
    int rc = gpioWrite(gpio, level);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
}


static void
jni_gpioPWM(JNIEnv *e, jclass lib, jint gpio, jint dutycycle)
{
    int rc = gpioPWM(gpio, dutycycle);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
}


static jint
jni_gpioGetPWMdutycycle(JNIEnv *e, jclass lib, jint gpio)
{
    int rc = gpioGetPWMdutycycle(gpio);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
    return rc;
}


static jint
jni_gpioGetPWMrange(JNIEnv *e, jclass lib, jint gpio)
{
    int rc = gpioGetPWMrange(gpio);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
    return rc;
}


static jint
jni_gpioGetPWMrealRange(JNIEnv *e, jclass lib, jint gpio)
{
    int rc = gpioGetPWMrealRange(gpio);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
    return rc;
}


static jint
jni_gpioSetPWMfrequency(JNIEnv *e, jclass lib, jint gpio, jint frequency)
{
    int rc = gpioSetPWMfrequency(gpio, frequency);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
    return rc;
}


static jint
jni_gpioGetPWMfrequency(JNIEnv *e, jclass lib, jint gpio)
{
    int rc = gpioGetPWMfrequency(gpio);
    if (rc < 0) {
        throw(e, PIGPIO_EXCEPTION, "Error code: %d", rc);
    }
    return rc;
}


static JNINativeMethod natives[] = {
    {"gpioInitialise",      "()V",   jni_gpioInitialise      },
    {"gpioTerminate",       "()V",   jni_gpioTerminate       },
    {"gpioHardwareRevision","()I",   jni_gpioHardwareRevision},
    {"gpioVersion",         "()I",   jni_gpioVersion         },
    {"gpioSetMode",         "(II)V", jni_gpioSetMode         },
    {"gpioGetMode",         "(I)I",  jni_gpioGetMode         },
    {"gpioSetPullUpDown",   "(II)V", jni_gpioSetPullUpDown   },
    {"gpioRead",            "(I)I",  jni_gpioRead            },
    {"gpioWrite",           "(II)V", jni_gpioWrite           },
    {"gpioPWM",             "(II)V", jni_gpioPWM             },
    {"gpioGetPWMdutycycle", "(I)I",  jni_gpioGetPWMdutycycle },
    {"gpioGetPWMrange",     "(I)I",  jni_gpioGetPWMrange     },
    {"gpioGetPWMrealRange", "(I)I",  jni_gpioGetPWMrealRange },
    {"gpioSetPWMfrequency", "(II)I", jni_gpioSetPWMfrequency },
    {"gpioGetPWMfrequency", "(I)I",  jni_gpioGetPWMfrequency }
};


jint
JNI_OnLoad(JavaVM *v, void *res)
{
    jint rc;
    jclass lib;
    JNIEnv *e = NULL;

    if ((*v)->GetEnv(v, (void **)&e, JNI_VERSION_1_6) != JNI_OK)
        goto out;

    lib = (*e)->FindClass(e, NATIVE_LIBRARY);
    if (!lib) {
        if (!(*e)->ExceptionOccurred(e)) {
            rc = throw(e, "java/lang/NoClassDefFoundError",
                  "JNI_OnLoad: Class %s not found", NATIVE_LIBRARY);
            /* If we failed to raise the exception, at least stop the VM by
             * returning an invalid JNI version. */
            if (rc < 0) return -1;
        }
        goto out;
    }

    rc = (*e)->RegisterNatives(e, lib, natives,
                               sizeof(natives) / sizeof(natives[0]));
    if ((*e)->ExceptionOccurred(e)) goto out;
    if (rc < 0) {
        rc = throw(e, "java/lang/IncompatibleClassChangeError",
                   "JNI_OnLoad: Can't register native methods");
        /* If we failed to raise the exception, at least stop the VM by
         * returning an invalid JNI version. */
        if (rc < 0) return -1;
    }

out:
    return JNI_VERSION_1_6;
}

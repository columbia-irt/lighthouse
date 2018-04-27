#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>
#include <string.h>
#include <stdarg.h>
#include <unistd.h>
#include <jni.h>

#define PACKAGE "io/sece/unix/"
#define NATIVE_LIBRARY PACKAGE "UnixDomainSock"


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


static jint
jni_socket(JNIEnv *e, jclass lib)
{
    int s;

    s = socket(AF_UNIX, SOCK_DGRAM, 0);
    if (s == -1)
        throw(e, "java/net/SocketException", "socket: %s", strerror(errno));
    return s;
}


static void
jni_close(JNIEnv *e, jint sock)
{
    if (sock >= 0) close(sock);
}


static void
jni_connect(JNIEnv *e, jclass lib, jint sock, jstring path)
{
    int rv;
    const char *p;
    struct sockaddr_un addr;

    p = (*e)->GetStringUTFChars(e, path, NULL);

    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    memcpy(&addr.sun_path, p, strlen(p) + 1);

    rv = connect(sock, (struct sockaddr *)&addr, sizeof(addr));
    if (rv < 0) {
        throw(e, "java/net/SocketException", "connect(%s): %s",
              addr.sun_path, strerror(errno));
    }

    (*e)->ReleaseStringUTFChars(e, path, p);
}


static void
jni_bind(JNIEnv *e, jclass lib, jint sock, jstring path)
{
    int rv;
    const char *p;
    struct sockaddr_un addr;

    p = (*e)->GetStringUTFChars(e, path, NULL);

    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    memcpy(&addr.sun_path, p, strlen(p) + 1);

    rv = bind(sock, (struct sockaddr *)&addr, sizeof(addr));
    if (rv < 0) {
        throw(e, "java/net/SocketException", "bind(%s): %s",
              addr.sun_path, strerror(errno));
    }

    (*e)->ReleaseStringUTFChars(e, path, p);
}


static jint
jni_send(JNIEnv *e, jclass lib, jint sock, jbyteArray data)
{
    int rv;
    jbyte *d;
    size_t len;

    d = (*e)->GetByteArrayElements(e, data, NULL);
    len = (*e)->GetArrayLength(e, data);

    rv = send(sock, d, len, 0);
    if (rv == -1) {
        throw(e, "java/net/SocketException", "send: %s",
              strerror(errno));
    }

    (*e)->ReleaseByteArrayElements(e, data, d, 0);
    return rv;
}


static jbyteArray
jni_recv(JNIEnv *e, jclass lib, jint sock)
{
    ssize_t rv;
    jbyteArray res;
    jbyte buf[65536];

    rv = recv(sock, buf, sizeof(buf), 0);
    if (rv < 0) {
        throw(e, "java/net/SocketException", "recv: %s", strerror(errno));
        return NULL;
    }

    res = (*e)->NewByteArray(e, rv);
    (*e)->SetByteArrayRegion(e, res, 0, rv, buf);
    return res;
}


static JNINativeMethod natives[] = {
    {"socket",   "()I",                    jni_socket},
    {"close",    "(I)V",                   jni_close},
    {"connect",  "(ILjava/lang/String;)V", jni_connect},
    {"bind",     "(ILjava/lang/String;)V", jni_bind},
    {"send",     "(I[B)I",                 jni_send},
    {"recv",     "(I)[B",                  jni_recv},
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

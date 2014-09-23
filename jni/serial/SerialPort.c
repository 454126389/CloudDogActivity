/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <pthread.h>
#include <string.h>
#include <jni.h>

#include <android/log.h>

#define TAG                                     "serialport"
#define LOGD(fmt, args...) __android_log_print(ANDROID_LOG_DEBUG, TAG, fmt, ##args)

static speed_t getBaudrate(jint baudrate)
{
	switch(baudrate)
	{
	case 0: return B0;
	case 50: return B50;
	case 75: return B75;
	case 110: return B110;
	case 134: return B134;
	case 150: return B150;
	case 200: return B200;
	case 300: return B300;
	case 600: return B600;
	case 1200: return B1200;
	case 1800: return B1800;
	case 2400: return B2400;
	case 4800: return B4800;
	case 9600: return B9600;
	case 19200: return B19200;
	case 38400: return B38400;
	case 57600: return B57600;
	case 115200: return B115200;
	case 230400: return B230400;
	case 460800: return B460800;
	case 500000: return B500000;
	case 576000: return B576000;
	case 921600: return B921600;
	case 1000000: return B1000000;
	case 1152000: return B1152000;
	case 1500000: return B1500000;
	case 2000000: return B2000000;
	case 2500000: return B2500000;
	case 3000000: return B3000000;
	case 3500000: return B3500000;
	case 4000000: return B4000000;
	default: return -1;
	}
}

pthread_t thread = -1;
int bExit = 0;

void *read_thread(void *arg) {
	int fd = (int)arg;
	char buf[16 + 1];
	int size = 0;

	while(!bExit) {
		size = read(fd, buf, 16);
		if(size > 0)
		{
			buf[size] = 0;
			LOGD("%s", buf);
		}
	}

	return NULL;
}

/*
 * Class:     cedric_serial_SerialPort
 * Method:    open
 * Signature: (Ljava/lang/String;)V
 */
jobject JniOpen(JNIEnv *env, jobject thiz, jstring path, jint baudrate)
{
	int fd;
	speed_t speed;
	jobject mFileDescriptor;

	/* Check arguments */
	{
		speed = getBaudrate(baudrate);
		if (speed == -1) {
			/* TODO: throw an exception */
			LOGD("Invalid baudrate");
			return NULL;
		}
	}

	/* Opening device */
	{
		jboolean iscopy;
		const char *path_utf = (*env)->GetStringUTFChars(env, path, &iscopy);
		LOGD("Opening serial port %s", path_utf);
		fd = open(path_utf, O_RDWR);
		LOGD("open() fd = %d", fd);
		(*env)->ReleaseStringUTFChars(env, path, path_utf);
		if (fd == -1)
		{
			/* Throw an exception */
			LOGD("Cannot open port");
			/* TODO: throw an exception */
			return NULL;
		}
	}

	/* Configure device */
	{
		struct termios cfg;
		LOGD("Configuring serial port");
		if (tcgetattr(fd, &cfg))
		{
			LOGD("tcgetattr() failed");
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}
#if 0
		cfg.c_cflag &= ~CSIZE;
		cfg.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);  /*Input*/
		cfg.c_oflag &= ~OPOST;   /*Output*/


		cfg.c_cflag |= CS8;
		cfg.c_cflag &= ~PARENB; /* Clear parity enable */

		cfg.c_iflag &= ~INPCK; /* Enable parity checking */
		cfg.c_cflag &= ~CSTOPB;

		tcflush(fd, TCIFLUSH);
		*/
		cfg.c_cc[VTIME] = 0; /* ���ó�ʱ0 seconds*/
		cfg.c_cc[VMIN] = 16; /* define the minimum bytes data to be readed*/
#endif
		cfmakeraw(&cfg);
		cfsetispeed(&cfg, speed);
		cfsetospeed(&cfg, speed);

		if (tcsetattr(fd, TCSANOW, &cfg))
		{
			LOGD("tcsetattr() failed");
			close(fd);
			/* TODO: throw an exception */
			return NULL;
		}
	}

	/* Create a corresponding file descriptor */
	{
		jclass cFileDescriptor = (*env)->FindClass(env, "java/io/FileDescriptor");
		jmethodID iFileDescriptor = (*env)->GetMethodID(env, cFileDescriptor, "<init>", "()V");
		jfieldID descriptorID = (*env)->GetFieldID(env, cFileDescriptor, "descriptor", "I");
		mFileDescriptor = (*env)->NewObject(env, cFileDescriptor, iFileDescriptor);
		(*env)->SetIntField(env, mFileDescriptor, descriptorID, (jint)fd);
	}

	//pthread_create(&thread, 0, read_thread, fd);

	return mFileDescriptor;
}



/*
 * Class:     cedric_serial_SerialPort
 * Method:    close
 * Signature: ()V
 */
static void JniClose(JNIEnv *env, jobject thiz)
{
	jclass SerialPortClass = (*env)->GetObjectClass(env, thiz);
	jclass FileDescriptorClass = (*env)->FindClass(env, "java/io/FileDescriptor");

	jfieldID mFdID = (*env)->GetFieldID(env, SerialPortClass, "mFd", "Ljava/io/FileDescriptor;");
	jfieldID descriptorID = (*env)->GetFieldID(env, FileDescriptorClass, "descriptor", "I");

	jobject mFd = (*env)->GetObjectField(env, thiz, mFdID);
	jint descriptor = (*env)->GetIntField(env, mFd, descriptorID);

	LOGD("close(fd = %d)", descriptor);
	close(descriptor);
}

static JNINativeMethod gMethods[] = {
	{"open", "(Ljava/lang/String;I)Ljava/io/FileDescriptor;", JniOpen},
	{"close", "()V", JniClose},
};

#define gClassName "com/weifer/search/SerialPort"
#define gMethodNum  (sizeof(gMethods)/sizeof(JNINativeMethod))

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
	JNIEnv *env;
	jclass cls;
	if ( (*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) )
		return JNI_ERR;

	cls = (*env)->FindClass(env, gClassName);
	(*env)->RegisterNatives(env, cls, gMethods, gMethodNum);

	return JNI_VERSION_1_4;
}

void JNI_OnUnload(JavaVM* vm, void* reserved)
{
	JNIEnv *env;
	jclass cls;
	if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4))
		return;
	cls = (*env)->FindClass(env, gClassName);
	(*env)->UnregisterNatives(env, cls);
}

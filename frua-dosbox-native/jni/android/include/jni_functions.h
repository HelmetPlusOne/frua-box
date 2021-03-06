/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_helmetplusone_android_dosbox_DosboxAndroid */

#ifndef _Included_com_helmetplusone_android_dosbox_DosboxAndroid
#define _Included_com_helmetplusone_android_dosbox_DosboxAndroid
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeSetSoundEnabled
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetSoundEnabled
  (JNIEnv *, jclass, jboolean);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeSetMemorySize
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetMemorySize
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeSetFrameskip
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetFrameskip
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeSetCycleHackEnabled
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetCycleHackEnabled
  (JNIEnv *, jclass, jboolean);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeSetRefreshHackEnabled
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeSetRefreshHackEnabled
  (JNIEnv *, jclass, jboolean);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeStart
 * Signature: (Ljava/lang/String;Landroid/graphics/Bitmap;IIIIIZZZ)V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeStart
  (JNIEnv *, jobject, jstring, jobject, jint, jint, jint, jint, jint, jboolean, jboolean, jboolean);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativePause
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativePause
  (JNIEnv *, jclass);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeResume
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeResume
  (JNIEnv *, jclass);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeStop
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeStop
  (JNIEnv *, jclass);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeMouse
 * Signature: (IIIIII)V
 */
JNIEXPORT void JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeMouse
  (JNIEnv *, jclass, jint, jint, jint, jint, jint, jint);

/*
 * Class:     com_helmetplusone_android_dosbox_DosboxAndroid
 * Method:    nativeKey
 * Signature: (IIZZZ)Z
 */
JNIEXPORT jboolean JNICALL Java_com_helmetplusone_android_dosbox_DosboxAndroid_nativeKey
  (JNIEnv *, jclass, jint, jint, jboolean, jboolean, jboolean);

#ifdef __cplusplus
}
#endif
#endif

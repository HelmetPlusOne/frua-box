///*
//*  Copyright (C) 2011 Locnet (android.locnet@gmail.com)
//*
//*  This program is free software; you can redistribute it and/or modify
//*  it under the terms of the GNU General Public License as published by
//*  the Free Software Foundation; either version 2 of the License, or
//*  (at your option) any later version.
//*
//*  This program is distributed in the hope that it will be useful,
//*  but WITHOUT ANY WARRANTY; without even the implied warranty of
//*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//*  GNU General Public License for more details.
//*
//*  You should have received a copy of the GNU General Public License
//*  along with this program; if not, write to the Free Software
//*  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//*/
//
//#include <stddef.h>
//#include <jni.h>
//#include "android_iface.h"
//#include "dosbox_android.h"
//
//void Java_com_helmetplusone_android_frua_dosbox_DosBoxLauncher_nativeStart(JNIEnv * env, jobject obj, jobject bitmap,
//        jint width, jint height, jstring conf)
//{
//    const char * confpath = (*env)->GetStringUTFChars(env, conf, NULL);
//	Android_Init(env, obj, bitmap, width, height);
//
//	const char * argv[] = { "dosbox", "-conf", confpath};
//	dosbox_main(3, argv);
//
//	Android_ShutDown();
//	(*env)->ReleaseStringUTFChars(env, conf, confpath);
//}
//
//void Java_com_helmetplusone_android_frua_dosbox_DosBoxLauncher_nativeSetOption(JNIEnv * env, jobject obj, jint option, jint value)
//{
//	switch (option) {
//		case 1:
//			myLoader.soundEnable = value;
//			enableSound = (value != 0);
//			break;
//		case 2:
//			myLoader.memsize = value;
//			break;
//		case 10:
//			myLoader.cycles = value;
//			CPU_CycleMax = value;
//			CPU_SkipCycleAutoAdjust = false;
//			CPU_CycleAutoAdjust = false;
//			break;
//		case 11:
//			myLoader.frameskip = value;
//			render.frameskip.max = value;
//			break;
//		case 12:
//			myLoader.refreshHack = value;
//			enableRefreshHack = (value != 0);
//			break;
//		case 13:
//			myLoader.cycleHack = value;
//			enableCycleHack = (value != 0);
//			break;
////		case 14:
////			DOSBOX_UnlockSpeed((value != 0));
////			break;
////		case 15:
////			JOYSTICK_Enable(0, (value != 0));
////			break;
////		case 51:
////			swapInNextDisk(true);
////			break;
//	}
//}
//
//void Java_com_helmetplusone_android_frua_dosbox_DosBoxLauncher_nativeInit(JNIEnv * env, jobject obj)
//{
//	loadf = 0;
//	myLoader.memsize = 2;
//	myLoader.bmph = 0;
//	myLoader.videoBuffer = 0;
//
//	myLoader.abort = 0;
//	myLoader.pause = 0;
//
//	myLoader.frameskip = 0;
//	myLoader.cycles = 1500;
//	myLoader.soundEnable = 1;
//	myLoader.cycleHack = 1;
//	myLoader.refreshHack = 1;
//}
//
//void Java_com_helmetplusone_android_frua_dosbox_DosBoxLauncher_nativePause(JNIEnv * env, jobject obj, jint state)
//{
//	if ((state == 0) || (state == 1))
//		myLoader.pause = state;
//	else
//		myLoader.pause = (myLoader.pause)?0:1;
//}
//
//void Java_com_helmetplusone_android_frua_dosbox_DosBoxLauncher_nativeStop(JNIEnv * env, jobject obj)
//{
//	myLoader.abort = 1;
//}
//
//void Java_com_helmetplusone_android_frua_dosbox_DosBoxLauncher_nativeShutDown(JNIEnv * env, jobject obj)
//{
//	myLoader.bmph = 0;
//	myLoader.videoBuffer = 0;
//}
//

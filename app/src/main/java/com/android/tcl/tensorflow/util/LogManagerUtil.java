package com.android.tcl.tensorflow.util;

import org.apache.log4j.Logger;

import android.util.Log;

public class LogManagerUtil {
	private static final String TAG = LogUtil.makeLogTag(LogManagerUtil.class);

	public static boolean isWriteLog = false;

	public LogManagerUtil() {

	}

	public static void setLogManagerUtil(boolean versionType) {
		Log.d(TAG, "setLogManagerUtil->" + versionType);

		if (versionType) {
			isWriteLog = false;
		} else {
			isWriteLog = true;
		}
		Log.d(TAG, "isWriteLog->" + isWriteLog);
	}

	public static void d(String TAG, String str) {
		if (isWriteLog) {
			Logger log = Logger.getLogger(TAG);

			// write log
			log.debug(str);
		} else {
			Log.d(TAG, str);
		}
	}

	public static void i(String TAG, String str) {
		if (isWriteLog) {
			Logger log = Logger.getLogger(TAG);

			// write log
			log.info(str);
		} else {
			Log.i(TAG, str);
		}
	}

	public static void e(String TAG, String str) {
		if (isWriteLog) {
			Logger log = Logger.getLogger(TAG);

			// write log
			log.debug(str);
		} else {
			Log.e(TAG, str);
		}
	}
}

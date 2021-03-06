package com.cashow.utils;

import android.util.Log;

import com.cashow.evermemo.MemoActivity;

public class Logger {

	public static Boolean DEBUG = false;
	public static String Tag = "调试信息";
	public static String[] Filters = { MemoActivity.LogTag };

	public static void i(String tag, String msg) {
		if (DEBUG)
			Log.i(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (DEBUG)
			Log.e(tag, msg);
	}

	public static void e(String msg) {
		if (DEBUG)
			Logger.e(Tag, msg);
	}

	public static void i(String msg) {
		if (DEBUG)
			Logger.i(Tag, msg);
	}

}

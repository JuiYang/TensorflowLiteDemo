package com.android.tcl.tensorflow.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Global {
	public static String NETWORKSCAN_VERSION = "Recognize_CNNdroid-0.0.01"; // Recognize_CNNdroid版本
	
	public static final String SHARED_PREFERENCE_NAME = "client_preferences";

	// is or no beta
	public static final boolean IsBeta = false;

	// is or no phone
	public static final boolean IsPhone = false;

	public static boolean getIsBeta(Context context) {

//		boolean isBeta = IsBeta;
//		String versionname = "";
//		try {
//			PackageManager packageManager = context.getPackageManager();
//			PackageInfo packInfo = null;
//			String packageName = context.getPackageName();
//			packInfo = packageManager.getPackageInfo(packageName, 0);
//			versionname = packInfo.versionName;
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		if (versionname != null && !"".equals(versionname)) {
//			if (!versionname.contains("beta")) {
//				isBeta = false;
//			} else {
//				isBeta = true;
//			}
//		}
//		return isBeta;
		String str = "beta";
		return getPhoneInfo(context,str);
	}

	public static boolean getIsPhone(Context context) {

//		boolean isPhone = IsPhone;
//		String versionname = "";
//		try {
//			PackageManager packageManager = context.getPackageManager();
//			PackageInfo packInfo = null;
//			String packageName = context.getPackageName();
//			packInfo = packageManager.getPackageInfo(packageName, 0);
//			versionname = packInfo.versionName;
//		} catch (NameNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		if (versionname != null && !"".equals(versionname)) {
//			if (!versionname.contains("phone")) {
//				isPhone = false;
//			} else {
//				isPhone = true;
//			}
//		}
//		return isPhone;
		String str = "phone";
		return getPhoneInfo(context, str);
	}

	public static boolean getPhoneInfo(Context context, String str){
		Boolean flag = false;
		String versionname = "";
		try {
			PackageManager packageManager = context.getPackageManager();
			PackageInfo packInfo = null;
			String packageName = context.getPackageName();
			packInfo = packageManager.getPackageInfo(packageName, 0);
			versionname = packInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if (versionname != null && !"".equals(versionname)) {
			if (!versionname.contains(str)) {
				flag = false;
			} else {
				flag = true;
			}
		}
		return flag;
	}
}

package com.android.tcl.tensorflow.util;

import android.os.Environment;

public class Constant {
    public static final String STORAGE_PATH = Environment.getExternalStorageDirectory().toString();
    public static final String TEST_PATH = STORAGE_PATH + "/DCIM" + "/Imgtest";
    	public static final int InputWidth = 272;
	public static final int InputHeight = 272;
//    public static final int InputWidth = 224;
//    public static final int InputHeight = 224;
//    	public static final int OutputWidth = 192;
//	public static final int OutputHeight = 192;
    public static final int OutputWidth = 136;
    public static final int OutputHeight = 136;
    public static final int MESSAGE_NO_SUBFLODER = 1001;

}

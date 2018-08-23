package com.android.tcl.tensorflow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Level;

import com.android.tcl.tensorflow.util.Global;
import com.android.tcl.tensorflow.util.LogManagerUtil;
import com.android.tcl.tensorflow.util.LogUtil;
import com.android.tcl.tensorflowlite.R;
import com.example.android.tflitecamerademo.ImageClassifier;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class RecognizeTensorFlow extends Activity {
	private static final String TAG = LogUtil.makeLogTag(RecognizeTensorFlow.class);

	public static boolean isPhone = false;
	public static boolean isBeta = false;

	public Button button_reset;

	public EditText messagecontent_RecognizeCNNdroid_VERSION;
	public EditText messagecontent_recognizeInterval;
	public EditText messagecontent_usbRootPath;

	public ImageClassifier imageClassifier;

	public ImageView imageview;

	public String imageAbsPath = "";

	public static final int REQUEST_CODE_PICK_IMAGE = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.cnndroid);

		// ningyb config log4j for write logg
		try {
			isPhone = Global.getIsPhone(RecognizeTensorFlow.this);
			Log.d(TAG, "isPhone->" + isPhone);

			if (!isPhone) {
				LogConfigurator logConfigurator = new LogConfigurator();
				File saveFile = new File(getFilesDir() + "/dailyLog");
				if (saveFile.isDirectory() == false) {
					saveFile.mkdirs();// 创建下载目录
				}
				try {// ningyb 修改文件权限
					Runtime runtime = Runtime.getRuntime();
					runtime.exec("chmod 777 " + getFilesDir() + "/dailyLog");
					runtime.exec("chmod 777 " + getFilesDir() + "/dailyLog/dailyLog.log");
				} catch (Exception e) {
					e.printStackTrace();
				}
				logConfigurator.setFileName(getFilesDir() + "/dailyLog/dailyLog.log");
				logConfigurator.setRootLevel(Level.DEBUG);
				logConfigurator.setLevel("org.apache", Level.ERROR);
				logConfigurator.setFilePattern("%d %-5p [%c{3}]-[%L] %m%n");
				logConfigurator.setMaxFileSize(1024 * 1024 * 1);// 文件最大1M
				logConfigurator.setImmediateFlush(true);
				logConfigurator.configure();
			} else {
				LogConfigurator logConfiguratorNew = new LogConfigurator();
				File saveFileNew = new File(getExternalFilesDir(null) + "/dailyLog");
				boolean mkdirsType = true;
				if (saveFileNew.isDirectory() == false) {
					mkdirsType = saveFileNew.mkdirs();// 创建下载目录
				}
				LogManagerUtil.d(TAG, "mkdirsType->" + mkdirsType);
				if (mkdirsType) {
					try {// ningyb 修改文件权限
						Runtime runtime = Runtime.getRuntime();
						runtime.exec("chmod 777 " + getExternalFilesDir(null) + "/dailyLog");
						runtime.exec("chmod 777 " + getExternalFilesDir(null) + "/dailyLog/dailyLog.log");
					} catch (Exception e) {
						e.printStackTrace();
					}
					logConfiguratorNew.setFileName(getExternalFilesDir(null) + "/dailyLog/dailyLog.log");
					logConfiguratorNew.setRootLevel(Level.DEBUG);
					logConfiguratorNew.setLevel("org.apache", Level.ERROR);
					logConfiguratorNew.setFilePattern("%d %-5p [%c{3}]-[%L] %m%n");
					logConfiguratorNew.setMaxFileSize(1024 * 1024 * 2);// 文件最大1M
					logConfiguratorNew.setImmediateFlush(true);
					logConfiguratorNew.configure();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogManagerUtil.d(TAG, "onCreate-isPhone->" + isPhone);

		isBeta = Global.getIsBeta(RecognizeTensorFlow.this);
		LogManagerUtil.d(TAG, "isBeta->" + isBeta);

		try {
			button_reset = (Button) findViewById(R.id.button_reset);

			messagecontent_RecognizeCNNdroid_VERSION = (EditText) findViewById(
					R.id.messagecontent_RecognizeCNNdroid_VERSION);
			messagecontent_recognizeInterval = (EditText) findViewById(R.id.messagecontent_recognizeInterval);
			messagecontent_usbRootPath = (EditText) findViewById(R.id.messagecontent_usbRootPath);

			imageview = (ImageView) findViewById(R.id.imageview);

			String RecognizeCNNdroid_VERSION = getAppInfo();
			messagecontent_RecognizeCNNdroid_VERSION.setText(RecognizeCNNdroid_VERSION);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// // for TV
		// try {
		// LogManagerUtil.d(TAG,
		// "RecognizeTensorFlowService.mRecognizeTensorFlowService->"
		// + RecognizeTensorFlowService.mRecognizeTensorFlowService);
		// if (RecognizeTensorFlowService.mRecognizeTensorFlowService == null) {
		// Intent it = new Intent(RecognizeTensorFlow.this,
		// RecognizeTensorFlowService.class);
		// startService(it);
		// } else {
		// LogManagerUtil.d(TAG,
		// "RecognizeTensorFlowService.mRecognizeTensorFlowService is no null");
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }

		// for Phone
		copyFile();

		try {
			imageClassifier = new ImageClassifier(RecognizeTensorFlow.this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onBackPressed() {
		LogManagerUtil.d(TAG, "onBackPressed");

		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		LogManagerUtil.d(TAG, "onDestroy");

		super.onDestroy();
	}

	public String getAppInfo() {
		LogManagerUtil.d(TAG, "getAppInfo");

		String appInfo = "";
		try {
			PackageManager pm = getPackageManager();
			PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
			appInfo = getPackageName() + "-" + packageInfo.versionName + "/" + packageInfo.versionCode;
		} catch (Exception e) {
			e.printStackTrace();

			LogManagerUtil.e(TAG, e.toString());
		}

		LogManagerUtil.d(TAG, "appInfo->" + appInfo);
		return appInfo;
	}

	public void appADSettingReset(View view) {
		LogManagerUtil.d(TAG, "appADSettingReset");

		try {
			button_reset.setText("Recognize process ...");

			String RecognizeCNNdroid_VERSION = getAppInfo();
			LogManagerUtil.d(TAG, "ResetforShow-RecognizeCNNdroid_VERSION->" + RecognizeCNNdroid_VERSION);

			messagecontent_RecognizeCNNdroid_VERSION.setText(RecognizeCNNdroid_VERSION);
			messagecontent_usbRootPath.setText("");

			// for Phone
			new Thread(new Runnable() {
				public void run() {
					LogManagerUtil.d(TAG, "RecognizeThread run");

					final String result = MainTvLogoRecognize();

					try {
						RecognizeTensorFlow.this.runOnUiThread(new Runnable() {
							public void run() {
								LogManagerUtil.d(TAG, "recognizeResulttoUI->" + result);

								button_reset.setText("Recognize start");
								messagecontent_usbRootPath.setText(result);
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String MainTvLogoRecognize() {
		LogManagerUtil.d(TAG, "MainTvLogoRecognize");

		String recognizeResult = "TensorFlow识别apk，默认识别结果";
		try {
			if (imageAbsPath == null || "".equals(imageAbsPath)) {
				return recognizeResult;
			}
			LogManagerUtil.d(TAG, "imageAbsPath->" + imageAbsPath);

			Bitmap bitmap = BitmapFactory.decodeFile(imageAbsPath);
			LogManagerUtil.d(TAG, "bitmap->" + bitmap);

			if (bitmap != null) {
				LogManagerUtil.d(TAG, "imageClassifier.classifyFrame(bitmap)");

				if (imageClassifier != null) {
					recognizeResult = imageClassifier.classifyFrame(RecognizeTensorFlow.this, bitmap, imageAbsPath);
				}

				bitmap.recycle();
			} else {
				recognizeResult = "TensorFlow识别apk，图片不存在";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		LogManagerUtil.d(TAG, "recognizeResult->" + recognizeResult);
		return recognizeResult;
	}

	@SuppressLint("SdCardPath")
	private void copyFile() {
		LogManagerUtil.d(TAG, "copyFile");

		try {
			LogManagerUtil.d(TAG, "copyFile start-labels.txt");

			String fileName = getFilesDir() + "/labels.txt";
			InputStream assetsFile;
			// File file = new File(fileName);
			// if (!file.exists()) {
			// LogManagerUtil.d(TAG, fileName + " file.exists() is false");

			try {
				assetsFile = getAssets().open("labels.txt");
				OutputStream fileOut = new FileOutputStream(fileName);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = assetsFile.read(buffer)) > 0) {
					fileOut.write(buffer, 0, length);
				}

				fileOut.flush();
				fileOut.close();
				assetsFile.close();

				LogManagerUtil.d(TAG, "copyFile end-labels.txt");
			} catch (IOException e) {
				e.printStackTrace();
			}
			// } else {
			// LogManagerUtil.d(TAG, fileName + " file.exists() is true");
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			LogManagerUtil.d(TAG, "copyFile start-mobilenet_quant_v1_224.tflite");

			String fileName = getFilesDir() + "/mobilenet_quant_v1_224.tflite";
			InputStream assetsFile;
			// File file = new File(fileName);
			// if (!file.exists()) {
			// LogManagerUtil.d(TAG, fileName + " file.exists() is false");

			try {
				assetsFile = getAssets().open("mobilenet_quant_v1_224.tflite");
				OutputStream fileOut = new FileOutputStream(fileName);
				byte[] buffer = new byte[1024];
				int length;
				while ((length = assetsFile.read(buffer)) > 0) {
					fileOut.write(buffer, 0, length);
				}

				fileOut.flush();
				fileOut.close();
				assetsFile.close();

				LogManagerUtil.d(TAG, "copyFile end-mobilenet_quant_v1_224.tflite");
			} catch (IOException e) {
				e.printStackTrace();
			}
			// } else {
			// LogManagerUtil.d(TAG, fileName + " file.exists() is true");
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void appPicturesChoose(View view) {
		LogManagerUtil.d(TAG, "appPicturesChoose");

		messagecontent_recognizeInterval.setText("");
		imageAbsPath = "";

		/**
		 * 打开选择图片的界面
		 */
		try {
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");// 相片类型
			startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogManagerUtil.d(TAG, "onActivityResult-requestCode/resultCode->" + requestCode + "/" + resultCode);

		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		/**
		 * 从相册中选取图片的请求标志
		 */
		case REQUEST_CODE_PICK_IMAGE:
			if (resultCode == RESULT_OK) {
				try {
					/**
					 * 该uri是上一个Activity返回的
					 */
					Uri uri = data.getData();
					imageAbsPath = getAbsoluteImagePath(uri);
					LogManagerUtil.d(TAG, "imageAbsPath->" + imageAbsPath);

					messagecontent_recognizeInterval.setText(imageAbsPath);
				} catch (Exception e) {
					e.printStackTrace();

					Log.d(TAG, "获取图片失败");
				}
			} else {
				Log.d(TAG, "获取图片失败");
			}
			break;

		default:
			break;
		}
	}

	public String getAbsoluteImagePath(Uri uri) {
		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(uri, proj, // Which columns to return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)

		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(column_index);
	}
}
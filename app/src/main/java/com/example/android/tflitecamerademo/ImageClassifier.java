/* Copyright 2017 The TensorFlow Authors. All Rights Reserved.



Licensed under the Apache License, Version 2.0 (the "License");

you may not use this file except in compliance with the License.

You may obtain a copy of the License at



    http://www.apache.org/licenses/LICENSE-2.0



Unless required by applicable law or agreed to in writing, software

distributed under the License is distributed on an "AS IS" BASIS,

WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

See the License for the specific language governing permissions and

limitations under the License.

==============================================================================*/

package com.example.android.tflitecamerademo;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.NativeInterpreterWrapper;
import org.tensorflow.lite.TensorFlowLite;

import com.android.tcl.tensorflow.util.Constant;
import com.android.tcl.tensorflow.util.LogManagerUtil;
import com.android.tcl.tensorflow.util.LogUtil;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.os.SystemClock;
import android.util.Log;

/**
 * Classifies images with Tensorflow Lite.
 */

public class ImageClassifier {
    private static final String TAG = LogUtil.makeLogTag(ImageClassifier.class);

    // /** Tag for the {@link Log}. */
    //
    // private static final String TAG = "ImageClassifier";

    /**
     * Name of the model file stored in Assets.
     */

    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";

    /**
     * Name of the label file stored in Assets.
     */

    private static final String LABEL_PATH = "labels.txt";

    /**
     * Number of results to show in the UI.
     */

    private static final int RESULTS_TO_SHOW = 3;

    /**
     * Dimensions of inputs.
     */

    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;

//    private static final int DIM_PIXEL_SIZE = 1;

    static final int DIM_IMG_SIZE_X = Constant.InputWidth;

    static final int DIM_IMG_SIZE_Y = Constant.InputHeight;

    /* Preallocated buffers for storing image data in. */

    private int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    /**
     * An instance of the driver class to run model inference with Tensorflow
     * Lite.
     */

    private Interpreter tflite;

    /**
     * Labels corresponding to the output of the vision model.
     */

    private List<String> labelList;

    /**
     * A ByteBuffer to hold image data, to be feed into Tensorflow Lite as
     * inputs.
     */

    private ByteBuffer imgData = null;
//    private float[][][][] imgData = new float[1][DIM_IMG_SIZE_X][DIM_IMG_SIZE_Y][3];
//	private FloatBuffer imgData = null;
    // private float[] imgData = new float[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * 3];

    // private float[][][][] imgData = null;
    // private float[][] labelProbArray = null;

    /**
     * An array to hold inference results, to be feed into Tensorflow Lite as
     * outputs.
     */

    private float[][] labelProbArrayFloat = null;
    private float[][] filterLabelProbArray = null;
    private static final int FILTER_STAGES = 3;
    // private static final float FILTER_FACTOR = 0.4f;
    private static final int numBytesPerChannel = 4;

    /**
     * The inception net requires additional normalization of the used input.
     */
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    private PriorityQueue<Map.Entry<String, Float>> sortedLabels =

            new PriorityQueue<>(RESULTS_TO_SHOW, new Comparator<Map.Entry<String, Float>>() {

                @Override
                public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {

                    return (o1.getValue()).compareTo(o2.getValue());
                }
            });

    /**
     * Initializes an {@code ImageClassifier}.
     */

    public ImageClassifier(Context context) throws IOException {
        Log.d(TAG, "ImageClassifier");

        String version = TensorFlowLite.version();
        Log.d(TAG, "TF-version->" + version);

        String filePath = context.getFilesDir() + "/" + MODEL_PATH;
        File modelFile = new File(filePath);
        tflite = new Interpreter(modelFile);

        labelList = loadLabelList(context);
        // labelList = Labels.labelList;
        Log.d(TAG, "labelList.size()->" + labelList.size());

        // imgByteBuffer Input
        imgData = ByteBuffer
                .allocateDirect(DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE * numBytesPerChannel);
        imgData.order(ByteOrder.nativeOrder());


        labelProbArrayFloat = new float[1][getNumLabels()];
        filterLabelProbArray = new float[FILTER_STAGES][getNumLabels()];
        Log.d(TAG, "NativeInterpreterWrapper.dataTypeOf(filterLabelProbArray)-init->"
                + NativeInterpreterWrapper.dataTypeOf(filterLabelProbArray));

        Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
    }

    /**
     * Classifies a frame from the preview stream.
     */

    public String classifyFrame(Context context, Bitmap croppedBitmap, String filepath) {

        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");

            return "Uninitialized Classifier.";
        }

        LogManagerUtil.d(TAG, "recognizeImage start");
        long startTime0 = SystemClock.uptimeMillis();

        // resize to 224x224
        Bitmap bitmap = Bitmap.createScaledBitmap(croppedBitmap, Constant.InputWidth, Constant.InputHeight, false);

//		float[][][] outputsInt = new float[152][152][3];
//        float[][][] outputsInt = new float[192][192][3];
        float[][][] outputsInt = new float[136][136][3];

        LogManagerUtil.d(TAG, "recognizeImage start1");

        convertBitmapToByteBuffer(bitmap);

        // Here's where the magic happens!!!

        long startTime = SystemClock.uptimeMillis();

//		Log.d(TAG, "NativeInterpreterWrapper.dataTypeOf(imgData)->" + NativeInterpreterWrapper.dataTypeOf(imgData));

//		Log.d(TAG, "NativeInterpreterWrapper.dataTypeOf(labelProbArrayFloat)->"
//				+ NativeInterpreterWrapper.dataTypeOf(labelProbArrayFloat));

        tflite.run(imgData, outputsInt);

        long endTime = SystemClock.uptimeMillis();

        Log.d(TAG, "Timecost to run model inference: " + Long.toString(endTime - startTime0));

//        int[] outputsIntResize = new int[Constant.OutputWidth * Constant.OutputHeight];
        int[] outputsIntResize_dep = new int[Constant.OutputWidth * Constant.OutputHeight];
        int[] outputsIntResize_sm = new int[Constant.OutputWidth * Constant.OutputHeight];

        int index_sm = 0;
        float xmax, xmin;
        xmax = xmin = outputsInt[0][0][0];

        int index_dep = 0;


        for (int i = 0; i < Constant.OutputWidth; i++) {
            for (int j = 0; j < Constant.OutputHeight; j++) {
//                int val = (int) outputsInt[i][j][0];
//                outputsIntResize[index_dep++] = Color.rgb(val, val, val);

                float val_dep = outputsInt[i][j][0];
                // 获取数组的最大最小值
                if (val_dep > xmax) {
                    xmax = val_dep;
                }
                if (val_dep <= xmin) {
                    xmin = val_dep;
                }
                int val_sm = (int) outputsInt[i][j][1];
                outputsIntResize_sm[index_sm++] = Color.rgb(val_sm, val_sm, val_sm);

            }
        }

//        Log.d("xmax value:-----------", Float.toString(xmax));
//        Log.d("xmin value:-----------", Float.toString(xmin));


        outputsIntResize_dep = normalize(xmax, xmin, outputsInt, outputsIntResize_dep);

        long t3 = System.currentTimeMillis();

//                Bitmap bitmapImg_dep = Bitmap.createBitmap(Constant.OutputWidth, Constant.OutputHeight, Config.RGB_565);
//                bitmapImg_dep.setPixels(outputsIntResize, 0, Constant.OutputWidth, 0, 0, Constant.OutputWidth, Constant.OutputHeight);
//                saveBitmap(context, bitmapImg_dep, filepath, false);


        Bitmap bitmapImg_dep = Bitmap.createBitmap(Constant.OutputWidth, Constant.OutputHeight, Config.RGB_565);
        bitmapImg_dep.setPixels(outputsIntResize_dep, 0, Constant.OutputWidth, 0, 0, Constant.OutputWidth, Constant.OutputHeight);
        saveBitmap(context, bitmapImg_dep, filepath, true);

        Bitmap bitmapImg_sm = Bitmap.createBitmap(Constant.OutputWidth, Constant.OutputHeight, Config.RGB_565);
        bitmapImg_sm.setPixels(outputsIntResize_sm, 0, Constant.OutputWidth, 0, 0, Constant.OutputWidth, Constant.OutputHeight);
        saveBitmap(context, bitmapImg_sm, filepath, false);

        // String textToShow = printTopKLabels();

        long endTime0 = SystemClock.uptimeMillis();

        String textToShow = "";
        textToShow = Long.toString(endTime0 - startTime0) + "ms ";
        LogManagerUtil.d(TAG, "recognizeImage end");

        return textToShow;
    }


    /**
     * bitemap rgb normalize
     */
    private int[] normalize(float xmax, float xmin, float[][][] outputsInt, int[] outputsIntResize_dep) {
        int ymax = 255;
        int ymin = 0;
        int index_dep = 0;
        for (int i = 0; i < Constant.OutputWidth; i++) {
            for (int j = 0; j < Constant.OutputHeight; j++) {
                float result = outputsInt[i][j][0];
                // 归一化
                float normalValue = ((ymax - ymin) * (result - xmin) / (xmax - xmin) + ymin);
                // outputsInt[i][j][0] = Math.round(normalValue);
                // 处理rgb
                int val = Math.round(normalValue);
                outputsIntResize_dep[index_dep++] = Color.rgb(val, val, val);
            }
        }
        return outputsIntResize_dep;
    }


    protected int getNumLabels() {
        return labelList.size();
    }

    /**
     * Closes tflite to release resources.
     */

    public void close() {

        tflite.close();

        tflite = null;
    }

    /**
     * Reads label list from Assets.
     */

    private List<String> loadLabelList(Context context) throws IOException {

        List<String> labelList = new ArrayList<String>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(LABEL_PATH)));

        String line;

        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }

        reader.close();

        return labelList;
    }

    /**
     * Memory-map the model file in Assets.
     */
    @SuppressWarnings({"resource", "unused"})
    private MappedByteBuffer loadModelFile(Context context) throws IOException {

        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

    }

    /**
     * Writes Image data into a {@code ByteBuffer}.
     */

    private void convertBitmapToByteBuffer(Bitmap bitmap) {

        if (imgData == null) {
            return;
        }

        imgData.rewind();

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        // Convert the image to floating point.

        int pixel = 0;

        long startTime = SystemClock.uptimeMillis();

        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];

                // imgData[0][i][j][0] = (float) ((val >> 16) & 0xFF);
                // imgData[0][i][j][1] = (float) ((val >> 8) & 0xFF);
                // imgData[0][i][j][2] = (float) (val & 0xFF);

//				 imgData.put((byte) ((val >> 16) & 0xFF));
//				 imgData.put((byte) ((val >> 8) & 0xFF));
//				 imgData.put((byte) (val & 0xFF));


//				img = (2.0 / 255.0) * img - 1.0
//						imgData.putFloat((float) (((val >> 16) & 0xFF)*(2.0 / 255.0)- 1.0) );
//				imgData.putFloat((float) (((val >> 8) & 0xFF)*(2.0 / 255.0)- 1.0));
//				imgData.putFloat((float) ((val & 0xFF)*(2.0 / 255.0)- 1.0) );

//				 imgData.put((byte) ((((val >> 16) & 0xFF)- IMAGE_MEAN)/IMAGE_STD));
//				 imgData.put((byte) ((((val >> 8) & 0xFF)- IMAGE_MEAN)/IMAGE_STD));
//				 imgData.put((byte) (((val & 0xFF)- IMAGE_MEAN)/IMAGE_STD));

                // byteBuffer Input
                imgData.putFloat(((val >> 16) & 0xFF));
                imgData.putFloat(((val >> 8) & 0xFF));
                imgData.putFloat(val & 0xFF);
/**
 *              // imgArray Input
                 imgData[0][i][j][0] = (float) ((val >> 16) & 0xFF);
                 imgData[0][i][j][1] = (float) ((val >> 8) & 0xFF);
                 imgData[0][i][j][2] = (float) (val & 0xFF);
 */
            }
        }

        long endTime = SystemClock.uptimeMillis();

        Log.d(TAG, "Timecost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
    }

    /**
     * Prints top-K labels, to be shown in UI as the results.
     */
    private String printTopKLabels() {
        Log.d(TAG, "printTopKLabels");

        for (int i = 0; i < labelList.size(); ++i) {
            // Log.d(TAG, "labelProbArrayFloat[0][i]->" +
            // labelProbArrayFloat[0][i]);

            sortedLabels.add(new AbstractMap.SimpleEntry<>(labelList.get(i), labelProbArrayFloat[0][i]));

            if (sortedLabels.size() > RESULTS_TO_SHOW) {
                sortedLabels.poll();// poll:相当于先get然后再remove掉,就是查看的同时,也将这个元素从容器中删除掉
            }
        }

        String textToShow = "";

        final int size = sortedLabels.size();
        Log.d(TAG, "size->" + size);

        float value = 0;

        for (int i = 0; i < size; ++i) {

            Map.Entry<String, Float> label = sortedLabels.poll();

            Log.d(TAG, "label.getKey() + : + Float.toString(label.getValue()->" + label.getKey() + ":"
                    + Float.toString(label.getValue()));

            if (value < label.getValue()) {
                value = label.getValue();
                // textToShow = label.getKey() + ":" +
                // Float.toString(label.getValue());
                textToShow = String.format("%s: %.2f", label.getKey(), label.getValue());
            }
        }

        return textToShow;
    }


    private String saveBitmap(Context context, Bitmap bitmap, String imgPath, Boolean flag) {
        LogManagerUtil.d(TAG, "saveBitmap");
        String filePath = "";
//		Bitmap cutImgBitmap = Bitmap.createScaledBitmap(bitmap, 1200, 1600, false);
        try {
            String imgName = imgPath.substring(0, imgPath.indexOf("."));
            if (flag) {
                filePath = imgName + "-dep.jpg";
            } else {
                filePath = imgName + "-sm.jpg";
            }
            File file = new File(filePath);
            FileOutputStream resultfos = new FileOutputStream(file);
            BufferedOutputStream resultbos = new BufferedOutputStream(resultfos);
            if (bitmap != null) {
                //whj 2018-07-09
                bitmap.compress(CompressFormat.JPEG, 100, resultbos);
            }
            resultfos.flush();
            resultbos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return filePath;
    }
}
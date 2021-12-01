package com.tsubauaaa.flutter_d2go;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>BitmapUtils</>
 *
 * Utility class to convert bitmaps from camera streaming images and metadata.
 */
public class BitmapUtils {

    private static Context context;

    public BitmapUtils(Context context) {
        this.context = context;
    }

    /**
     * <p>Convert to Bitmap for inferring from imageMap</>
     *
     * @param imageMap Map of camera streaming images and metadata.
     * @param inputWidth Width size for inference image resizing.
     * @param inputHeight Height size for inference image resizing.
     * @return Bitmap for inference converted from imagemap
     */
    public static Bitmap getBitmap(HashMap imageMap, int inputWidth, int inputHeight){
        // Resize bitmap for inference
        Bitmap bitmap = Bitmap.createScaledBitmap(yuv420toBitMap(imageMap), inputWidth, inputHeight, true);

        // Tilt the bitmap 90 degrees, taking into account the impact of orientation
        Matrix matrix = new Matrix();
        matrix.postRotate((int) imageMap.get("rotation"));
        return Bitmap.createBitmap(bitmap, 0, 0, inputWidth, inputHeight, matrix, true);
    }


    /**
     * <p>Convert imageMap to YUV420 NV21 format byte[] and convert to Bitmap</>
     *
     * @param imageMap Map of camera streaming images and metadata.
     * @return Bitmap converted from imageMap.
     */
    private static Bitmap yuv420toBitMap(final HashMap imageMap) {
        ArrayList<Map> planes = (ArrayList) imageMap.get("planes");
        int w = (int) imageMap.get("width");
        int h = (int)imageMap.get("height");
        byte[] data = yuv420toNV21(w, h, planes);

        RenderScript rs = RenderScript.create(context);

        Bitmap bitmap = Bitmap.createBitmap(w, h,   Bitmap.Config.ARGB_8888);
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(data.length);
        Allocation in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        in.copyFrom(data);

        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(w).setY(h);
        Allocation out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);

        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);

        out.copyTo(bitmap);
        return bitmap;
    }


    /**
     * <p>Convert camera streaming images to YUV420 NV21 format byte[]</>
     *
     * @param width Width size of the image to be inferred.
     * @param height Height size of the image to be inferred
     * @param planes Plain data of the image to be inferred.
     * @return YUV420 NV21 format byte [] converted from camera streaming images.
     */
    private static byte[] yuv420toNV21(int width,int height, ArrayList<Map> planes){
        byte[] yBytes = (byte[]) planes.get(0).get("bytes"),
                uBytes= (byte[]) planes.get(1).get("bytes"),
                vBytes= (byte[]) planes.get(2).get("bytes");
        final int color_pixel_stride =(int) planes.get(1).get("bytesPerPixel");

        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
        try {
            outputBytes.write(yBytes);
            outputBytes.write(vBytes);
            outputBytes.write(uBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] data = outputBytes.toByteArray();
        final int y_size = yBytes.length;
        final int u_size = uBytes.length;
        final int data_offset = width * height;
        for (int i = 0; i < y_size; i++) {
            data[i] = (byte) (yBytes[i] & 255);
        }
        for (int i = 0; i < u_size / color_pixel_stride; i++) {
            data[data_offset + 2 * i] = vBytes[i * color_pixel_stride];
            data[data_offset + 2 * i + 1] = uBytes[i * color_pixel_stride];
        }
        return data;
    }

}

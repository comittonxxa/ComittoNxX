package src.comitton.jni;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import jp.dip.muracoro.comittonx.R;
import src.comitton.common.DEF;

public class CallImgLibrary {
	private static final String TAG = "CallImgLibrary";

	static {
		// JNIライブラリのロード
		System.loadLibrary("comitton");
	}
	public static final int RESULT_OK = 0;
	public static final int RESULT_ALLOC_ERR = 1;
	
	private static native int ImageInitialize(long loadsize, int buffnum, int totalpage, int maxthreadnum);
	private static native int ImageTerminate(int index);
	private static native int ImageGetFreeSize(int index);
	private static native int ImageFree(int index, int page);
	private static native int ImageScaleFree(int index, int page, int half);

	private static native int ImageSetPage(int index, int page, long size);
	private static native int ImageSetData(int index, byte[] data, int size);
	private static native int ImageSetFileSize(int index, long size);
	private static native int ImageGetSize(int index, int type, int[] imagesize);
	private static native int ImageSetBitmap(int index, Bitmap bitmap);
	private static native int ImageConvert(int index, int type, int scale);
	private static native int ImageGetBitmap(int index, int type, int scale, Bitmap bitmap);

	private static native int GetMarginSize(int index, int Page, int Half, int Index, int Margin, int MarginColor, int[] size);
	private static native int ImageScale(int index, int page, int half, int width, int height, int left, int right, int top, int bottom, int algorithm, int rotate, int margin, int margincolor, int sharpen, int bright, int gamma, int param, int size[]);
	private static native int ImageDraw(int index, int page, int half, int x, int y, Bitmap bitmap);
	private static native int ImageScaleDraw(int index, int page, int rotate, int sx, int sy, int scx, int scy, int dx, int dy, int dcx, int dcy, int psel, Bitmap bm, int cutLeft, int cutRight, int cutTop, int cutBottom);
	private static native int ImageCancel(int index, int flag);

	public static native int ThumbnailInitialize(long id, int pagesize, int pagenum, int imageNum);
	public static native int ThumbnailCheck(long id, int index);
	public static native int ThumbnailSetNone(long id, int index);
	public static native int ThumbnailCheckAll(long id);
	public static native int ThumbnailMemorySizeCheck(long id, int width, int height);
	public static native int ThumbnailImageAlloc(long id, int blocks, int index);
	public static native int ThumbnailSave(long id, Bitmap bitmap, int index);
	public static native int ThumbnailImageSize(long id, int index);
	public static native int ThumbnailDraw(long id, Bitmap bitmap, int index);
	public static native int ThumbnailFree(long id);

	// スレッド数設定
	public static native int SetParameter(int threadnum);

	public static int ImageScaleParam(int invert, int gray, int coloring, int moire, int pseland) {
		int val = (invert != 0 ? 2 : 0)
				+ (gray != 0 ? 4 : 0)
				+ (coloring != 0 ? 8 : 0)
				+ (moire != 0 ? 16 : 0)
				+ (pseland != 0 ? 32 : 0);
		return val;
	}

	public static int ImageInitialize(Context context, Handler handler, long loadsize, int buffnum, int totalpage, int maxthreadnum){
		int ret =  ImageInitialize(loadsize, buffnum, totalpage, maxthreadnum);
		//Log.d(TAG, "ImageInitialize: index=" + ret);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageTerminate(Context context, Handler handler, int index) {
		//Log.d(TAG, "ImageTerminate: index=" + index);
		int ret = ImageTerminate(index);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageGetFreeSize(Context context, Handler handler, int index) {
		//Log.d(TAG, "ImageGetFreeSize: index=" + index);
		int ret = ImageGetFreeSize(index);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageFree(Context context, Handler handler, int index, int page) {
		//Log.d(TAG, "ImageFree: index=" + index);
		int ret = ImageFree(index, page);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageScaleFree(Context context, Handler handler, int index, int page, int half) {
		//Log.d(TAG, "ImageScaleFree: index=" + index);
		int ret = ImageScaleFree(index, page, half);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageSetPage(Context context, Handler handler, int index, int page, long size) {
		//Log.d(TAG, "ImageSetPage: index=" + index);
		int ret = ImageSetPage(index, page, size);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageSetData(Context context, Handler handler, int index, byte[] data, int size) {
		//Log.d(TAG, "ImageSetData: index=" + index);
		int ret = ImageSetData(index, data, size);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageSetFileSize(Context context, Handler handler, int index, long size) {
		//Log.d(TAG, "ImageSetFileSize: index=" + index);
		int ret = ImageSetFileSize(index, size);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageGetSize(Context context, Handler handler, int index, int type, int[] imagesize) {
		//Log.d(TAG, "ImageGetSize: index=" + index);
		int ret = ImageGetSize(index, type, imagesize);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageSetBitmap(Context context, Handler handler, int index, Bitmap bitmap) {
		//Log.d(TAG, "ImageSetBitmap: index=" + index);
		int ret = ImageSetBitmap(index, bitmap);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageConvert(Context context, Handler handler, int index, int type, int scale) {
		//Log.d(TAG, "ImageConvert: index=" + index);
		int ret = ImageConvert(index, type, scale);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageGetBitmap(Context context, Handler handler, int index, int type, int scale, Bitmap bitmap) {
		//Log.d(TAG, "ImageGetBitmap: index=" + index);
		int ret = ImageGetBitmap(index, type, scale, bitmap);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int GetMarginSize(Context context, Handler handler, int index, int Page, int Half, int Index, int Margin, int MarginColor, int[] size) {
		//Log.d(TAG, "GetMarginSize: index=" + index);
		int ret = GetMarginSize(index, Page, Half, Index, Margin, MarginColor, size);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageScale(Context context, Handler handler, int index, int page, int half, int width, int height, int left, int right, int top, int bottom, int algorithm, int rotate, int margin, int margincolor, int sharpen, int bright, int gamma, int param, int[] size) {
		//Log.d(TAG, "ImageScale: index=" + index);
		int ret = ImageScale(index, page, half, width, height, left, right, top, bottom, algorithm, rotate, margin, margincolor, sharpen, bright, gamma, param, size);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageDraw(Context context, Handler handler, int index, int page, int half, int x, int y, Bitmap bitmap) {
		//Log.d(TAG, "ImageDraw: index=" + index);
		int ret = ImageDraw(index, page, half, x, y, bitmap);
		checkResult(context, handler, ret);
		return ret;
	}

	public static int ImageScaleDraw(Context context, Handler handler, int index, int page, int rotate, int sx, int sy, int scx, int scy, int dx, int dy, int dcx, int dcy, int psel, Bitmap bm, int cutLeft, int cutRight, int cutTop, int cutBottom) {
		//Log.d(TAG, "ImageScaleDraw: index=" + index);
		int ret = ImageScaleDraw(index, page, rotate, sx, sy, scx, scy, dx, dy, dcx, dcy, psel, bm, cutLeft, cutRight, cutTop, cutBottom);
		checkResult(context, handler, ret);
		return ret;
	}


	public static int ImageCancel(Context context, Handler handler, int index, int flag) {
		//Log.d(TAG, "ImageCancel: index=" + index);
		int ret = ImageCancel(index, flag);
		checkResult(context, handler, ret);
		return ret;
	}

	private static void checkResult(Context context, Handler handler, int returnCode) {
		switch (returnCode) {
			case DEF.ERROR_CODE_MALLOC_FAILURE:
				DEF.sendMessage(context, R.string.MallocFailure, Toast.LENGTH_LONG, handler);
				Log.e(TAG, context.getString(R.string.MallocFailure));
				break;

			case DEF.ERROR_CODE_CACHE_COUNT_LIMIT_EXCEEDED:
				DEF.sendMessage(context, R.string.CacheLimitExceeded, Toast.LENGTH_LONG, handler);
				Log.e(TAG, context.getString(R.string.CacheLimitExceeded));
				break;

			case DEF.ERROR_CODE_CACHE_INDEX_OUT_OF_RANGE:
				//DEF.sendMessage(context, R.string.CacheIndexOutOfRange, Toast.LENGTH_LONG, handler);
				Log.w(TAG, context.getString(R.string.CacheIndexOutOfRange));
				break;

			case DEF.ERROR_CODE_CACHE_NOT_INITIALIZED:
				//DEF.sendMessage(context, R.string.CacheNotInitialized, Toast.LENGTH_LONG, handler);
				Log.e(TAG, context.getString(R.string.CacheNotInitialized));
				break;

		}
	}

}

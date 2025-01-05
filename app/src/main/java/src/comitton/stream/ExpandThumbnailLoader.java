package src.comitton.stream;

import java.util.ArrayList;

import src.comitton.common.DEF;
import src.comitton.common.ImageAccess;
import src.comitton.data.FileData;


import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ExpandThumbnailLoader extends ThumbnailLoader implements Runnable {
	private ImageManager mImageMgr = null;

	Thread mThread;

	public ExpandThumbnailLoader(String uri, String path, Handler handler, long id, ImageManager imagemgr, ArrayList<FileData> files, int sizeW, int sizeH, int cachenum, int crop, int margin) {
		super(uri, path, handler, id, files, sizeW, sizeH, cachenum, crop, margin);

		mImageMgr = imagemgr;

		// スレッド起動
		mThread = new Thread(this);
		mThread.start();
		return;
	}

	// 解放
	public void releaseThumbnail() {
		super.releaseThumbnail();
		return;
	}

	// スレッド停止
	public void breakThread() {
		super.breakThread();

		// 読み込み終了
		if (mImageMgr != null) {
			mImageMgr.setBreakTrigger();
		}
		return;
	}

	protected void interruptThread() {
		if (mThread != null) {
			mThread.interrupt();
		}
	}

	// スレッド開始
	public void run() {
		boolean debug = false;
		if (debug) {Log.d("ExpandThumbnailLoader", "run: 開始します.");}
//		synchronized (mLock) {
			if (mThreadBreak == true || mCachePath == null || mFiles == null) {
				Log.e("ExpandThumbnailLoader", "run: mThreadBreak == true || mCachePath == null || mFiles == null です.");
				return;
			}
			int fileNum = mFiles.size();
			if (fileNum <= 0) {
				Log.e("ExpandThumbnailLoader", "run: ファイルの数が0以下です.");
				return;
			}
			// サムネイル保持領域初期化
			int ret = CallImgLibrary.ThumbnailInitialize(mID, DEF.THUMBNAIL_PAGESIZE, DEF.THUMBNAIL_MAXPAGE, fileNum);
			if (ret < 0) {
				Log.e("ExpandThumbnailLoader", "run: サムネイル保持領域初期化に失敗しました.");
				return;
			}
			
			int thum_cx = mThumbSizeW;
			int thum_cy = mThumbSizeH;
			int firstindex = -1;
			int lastindex = -1;

		if (debug) {Log.d("ExpandThumbnailLoader", "run: mFirstIndex=" + mFirstIndex+ ", mLastIndex=" + mLastIndex);}
			if (debug) {Log.d("ExpandThumbnailLoader", "run: メインループを開始します.");}
			while (mThreadBreak == false) {
				// 初回又は変化があるかのチェック
				if (firstindex != mFirstIndex || lastindex != mLastIndex) {
					if (debug) {Log.d("ExpandThumbnailLoader", "run: firstindex != mFirstIndex です. firstindex=" + firstindex + ", mFirstIndex=" + mFirstIndex);}
					// 表示範囲
					firstindex = mFirstIndex;
					lastindex = mLastIndex;
					
					// 最初は表示範囲を優先
					for (int loop = 0 ; loop < 2 ; loop ++) {
						//if (debug) {Log.d("ExpandThumbnailLoader", "run: loop=" + loop + "です.");}
						// 1週目キャッシュからのみ、2週目は実体も
						for (int i = firstindex ; i <= lastindex && firstindex == mFirstIndex; i++) {
							if (debug) {Log.d("ExpandThumbnailLoader", "run: ループします. i=" + i + ", lastindex=" + lastindex + ", firstindex=" + firstindex + ", mFirstIndex=" + mFirstIndex);}
							if (i < 0 || i >= fileNum) {
								if (debug) {Log.d("ExpandThumbnailLoader", "run: 範囲外です. i=" + i + ", fileNum=" + fileNum);}
								// 範囲内だけ処理する
								continue;
							}

							if (debug) {Log.d("ExpandThumbnailLoader", "run: キャッシュから読み込みます. index=" + i);}
							// 1周目は新たに読み込みしない
							loadBitmap(i, thum_cx, thum_cy, loop == 0, true);
							if (mThreadBreak == true) {
								if (debug) {Log.d("ExpandThumbnailLoader", "run: mThreadBreak == true です.");}
								return;
							}
						}
					}
					if (firstindex != mFirstIndex) {
						if (debug) {Log.d("ExpandThumbnailLoader", "run: 再チェックします(1). firstindex=" + firstindex + ", mFirstIndex=" + mFirstIndex);}
						// 選択範囲が変わったら再チェック
						continue;
					}

					// 前後をキャッシュから読み込み
					// 前後をキャッシュから読み込み
					int range = (lastindex - firstindex) * 2;
					boolean isBreak = false;
					boolean prevflag = false;
					boolean nextflag = false;
					for (int count = 1 ; count <= range && isBreak == false ; count ++) {
						if (debug) {Log.d("ExpandThumbnailLoader", "run: count=" + count + "です.(1)");}
						if ((prevflag && nextflag) || firstindex != mFirstIndex) {
							if (debug) {Log.d("ExpandThumbnailLoader", "run: 範囲オーバーです. prevflag=" + prevflag + ", nextflag=" + nextflag + ", firstindex=" + firstindex + ", mFirstIndex=" + mFirstIndex);}
							// 範囲オーバー、選択範囲変更 
							break;
						}
						for (int way = 0 ; way < 2 && firstindex == mFirstIndex; way ++) {
							if (debug) {Log.d("ExpandThumbnailLoader", "run: way=" + way);}
							if (mThreadBreak == true) {
								return;
							}
							// キャッシュからのみ
							int index;
							if (way == 0) {
								// 前側
								index = firstindex - count;
								if (index < 0) {
									// 範囲内だけ処理する
									prevflag = true;
									continue;
								}
							}
							else {
								// 後側
								index = lastindex + count;
								if (index >= fileNum) {
									// 範囲内だけ処理する
									nextflag = true;
									continue;
								}
							}
							if (debug) {Log.d("ExpandThumbnailLoader", "run: キャッシュから読み込みます. index=" + index);}
							// キャッシュからのみ読み込み
							if (loadBitmap(index, thum_cx, thum_cy, true, false) == false) {
								// メモリ不足で中断
								isBreak = true;
								break;
							}
						}
					}
					if (firstindex != mFirstIndex) {
						if (debug) {Log.d("ExpandThumbnailLoader", "run: 再チェックします(2). firstindex=" + firstindex + ", mFirstIndex=" + mFirstIndex);}
						continue;
					}

					// 前後をキャッシュから読み込み
					range = (lastindex - firstindex);
					isBreak = false;
					prevflag = false;
					nextflag = false;
					for (int count = 1 ; count <= range && firstindex == mFirstIndex ; count ++) {
						if (debug) {Log.d("ExpandThumbnailLoader", "run: count=" + count + "です.(2)");}
						if ((prevflag && nextflag) || firstindex != mFirstIndex) {
							if (debug) {Log.d("ExpandThumbnailLoader", "run: 範囲オーバーです. prevflag=" + prevflag + ", nextflag=" + nextflag + ", firstindex=" + firstindex + ", mFirstIndex=" + mFirstIndex);}
							// 範囲オーバー、選択範囲変更
							break;
						}
						for (int way = 0 ; way < 2 && firstindex == mFirstIndex; way ++) {
							if (debug) {Log.d("ExpandThumbnailLoader", "run: way=" + way);}
							if (mThreadBreak == true) {
								return;
							}
							// 実体も読み込み
							int index;
							if (way == 0) {
								// 前側
								index = firstindex - count;
								if (index < 0) {
									// 範囲内だけ処理する
									prevflag = true;
									continue;
								}
							}
							else {
								// 後側
								index = lastindex + count;
								if (index >= fileNum) {
									// 範囲内だけ処理する
									nextflag = true;
									continue;
								}
							}
							if (debug) {Log.d("ExpandThumbnailLoader", "run: キャッシュから読み込みます. index=" + index);}
							// キャッシュからのみ読み込み
							if (loadBitmap(index, thum_cx, thum_cy, false, true) == false) {
								break;
							}
						}
					}
					if (firstindex != mFirstIndex) {
						if (debug) {Log.d("ExpandThumbnailLoader", "run: 再チェックします(3). firstindex=" + firstindex + ", mFirstIndex=" + mFirstIndex);}
						continue;
					}
				}
				
				if (CallImgLibrary.ThumbnailCheckAll(mID) == 0) {
					if (debug) {Log.d("ExpandThumbnailLoader", "run: CallImgLibrary.ThumbnailCheckAll(mID) == 0, mID=" + mID);}
					// 全部読み込めた
					break;
				}
				else {
					if (debug) {Log.d("ExpandThumbnailLoader", "run: CallImgLibrary.ThumbnailCheckAll(mID) != 0, mID=" + mID);}
					// ページ選択待ちに入る
					try {
						Thread.sleep(60000);
					} catch (InterruptedException e) {
						// 中断
					}
				}
			}
		if (debug) {Log.d("ExpandThumbnailLoader", "run: メインループを終了しました.");}

			if (mThreadBreak == false) {
				// サムネイルキャッシュ削除
				if (mCachePath != null) {
					deleteThumbnailCache(mThumbCacheNum);
				}
			}
//		}
		if (debug) {Log.d("ExpandThumbnailLoader", "run: 終了します.");}
	}

	// ビットマップ読み込み
	private boolean loadBitmap(int index, int thum_cx, int thum_cy, boolean firstloop, boolean priority) {
		boolean debug = false;
		// 読み込み済みチェック
		int result = CallImgLibrary.ThumbnailCheck(mID, index);
		if (result > 0) {
			// 既に読み込み済み
			return true;
		}

		// ファイル情報取得
		FileData data = mFiles.get(index);
		int filetype = data.getType();
		// 拡張子分の文字列長がない
		if (filetype == FileData.FILETYPE_TXT || filetype == FileData.FILETYPE_PARENT) {
			// 対象外のファイル
			CallImgLibrary.ThumbnailSetNone(mID, index);
			return true;
		}

		Bitmap bm = null;
		String filepath = mUri + mPath + ":" + data.getName();
		String pathcode = DEF.makeCode(filepath, thum_cx, thum_cy);
		boolean skipFile = false;
		boolean retval = true;

		// キャッシュファイルパス
		if (checkThumbnailCache(pathcode)) {
			bm = loadThumbnailCache(pathcode);
		}

		if (mThreadBreak == true) {
			return true;
		}

		if (firstloop && bm == null) {
			// 初回ループはキャッシュからのみ読み込み
			skipFile = true;
		}
		if (skipFile == false) {
			if (bm == null) {
				try {
					bm = mImageMgr.LoadThumbnail(index, mThumbSizeW, mThumbSizeH);
				}
				catch (Exception ex) {
					Message message = new Message();
					message.what = DEF.HMSG_ERROR;
					message.obj = ex.getMessage();
					mHandler.sendMessage(message);
				}

				if (bm == null) {
					// NoImageであればステータス設定
					CallImgLibrary.ThumbnailSetNone(mID, index);
				}
			}

			if (bm != null) {
				if (bm.getConfig() != Config.RGB_565) {
					if (debug) {Log.d("ExpandThumbnailLoader", "loadBitmap: RGB_565に変換します.");}
					bm = bm.copy(Config.RGB_565, true);
					if (debug) {Log.d("ExpandThumbnailLoader", "loadBitmap: RGB_565に変換しました.");}
				}
				// キャッシュとして保存
				saveThumbnailCache(pathcode, bm);
				if (retval == false) {
					// メモリ保持失敗の場合は描画しない
					bm = null;
				}
			}

			// ビットマップをサムネイルサイズぴったりにリサイズする
			if (bm != null) {
				if (debug) {Log.d("ExpandThumbnailLoader", "loadBitmap: リサイズします. thum_cx=" + thum_cx + ", thum_cy=" + thum_cy + ", crop=" + mThumbCrop + ", margin=" + mThumbMargin);}
				bm = ImageAccess.resizeTumbnailBitmap(bm, thum_cx, thum_cy, mThumbCrop, mThumbMargin);
			}
			if (bm != null) {
				int w = bm.getWidth();
				int h = bm.getHeight();
				boolean chg = false;
				if (w > mThumbSizeW) {
					w = mThumbSizeW;
					chg = true;
				}
				if (h > mThumbSizeH) {
					h = mThumbSizeH;
					chg = true;
				}

				// ビットマップを切り出す
				if (chg || bm.getConfig() != Config.RGB_565) {
					Bitmap bm2 = Bitmap.createBitmap(w, h, Config.RGB_565);
					bm = Bitmap.createBitmap(bm, 0, 0, w, h);
					Paint drawBmp = new Paint();
					Canvas offScreen = new Canvas(bm2);
					drawBmp.setColor(0xFFFFFFFF);
					drawBmp.setStyle(Style.FILL);
					offScreen.drawRect(0, 0, w, h, drawBmp);
					offScreen.drawBitmap(bm, 0, 0, null);
					bm = bm2;
				}
			}

			boolean save = false;
			if (bm != null) {
				// 空きメモリがあるかをチェック
				result = CallImgLibrary.ThumbnailMemorySizeCheck(mID, bm.getWidth(), bm.getHeight());
				if (result == 0) {
					// メモリあり
					save = true;
				}
				else if (result > 0 && priority) {
					// 表示の中心から外れたものを解放してメモリを空ける
					result = CallImgLibrary.ThumbnailImageAlloc(mID, result, (mFirstIndex + mLastIndex) / 2);
					if (result == 0) {
						// メモリ獲得成功
						save = true;	
					}
					else {
						// メモなし
						retval = false;
					}
				}
			}
			if (bm != null && save) {
				result = CallImgLibrary.ThumbnailSave(mID, bm, index);
				if (result != CallImgLibrary.RESULT_OK) {
					// メモリ保持成功
					retval = false;
				}
			}
		}
		// 通知
		if ((firstloop == true && bm != null) || firstloop == false) {
			Message message = new Message();
			message.what = DEF.HMSG_THUMBNAIL;
			message.arg1 = bm != null ? index : DEF.THUMBSTATE_ERROR;
			message.obj = data.getName();
			mHandler.sendMessage(message);
		}
		return retval;
	}
}

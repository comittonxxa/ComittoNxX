#include <malloc.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>
#include <pthread.h>
#ifdef _WIN32
#include <stdio.h>
#else
#include <android/log.h>
#endif

#include "Image.h"

extern LONG			**gLinesPtr[];
extern LONG			**gSclLinesPtr[];
extern int			gCancel[];

extern int			gMaxThreadNum;

static int colorConvert_5bit[256];
static int colorConvert_6bit[256];

void *ImageBright_ThreadFunc(void *param)
{
	int *range = (int*)param;
	int stindex   = range[0];
	int edindex   = range[1];
	int OrgWidth  = range[2];
	int OrgHeight = range[3];
    int index = range[4];

    LONG *buffptr = NULL;

    // 使用するバッファを保持
    LONG *orgbuff;

	int		xx;	// x座標
	int		yy;	// y座標

	int cc, rr, gg, bb;

	// ラインサイズ
	for (yy = stindex ; yy < edindex ; yy ++) {
//		LOGD("ImageColoring : loop yy=%d", yy);
		if (gCancel[index]) {
			LOGD("ImageColoring : cancel.");
//			ReleaseBuff(Page, 1, Half);
			return (void*)ERROR_CODE_USER_CANCELED;
		}

        // バッファ位置
        buffptr = gSclLinesPtr[index][yy];

        orgbuff = gLinesPtr[index][yy + HOKAN_DOTS / 2];

		for (xx =  0 ; xx < OrgWidth + HOKAN_DOTS ; xx++) {
			// 色の変換
			rr = colorConvert_5bit[RGB888_RED(orgbuff[xx])];
			gg = colorConvert_6bit[RGB888_GREEN(orgbuff[xx])];
			bb = colorConvert_5bit[RGB888_BLUE(orgbuff[xx])];
            buffptr[xx - HOKAN_DOTS / 2] = MAKE8888(rr, gg, bb);
		}

		// 補完用の余裕
        buffptr[-2] = buffptr[0];
        buffptr[-1] = buffptr[0];
        buffptr[OrgWidth + 0] = buffptr[OrgWidth - 1];
        buffptr[OrgWidth + 1] = buffptr[OrgWidth - 1];
	}
	return nullptr;
}

// 自動着色
int ImageBright(int index, int Page, int Half, int Count, int OrgWidth, int OrgHeight, int Bright, int Gamma)
{
//	LOGD("ImageColoring : p=%d, h=%d, c=%d, ow=%d, oh=%d", Page, Half, Count, OrgWidth, OrgHeight);
	int ret = 0;
	double f = 1.0f;;
	double base = 1.0f;
	double scale = 0;;

	f = 1.0f / (1.0f + ((double)Gamma) * 0.1);

	if (Bright < 0) {
		scale = 1.0f + ((double)Bright) * 0.1;
	}
	else {
		scale = 1.0f - ((double)Bright) * 0.1;
		base = 255 * (1.0f - scale);
	}

    for (int i = 0; i < 256; i ++) {
        colorConvert_5bit[i] = (int)(pow(((float)(i) / 255.0f), f) * 255.0f) * scale + base;
    }
    for (int i = 0; i < 256; i ++) {
        colorConvert_6bit[i] = (int)(pow(((float)(i) / 255.0f), f) * 255.0f) * scale + base;
    }

    int linesize;

    // ラインサイズ
    linesize  = OrgWidth + HOKAN_DOTS;

    //  サイズ変更画像待避用領域確保
    if (ScaleMemAlloc(index, linesize, OrgHeight) < 0) {
        return -6;
    }

    // データの格納先ポインタリストを更新
    if (RefreshSclLinesPtr(index, Page, Half, Count, OrgHeight, linesize) < 0) {
        return -7;
    }

	pthread_t thread[gMaxThreadNum];
	int start = 0;
	int param[gMaxThreadNum][5];
	void *status[gMaxThreadNum];

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		param[i][0] = start;
		param[i][1] = start = OrgHeight * (i + 1)  / gMaxThreadNum;
		param[i][2] = OrgWidth;
		param[i][3] = OrgHeight;
        param[i][4] = index;

		if (i < gMaxThreadNum - 1) {
			/* スレッド起動 */
			if (pthread_create(&thread[i], nullptr, ImageBright_ThreadFunc, (void*)param[i]) != 0) {
				LOGE("pthread_create()");
			}
		}
		else {
			// ループの最後は直接実行
			status[i] = ImageBright_ThreadFunc((void*)param[i]);
		}
	}

	for (int i = 0 ; i < gMaxThreadNum ; i ++) {
		/*thread_func()スレッドが終了するのを待機する。thread_func()スレッドが終了していたら、この関数はすぐに戻る*/
		if (i < gMaxThreadNum - 1) {
			pthread_join(thread[i], &status[i]);
		}
		if (status[i] != nullptr) {
//			LOGD("CreateScaleCubic : cancel");
			ret = (long)status[i];
		}
	}
//	LOGD("ImageColoring : complete");
	return ret;
}

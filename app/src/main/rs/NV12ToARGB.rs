/*
     NV12ToARGB.rs
     RenderScript convert NV12(COLOR_FormatYUV420SemiPlanar) to ARGB
     ScriptIntrinsicYuvToRGB supports NV21 not NV12
*/

#pragma version(1)
#pragma rs java_package_name(com.wterry.fei.renderscriptdemo)

#include "common.rs"


rs_allocation src_2d;
uchar * src;

static int width;
static int height;

static int L1[256], L2[256], L3[256];
static int L4[256], L5[256];

void init() {
    rsDebug("Init NV12ToARGB", 0);
    for (int i = 0; i < 256; i++) {
        L1[i] = (int) (1.164 * (i - 16));
        L2[i] = (int) (1.596 * (i - 128));
        L3[i] = (int) (-0.813 * (i - 128));
        L4[i] = (int) (2.018 * (i - 128));
        L5[i] = (int) (-0.391 * (i - 128));
    }
}

void prepare(int w, int h) {
    width = w;
    height = h;
    init_offset_y(w, h*3/2);
}

static inline  uchar4 YUVToARGB(uchar Y, uchar U, uchar V) {
    int r = L1[Y] + L2[V];
    int g = L1[Y] + L3[U] + L5[V];
    int b = L1[Y] + L4[U];
    uchar4 ret;
    ret.r = CLIP(r);
    ret.g = CLIP(g);
    ret.b = CLIP(b);
    ret.a = 255;
    return ret; //((0x00<<24)|(0x00<<16)|(0x00<<8)|(0x00));
}

uchar4 __attribute__((kernel)) convert(int x, int y) {
    int uvoffset = offset_y[height+(y>>1)]+((x>>1)<<1);
    uchar U = src[uvoffset];
    uchar V = src[uvoffset+1];
    return YUVToARGB(src[offset_y[y]+x], U, V);
}

uchar4 __attribute__((kernel)) convert2d(int x, int y) {
    int ux = x&(~1);
    int uy = height + (y>>1);
    int Y = rsGetElementAt_uchar(src_2d, x, y);
    int U = rsGetElementAt_uchar(src_2d, ux, uy);
    int V = rsGetElementAt_uchar(src_2d, ux+1, uy);
    return YUVToARGB(Y, U, V);
}

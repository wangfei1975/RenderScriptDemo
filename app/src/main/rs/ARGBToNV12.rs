#pragma version(1)
#pragma rs java_package_name(com.wterry.fei.renderscriptdemo)
#include "common.rs"

uchar * out;

static int width;
static int height;

static int start_x;
static int start_y;

static int L1[256], L2[256], L3[256];
static uchar L4[512], L5[512];


void init() {
    rsDebug("Init ARGBToNV12", 0);

    /*

       Y = 0.299R + 0.587G + 0.114B
       U = 0.492 (B-Y)
       V = 0.877 (R-Y)

    */
    for (int i = -255; i < 256; i++) {
         if (i >= 0) {
            L1[i] = 0.299 * 256 * i;
            L2[i] = 0.587 * 256 * i;
            L3[i] = 0.114 * 256 * i;
         }
         int u = (0.492 * i) + 128;
         int v = (0.877 * i) + 128;
         L4[i+256] = CLIP(u);
         L5[i+256] = CLIP(v);
    }
}


void prepare_converter(int w, int h, int left, int top) {
    width = w;
    height = h;
    start_x = left;
    start_y = top;
    init_offset_y(w, h*3/2);
}

void __attribute__((kernel)) convert(uchar4 in, int x, int y) {
    int yyy = (L1[in.r] + L2[in.g] + L3[in.b]) >> 8;
    uchar yy = (uchar)(yyy > 255 ? 255 : yyy);
    y += start_y;
    x += start_x;
    out[offset_y[y]+x] = yy;
    if (!((x|y)&1)) {
        int pos = offset_y[height+(y>>1)] + x;
        out[pos] = L4[in.b-yy+256];
        out[pos+1] = L5[in.r-yy+256];
    }
}

static inline uchar alpha_blend(uchar v1, uchar v2, uchar alpha) {
    int v = (v1 * (255 - alpha) + v2 * alpha)>>8;
    return CLIP(v);
}
void __attribute__((kernel)) blit(uchar4 in, int x, int y) {
    int yyy = (L1[in.r] + L2[in.g] + L3[in.b]) >> 8;
    uchar yy = (uchar)(yyy > 255 ? 255 : yyy);

    y += start_y;
    x += start_x;

    int yoff = offset_y[y]+x;
    out[yoff] = alpha_blend(out[yoff], yy, in.a);
    if (!((x|y)&1)) {
        int pos = offset_y[height+(y>>1)] + x;
        out[pos] = alpha_blend(out[pos], L4[in.b-yy+256], in.a);
        out[pos+1] = alpha_blend(out[pos+1], L5[in.r-yy+256], in.a);
    }
}

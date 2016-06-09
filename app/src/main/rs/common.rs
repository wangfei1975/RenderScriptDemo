#pragma version(1)
#pragma rs java_package_name(com.wterry.fei.renderscriptdemo)
#include "rs_core.rsh"
#define CLIP(v) (((v) < 0) ? 0 : ((v) > 255 ?  255 : (v)))

int * offset_y;

static void init_offset_y(int w, int h) {
    for (int i = 0; i < h; i++) {
        offset_y[i] = i * w;
    }
}
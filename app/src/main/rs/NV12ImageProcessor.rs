
#pragma version(1)
#pragma rs java_package_name(com.wterry.fei.renderscriptdemo)

#include "common.rs"


uchar * src;

static int src_width;
static int src_height;
static int dst_width;
static int dst_height;

static int crop_left;
static int crop_top;
static int crop_right;
static int crop_bottom;
static int crop_width;
static int crop_height;


uint16_t  * map_x;
uint16_t  * map_y;


void init() {
    rsDebug("Init NV12ImageProcessor.rs", 0);
}

void prepare_processor(int sw, int sh, int c_left, int c_top, int c_right, int c_bottom,  int dw, int dh) {
    src_width  = sw;
    src_height = sh;
    dst_width  = dw;
    dst_height = dh;

    crop_left   = c_left;
    crop_top    = c_top;
    crop_right  = c_right;
    crop_bottom = c_bottom;

    crop_width = c_right - c_left + 1;
    crop_height = c_bottom - c_top + 1;

    for (int x = 0; x < dw; x++) {
        map_x[x] = (x * crop_width)/dw;
    }
    for (int y = 0; y < dh; y++) {
        map_y[y] = (y * crop_height)/dh;
    }
    init_offset_y(sw, sh*3/2);
}

uchar __attribute__((kernel)) crop_resize_rot00_mir0_y(int x, int y) {
/*
     Easy understand code(Same result):
     int sx = crop_left + x * src_width/dst_width;
     int sy = crop_top + y * src_height/dst_height;
     return  src[sy*src_width + sx];

*/
     return src[offset_y[crop_top+map_y[y]] + crop_left+map_x[x]];
}
uchar __attribute__((kernel)) crop_resize_rot00_mir0_uv(int x, int y) {
/*
      Easy understand code(Same result):
      int dx = x/2;
      int dy = (y-dst_height);

      int sx = crop_left + (dx * src_width/dst_width)*2 + (x%2);
      int sy = crop_top + dy * src_height/dst_height;
      return src[src_height*src_width + sy*src_width + sx];
*/
      int sx = (((crop_left>>1) + map_x[x>>1])<<1) + (x&1);
      int sy = ((crop_top>>1) + map_y[y-dst_height]);
      return src[offset_y[src_height+ sy] + sx];
}


uchar __attribute__((kernel)) crop_resize_rot00_mir1_y(int x, int y) {
    x = dst_width - x - 1;
    return src[offset_y[crop_top+map_y[y]] + crop_left+map_x[x]];
}
uchar __attribute__((kernel)) crop_resize_rot00_mir1_uv(int x, int y) {
    int xx = dst_width - x - 1;
    int sx = (((crop_left>>1) + map_x[xx>>1])<<1) + (x&1);
    int sy = ((crop_top>>1) + map_y[y-dst_height]);
    return src[offset_y[src_height+ sy] + sx];
}

uchar __attribute__((kernel)) crop_resize_rot00_mir2_y(int x, int y) {
    y = dst_height - y - 1;
    return src[offset_y[crop_top+map_y[y]] + crop_left+map_x[x]];
}
uchar __attribute__((kernel)) crop_resize_rot00_mir2_uv(int x, int y) {
    y = (dst_height>>1)-1-(y - dst_height);
    int sx = (((crop_left>>1) + map_x[x>>1])<<1) + (x&1);
    int sy = ((crop_top>>1) + map_y[y]);
    return src[offset_y[src_height+ sy] + sx];
}


uchar __attribute__((kernel)) crop_resize_rot90_mir0_y(int x, int y) {
     int xx = y;
     int yy = dst_height-x-1;
     int sx = crop_left+map_x[xx];
     int sy = crop_top+map_y[yy];
     return src[offset_y[sy] + sx];
}

uchar __attribute__((kernel)) crop_resize_rot90_mir0_uv(int x, int y) {
    int sx = (crop_left >> 1) + map_x[y - dst_width];
    int sy = (crop_top >> 1) + map_y[(dst_height-x-1)>>1];
    return src[offset_y[src_height+ sy] + (sx<<1) + (x&1)];
}



uchar __attribute__((kernel)) crop_resize_rot90_mir1_y(int x, int y) {
    int sx = crop_left+map_x[y];
    int sy = crop_top+map_y[x];
    return src[offset_y[sy] + sx];
}

uchar __attribute__((kernel)) crop_resize_rot90_mir1_uv(int x, int y) {
    int sx = (crop_left >> 1) + map_x[y-dst_width];
    int sy = (crop_top >> 1) + map_y[x>>1];
    return src[offset_y[src_height+ sy] + (sx<<1) + (x&1)];
}

uchar __attribute__((kernel)) crop_resize_rot90_mir2_y(int x, int y) {
    int xx = dst_width-y-1;
    int yy = dst_height-x-1;
    int sx = crop_left+map_x[xx];
    int sy = crop_top+map_y[yy];
    return src[offset_y[sy] + sx];
}

uchar __attribute__((kernel)) crop_resize_rot90_mir2_uv(int x, int y) {
    int xx = (dst_width>>1)- 1 - y + dst_width;
    int sx = (crop_left >> 1) + map_x[xx];
    int sy = (crop_top >> 1) + map_y[(dst_height-x-1)>>1];
    return src[offset_y[src_height+ sy] + (sx<<1) + (x&1)];
}

uchar __attribute__((kernel)) crop_resize_rot180_mir0_y(int x, int y) {
     x = dst_width - x - 1;
     y = dst_height - y - 1;
     return src[offset_y[crop_top+map_y[y]] + crop_left+map_x[x]];
}
uchar __attribute__((kernel)) crop_resize_rot180_mir0_uv(int x, int y) {
    int xx = dst_width - x - 1;
    int yy = (dst_height>>1)-1-(y - dst_height);
    int sx = (((crop_left>>1) + map_x[xx>>1])<<1) + (x&1);
    int sy = ((crop_top>>1) + map_y[yy]);
    return src[offset_y[src_height+ sy] + sx];
}

uchar __attribute__((kernel)) crop_resize_rot270_mir0_y(int x, int y) {
    int xx = dst_width - y - 1;
    int sx = crop_left+map_x[xx];
    int sy = crop_top+map_y[x];
    return src[offset_y[sy] + sx];
}

uchar __attribute__((kernel)) crop_resize_rot270_mir0_uv(int x, int y) {
    int xx = (dst_width>>1) - (y - dst_width) - 1;
    int sx = (crop_left >> 1) + map_x[xx];
    int sy = (crop_top >> 1) + map_y[x>>1];
    return src[offset_y[src_height+ sy] + (sx<<1) + (x&1)];
}


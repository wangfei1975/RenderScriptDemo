# RenderScriptDemo
An Android project demonstrates how to use RenderScript to 

1. Do colorspace conversion(NV12=>ARGB, ARGB=>NV12) 

2. Do Image processing(Rotate, Mirror, Resize, Crop) 

3. Do Video Post-posessing and Editing(Rotate, Mirror, Resize, Crop) 

4. Create watermark on top of video.

The project contains a RenderScript that manipulate NV12 image. 

    *   Create a processor that processes NV12 images.
    *   The result equal to do following process orderly.
    *     1. crop source by using cropLeft, cropTop, cropRight, cropBottom
    *     2. Resize the corpped image to dstWidth, dstHeight
    *     3. rotation the resized image by rotation
    *     4. mirror the result
    *
    *    The cropLeft, cropTop, cropRight, cropBottom specify a rectangle area
    *      must inside srcWidth and srcHeight
    *
    *    Rotation can be one of 0, 90, 180, 270
    *    Mirror can be one of MIRROR_NONE, MIRROR_HORIZONTAL, MIRROR_VERTICAL

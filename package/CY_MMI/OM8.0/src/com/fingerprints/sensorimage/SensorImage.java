/*
*
* Copyright (c) 2015 Fingerprint Cards AB <tech@fingerprints.com>
*
* All rights are reserved.
* Proprietary and confidential.
* Unauthorized copying of this file, via any medium is strictly prohibited.
* Any use is subject to an appropriate license granted by Fingerprint Cards AB.
*
*/

package com.fingerprints.sensorimage;

/** Image captured from a fingerprint sensor. The image is represented as an uncompressed bitmap.
  */
public class SensorImage{

    /** List of supported pixel depths */
    public enum BitsPerPixel {
        /** 8 bits per pixel */
        BPP_8
    }

    private BitsPerPixel bitsPerPixel;
    private int width;
    private int height;
    private byte[] pixels;

    /** Creates an image.
      *
      * @param bitsPerPixel Pixel depth
      * @param width Image width (in pixels)
      * @param height Image height (in pixels)
      * @param pixels Byte array of pixels.
      */
    public SensorImage(BitsPerPixel bitsPerPixel, int width, int height, byte[] pixels) {
        this.bitsPerPixel = bitsPerPixel;
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    /** Returns pixel depth (bits per pixel)
      */
    public BitsPerPixel getBitsPerPixel() {
        return this.bitsPerPixel;
    }

    /** Returns the width (in pixels).
      */
    public int getWidth() {
        return this.width;
    }

    /** Returns the height (in pixels).
      */
    public int getHeight() {
        return this.height;
    }

    /** Returns a byte array with the pixels.
      */
    public byte[] getPixels() {
        return this.pixels;
    }
}

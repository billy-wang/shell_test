/*
 * Copyright (c) 2015 Fingerprint Cards AB <tech@fingerprints.com>
 *
 * All rights are reserved.
 * Proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Any use is subject to an appropriate license granted by Fingerprint Cards AB.
 */

package com.fingerprints.service;

import java.lang.reflect.Method;

import com.fingerprints.sensorimage.SensorImage;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import com.gionee.util.DswLog;

import java.lang.System;

/**
 * This class is is used to interact with the Fingerprint Service to perform engineering operations.
 * Functionality exposed here will typically not be available in commercial devices.
 *
 * @author fpc
 */
public class FingerprintEngineering {
    private final static String TAG = "FingerprintEngineering";
    static final String SERVICE_NAME = "engineering";

    /**
     * ImageState values represent different stages of processing of the image.
     */
    public enum ImageState {
        /** Raw image (not preprocessed) */
        RAW,
        /** Preprocessed image */
        PREPROCESSED
    };

    private IFingerprintServiceEngineering mService;
    private ImageSubscriptionListener mImageSubscriptionListener;
    private Handler mHandler;

    private IImageSubscriptionListener mIImageSubscriptionListener = new IImageSubscriptionListener.Stub() {

        @Override
        public void onImage(final int bitsPerPixel,
                final int width,
                final int height,
                final byte[] pixels,
                final int imageState,
                final int imageSequenceNumber) throws RemoteException {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    int imageCnt = pixels.length / (width * height);
                    SensorImage[] images = new SensorImage[imageCnt];
                    int start = 0;

                    for (int i = 0; i < imageCnt; ++i) {
                        byte[] tmp = new byte[width * height];
                        try {
                            System.arraycopy(pixels, start, tmp, 0, width * height);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                        images[i] = new SensorImage(
                                SensorImage.BitsPerPixel.values()[bitsPerPixel],
                                width,
                                height,
                                tmp);
                        start += width * height;
                    }

                    mImageSubscriptionListener.onImage(images,
                            ImageState.values()[imageState],
                            imageSequenceNumber);
                }
            });
        }

    };

    /**
     * Callback interface for image subscription, to be implemented by client.
     */
    public interface ImageSubscriptionListener {

        /**
         * Callback for StartImageSubscription. Provides one or more images.
         *
         * @param images Array of images. For preprocessed images this will contain a single image.
         *            For raw images it may contain several images, in case multiple captures are
         *            used to eventually produce a single image.
         * @param imageState State of the image: RAW (not preprocessed) or PREPROCESSED.
         * @param imageSequenceNumber Sequence number for the image. The sequence numbering starts
         *            at 0 upon calling StartImageSubscription and is increased by one for each
         *            complete image capture. This means that a preprocessed image will keep the
         *            same sequence number as the raw image it is created from.
         * @see startImageSubscription
         */
        void onImage(SensorImage[] images, ImageState imageState, int imageSequenceNumber);
    }

    /**
     * Callback interface for image injection, to be implemented by client.
     */
    public interface ImageInjectionListener {

        public enum InjectionError {
            /** Unspecified error during image injection */
            UNSPECIFIED_INJECTION_ERROR,
            /**
             * The image array returned from onImageInjectRequest contained the wrong number of
             * images
             */
            WRONG_NUMBER_OF_IMAGES,
            /**
             * One or more images returned from onImageInjectRequest had inconsistent format
             */
            IMAGE_FORMAT_INCONSISTENT,
            /**
             * One or more images returned from onImageInjectRequest did not match the required
             * format
             */
            IMAGE_FORMAT_NOT_SUPPORTED
        };

        /**
         * Callback for StartImageInjection. Requests one or more images to be used instead of
         * images captured from the sensor. The images are passed in the return value from the
         * function as an array of SensorImage.
         *
         * @param imageState Expected state of the image: RAW (not preprocessed) or PREPROCESSED.
         * @param nbrOfImages Number of images requested. The SensorImage array returned from the
         *            function must contain exactly this number of images. For raw images it may be
         *            required to provide several images, in case multiple captures are used to
         *            eventually produce a single image.
         * @param imageSequenceNumber Sequence number for the image. The sequence numbering starts
         *            at 0 upon calling StartImageInjection and is increased by one for each image
         *            injection. The same sequence number will be used for both RAW and PREPROCESSED
         *            image state.
         * @return Array of SensorImage. If the array is of different size than specified in
         *         nbrOfImages, or if any of the images is inconsistent or does not match the
         *         required format, onInjectionError will be called. NULL is valid as return value,
         *         and means that no image is injected. For raw images the image(s) will be captured
         *         from the sensor instead. For preprocessed images the image will be created by
         *         running preprocessor on the raw image (captured or injected in the previous
         *         callback).
         * @see startImageInjection
         */
        SensorImage[] onImageInjectRequest(ImageState imageState,
                int nbrOfImages,
                int imageSequenceNumber);

        /**
         * Callback for StartImageInjection. Reports an error during image injection.
         *
         * @param errorCode
         * @param imageSequenceNumber Sequence number for the image. The sequence numbering starts
         *            at 0 upon calling StartImageInjection and is increased by one for each image
         *            injection. The same sequence number will be used for both RAW and PREPROCESSED
         *            image state.
         * @return void
         * @see startImageInjection
         */
        void onInjectionError(InjectionError errorCode, int imageSequenceNumber);
    }

    /**
     * Starts subscription to captured images. Images are received through callbacks to
     * ImageSubscriptionListener.onImage.
     *
     * @param listener Object instance implementing the ImageSubscriptionListener interface.
     * @return void
     * @see stopImageSubscription
     * @see ImageSubscriptionListener
     */
    public void startImageSubscription(ImageSubscriptionListener listener) {
        mImageSubscriptionListener = listener;
        try {
            mService.startImageSubscription(mIImageSubscriptionListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stops image subscription.
     *
     * @see startImageSubscription
     */
    public void stopImageSubscription() {
        try {
            mService.stopImageSubscription();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts injection of images. A callback is received through
     * ImageInjectionListener.onImageInjectRequest each time an image is needed.
     * 
     * @param listener Object instance implementing the ImageInjectionListener interface.
     * @return void
     * @see stopImageInjection
     * @see ImageInjectionListener
     */
    public void startImageInjection(ImageInjectionListener listener) {
        DswLog.e(TAG, "startImageInjection not implemented!");
    }

    /**
     * Stops image injection.
     *
     * @see startImageInjection
     */
    public void stopImageInjection() {
        DswLog.e(TAG, "stopImageInjection not implemented!");
    }

    /**
     * This class is used to interact with the Fingerprint Extension Service to perform
     * engineering operations. Functionality exposed here will typically not be available in
     * commercial devices. Calling applications must have permission
     * com.fingerprints.service.ACCESS_EXTENSION_SERVICE
     * 
     * @author fpc
     */
    public FingerprintEngineering() throws Exception {
        mHandler = new Handler();
        Class<?> servicemanager;
        IFingerprintService service = null;

        try {
            servicemanager = Class.forName("android.os.ServiceManager");

            Method getService = servicemanager.getMethod("getService", String.class);

            IBinder binder;

            binder = (IBinder) getService.invoke(null, "fingerprints_service");

            service = IFingerprintService.Stub.asInterface(binder);
        } catch (Exception e) {
            // TODO Add clearer Exception handling
            e.printStackTrace();
        }
        if (service == null) {
            throw new Exception("The FPC extension service could not be loaded");
        }

        IBinder engiBinder = service.getService("engineering");
        if (engiBinder == null) {
            throw new Exception("Engineering API could not be loaded");
        }
        mService = IFingerprintServiceEngineering.Stub.asInterface(engiBinder);

    }
}

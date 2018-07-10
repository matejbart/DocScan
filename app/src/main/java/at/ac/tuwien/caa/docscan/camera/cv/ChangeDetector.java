package at.ac.tuwien.caa.docscan.camera.cv;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorKNN;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.video.Video;

/**
 * Created by fabian on 12.01.2017.
 */
public class ChangeDetector {

    private static int mFrameWidth, mFrameHeight;

    private static int FRAME_SIZE = 300;
    private static final String TAG = "ChangeDetector";
    private static final long MIN_STEADY_TIME = 500;        // The time in which there must be no movement.
    private static final double CHANGE_THRESH = 0.05;       // A threshold describing a movement between successive frames.
    private static final double DIFFERENCE_THRESH = 0.1;    // Used to measure the difference between a current frame and the frame that has been written to disk

    private static Mat mInitMat;
    private static boolean mLastFrameChanged = false;
    private static long mSteadyStartTime;
    private static boolean mIsMovementDetectorInit = false;
    private static boolean mIsNewFrameDetectorInit = false;
    private static Mat mSubtractMat = null;

//    private static final double PERC_THRESH = 0.05;

    private static BackgroundSubtractorMOG2 mMovementDetector, mNewFrameDetector;
//    private static BackgroundSubtractorKNN mMovementDetector, mNewFrameDetector;
    /**
     * This is just called after the camera is started and no picture has been taken yet. With this function
     * @param frame
     */
    public static void firstTimeInit(Mat frame) {

    }

    public static void initMovementDetector(Mat frame) {

        mIsMovementDetectorInit = true;
        mInitMat = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, mInitMat, Imgproc.COLOR_RGB2GRAY);

        initFrameSize(frame);

        mMovementDetector = Video.createBackgroundSubtractorMOG2();
//        mMovementDetector = Video.createBackgroundSubtractorKNN();

    }

    public static void initNewFrameDetector(Mat frame) {

        mIsNewFrameDetectorInit = true;
        mInitMat = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, mInitMat, Imgproc.COLOR_RGB2GRAY);

        mNewFrameDetector = Video.createBackgroundSubtractorMOG2();
//        mNewFrameDetector = Video.createBackgroundSubtractorKNN();

        Mat tmp = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, tmp, Imgproc.COLOR_RGB2GRAY);
        Imgproc.resize(tmp, tmp, new Size(mFrameWidth, mFrameHeight));
        Mat fg = new Mat(frame.rows(), mFrameWidth, CvType.CV_8UC1);

        mNewFrameDetector.apply(tmp, fg, 1);


    }

    public static boolean isMovementDetectorInitialized() {
        return mIsMovementDetectorInit;
    }

    public static boolean isNewFrameDetectorInitialized() {
        return mIsNewFrameDetectorInit;
    }

    private static void initFrameSize(Mat frame) {

        if (frame.cols() == 0 || frame.rows() == 0)
            return;
//        The frame height might be larger than the frame width:
        double resizeFac = frame.cols() > frame.rows() ? (double) FRAME_SIZE / (double)frame.rows() : (double)FRAME_SIZE / (double)frame.cols();

        mFrameWidth = (int) Math.round(frame.cols() * resizeFac);
        mFrameHeight = (int) Math.round(frame.rows() * resizeFac);

    }

    public static void resetNewFrameDetector() {

        mIsNewFrameDetectorInit = false;

    }

    public static boolean subtractMat(Mat frame) {

        if (!mIsMovementDetectorInit|| ((frame.rows() != mInitMat.rows()) || (frame.cols() != mInitMat.cols()))) {
            initMovementDetector(frame);
            return false;
        }

        if (mSubtractMat == null) {
            mSubtractMat = new Mat(mFrameHeight, mFrameWidth, CvType.CV_8UC1);
            Mat tmp = new Mat(mInitMat.rows(), mInitMat.cols(), CvType.CV_8UC1);
//            Imgproc.cvtColor(mInitMat, tmp, Imgproc.COLOR_RGB2GRAY);
            Imgproc.resize(mInitMat, tmp, new Size(mFrameWidth, mFrameHeight));
            mSubtractMat = tmp;
        }


        Mat currentMat = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, currentMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.resize(currentMat, currentMat, new Size(mFrameWidth, mFrameHeight));

        Mat result = new Mat();
        Core.absdiff(currentMat, mSubtractMat, result);

        Imgproc.threshold(result, result, 120, 1, Imgproc.THRESH_BINARY); // 127 is a shadow, but the majority of the pixels is classified as shadow - we do not know why.

        Scalar fgPixels = Core.sumElems(result);
//            double percChanged = fgPixels.val[0] / (mFrameWidth * mFrameHeight);
        double changeRatio = fgPixels.val[0] / (mFrameWidth * mFrameHeight);

//        Mat fg = new Mat(frame.rows(), mFrameWidth, CvType.CV_8UC1);
//
//        subtractor.apply(tmp, fg, learnRate);
//        Imgproc.threshold(fg, fg, 120, 1, Imgproc.THRESH_BINARY); // 127 is a shadow, but the majority of the pixels is classified as shadow - we do not know why.
//        return false;

        return changeRatio < .05;
    }

    public static boolean isFrameSteady(Mat frame) {

        if (!mIsMovementDetectorInit|| ((frame.rows() != mInitMat.rows()) || (frame.cols() != mInitMat.cols()))) {
            initMovementDetector(frame);
            return false;
        }

        double changeRatio = getChangeRatio(frame, mMovementDetector, 0.8);
        boolean isCurrentChange = changeRatio > CHANGE_THRESH;
        boolean isFrameSteady = false;

        if (!isCurrentChange) {
//            Check if a change occurred in the last frame:
            if (mLastFrameChanged)
                mSteadyStartTime = System.currentTimeMillis();
            else {
//                Check if there has no change happened during the MIN_STEADY_TIME:
                if ((System.currentTimeMillis() - mSteadyStartTime) > MIN_STEADY_TIME)
                    isFrameSteady = true;
            }
        }

        mLastFrameChanged = isCurrentChange;
        return isFrameSteady;

    }

    public static boolean isNewFrame(Mat frame) {

        double changeRatio = getChangeRatio(frame, mNewFrameDetector, 0);

        if (changeRatio > .05)
            return true;
        else
            return false;
    }


    private static double getChangeRatio(Mat frame, BackgroundSubtractorMOG2 subtractor, double learnRate) {

        Mat tmp = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, tmp, Imgproc.COLOR_RGB2GRAY);

        Bitmap bm = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, bm);

        Log.d(TAG, "frame rows: " + frame.rows() + " frame cols: " + frame.cols());
        Log.d(TAG, "mFrameWidth: " + mFrameWidth + " mFrameHeight " + mFrameHeight);
        Imgproc.resize(tmp, tmp, new Size(mFrameWidth, mFrameHeight));

        Mat fg = new Mat(frame.rows(), mFrameWidth, CvType.CV_8UC1);

        subtractor.apply(tmp, fg, learnRate);
        Imgproc.threshold(fg, fg, 120, 1, Imgproc.THRESH_BINARY); // 127 is a shadow, but the majority of the pixels is classified as shadow - we do not know why.

        Scalar fgPixels = Core.sumElems(fg);
//            double percChanged = fgPixels.val[0] / (mFrameWidth * mFrameHeight);
        double changeRatio = fgPixels.val[0] / (mFrameWidth * mFrameHeight);

        return changeRatio;

    }

    private static double getChangeRatio(Mat frame, BackgroundSubtractorKNN subtractor, double learnRate) {

        Mat tmp = new Mat(frame.rows(), frame.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(frame, tmp, Imgproc.COLOR_RGB2GRAY);

        Bitmap bm = Bitmap.createBitmap(frame.cols(), frame.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(frame, bm);

        Log.d(TAG, "frame rows: " + frame.rows() + " frame cols: " + frame.cols());
        Log.d(TAG, "mFrameWidth: " + mFrameWidth + " mFrameHeight " + mFrameHeight);
        Imgproc.resize(tmp, tmp, new Size(mFrameWidth, mFrameHeight));

        Mat fg = new Mat(frame.rows(), mFrameWidth, CvType.CV_8UC1);

        subtractor.apply(tmp, fg, learnRate);
        Imgproc.threshold(fg, fg, 120, 1, Imgproc.THRESH_BINARY); // 127 is a shadow, but the majority of the pixels is classified as shadow - we do not know why.

        Scalar fgPixels = Core.sumElems(fg);
//            double percChanged = fgPixels.val[0] / (mFrameWidth * mFrameHeight);
        double changeRatio = fgPixels.val[0] / (mFrameWidth * mFrameHeight);

        return changeRatio;

    }


}

package at.ac.tuwien.caa.docscan.camera.threads.at;

import org.opencv.core.Mat;

import at.ac.tuwien.caa.docscan.camera.cv.DkPolyRect;
import at.ac.tuwien.caa.docscan.camera.cv.Patch;

public class CVTask implements CVRunnable.TaskRunnableCVMethods {

    private Mat mMat;
    private DkPolyRect[] mPolyRects;
    private Patch[] mPatches;

    Runnable mRunnable;

    private static CVManager sCVManager;

    @Override
    public void recycle() {

        mMat.release();
        mMat = null;

    }

    public void setMat(Mat mat) {

        mMat = mat;

    }

    @Override
    public Mat getMat() {
        return mMat;
    }

    @Override
    public void setPolyRect(DkPolyRect[] polyRects) {

        mPolyRects = polyRects;

    }

    @Override
    public void setPatch(Patch[] patch) {
        mPatches = patch;
    }

    @Override
    public Patch[] getPatch() {
        return mPatches;
    }


    @Override
    public DkPolyRect[] getPolyRect() {
        return mPolyRects;
    }

    @Override
    public void handleState(int state) {
        sCVManager.handleState(this, state);
    }

    public void initializeTask(CVManager cvManager) {

        sCVManager = cvManager;

    }

    public Runnable getRunnable() {
        return mRunnable;
    }

}

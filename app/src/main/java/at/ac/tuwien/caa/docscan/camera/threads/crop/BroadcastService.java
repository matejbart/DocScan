package at.ac.tuwien.caa.docscan.camera.threads.crop;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

public class BroadcastService extends Service {

    private static final String CLASS_NAME = "CLASS_NAME";

    @Override
    public void onCreate() {

        getApplicationContext();
        super.onCreate();
        Log.d(CLASS_NAME, "onCreate:");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
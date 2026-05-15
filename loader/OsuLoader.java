// osu!lazer LOADER - Launcher
// =======================
// Запускает osu!lazer автоматически
// =======================

package com.osuloader;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

public class OsuLoader extends Activity {

    private static final String TAG = "OsuLoader";
    private static final String TARGET = "com.ppy.osulazer";
    private static final int DELAY = 1000; // 1 секунда

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "Starting osu!lazer...");
        
        // Запускаем игру
        launchGame();
        
        // Закрываем лоадер
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, DELAY);
    }
    
    private void launchGame() {
        try {
            Intent intent = getPackageManager().getLaunchIntentForPackage(TARGET);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d(TAG, "osu!lazer launched");
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
}
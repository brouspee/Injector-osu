package com.osuinjector;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

public class OverlayService extends Service {
    
    private WindowManager windowManager;
    private View floatingView;
    private boolean isShown = false;
    private CheckBox checkTiming, checkRadius;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            hideOverlay();
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!isShown) showOverlay();
        return START_STICKY;
    }

    private void showOverlay() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.overlay_menu, null);
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 200;

        checkTiming = floatingView.findViewById(R.id.overlay_switchTiming);
        checkRadius = floatingView.findViewById(R.id.overlay_switchRadius);

        floatingView.findViewById(R.id.overlay_btnClose).setOnClickListener(v -> {
            hideOverlay(); stopSelf();
        });
        floatingView.findViewById(R.id.overlay_btnSave).setOnClickListener(v -> saveOverlaySettings());
        floatingView.findViewById(R.id.overlay_btnToggle).setOnClickListener(v -> {
            View content = floatingView.findViewById(R.id.overlay_content);
            content.setVisibility(content.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        checkTiming.setChecked(getSharedPreferences("osu_injector_settings", MODE_PRIVATE).getBoolean("timing_enabled", true));
        checkRadius.setChecked(getSharedPreferences("osu_injector_settings", MODE_PRIVATE).getBoolean("radius_enabled", true));

        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    initialX = params.x; initialY = params.y;
                    initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    params.x = initialX + (int)(event.getRawX() - initialTouchX);
                    params.y = initialY + (int)(event.getRawY() - initialTouchY);
                    windowManager.updateViewLayout(floatingView, params);
                }
                return true;
            }
        });

        try {
            windowManager.addView(floatingView, params);
            isShown = true;
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideOverlay() {
        if (isShown && floatingView != null) {
            try { windowManager.removeView(floatingView); } catch (Exception e) {}
            isShown = false;
        }
    }

    private void saveOverlaySettings() {
        getSharedPreferences("osu_injector_settings", MODE_PRIVATE).edit()
            .putBoolean("timing_enabled", checkTiming.isChecked())
            .putBoolean("radius_enabled", checkRadius.isChecked())
            .apply();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() { hideOverlay(); super.onDestroy(); }
}

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
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

public class OverlayService extends Service {
    
    private WindowManager windowManager;
    private View floatingView;
    private boolean isShown = false;
    
    private Switch switchTiming, switchRadius;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
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
        
        if (!isShown) {
            showOverlay();
        }
        return START_STICKY;
    }
    
    private void showOverlay() {
        LayoutInflater inflater = LayoutInflater.from(this);
        floatingView = inflater.inflate(R.layout.overlay_menu, null);
        
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
        
        initOverlayViews();
        
        floatingView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
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
            try {
                windowManager.removeView(floatingView);
            } catch (Exception e) {
                // ignore
            }
            isShown = false;
        }
    }
    
    private void initOverlayViews() {
        switchTiming = floatingView.findViewById(R.id.overlay_switchTiming);
        switchRadius = floatingView.findViewById(R.id.overlay_switchRadius);
        
        Button btnClose = floatingView.findViewById(R.id.overlay_btnClose);
        btnClose.setOnClickListener(v -> {
            hideOverlay();
            stopSelf();
        });
        
        Button btnSave = floatingView.findViewById(R.id.overlay_btnSave);
        btnSave.setOnClickListener(v -> {
            saveOverlaySettings();
        });
        
        Button btnToggle = floatingView.findViewById(R.id.overlay_btnToggle);
        btnToggle.setOnClickListener(v -> {
            LinearLayout content = floatingView.findViewById(R.id.overlay_content);
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
            } else {
                content.setVisibility(View.VISIBLE);
            }
        });
        
        // Load current settings
        switchTiming.setChecked(getSharedPreferences("osu_injector_settings", MODE_PRIVATE)
            .getBoolean("timing_enabled", true));
        switchRadius.setChecked(getSharedPreferences("osu_injector_settings", MODE_PRIVATE)
            .getBoolean("radius_enabled", true));
    }
    
    private void saveOverlaySettings() {
        getSharedPreferences("osu_injector_settings", MODE_PRIVATE).edit()
            .putBoolean("timing_enabled", switchTiming.isChecked())
            .putBoolean("radius_enabled", switchRadius.isChecked())
            .apply();
        Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDestroy() {
        hideOverlay();
        super.onDestroy();
    }
}

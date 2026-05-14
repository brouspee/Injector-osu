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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class OverlayService extends Service {
    
    private WindowManager windowManager;
    private View floatingView;
    private boolean isShown = false;
    
    // Settings references  
    private Switch switchTiming, switchRadius, switchAR, switchCS, switchOD;
    private EditText editGreat, editOK, editMeh, editRadius, editAR, editCS, editOD;
    
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
        // Create floating window
        LayoutInflater inflater = LayoutInflater.from(this);
        floatingView = inflater.inflate(R.layout.overlay_menu, null);
        
        // Window params
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
        
        // Setup views
        initOverlayViews();
        
        // Touch listener for dragging
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
            e.printStackTrace();
        }
    }
    
    private void hideOverlay() {
        if (isShown && floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isShown = false;
        }
    }
    
    private void initOverlayViews() {
        // Get views from overlay layout
        switchTiming = floatingView.findViewById(R.id.overlay_switchTiming);
        switchRadius = floatingView.findViewById(R.id.overlay_switchRadius);
        
        // Close button
        Button btnClose = floatingView.findViewById(R.id.overlay_btnClose);
        btnClose.setOnClickListener(v -> {
            hideOverlay();
            stopSelf();
        });
        
        // Save button
        Button btnSave = floatingView.findViewById(R.id.overlay_btnSave);
        btnSave.setOnClickListener(v -> saveOverlaySettings());
        
        // Toggle visibility
        Button btnToggle = floatingView.findViewById(R.id.overlay_btnToggle);
        btnToggle.setOnClickListener(v -> {
            LinearLayout content = floatingView.findViewById(R.id.overlay_content);
            if (content.getVisibility() == View.VISIBLE) {
                content.setVisibility(View.GONE);
            } else {
                content.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void saveOverlaySettings() {
        // Save to shared prefs - will be read by inject()
        // Settings already saved in MainActivity
    }
    
    @Override
    public void onDestroy() {
        hideOverlay();
        super.onDestroy();
    }
}

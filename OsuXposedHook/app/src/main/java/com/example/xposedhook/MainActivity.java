package com.example.xposedhook;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Главная активити Xposed модуля
 * Показывает информацию о модуле
 */
public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Toast.makeText(
            this,
            "OsuHook module active!\nEnable in LSPosed",
            Toast.LENGTH_LONG
        ).show();
        
        finish();
    }
}
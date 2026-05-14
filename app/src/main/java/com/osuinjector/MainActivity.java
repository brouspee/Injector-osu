package com.osuinjector;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    
    private SwitchCompat switchTiming, switchRadius, switchAR, switchCS, switchOD;
    private EditText editGreat, editOK, editMeh, editRadius, editAR, editCS, editOD;
    private SharedPreferences prefs;
    private TextView txtStatus;
    
    private static final String PREFS_NAME = "osu_injector_settings";
    public static final String DLL_ASSETS_PATH = "/data/local/tmp/OsuInjectorMod.dll";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initViews();
        loadSettings();
    }
    
    private void initViews() {
        // Timing switches
        switchTiming = findViewById(R.id.switchTiming);
        switchRadius = findViewById(R.id.switchRadius);
        switchAR = findViewById(R.id.switchAR);
        switchCS = findViewById(R.id.switchCS);
        switchOD = findViewById(R.id.switchOD);
        
        // EditTexts
        editGreat = findViewById(R.id.editGreat);
        editOK = findViewById(R.id.editOK);
        editMeh = findViewById(R.id.editMeh);
        editRadius = findViewById(R.id.editRadius);
        editAR = findViewById(R.id.editAR);
        editCS = findViewById(R.id.editCS);
        editOD = findViewById(R.id.editOD);
        
        txtStatus = findViewById(R.id.txtStatus);
        
        // Save button
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveSettings());
        
        // Inject button
        Button btnInject = findViewById(R.id.btnInject);
        btnInject.setOnClickListener(v -> inject());
        
        // Patch DLL button
        Button btnPatchDLL = findViewById(R.id.btnPatchDLL);
        btnPatchDLL.setOnClickListener(v -> patchDLL());
    }
    
    private void loadSettings() {
        editGreat.setText(prefs.getString("great", "500"));
        editOK.setText(prefs.getString("ok", "800"));
        editMeh.setText(prefs.getString("meh", "1200"));
        editRadius.setText(prefs.getString("radius", "3"));
        editAR.setText(prefs.getString("ar", "10"));
        editCS.setText(prefs.getString("cs", "10"));
        editOD.setText(prefs.getString("od", "10"));
        
        switchTiming.setChecked(prefs.getBoolean("timing_enabled", true));
        switchRadius.setChecked(prefs.getBoolean("radius_enabled", true));
        switchAR.setChecked(prefs.getBoolean("ar_enabled", false));
        switchCS.setChecked(prefs.getBoolean("cs_enabled", false));
        switchOD.setChecked(prefs.getBoolean("od_enabled", false));
    }
    
    private void saveSettings() {
        prefs.edit()
            .putString("great", editGreat.getText().toString())
            .putString("ok", editOK.getText().toString())
            .putString("meh", editMeh.getText().toString())
            .putString("radius", editRadius.getText().toString())
            .putString("ar", editAR.getText().toString())
            .putString("cs", editCS.getText().toString())
            .putString("od", editOD.getText().toString())
            .putBoolean("timing_enabled", switchTiming.isChecked())
            .putBoolean("radius_enabled", switchRadius.isChecked())
            .putBoolean("ar_enabled", switchAR.isChecked())
            .putBoolean("cs_enabled", switchCS.isChecked())
            .putBoolean("od_enabled", switchOD.isChecked())
            .apply();
        
        txtStatus.setText("Settings saved!");
    }
    
    private void inject() {
        // Копируем DLL в /data/local/tmp если нужно
        try {
            File dllFile = new File(DLL_ASSETS_PATH);
            if (!dllFile.exists()) {
                // Проверяем в assets
                java.io.InputStream is = getAssets().open("OsuInjectorMod.dll");
                java.io.FileOutputStream fos = new FileOutputStream(dllFile);
                byte[] buffer = new byte[4096];
                int len;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
            }
            
            // Проверяем root и инжектим
            Process su = Runtime.getRuntime().exec("su");
            su.waitFor();
            
            // TODO: Инжект через /data/local/tmp/bepinex/plugins/
            txtStatus.setText("DLL ready. Install BepInEx/MelonLoader in osu!lazer first.");
            
        } catch (Exception e) {
            txtStatus.setText("Error: " + e.getMessage());
        }
    }
    
    private void patchDLL() {
        // Генерируем DLL с текущими настройками
        try {
            // Здесь нужен компилятор C# -暫时不 реализовано
            txtStatus.setText("DLL patching requires build server.");
        } catch (Exception e) {
            txtStatus.setText("Error: " + e.getMessage());
        }
    }
}
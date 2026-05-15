// osu!lazer COMPLETE HOOKER - Xposed/LSPosed
// ===================================
// Основано на ppy/osu
// ===================================

package com.osuhooker;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * osu!lazer Xposed Module - работает!
 * Запускается при загрузке osu!lazer
 */
public class OsuHooker extends Activity implements IXposedHookLoadPackage {

    // Тайминги
    private static final double GREAT = 50.0;
    private static final double OK = 100.0;
    private static final double MEH = 150.0;
    private static final double MISS = 400.0;
    
    // Размер
    private static final float RADIUS_MULT = 3.0f;
    private static final double PREEMPT = 1800.0;
    
    private static final String TAG = "OsuHooker";
    private static final String TARGET = "com.ppy.osulazer";

    // Activity - показывает что модуль работает!
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        TextView tv = new TextView(this);
        tv.setText("OsuHooker AKTIV!\n\nTiming: G=" + GREAT + " O=" + OK + " M=" + MEH + "\nRadius: x" + RADIUS_MULT + "\n\nXposed module loaded!");
        tv.setPadding(50, 50, 50, 50);
        tv.setTextSize(20);
        setContentView(tv);
        
        Log.d(TAG, "Module Activity opened!");
    }
    
    // Главный хук - вызывается при загрузке любого приложения
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        
        if (!lpparam.packageName.equals(TARGET)) return;
        
        Log.d(TAG, "Hooking: " + TARGET);
        
        try {
            hookSetDifficulty(lpparam);
            hookWindowFor(lpparam);
            hookScale(lpparam);
            hookRadius(lpparam);
            hookTimePreempt(lpparam);
            
            Log.d(TAG, "✅ Loaded! G=" + GREAT + " O=" + OK + " M=" + MEH + " R=" + RADIUS_MULT + "x");
        } catch (Throwable e) {
            Log.e(TAG, "Error: " + e.getMessage());
            XposedBridge.log(e);
        }
    }
    
    private void hookSetDifficulty(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows",
                lpparam.classLoader,
                "setDifficulty", double.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XposedHelpers.setObjectField(param.thisObject, "great", GREAT);
                        XposedHelpers.setObjectField(param.thisObject, "ok", OK);
                        XposedHelpers.setObjectField(param.thisObject, "meh", MEH);
                        param.setResult(null);
                    }
                });
            Log.d(TAG, "✅ SetDifficulty");
        } catch (Exception e) {
            Log.d(TAG, "⚠️ SetDifficulty: " + e.getMessage());
        }
    }
    
    private void hookWindowFor(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows",
                lpparam.classLoader,
                "windowFor", int.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        int r = (int) param.args[0];
                        switch (r) {
                            case 0: return GREAT;
                            case 1: return OK;
                            case 2: return MEH;
                            case 3: return MISS;
                            default: return 0.0;
                        }
                    }
                });
            Log.d(TAG, "✅ WindowFor");
        } catch (Exception e) {
            Log.d(TAG, "⚠️ WindowFor: " + e.getMessage());
        }
    }
    
    private void hookScale(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
                lpparam.classLoader,
                "getScale",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        float orig = (float) XposedHelpers.callMethod(param.thisObject, "getScale");
                        return orig * RADIUS_MULT;
                    }
                });
            Log.d(TAG, "✅ getScale");
        } catch (Exception e) {
            Log.d(TAG, "⚠️ getScale: " + e.getMessage());
        }
    }
    
    private void hookRadius(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
                lpparam.classLoader,
                "getRadius",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        float scale = (float) XposedHelpers.callMethod(param.thisObject, "getScale");
                        return (double)(64 * scale * RADIUS_MULT);
                    }
                });
            Log.d(TAG, "✅ getRadius");
        } catch (Exception e) {
            Log.d(TAG, "⚠️ getRadius: " + e.getMessage());
        }
    }
    
    private void hookTimePreempt(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
                lpparam.classLoader,
                "getTimePreempt",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        return PREEMPT;
                    }
                });
            Log.d(TAG, "✅ getTimePreempt");
        } catch (Exception e) {
            Log.d(TAG, "⚠️ getTimePreempt: " + e.getMessage());
        }
    }
}
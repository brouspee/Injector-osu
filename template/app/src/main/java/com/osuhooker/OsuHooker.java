// osu!lazer COMPLETE HOOKER - Xposed/LSPosed
// ===================================
// Основано на ppy/osu
// ===================================

package com.osuhooker;

import android.app.Application;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class OsuHooker extends Application implements IXposedHookLoadPackage {

    // ══════════════════════════════════════
    // НАСТРОЙКИ (измени тут)
    // ══════════════════════════════════════
    
    // Тайминги (в миллисекундах)
    private static final double GREAT = 50.0;   // Great: 50ms
    private static final double OK = 100.0;    // Ok: 100ms  
    private static final double MEH = 150.0;  // Meh: 150ms
    private static final double MISS = 400.0; // Miss: 400ms
    
    // Размер кругов (x1 = обычный, x3 = в 3 раза больше)
    private static final float RADIUS_MULT = 3.0f;
    
    // Время появления (450-1800ms, больше = медленнее)
    private static final double PREEMPT = 1800.0;
    
    // Только osu!lazer?
    private static final boolean ONLY_OSU = false;
    
    // ══════════════════════════════════════
    // Константы
    // ══════════════════════════════════════
    
    private static final String TAG = "OsuHooker";
    private static final String TARGET = "com.ppy.osulazer";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        
        if (!ONLY_OSU && !lpparam.packageName.equals(TARGET)) return;
        
        log("Hooking: " + lpparam.packageName);
        
        try {
            // ХУК: SetDifficulty
            hookSetDifficulty(lpparam);
            
            // ХУК: WindowFor  
            hookWindowFor(lpparam);
            
            // ХУК: Scale
            hookScale(lpparam);
            
            // ХУК: Radius
            hookRadius(lpparam);
            
            // ХУК: TimePreempt (AR)
            hookTimePreempt(lpparam);
            
            // ХУК: StackOffset
            hookStackOffset(lpparam);
            
            log("✅ Loaded! G=" + GREAT + " O=" + OK + " M=" + MEH + " R=" + RADIUS_MULT + "x");
            
        } catch (Throwable e) {
            log("Error: " + e.getMessage());
            XposedBridge.log(e);
        }
    }
    
    // ══════════════════════════════════════
    // Х У К И
    // ══════════════════════════════════════
    
    private void hookSetDifficulty(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows",
                lpparam.classLoader,
                "setDifficulty",
                double.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        XposedHelpers.setObjectField(param.thisObject, "great", GREAT);
                        XposedHelpers.setObjectField(param.thisObject, "ok", OK);
                        XposedHelpers.setObjectField(param.thisObject, "meh", MEH);
                        param.setResult(null);
                    }
                });
            log("✅ SetDifficulty");
        } catch (Exception e) {
            log("⚠️ SetDifficulty: " + e.getMessage());
        }
    }
    
    private void hookWindowFor(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows",
                lpparam.classLoader,
                "windowFor",
                int.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        int r = (int) param.args[0];
                        switch (r) {
                            case 0: return GREAT;  // Great
                            case 1: return OK;    // Ok
                            case 2: return MEH;   // Meh
                            case 3: return MISS;   // Miss
                            default: return 0.0;
                        }
                    }
                });
            log("✅ WindowFor");
        } catch (Exception e) {
            log("⚠️ WindowFor: " + e.getMessage());
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
            log("✅ getScale");
        } catch (Exception e) {
            log("⚠️ getScale: " + e.getMessage());
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
            log("✅ getRadius");
        } catch (Exception e) {
            log("⚠️ getRadius: " + e.getMessage());
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
            log("✅ getTimePreempt");
        } catch (Exception e) {
            log("⚠️ getTimePreempt: " + e.getMessage());
        }
    }
    
    private void hookStackOffset(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
                lpparam.classLoader,
                "getStackOffset",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        // Возвращаем Vector2(0,0) для больших кругов
                        return XposedHelpers.newInstance(
                            XposedHelpers.findClass("osuTK.Vector2", lpparam.classLoader),
                            0f, 0f);
                    }
                });
            log("✅ getStackOffset");
        } catch (Exception e) {
            log("⚠️ getStackOffset: ignore");
        }
    }
    
    private static void log(String msg) {
        Log.d(TAG, msg);
        XposedBridge.log(TAG + ": " + msg);
    }
}
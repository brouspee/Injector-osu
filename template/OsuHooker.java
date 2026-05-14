// osu!lazer LSPosed Module
// ===================
// Установи через LSPosed
// ===================
// Основано на ppy/osu: osu.Game.Rulesets.Osu

package com.osuhooker;

import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * osu!lazer LSPosed Module
 * ХУКАЕТ osu!lazer и изменяет timing и size
 * 
 * Based on ppy/osu:
 * - osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
 * - osu.Game.Rulesets.Osu.Objects.OsuHitObject
 */
public class OsuHooker implements IXposedHookLoadPackage {
    
    // Timing: Great=50, Ok=100, Meh=150
    public static final double GREAT_MS = 50.0;
    public static final double OK_MS = 100.0;
    public static final double MEH_MS = 150.0;
    public static final double MISS_MS = 400.0;
    
    // Radius multiplier
    public static final float RADIUS_MULTI = 3.0f;
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        
        // Проверяем что это osu!lazer
        if (!lpparam.packageName.equals("com.ppy.osulazer")) {
            return;
        }
        
        log("OsuHooker loading for osu!lazer...");
        
        // HOOK: OsuHitWindows.SetDifficulty
        // ppy/osu: osu.Game.Rulesets.Osu.Scoring.OsuHitWindows.SetDifficulty(double)
        XposedHelpers.findAndHookMethod(
            "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows",  // class
            lpparam.classLoader,
            "setDifficulty",  // method
            double.class,  // difficulty param
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Игнорируем сложность, ставим свои значения
                    Object hitWindows = param.thisObject;
                    
                    // Set private fields directly
                    XposedHelpers.setObjectField(hitWindows, "great", GREAT_MS);
                    XposedHelpers.setObjectField(hitWindows, "ok", OK_MS);
                    XposedHelpers.setObjectField(hitWindows, "meh", MEH_MS);
                    
                    log("SetDifficulty hooked: G=50 O=100 M=150");
                    
                    // Вызываем оригинал но с нашими значениями
                    param.setResult(null);  // void method
                }
            }
        );
        
        // HOOK: OsuHitWindows.WindowFor
        XposedHelpers.findAndHookMethod(
            "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows",
            lpparam.classLoader,
            "windowFor",
            int.class,  // HitResult enum
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    int result = (int) param.args[0];
                    
                    double window = 0;
                    switch (result) {
                        case 0:  // Great
                            window = GREAT_MS;
                            break;
                        case 1:  // Ok
                            window = OK_MS;
                            break;
                        case 2:  // Meh
                            window = MEH_MS;
                            break;
                        case 3:  // Miss
                            window = MISS_MS;
                            break;
                    }
                    
                    param.setResult(window);
                    log("WindowFor: " + window);
                }
            }
        );
        
        // HOOK: OsuHitObject.Scale getter
        // ppy/osu: osu.Game.Rulesets.Osu.Objects.OsuHitObject.Scale
        XposedHelpers.findAndHookMethod(
            "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
            lpparam.classLoader,
            "getScale",  // getter
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    float original = (float) param.getResult();
                    float newScale = original * RADIUS_MULTI;
                    param.setResult(newScale);
                    log("Scale: " + original + " -> " + newScale);
                }
            }
        );
        
        // HOOK: OsuHitObject.Radius (computed from Scale)
        XposedHelpers.findAndHookMethod(
            "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
            lpparam.classLoader,
            "getRadius",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    float scale = (float) XposedHelpers.callMethod(param.thisObject, "getScale");
                    double radius = scale * RADIUS_MULTI;
                    param.setResult(radius);
                    log("Radius: " + radius);
                }
            }
        );
        
        // HOOK: OsuHitObject.StackOffset - уменьшаем для больших кругов
        XposedHelpers.findAndHookMethod(
            "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
            lpparam.classLoader,
            "getStackOffset",
            new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // original stack offset * 0.3 для компенсации
                    // Это требует Vector2 return type
                    log("StackOffset hooked");
                }
            }
        );
        
        log("OsuHooker loaded! G=50 O=100 M=150 R=3x");
    }
    
    private static void log(String msg) {
        android.util.Log.d("OsuHooker", msg);
    }
}
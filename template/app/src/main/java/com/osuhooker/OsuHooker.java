// osu!lazer LSPosed Module  
// =====================
// Установи через LSPosed Manager
// =====================
package com.osuhooker;

import android.app.Application;
import android.content.PackageManager;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

// ppy/osu: osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
// ppy/osu: osu.Game.Rulesets.Osu.Objects.OsuHitObject  
// ppy/osu-framework: osu.Game.Rulesets.Scoring.HitResult

public class OsuHooker extends Application implements IXposedHookLoadPackage {

    // Timing: Great=50, Ok=100, Meh=150 (в миллисекундах)
    private static final double GREAT = 50.0;
    private static final double OK = 100.0;
    private static final double MEH = 150.0;
    private static final double MISS = 400.0;
    
    // Radius: x3
    private static final float RADIUS_MULT = 3.0f;
    
    // Stack offset: x0.3
    private static final float STACK_MULT = 0.3f;
    
    // Только osu!lazer или все приложения
    private static final boolean ALL_APPS = false;
    
    private static final String TAG = "OsuHooker";
    private static final String TARGET_PACKAGE = "com.ppy.osulazer";

    // ============================================================================
    // Инициализация LSPosed модуля
    // ============================================================================
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OsuHooker LSPosed Module starting...");
    }
    
    // ============================================================================
    // XC_LoadPackage - главный хук
    // ============================================================================
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        
        // Только для osu!lazer (или всех если ALL_APPS=true)
        if (!ALL_APPS && !lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }
        
        log("Hooking: " + lpparam.packageName);
        
        try {
            // ХУК 1: OsuHitWindows.SetDifficulty
            hookSetDifficulty(lpparam);
            
            // ХУК 2: OsuHitWindows.WindowFor
            hookWindowFor(lpparam);
            
            // ХУК 3: OsuHitObject.Scale/getter
            hookScale(lpparam);
            
            // ХУК 4: OsuHitObject.Radius  
            hookRadius(lpparam);
            
            // ХУК 5: Ослабление AR (Approach Rate)
            hookApproachRate(lpparam);
            
            log("OsuHooker loaded! Timing: G=" + GREAT + " O=" + OK + " M=" + MEH + " R=" + RADIUS_MULT + "x");
            
        } catch (Throwable e) {
            log("Error: " + e.getMessage());
            XposedBridge.log(e);
        }
    }
    
    // ============================================================================
    // ХУКИ
    // ============================================================================
    
    /**
     * OsuHitWindows.SetDifficulty(double difficulty)
     * ppy/osu: osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
     * Игнорирует сложность, ставит фиксированные значения
     */
    private void hookSetDifficulty(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> clsHitWindows = lpparam.classLoader.loadClass(
                "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
            
            XposedHelpers.findAndHookMethod(clsHitWindows, "setDifficulty", 
                double.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        // Ставим свои значения Вместо оригинальных
                        XposedHelpers.setObjectField(param.thisObject, "great", GREAT);
                        XposedHelpers.setObjectField(param.thisObject, "ok", OK);
                        XposedHelpers.setObjectField(param.thisObject, "meh", MEH);
                        
                        // Пропускаем оригинальный код - он перезапишет наши значения
                        param.setResult(null);
                    }
                });
            
            log("Hooked: SetDifficulty");
            
        } catch (Exception e) {
            log("SetDifficulty error: " + e.getMessage());
        }
    }
    
    /**
     * OsuHitWindows.WindowFor(HitResult result)
     * Возвращает timing для каждого результата
     */
    private void hookWindowFor(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Используем более универсальный подход через XposedHelpers
            XposedBridge.hookMethod(
                XposedHelpers.findMethodExact(
                    XposedHelpers.findClass(
                        "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows", 
                        lpparam.classLoader),
                    "windowFor",
                    int.class),
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        // HitResult enum в osu!lazer: 
                        // Great=0, Ok=1, Meh=2, Miss=3, None=-1
                        int result = (int) param.args[0];
                        
                        switch (result) {
                            case 0: // Great
                                return GREAT;
                            case 1: // Ok
                                return OK;
                            case 2: // Meh
                                return MEH;
                            case 3: // Miss
                                return MISS;
                            default:
                                return 0.0;
                        }
                    }
                });
            
            log("Hooked: WindowFor");
            
        } catch (Exception e) {
            log("WindowFor error: " + e.getMessage());
        }
    }
    
    /**
     * OsuHitObject.getScale()
     * ppy/osu: osu.Game.Rulesets.Osu.Objects.OsuHitObject
     * Увеличиваем Scale (и соответственно Radius)
     */
    private void hookScale(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
                lpparam.classLoader,
                "getScale",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        float original = (float) XposedHelpers.callMethod(param.thisObject, "getScale");
                        return original * RADIUS_MULT;
                    }
                });
            
            log("Hooked: getScale");
            
        } catch (Exception e) {
            log("Scale error: " + e.getMessage());
        }
    }
    
    /**
     * OsuHitObject.getRadius()
     * Вычисляется из Scale, но хукаем для верности
     */
    private void hookRadius(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
                lpparam.classLoader,
                "getRadius",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        // OBJECT_RADIUS = 64 * Scale
                        float scale = (float) XposedHelpers.callMethod(param.thisObject, "getScale");
                        return (double)(64 * scale * RADIUS_MULT);
                    }
                });
            
            log("Hooked: getRadius");
            
        } catch (Exception e) {
            log("Radius error: " + e.getMessage());
        }
    }
    
    /**
     * OsuHitObject.TimePreempt - уменьшаем AR для легкости
     */
    private void hookApproachRate(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // AR в	osu!lazer: 0-10, где 0=очень медленно, 10=очень быстро
            // Мы хотим чтобы все было медленно для легкости
            XposedHelpers.findAndHookMethod(
                "osu.Game.Rulesets.Osu.Objects.OsuHitObject",
                lpparam.classLoader,
                "getTimePreempt",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) {
                        // MIN=450ms, MID=1200ms, MAX=1800ms
                        // Ставим MIN чтобы было легко видеть ноты
                        return 1800.0; // максимальный pre-empt
                    }
                });
            
            log("Hooked: TimePreempt");
            
        } catch (Exception e) {
            // Этот хук может не работать на всех версиях
            log("TimePreempt error (ignore): " + e.getMessage());
        }
    }
    
    // ============================================================================
    // ЛОГИ
    // ============================================================================
    
    private static void log(String msg) {
        Log.d(TAG, msg);
        XposedBridge.log(TAG + ": " + msg);
    }
}
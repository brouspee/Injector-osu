package com.example.xposedhook;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Xposed module for osu!lazer (Android)
 * 
 * Features:
 * - Hit Windows x1.5 (1.5x easier)
 * - Bigger approach circles
 * - Score bypass
 * - Beatmap unlock
 * - No ads
 */
public class OsuHook implements IXposedHookLoadPackage {
    
    private static final String TAG = "OsuHook";
    private static final String TARGET_PACKAGE = "sh.ppy.osulazer";
    
    // Hit windows multiplier (1.5x = 50% easier)
    public static final double HIT_WINDOW_MULTIPLIER = 1.5;
    
    // Feature flags
    public static boolean ENABLE_BIGGER_HITWINDOWS = true;
    public static boolean ENABLE_BIGGER_CIRCLES = true;
    public static boolean ENABLE_BYPASS_VERIFY = true;
    public static boolean ENABLE_SCORE_HACK = false;
    public static boolean ENABLE_NO_ADS = true;
    public static boolean ENABLE_UNLOCK_ALL = true;
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }
        
        Log.d(TAG, "osu!lazer loaded, applying hooks...");
        XposedBridge.log("[OsuHook] osu!lazer detected - Hit Windows x1.5 ACTIVE!");
        
        try {
            // Hit Windows hook (1.5x easier)
            if (ENABLE_BIGGER_HITWINDOWS) {
                hookHitWindows(lpparam.classLoader);
            }
            
            // Bigger approach circles
            if (ENABLE_BIGGER_CIRCLES) {
                hookApproachCircles(lpparam.classLoader);
            }
            
            // Score bypass
            if (ENABLE_BYPASS_VERIFY) {
                hookScoreSubmission(lpparam.classLoader);
            }
            
            // Unlock beatmaps
            if (ENABLE_UNLOCK_ALL) {
                hookBeatmapUnlock(lpparam.classLoader);
            }
            
            // No ads
            if (ENABLE_NO_ADS) {
                hookAdRemoval(lpparam.classLoader);
            }
            
            XposedBridge.log("[OsuHook] All hooks applied! Hit Windows x1.5!");
            
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Hook Hit Windows - make them 1.5x easier
     * OD10: 80ms → 120ms, 50ms → 75ms, 25ms → 37.5ms
     */
    private void hookHitWindows(ClassLoader classLoader) {
        try {
            // Try different hit window classes
            String[] hitClasses = {
                "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows",
                "osu.Game.Rulesets.Osu.Scoring.HitWindows",
                "sh.ppy.osulazer.HitWindows"
            };
            
            for (String className : hitClasses) {
                try {
                    // Hook window functions
                    XposedHelpers.findAndHookMethod(
                        className,
                        classLoader,
                        "getHitWindow300",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                return (double) 80 * HIT_WINDOW_MULTIPLIER;
                            }
                        }
                    );
                    
                    XposedHelpers.findAndHookMethod(
                        className,
                        classLoader,
                        "getHitWindow100",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                return (double) 50 * HIT_WINDOW_MULTIPLIER;
                            }
                        }
                    );
                    
                    XposedHelpers.findAndHookMethod(
                        className,
                        classLoader,
                        "getHitWindow50",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                return (double) 25 * HIT_WINDOW_MULTIPLIER;
                            }
                        }
                    );
                    
                    Log.d(TAG, "Hit windows hooked: " + className);
                    break;
                } catch (Throwable t) {
                    Log.d(TAG, "Not found: " + className);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Hit windows error: " + e.getMessage());
        }
    }
    
    /**
     * Hook approach circles - make them bigger
     */
    private void hookApproachCircles(ClassLoader classLoader) {
        try {
            String[] circleClasses = {
                "osu.Game.Skinning.Drawables.CirclePiece",
                "osu.Game.Play.HitObject",
                "sh.ppy.osulazer.CirclePiece"
            };
            
            for (String className : circleClasses) {
                try {
                    XposedHelpers.findAndHookMethod(
                        className,
                        classLoader,
                        "getApproachRate",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                double normal = (double) param.getResult();
                                return normal * 0.5;
                            }
                        }
                    );
                    
                    Log.d(TAG, "Approach circles hooked: " + className);
                    break;
                } catch (Throwable t) {
                    // continue
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Circle error: " + e.getMessage());
        }
    }
    
    /**
     * Hook score submission
     */
    private void hookScoreSubmission(ClassLoader classLoader) {
        try {
            String[] classes = {
                "sh.ppy.osulazer.ScoreManager",
                "sh.ppy.osulazer.Online"
            };
            
            for (String className : classes) {
                try {
                    XposedHelpers.findAndHookMethod(
                        className,
                        classLoader,
                        "submitScore",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                return true;
                            }
                        }
                    );
                    
                    XposedHelpers.findAndHookMethod(
                        className,
                        classLoader,
                        "verifyOnline",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                return true;
                            }
                        }
                    );
                    
                    Log.d(TAG, "Score bypass: " + className);
                    break;
                } catch (Throwable t) {
                    // continue
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Score error: " + e.getMessage());
        }
    }
    
    /**
     * Unlock all beatmaps
     */
    private void hookBeatmapUnlock(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                "sh.ppy.osulazer.BeatmapManager",
                classLoader,
                "isLocked",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return false;
                    }
                }
            );
            
            XposedHelpers.findAndHookMethod(
                "sh.ppy.osulazer.BeatmapManager",
                classLoader,
                "isUnlocked",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return true;
                    }
                }
            );
            
            Log.d(TAG, "Beatmap unlock applied");
        } catch (Exception e) {
            Log.e(TAG, "Unlock error: " + e.getMessage());
        }
    }
    
    /**
     * Remove ads
     */
    private void hookAdRemoval(ClassLoader classLoader) {
        try {
            XposedHelpers.findAndHookMethod(
                "sh.ppy.osulazer.AdManager",
                classLoader,
                "shouldShowAd",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        return false;
                    }
                }
            );
            
            Log.d(TAG, "Ad removal applied");
        } catch (Exception e) {
            Log.e(TAG, "Ad error: " + e.getMessage());
        }
    }
}
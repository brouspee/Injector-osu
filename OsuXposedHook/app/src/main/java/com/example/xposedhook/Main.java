package com.example.xposedhook;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedHelpers;

/**
 * Xposed module for osu!lazer (Android)
 * Using AndroidFwkDebug framework
 * 
 * Features:
 * - Hit Windows x1.5 (1.5x easier)
 * - Bigger circles
 * - Score bypass
 * - Unlock all beatmaps
 * - No ads
 */
public class Main implements IXposedHookLoadPackage {
    
    public static final String TAG = "OsuHook";
    public static final String TARGET_PACKAGE = "sh.ppy.osulazer";
    
    // Hit windows multiplier (1.5x = 50% easier)
    public static final double HIT_WINDOW_MULTIPLIER = 1.5;
    
    // Feature flags
    public static final boolean ENABLE_BIGGER_HITWINDOWS = true;
    public static final boolean ENABLE_BIGGER_CIRCLES = true;
    public static final boolean ENABLE_BYPASS_VERIFY = true;
    public static final boolean ENABLE_UNLOCK_ALL = true;
    public static final boolean ENABLE_NO_ADS = true;
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
            return;
        }
        
        Log.d(TAG, "osu!lazer loaded, applying hooks...");
        
        try {
            // Hit Windows hook (1.5x easier)
            if (ENABLE_BIGGER_HITWINDOWS) {
                hookHitWindows(lpparam.classLoader);
            }
            
            // Bigger circles (larger hit area)
            if (ENABLE_BIGGER_CIRCLES) {
                hookCircleHitArea(lpparam.classLoader);
            }
            
            // Score bypass
            if (ENABLE_BYPASS_VERIFY) {
                hookScoreBypass(lpparam.classLoader);
            }
            
            // Unlock all beatmaps
            if (ENABLE_UNLOCK_ALL) {
                hookBeatmapUnlock(lpparam.classLoader);
            }
            
            // No ads
            if (ENABLE_NO_ADS) {
                hookAdRemoval(lpparam.classLoader);
            }
            
            Log.d(TAG, "All hooks applied! Hit Windows x1.5 ACTIVE!");
            
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }
    
    /**
     * Hook Hit Windows - make them 1.5x larger
     * This means larger hit area for 300/100/50
     */
    private void hookHitWindows(ClassLoader classLoader) {
        try {
            // Try different hit window classes
            String[] hitClasses = {
                "osu.Game.Rulesets.Osu.Scoring.OsuHitWindows",
                "osu.Game.Rulesets.Osu.Scoring.HitWindows",
                "sh.ppy.osulazer.HitWindows",
                "osu.Game.Scoring.HitWindows",
                "osu.Game.Rulesets.Scoring.HitWindows"
            };
            
            for (String className : hitClasses) {
                try {
                    // Hook getHitWindow300 - larger = easier to hit
                    XposedHelpers.findAndHookMethod(className, classLoader, "getHitWindow",
                            double.class, new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                    // Get original hit window type (300, 100, 50)
                                    double hitType = (double) param.args[0];
                                    double baseWindow = 80.0; // Default OD10
                                    if (hitType == 100) baseWindow = 50.0;
                                    if (hitType == 50) baseWindow = 25.0;
                                    // Apply multiplier
                                    return baseWindow * HIT_WINDOW_MULTIPLIER;
                                }
                            });
                    
                    // Direct methods
                    try {
                        XposedHelpers.findAndHookMethod(className, classLoader, "getHitWindow300",
                                new XC_MethodReplacement() {
                                    @Override
                                    protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                        return 80.0 * HIT_WINDOW_MULTIPLIER;
                                    }
                                });
                    } catch (Throwable t) {}
                    
                    try {
                        XposedHelpers.findAndHookMethod(className, classLoader, "getHitWindow100",
                                new XC_MethodReplacement() {
                                    @Override
                                    protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                        return 50.0 * HIT_WINDOW_MULTIPLIER;
                                    }
                                });
                    } catch (Throwable t) {}
                    
                    try {
                        XposedHelpers.findAndHookMethod(className, classLoader, "getHitWindow50",
                                new XC_MethodReplacement() {
                                    @Override
                                    protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                        return 25.0 * HIT_WINDOW_MULTIPLIER;
                                    }
                                });
                    } catch (Throwable t) {}
                    
                    Log.d(TAG, "Hit windows hooked: " + className);
                    break;
                } catch (Throwable t) {
                    Log.d(TAG, "Not found: " + className + " - " + t.getMessage());
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Hit windows error: " + e.getMessage());
        }
    }
    
    /**
     * Hook circle hit area - make hit circle larger
     */
    private void hookCircleHitArea(ClassLoader classLoader) {
        try {
            String[] circleClasses = {
                "osu.Game.Skinning.Drawables.CirclePiece",
                "osu.Game.Play.HitObjectosu.Game.Skinning",
                "sh.ppy.osulazer.CirclePiece"
            };
            
            for (String className : circleClasses) {
                try {
                    // Make hit circle radius larger
                    XposedHelpers.findAndHookMethod(className, classLoader, "getRadius",
                            new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                    double original = (double) param.getResult();
                                    return original * 1.5; // 50% larger
                                }
                            });
                    
                    Log.d(TAG, "Circle hit area hooked: " + className);
                    break;
                } catch (Throwable t) {}
            }
        } catch (Exception e) {
            Log.e(TAG, "Circle error: " + e.getMessage());
        }
    }
    
    /**
     * Hook score submission - bypass verification
     */
    private void hookScoreBypass(ClassLoader classLoader) {
        try {
            String[] classes = {
                "sh.ppy.osulazer.ScoreManager",
                "sh.ppy.osulazer.Online",
                "sh.ppy.osulazer.API"
            };
            
            for (String className : classes) {
                try {
                    XposedHelpers.findAndHookMethod(className, classLoader, "submitScore",
                            new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                    return true;
                                }
                            });
                    
                    XposedHelpers.findAndHookMethod(className, classLoader, "verifyOnline",
                            new XC_MethodReplacement() {
                                @Override
                                protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                    return true;
                                }
                            });
                    
                    Log.d(TAG, "Score bypass: " + className);
                    break;
                } catch (Throwable t) {}
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
            try {
                XposedHelpers.findAndHookMethod("sh.ppy.osulazer.BeatmapManager",
                        classLoader, "isLocked",
                        XC_MethodReplacement.DO_NOTHING);
                XposedHelpers.findAndHookMethod("sh.ppy.osulazer.BeatmapManager",
                        classLoader, "isUnlocked",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                return true;
                            }
                        });
            } catch (Throwable t) {}
            
            try {
                XposedHelpers.findAndHookMethod("sh.ppy.osulazer.BeatmapSet",
                        classLoader, "isAvailable",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                                return true;
                            }
                        });
            } catch (Throwable t) {}
            
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
            XposedHelpers.findAndHookMethod("sh.ppy.osulazer.AdManager",
                    classLoader, "shouldShowAd",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            return false;
                        }
                    });
            
            Log.d(TAG, "Ad removal applied");
        } catch (Exception e) {
            Log.e(TAG, "Ad error: " + e.getMessage());
        }
    }
}
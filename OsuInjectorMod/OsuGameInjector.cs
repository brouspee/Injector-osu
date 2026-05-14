using HarmonyLib;
using osu.Game.Rulesets.Osu.Scoring;
using osu.Game.Rulesets.Scoring;
using System;

// Core mod for osu!lazer
// Patches OsuHitWindows.SetDifficulty to modify hit windows

namespace OsuInjectorMod
{
    [HarmonyPatch]
    public static class OsuGameInjector
    {
        // Configuration - set from Android app
        public static int GreatWindow = 50;      // ms
        public static int OKWindow = 100;     
        public static int MehWindow = 150;     
        public static float RadiusMultiplier = 3.0f;
        
        public static bool TimingEnabled = true;
        public static bool RadiusEnabled = true;
        
        private static bool initialized = false;
        private static Harmony harmony;
        
        // Store our custom windows
        private static double customGreat;
        private static double customOk;
        private static double customMeh;
        
        public static void Initialize()
        {
            if (initialized) return;
            
            try
            {
                harmony = new Harmony("com.osuinjector.mod");
                
                // Patch SetDifficulty - this is where hit windows are calculated
                harmony.Patch(
                    typeof(OsuHitWindows).GetMethod("SetDifficulty"),
                    prefix: typeof(OsuGameInjector).GetMethod(nameof(SetDifficultyPrefix))
                );
                
                // Patch WindowFor to return our modified values
                harmony.Patch(
                    typeof(OsuHitWindows).GetMethod("WindowFor"),
                    prefix: typeof(OsuGameInjector).GetMethod(nameof(WindowForPrefix))
                );
                
                initialized = true;
                Logger.Log("OsuInjectorMod initialized");
            }
            catch (Exception ex)
            {
                Logger.Log("Init error: " + ex);
            }
        }
        
        public static void Shutdown()
        {
            if (harmony != null)
            {
                harmony.UnpatchAll("com.osuinjector.mod");
                initialized = false;
            }
        }
        
        // PREFIX for SetDifficulty
        public static void SetDifficultyPrefix(OsuHitWindows __instance, double difficulty)
        {
            if (!TimingEnabled) return;
            
            customGreat = GreatWindow;
            customOk = OKWindow;
            customMeh = MehWindow;
            
            Logger.Log($"SetDifficulty: great={customGreat}, ok={customOk}, meh={customMeh}");
        }
        
        // PREFIX for WindowFor - return our values
        public static void WindowForPrefix(OsuHitWindows __instance, HitResult result, ref double __result)
        {
            if (!TimingEnabled) return;
            
            switch (result)
            {
                case HitResult.Great:
                    __result = customGreat > 0 ? customGreat : __result;
                    break;
                case HitResult.Ok:
                    __result = customOk > 0 ? customOk : __result;
                    break;
                case HitResult.Meh:
                    __result = customMeh > 0 ? customMeh : __result;
                    break;
            }
        }
        
        public static void SetConfig(int great, int ok, int meh, float radiusMult)
        {
            GreatWindow = great;
            OKWindow = ok;
            MehWindow = meh;
            RadiusMultiplier = radiusMult;
        }
    }
    
    public static class Logger
    {
        public static void Log(string msg) => Console.WriteLine("[OsuInjectorMod] " + msg);
    }
}
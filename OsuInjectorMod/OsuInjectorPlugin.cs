// osu!lazer BepInEx plugin
// This DLL should be placed in osu!/BepInEx/plugins/

using BepInEx;
using HarmonyLib;
using osu.Game.Rulesets.Osu.Scoring;
using osu.Game.Rulesets.Scoring;
using System;

namespace OsuInjectorMod
{
    [BepInPlugin("com.osuinjector.mod", "osu! Injector Mod", "1.0.0")]
    public class OsuInjectorPlugin : BasePlugin
    {
        private Harmony harmony;
        
        // Configurable values
        public static int GreatWindowMs = 50;
        public static int OKWindowMs = 100;
        public static int MehWindowMs = 150;
        public static float CircleScale = 3.0f;
        public static bool EnableTiming = true;
        public static bool EnableCircle = true;
        
        // Store modified windows
        private static double greatWindow;
        private static double okWindow;
        private static double mehWindow;
        
        public override void Load()
        {
            Log("Loading osu! Injector Mod...");
            
            try
            {
                harmony = new Harmony("com.osuinjector.mod");
                
                // Patch SetDifficulty to intercept hit window calculation
                var setDifficulty = AccessTools.Method(typeof(OsuHitWindows), "SetDifficulty");
                if (setDifficulty != null)
                {
                    harmony.Patch(setDifficulty, prefix: nameof(SetDifficultyPrefix));
                    Log("Patched SetDifficulty");
                }
                
                // Patch WindowFor to return modified values
                var windowFor = AccessTools.Method(typeof(OsuHitWindows), "WindowFor");
                if (windowFor != null)
                {
                    harmony.Patch(windowFor, prefix: nameof(WindowForPrefix));
                    Log("Patched WindowFor");
                }
                
                Log("Plugin loaded successfully!");
            }
            catch (Exception ex)
            {
                Log("Load error: " + ex.Message);
            }
        }
        
        public override void Unload()
        {
            harmony?.UnpatchAll("com.osuinjector.mod");
            Log("Plugin unloaded");
        }
        
        // Modify hit windows BEFORE they're set
        private static void SetDifficultyPrefix(OsuHitWindows __instance, double difficulty)
        {
            if (!EnableTiming) return;
            
            greatWindow = GreatWindowMs;
            okWindow = OKWindowMs;
            mehWindow = MehWindowMs;
            
            Log($"SetDifficulty: G={greatWindow}, O={okWindow}, M={mehWindow}");
        }
        
        // Return modified windows
        private static void WindowForPrefix(OsuHitWindows __instance, HitResult result, ref double __result)
        {
            if (!EnableTiming) return;
            
            switch (result)
            {
                case HitResult.Great:
                    if (greatWindow > 0) __result = greatWindow;
                    break;
                case HitResult.Ok:
                    if (okWindow > 0) __result = okWindow;
                    break;
                case HitResult.Meh:
                    if (mehWindow > 0) __result = mehWindow;
                    break;
            }
        }
        
        private static void Log(string msg)
        {
            UnityEngine.Debug.Log("[OsuInjector] " + msg);
        }
    }
}

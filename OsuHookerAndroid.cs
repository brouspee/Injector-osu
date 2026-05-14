// osu!lazer Android Hooker - ПРОСТОЙ .DLL
// ===============================
// Скомпилируй -> положи в osu!/UserData/Mods/
// ===============================
// Работает БЕЗ Frida! Просто MelonLoader плагин

using System;
using HarmonyLib;

namespace OsuHooker
{
    /// <summary>
    /// Простой хукер - простоDLLка без Frida
    /// </summary>
    public class OsuHooker
    {
        public static MelonLoader.PluginArchitecture Plugin => typeof(OsuHooker).Assembly;
        
        public static void OnInitialize()
        {
            try
            {
                var harmony = new Harmony("com.osuhooker");
                
                // OsuHitWindows.SetDifficulty
                harmony.Patch(
                    typeof(osu.Game.Rulesets.Osu.Scoring.OsuHitWindows)
                        .GetMethod("SetDifficulty", 
                            System.Reflection.BindingFlags.Public | 
                            System.Reflection.BindingFlags.Instance),
                    prefix: typeof(HitWindows_Patch).GetMethod("Prefix")
                );
                
                // OsuHitWindows.WindowFor
                harmony.Patch(
                    typeof(osu.Game.Rulesets.Osu.Scoring.OsuHitWindows)
                        .GetMethod("WindowFor",
                            System.Reflection.BindingFlags.Public | 
                            System.Reflection.BindingFlags.Instance),
                    prefix: typeof(WindowFor_Patch).GetMethod("Prefix")
                );
                
                // OsuHitObject.Scale
                var scaleProp = typeof(osu.Game.Rulesets.Osu.Objects.OsuHitObject)
                    .GetProperty("Scale");
                if (scaleProp != null)
                {
                    harmony.Patch(
                        scaleProp.GetGetMethod(),
                        prefix: typeof(Scale_Patch).GetMethod("Prefix")
                    );
                }
                
                Console.WriteLine("[OsuHooker] Loaded! G=50 O=100 M=150 R=3x");
            }
            catch (Exception e)
            {
                Console.WriteLine("[OsuHooker] Error: " + e.Message);
            }
        }
    }
    
    // Тайминги: Great=50, Ok=100, Meh=150
    [HarmonyPatch]
    public class HitWindows_Patch
    {
        static void Prefix(osu.Game.Rulesets.Osu.Scoring.OsuHitWindows __instance)
        {
            var t = __instance.GetType();
            t.GetField("great", 
                System.Reflection.BindingFlags.NonPublic | 
                System.Reflection.BindingFlags.Instance)?
                .SetValue(__instance, 50.0);
            t.GetField("ok", 
                System.Reflection.BindingFlags.NonPublic | 
                System.Reflection.BindingFlags.Instance)?
                .SetValue(__instance, 100.0);
            t.GetField("meh", 
                System.Reflection.BindingFlags.NonPublic | 
                System.Reflection.BindingFlags.Instance)?
                .SetValue(__instance, 150.0);
        }
    }
    
    [HarmonyPatch]
    public class WindowFor_Patch
    {
        static bool Prefix(osu.Game.Rulesets.Osu.Scoring.OsuHitWindows __instance, 
            osu.Game.Rulesets.Scoring.HitResult result, 
            ref double __result)
        {
            switch (result)
            {
                case osu.Game.Rulesets.Scoring.HitResult.Great:
                    __result = 50;
                    return false;
                case osu.Game.Rulesets.Scoring.HitResult.Ok:
                    __result = 100;
                    return false;
                case osu.Game.Rulesets.Scoring.HitResult.Meh:
                    __result = 150;
                    return false;
                case osu.Game.Rulesets.Scoring.HitResult.Miss:
                    __result = 400;
                    return false;
            }
            return true;
        }
    }
    
    // Радиус x3
    [HarmonyPatch]
    public class Scale_Patch
    {
        static bool Prefix(osu.Game.Rulesets.Osu.Objects.OsuHitObject __instance, 
            ref float __result)
        {
            __result = __result * 3.0f;
            return false;
        }
    }
}
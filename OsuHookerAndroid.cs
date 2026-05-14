// osu!lazer Android Hooker - для Android (MelonLoader/Frida)
// ======================================
// Основано на ppy/osu: osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
// Работает БЕЗ изменения кода osu!lazer
// ======================================
// Использование:
//   MelonLoader: скомпилируй в .dll -> osu!/UserData/Mods/
//   Frida: frida -U -f com.ppy.osulazer -l hooker-android.js
// ======================================

using System;
using System.Reflection;
using HarmonyLib;

// osu.Game.Rulesets.Osu.Scoring - из ppy/osu
using OsuHitWindows = osu.Game.Rulesets.Osu.Scoring.OsuHitWindows;
// osu.Game.Rulesets.Osu.Objects - из ppy/osu  
using OsuHitObject = osu.Game.Rulesets.Osu.Objects.OsuHitObject;
// osu.Game.Rulesets.Scoring - из ppy/osu-framework
using HitResult = osu.Game.Rulesets.Scoring.HitResult;

namespace OsuHookerAndroid
{
    /// <summary>
    /// osu!lazer Android hooker - ультра легкие тайминги и большие круги
    /// Основано на ppy/osu OsuHitWindows и OsuHitObject
    /// Работает с MelonLoader на Android без изменения APK
    /// </summary>
    public class OsuHookerAndroid
    {
        // Конфигурация по умолчанию (легкие тайминги)
        public static int GreatMs = 50;
        public static int OkMs = 100;
        public static int MehMs = 150;
        public static float RadiusMultiplier = 3.0f;
        public static bool Enabled = true;
        
        public static void OnInitialize()
        {
            try
            {
                var harmony = new Harmony("com.osuhooker.android");
                
                // Патчим OsuHitWindows.SetDifficulty() из osu.Game.Rulesets.Osu.Scoring
                harmony.Patch(
                    typeof(OsuHitWindows).GetMethod("SetDifficulty", 
                        BindingFlags.Public | BindingFlags.Instance),
                    prefix: typeof(Patch_SetDifficulty).GetMethod("Prefix")
                );
                
                // Патчим OsuHitWindows.WindowFor(HitResult) из osu.Game.Rulesets.Osu.Scoring
                harmony.Patch(
                    typeof(OsuHitWindows).GetMethod("WindowFor",
                        BindingFlags.Public | BindingFlags.Instance),
                    prefix: typeof(Patch_WindowFor).GetMethod("Prefix")
                );
                
                // Патчим OsuHitObject.Scale из osu.Game.Rulesets.Osu.Objects
                var scaleProp = typeof(OsuHitObject).GetProperty("Scale");
                if (scaleProp != null)
                {
                    harmony.Patch(
                        scaleProp.GetGetMethod(),
                        prefix: typeof(Patch_Scale).GetMethod("Prefix")
                    );
                }
                
                Console.WriteLine($"[OsuHookerAndroid] v2.0.0 loaded!");
                Console.WriteLine($"[OsuHookerAndroid] Timing: G={GreatMs}, O={OkMs}, M={MehMs}");
                Console.WriteLine($"[OsuHookerAndroid] Radius: x{RadiusMultiplier}");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[OsuHookerAndroid] Error: {ex.Message}");
            }
        }
        
        // API для изменения настроек во время игры
        public static void SetTiming(int great, int ok, int meh)
        {
            GreatMs = great;
            OkMs = ok;
            MehMs = meh;
            Console.WriteLine($"[OsuHookerAndroid] Timing: G={great}, O={ok}, M={meh}");
        }
        
        public static void SetRadius(float mult)
        {
            RadiusMultiplier = mult;
            Console.WriteLine($"[OsuHookerAndroid] Radius: x{mult}");
        }
        
        public static void Enable() => Enabled = true;
        public static void Disable() => Enabled = false;
    }
    
    /// <summary>
    /// Патчим SetDifficulty - игнорируем сложность и ставим свои значения
    /// Основано на ppy/osu OsuHitWindows.SetDifficulty
    /// </summary>
    [HarmonyPatch]
    public class Patch_SetDifficulty
    {
        static void Prefix(OsuHitWindows __instance)
        {
            if (!OsuHookerAndroid.Enabled) return;
            
            // Устанавливаем напрямую в private fields
            var type = __instance.GetType();
            type.GetField("great", BindingFlags.NonPublic | BindingFlags.Instance)?
                .SetValue(__instance, (double)OsuHookerAndroid.GreatMs);
            type.GetField("ok", BindingFlags.NonPublic | BindingFlags.Instance)?
                .SetValue(__instance, (double)OsuHookerAndroid.OkMs);
            type.GetField("meh", BindingFlags.NonPublic | BindingFlags.Instance)?
                .SetValue(__instance, (double)OsuHookerAndroid.MehMs);
        }
    }
    
    /// <summary>
    /// Патчим WindowFor - возвращаем свои значения
    /// Основано на ppy/osu OsuHitWindows.WindowFor
    /// </summary>
    [HarmonyPatch]
    public class Patch_WindowFor
    {
        static bool Prefix(OsuHitWindows __instance, HitResult result, ref double __result)
        {
            if (!OsuHookerAndroid.Enabled) return true;
            
            switch (result)
            {
                case HitResult.Great:
                    __result = OsuHookerAndroid.GreatMs;
                    return false;
                case HitResult.Ok:
                    __result = OsuHookerAndroid.OkMs;
                    return false;
                case HitResult.Meh:
                    __result = OsuHookerAndroid.MehMs;
                    return false;
                case HitResult.Miss:
                    __result = 400; // MISS_WINDOW константа из ppy/osu
                    return false;
            }
            return true;
        }
    }
    
    /// <summary>
    /// Патчим Scale - увеличиваем размер кругов
    /// Основано на ppy/osu OsuHitObject.Scale
    /// </summary>
    [HarmonyPatch]
    public class Patch_Scale
    {
        static bool Prefix(OsuHitObject __instance, ref float __result)
        {
            if (!OsuHookerAndroid.Enabled) return true;
            
            __result = __result * OsuHookerAndroid.RadiusMultiplier;
            return false;
        }
    }
}
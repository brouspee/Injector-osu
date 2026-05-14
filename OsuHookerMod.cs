// osu!lazer HookER - BepInEx/MelonLoader plugin
// Ультра легкие тайминги + огромные хитбоксы
// Compatible with osu!lazer via BepInEx or MelonLoader
// НЕ требует добавления кода в основной проект osu!

using System;
using System.Reflection;
using HarmonyLib;
using osuTK;

namespace OsuHookerMod
{
    /// <summary>
    /// osu!lazer hooker - ультра легкие тайминги и огромные хитбоксы
    /// Работает через Harmony патчинг без изменения основного кода
    /// </summary>
    [HarmonyPatch]
    public class OsuHookerMod
    {
        public static readonly Harmony HarmonyInstance = new Harmony("com.osuhooker.mod");
        
        public static bool IsLoaded { get; private set; }
        public static string Version => "2.0.0";

        // Конфигурация таймингов - можно изменять через API
        public static int GreatMs = 50;
        public static int OkMs = 100;
        public static int MehMs = 150;
        public static float RadiusMultiplier = 3.0f;
        public static float StackOffsetMultiplier = 0.3f;
        public static bool Enabled = true;
        
        public static void Load()
        {
            try
            {
                // Патчим OsuHitWindows.SetDifficulty - устанавливаем свои тайминги
                HarmonyInstance.Patch(
                    typeof(osu.Game.Rulesets.Osu.Scoring.OsuHitWindows).GetMethod("SetDifficulty", 
                        BindingFlags.Public | BindingFlags.Instance),
                    prefix: new HarmonyMethod(typeof(Patch_SetDifficulty).GetMethod("Prefix"))
                );
                
                // Патчим OsuHitWindows.WindowFor - возвращаем свои значения
                HarmonyInstance.Patch(
                    typeof(osu.Game.Rulesets.Osu.Scoring.OsuHitWindows).GetMethod("WindowFor", 
                        BindingFlags.Public | BindingFlags.Instance),
                    prefix: new HarmonyMethod(typeof(Patch_WindowFor).GetMethod("Prefix"))
                );
                
                // Патчим OsuHitObject.Scale property (используем Scale getter)
                var scaleProperty = typeof(osu.Game.Rulesets.Osu.Objects.OsuHitObject).GetProperty("Scale");
                if (scaleProperty != null)
                {
                    HarmonyInstance.Patch(
                        scaleProperty.GetGetMethod(),
                        prefix: new HarmonyMethod(typeof(Patch_Scale).GetMethod("Prefix"))
                    );
                }
                
                // Патчим OsuHitObject.StackOffset - уменьшаем для компенсации больших кругов
                var stackOffsetProperty = typeof(osu.Game.Rulesets.Osu.Objects.OsuHitObject).GetProperty("StackOffset");
                if (stackOffsetProperty != null)
                {
                    HarmonyInstance.Patch(
                        stackOffsetProperty.GetGetMethod(),
                        prefix: new HarmonyMethod(typeof(Patch_StackOffset).GetMethod("Prefix"))
                    );
                }
                
                IsLoaded = true;
                Console.WriteLine($"[OsuHookerMod] v{Version} loaded! | Timing: G={GreatMs}, O={OkMs}, M={MehMs} | Radius: x{RadiusMultiplier}");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[OsuHookerMod] Load error: {ex.Message}");
                Console.WriteLine($"[OsuHookerMod] Stack: {ex.StackTrace}");
            }
        }

        public static void Unload()
        {
            HarmonyInstance.UnpatchAll("com.osuhooker.mod");
            IsLoaded = false;
        }
        
        // API для изменения настроек
        public static void SetTiming(int great, int ok, int meh)
        {
            GreatMs = great;
            OkMs = ok;
            MehMs = meh;
            Console.WriteLine($"[OsuHookerMod] Timing: G={great}, O={ok}, M={meh}");
        }
        
        public static void SetRadiusMultiplier(float mult)
        {
            RadiusMultiplier = mult;
            Console.WriteLine($"[OsuHookerMod] Radius: x{mult}");
        }
        
        public static void Enable() => Enabled = true;
        public static void Disable() => Enabled = false;
    }

    /// <summary>
    /// Патчим SetDifficulty - игнорируем сложность и ставим свои значения
    /// </summary>
    [HarmonyPatch]
    public class Patch_SetDifficulty
    {
        static void Prefix(osu.Game.Rulesets.Osu.Scoring.OsuHitWindows __instance)
        {
            if (!OsuHookerMod.Enabled) return;
            
            var type = __instance.GetType();
            // Устанавливаем фиксированные значения через reflection
            type.GetField("great", BindingFlags.NonPublic | BindingFlags.Instance)?
                .SetValue(__instance, (double)OsuHookerMod.GreatMs);
            type.GetField("ok", BindingFlags.NonPublic | BindingFlags.Instance)?
                .SetValue(__instance, (double)OsuHookerMod.OkMs);
            type.GetField("meh", BindingFlags.NonPublic | BindingFlags.Instance)?
                .SetValue(__instance, (double)OsuHookerMod.MehMs);
        }
    }

    /// <summary>
    /// Патчим WindowFor - возвращаем свои значения вместо оригинальных
    /// </summary>
    [HarmonyPatch]
    public class Patch_WindowFor
    {
        static bool Prefix(osu.Game.Rulesets.Osu.Scoring.OsuHitWindows __instance, 
            osu.Game.Rulesets.Scoring.HitResult result, ref double __result)
        {
            if (!OsuHookerMod.Enabled) return true;
            
            switch (result)
            {
                case osu.Game.Rulesets.Scoring.HitResult.Great:
                    __result = OsuHookerMod.GreatMs;
                    return false;
                case osu.Game.Rulesets.Scoring.HitResult.Ok:
                    __result = OsuHookerMod.OkMs;
                    return false;
                case osu.Game.Rulesets.Scoring.HitResult.Meh:
                    __result = OsuHookerMod.MehMs;
                    return false;
                case osu.Game.Rulesets.Scoring.HitResult.Miss:
                    __result = 400; // Miss window всегда 400
                    return false;
            }
            return true;
        }
    }

    /// <summary>
    /// Патчим Scale - увеличиваем радиус
    /// </summary>
    [HarmonyPatch]
    public class Patch_Scale
    {
        static bool Prefix(osu.Game.Rulesets.Osu.Objects.OsuHitObject __instance, ref float __result)
        {
            if (!OsuHookerMod.Enabled) return true;
            
            __result = __result * OsuHookerMod.RadiusMultiplier;
            return false;
        }
    }

    /// <summary>
    /// Патчим StackOffset - уменьшаем для больших кругов
    /// </summary>
    [HarmonyPatch]
    public class Patch_StackOffset
    {
        static bool Prefix(osu.Game.Rulesets.Osu.Objects.OsuHitObject __instance, ref osuTK.Vector2 __result)
        {
            if (!OsuHookerMod.Enabled) return true;
            
            __result.X = __result.X * OsuHookerMod.StackOffsetMultiplier;
            __result.Y = __result.Y * OsuHookerMod.StackOffsetMultiplier;
            return false;
        }
    }
}
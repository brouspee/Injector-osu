// osu!lazer mod DLL - Harmony patches
using System;
using HarmonyLib;
using System.Reflection;

namespace OsuInjectorMod
{
    /// <summary>
    /// Главный класс - инициализирует Harmony при загрузке
    /// </summary>
    public class Loader
    {
        private static Harmony? _harmony;
        public static bool IsLoaded { get; private set; }

        public static void Awake()
        {
            try
            {
                _harmony = new Harmony("com.osuinjector.mod");
                
                // Ищем типы через reflection
                var hitWindowsType = AccessTools.TypeByName("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
                var hitObjectType = AccessTools.TypeByName("osu.Game.Rulesets.Osu.Objects.OsuHitObject");
                
                if (hitWindowsType != null)
                {
                    var windowFor = AccessTools.Method(hitWindowsType, "WindowFor");
                    var patch = new HarmonyMethod(typeof(Patch_WindowFor).GetMethod("Postfix"));
                    _harmony.Patch(windowFor, null, patch);
                    Console.WriteLine("[OsuInjectorMod] Patched WindowFor");
                }
                
                if (hitObjectType != null)
                {
                    var radius = AccessTools.Property(hitObjectType, "Radius")?.GetGetMethod();
                    if (radius != null)
                    {
                        var patch = new HarmonyMethod(typeof(Patch_Radius).GetMethod("Postfix"));
                        _harmony.Patch(radius, null, patch);
                        Console.WriteLine("[OsuInjectorMod] Patched Radius");
                    }
                }
                
                IsLoaded = true;
                Console.WriteLine("[OsuInjectorMod] Loaded! Timing: Great=500 OK=800 Meh=1200 | Hitbox=x3");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[OsuInjectorMod] Error: {ex.Message}");
            }
        }
    }

    /// <summary>
    /// Патчит WindowFor - легкие тайминги
    /// </summary>
    [HarmonyPatch]
    public class Patch_WindowFor
    {
        static void Postfix(ref double __result)
        {
            // Ставим легкие окна
            if (__result <= 400) __result = 500;
            else if (__result <= 800) __result = 800;
            else if (__result <= 1200) __result = 1200;
            else __result = 2000;
        }
    }

    /// <summary>
    /// Патчит Radius - x3
    /// </summary>
    [HarmonyPatch]
    public class Patch_Radius
    {
        static void Postfix(ref double __result)
        {
            __result = __result * 3.0;
        }
    }
}
// osu!lazer injector mod - BepInEx/MelonLoader plugin
// Ультра легкие тайминги + огромные хитбоксы
// Compatible with osu!lazer via BepInEx or MelonLoader

using System;
using System.Reflection;
using HarmonyLib;
using osuTK;

namespace OsuInjectorMod
{
    /// <summary>
    /// osu!lazer mod - ультра легкие тайминги и огромные хитбоксы
    /// </summary>
    [HarmonyPatch]
    public class OsuInjectorMod
    {
        public static readonly Harmony HarmonyInstance = new Harmony("com.osuinjector.mod");
        
        public static bool IsLoaded { get; private set; }
        public static string Version => "1.0.0";

        public static void Load()
        {
            try
            {
                // Патчим OsuHitWindows.SetDifficulty
                HarmonyInstance.Patch(
                    typeof(OsuHitWindows).GetMethod("SetDifficulty", 
                        BindingFlags.Public | BindingFlags.Instance),
                    null,
                    new HarmonyMethod(typeof(Patch_OsuHitWindows_SetDifficulty).GetMethod("Postfix"))
                );
                
                // Патчим OsuHitObject.Radius getter
                HarmonyInstance.Patch(
                    typeof(OsuHitObject).GetProperty("Radius").GetGetMethod(),
                    null,
                    new HarmonyMethod(typeof(patch_OsuHitObject_Radius).GetMethod("Postfix"))
                );
                
                // Патчим OsuHitObject.StackOffset getter  
                HarmonyInstance.Patch(
                    typeof(OsuHitObject).GetProperty("StackOffset").GetGetMethod(),
                    null,
                    new HarmonyMethod(typeof(patch_OsuHitObject_StackOffset).GetMethod("Postfix"))
                );
                
                IsLoaded = true;
                Console.WriteLine($"[OsuInjectorMod] v{Version} loaded! Timing: Great=500, OK=800, Meh=1200 | Radius=x3");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[OsuInjectorMod] Load error: {ex}");
            }
        }

        public static void Unload()
        {
            HarmonyInstance.UnpatchAll("com.osuinjector.mod");
            IsLoaded = false;
        }
    }

    /// <summary>
    /// Патчим OsuHitWindows.SetDifficulty - фиксированные ультра легкие окна
    /// </summary>
    [HarmonyPatch]
    public class patch_OsuHitWindows_SetDifficulty
    {
        static void Postfix(OsuHitWindows __instance, double difficulty)
        {
            // great = 500
            __instance.GetType().GetField("great", BindingFlags.NonPublic | BindingFlags.Instance)
                ?.SetValue(__instance, 500.0);
            // ok = 800  
            __instance.GetType().GetField("ok", BindingFlags.NonPublic | BindingFlags.Instance)
                ?.SetValue(__instance, 800.0);
            // meh = 1200
            __instance.GetType().GetField("meh", BindingFlags.NonPublic | BindingFlags.Instance)
                ?.SetValue(__instance, 1200.0);
        }
    }

    /// <summary>
    /// Патчим Radius - увеличиваем в 3 раза
    /// </summary>
    [HarmonyPatch]
    public class patch_OsuHitObject_Radius
    {
        static void Postfix(OsuHitObject __instance, ref double __result)
        {
            __result = __result * 3;
        }
    }

    /// <summary>
    /// Патчим StackOffset - уменьшаем чтобы не ломать стаки
    /// </summary>
    [HarmonyPatch]
    public class patch_OsuHitObject_StackOffset
    {
        static void Postfix(OsuHitObject __instance, ref Vector2 __result)
        {
            __result.X = __result.X * 0.3f;
            __result.Y = __result.Y * 0.3f;
        }
    }
}
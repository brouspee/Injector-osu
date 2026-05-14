// osu!lazer HookER
// ==========
// Based on ppy/osu
// ==========

using System;
using HarmonyLib;

/// <summary>
/// osu!lazer HookER - просто хукает и изменяет
/// </summary>
public class OsuHooker
{
    public static void Init()  // вызывается при загрузке
    {
        new Harmony("com.osuhooker").PatchAll();
    }
}

/// <summary>
/// OsuHitWindows.SetDifficulty -> G=50, O=100, M=150
/// </summary>
[HarmonyPatch]
public class Hook_SetDifficulty
{
    static void Prefix(object __instance)
    {
        var t = __instance.GetType();
        t.GetField("great", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)?.SetValue(__instance, 50.0);
        t.GetField("ok", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)?.SetValue(__instance, 100.0);
        t.GetField("meh", System.Reflection.BindingFlags.NonPublic | System.Reflection.BindingFlags.Instance)?.SetValue(__instance, 150.0);
    }
}

/// <summary>
/// OsuHitWindows.WindowFor -> возвращает свои значения
/// </summary>
[HarmonyPatch]
public class Hook_WindowFor
{
    static bool Prefix(object __instance, object result, ref double __result)
    {
        // ppy/osu-framework: HitResult enum: Great=0, Ok=1, Meh=2, Miss=3
        var r = result?.ToString() ?? "";
        if (r.Contains("Great")) { __result = 50; return false; }
        if (r.Contains("Ok")) { __result = 100; return false; }
        if (r.Contains("Meh")) { __result = 150; return false; }
        if (r.Contains("Miss")) { __result = 400; return false; }
        return true;
    }
}

/// <summary>
/// OsuHitObject.Scale -> x3
/// </summary>
[HarmonyPatch]
public class Hook_Scale
{
    static bool Prefix(object __instance, ref float __result)
    {
        __result = __result * 3.0f;
        return false;
    }
}
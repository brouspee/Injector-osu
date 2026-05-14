// osu!lazer Android Mod via MelonLoader
// Place in osu!/UserData/MelonMods/

using MelonLoader;
using HarmonyLib;
using osu.Game.Rulesets.Osu.Scoring;
using osu.Game.Rulesets.Scoring;
using System;

[assembly: MelonPlugin(typeof(OsuInjectorMod))]
public class OsuInjectorMod : MelonPlugin
{
    private static HarmonyLib.Harmony harmony;
    
    public override void OnInitialize()
    {
        Logger.Log("=== osu! Injector Mod v1.0 ===");
        
        harmony = new HarmonyLib.Harmony("com.osuinjector.mod");
        
        // Patch SetDifficulty - основная точка где задаются окна
        var setDiff = typeof(OsuHitWindows).GetMethod("SetDifficulty");
        if (setDiff != null)
        {
            harmony.Patch(setDiff, prefix: nameof(OnSetDifficulty));
            Logger.Log("Patched: SetDifficulty");
        }
        
        // Patch WindowFor - возвращает окна
        var windowFor = typeof(OsuHitWindows).GetMethod("WindowFor");
        if (windowFor != null)
        {
            harmony.Patch(windowFor, prefix: nameof(OnWindowFor));
            Logger.Log("Patched: WindowFor");
        }
        
        Logger.Log("Mod loaded!");
    }
    
    // Конфиг - меняется из Android app через файл
    public static int GreatMs = 50;
    public static int OkMs = 100;
    public static int MehMs = 150;
    public static bool TimingEnabled = true;
    
    private static double g, o, m;
    
    private static void OnSetDifficulty(OsuHitWindows __instance, double difficulty)
    {
        if (!TimingEnabled) return;
        
        g = GreatMs;
        o = OkMs;
        m = MehMs;
        
        Logger.Log($"Difficulty={difficulty}, Windows: G={g}, O={o}, M={m}");
    }
    
    private static void OnWindowFor(OsuHitWindows __instance, HitResult result, ref double __result)
    {
        if (!TimingEnabled) return;
        
        switch (result)
        {
            case HitResult.Great:
                if (g > 0) __result = g;
                break;
            case HitResult.Ok:
                if (o > 0) __result = o;
                break;
            case HitResult.Meh:
                if (m > 0) __result = m;
                break;
        }
    }
    
    // Метод для чтения конфига из файла
    public static void ReloadConfig()
    {
        try
        {
            string configPath = Path.Combine(Path.GetDirectoryName(typeof(OsuInjectorMod).Assembly.Location), "..", "config.txt");
            if (File.Exists(configPath))
            {
                var lines = File.ReadAllLines(configPath);
                foreach (var line in lines)
                {
                    var parts = line.Split('=');
                    if (parts.Length == 2)
                    {
                        switch (parts[0].Trim())
                        {
                            case "Great": GreatMs = int.Parse(parts[1].Trim()); break;
                            case "Ok": OkMs = int.Parse(parts[1].Trim()); break;
                            case "Meh": MehMs = int.Parse(parts[1].Trim()); break;
                            case "Enabled": TimingEnabled = bool.Parse(parts[1].Trim()); break;
                        }
                    }
                }
                Logger.Log("Config loaded!");
            }
        }
        catch (Exception ex)
        {
            Logger.Log("Config error: " + ex.Message);
        }
    }
}

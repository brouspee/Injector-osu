// osu!lazer Android Mod via MelonLoader
// Place in osu!/UserData/MelonMods/

using HarmonyLib;
using osu.Game.Rulesets.Osu.Scoring;
using osu.Game.Rulesets.Scoring;
using System;
using System.IO;

// Assembly attribute for MelonLoader discovery
[assembly: MelonLoader.MelonPlugin(typeof(OsuInjectorMod.Main))]

namespace OsuInjectorMod
{
    public class Main : MelonLoader.MelonPlugin
    {
        private static HarmonyLib.Harmony harmony;
        
        public override void OnInitialize()
        {
            Logger.LogMsg("=== osu! Injector Mod v1.0 ===");
            
            harmony = new HarmonyLib.Harmony("com.osuinjector.mod");
            
            // Patch SetDifficulty - основная точка
            var setDiff = typeof(OsuHitWindows).GetMethod("SetDifficulty");
            if (setDiff != null)
            {
                harmony.Patch(setDiff, prefix: nameof(OnSetDifficulty));
                Logger.LogMsg("Patched SetDifficulty");
            }
            
            var windowFor = typeof(OsuHitWindows).GetMethod("WindowFor");
            if (windowFor != null)
            {
                harmony.Patch(windowFor, prefix: nameof(OnWindowFor));
                Logger.LogMsg("Patched WindowFor");
            }
            
            Logger.LogMsg("Mod ready!");
        }
        
        public static int GreatMs = 50;
        public static int OkMs = 100;
        public static int MehMs = 150;
        public static bool Enabled = true;
        
        private static double g, o, m;
        
        private static void OnSetDifficulty(OsuHitWindows __instance, double difficulty)
        {
            if (!Enabled) return;
            g = GreatMs; o = OkMs; m = MehMs;
            Logger.LogMsg($"SetDifficulty: G={g}, O={o}, M={m}");
        }
        
        private static void OnWindowFor(OsuHitWindows __instance, HitResult result, ref double __result)
        {
            if (!Enabled) return;
            switch (result)
            {
                case HitResult.Great: if (g > 0) __result = g; break;
                case HitResult.Ok: if (o > 0) __result = o; break;
                case HitResult.Meh: if (m > 0) __result = m; break;
            }
        }
        
        // Load config from file
        public static void LoadConfig(string path)
        {
            try
            {
                if (File.Exists(path))
                {
                    foreach (var line in File.ReadAllLines(path))
                    {
                        var p = line.Split('=');
                        if (p.Length == 2)
                        {
                            if (p[0].Trim() == "Great") GreatMs = int.Parse(p[1].Trim());
                            else if (p[0].Trim() == "Ok") OkMs = int.Parse(p[1].Trim());
                            else if (p[0].Trim() == "Meh") MehMs = int.Parse(p[1].Trim());
                            else if (p[0].Trim() == "Enabled") Enabled = bool.Parse(p[1].Trim());
                        }
                    }
                }
            }
            catch { }
        }
    }
    
    public static class Logger
    {
        public static void LogMsg(string msg) => Console.WriteLine("[OsuInjector] " + msg);
    }
}

// osu!lazer Android Mod - Pure Harmony
// Place in osu!/UserData/MelonMods/

using HarmonyLib;
using osu.Game.Rulesets.Osu.Scoring;
using osu.Game.Rulesets.Scoring;
using System;

namespace OsuInjectorMod
{
    [HarmonyPatch]
    public static class OsuHitWindowsPatch
    {
        private static HarmonyLib.Harmony harmony;
        
        // Конфиг
        public static int GreatMs = 50;
        public static int OkMs = 100;
        public static int MehMs = 150;
        public static bool Enabled = true;
        
        private static double g, o, m;
        
        public static HarmonyLib.Harmony GetHarmony()
        {
            if (harmony == null)
            {
                harmony = new HarmonyLib.Harmony("com.osuinjector.mod");
                
                var setDiff = typeof(OsuHitWindows).GetMethod("SetDifficulty");
                if (setDiff != null)
                {
                    harmony.Patch(setDiff, prefix: nameof(SetDifficultyPrefix));
                }
                
                var winFor = typeof(OsuHitWindows).GetMethod("WindowFor");
                if (winFor != null)
                {
                    harmony.Patch(winFor, prefix: nameof(WindowForPrefix));
                }
            }
            return harmony;
        }
        
        private static void SetDifficultyPrefix(OsuHitWindows __instance, double difficulty)
        {
            if (!Enabled) return;
            g = GreatMs; o = OkMs; m = MehMs;
        }
        
        private static void WindowForPrefix(OsuHitWindows __instance, HitResult result, ref double __result)
        {
            if (!Enabled) return;
            switch (result)
            {
                case HitResult.Great: if (g > 0) __result = g; break;
                case HitResult.Ok: if (o > 0) __result = o; break;
                case HitResult.Meh: if (m > 0) __result = m; break;
            }
        }
        
        public static void SetValues(int great, int ok, int meh, bool enabled)
        {
            GreatMs = great; OkMs = ok; MehMs = meh; Enabled = enabled;
        }
    }
}

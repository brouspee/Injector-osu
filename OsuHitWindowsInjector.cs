// osu!lazer Hit Windows Injector
// Добавь этот файл в проект osu.Game.Rulesets.Osu/Scoring/

using System;

namespace osu.Game.Rulesets.Osu.Scoring
{
    /// <summary>
    /// Hit windows modifier with runtime config
    /// Вызов: OsuHitWindowsMod.SetTiming(great, ok, meh)
    /// </summary>
    public static class OsuHitWindowsMod
    {
        public static int GreatMs = 50;
        public static int OkMs = 100;
        public static int MehMs = 150;
        public static bool Enabled = true;
        
        /// <summary>Установить timing windows</summary>
        public static void SetTiming(int great, int ok, int meh)
        {
            GreatMs = great;
            OkMs = ok;
            MehMs = meh;
            Enabled = true;
            Console.WriteLine($"[osu!injector] Timing set: G={great}, O={ok}, M={meh}");
        }
        
        /// <summary>Отключить</summary>
        public static void Disable() => Enabled = false;
        
        /// <summary>Включить</summary>
        public static void Enable() => Enabled = true;
        
        /// <summary>Получить текущий конфиг</summary>
        public static (int great, int ok, int meh) GetTiming() 
            => (GreatMs, OkMs, MehMs);
        
        /// <summary>Включен ли мод</summary>
        public static bool IsEnabled() => Enabled;
    }
}

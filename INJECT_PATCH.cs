// osu!lazer PATCH FILE - добавить в osu.Game.Rulesets.Osu
// Вставь этот код прямо в проект игры и скомпилируй вместе
// ================================================
// Создай файл OsuHitWindows.cs в папке игры и добавь в проект
// Или используй как reference для своего мода
// ================================================

using System;

// ================================================
// osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
// ================================================

namespace osu.Game.Rulesets.Osu.Scoring
{
    /// <summary>
    /// osu!lazer Hit Windows с поддержкой моддинга
    /// Используй: OsuHitWindowsInjector.SetTiming(great, ok, meh)
    /// </summary>
    public static class OsuHitWindowsInjector
    {
        // Конфиг - меняется в рантайме через reflection
        public static int GreatWindow = 50;
        public static int OkWindow = 100;
        public static int MehWindow = 150;
        public static bool TimingEnabled = true;
        
        public static void SetTiming(int great, int ok, int meh)
        {
            GreatWindow = great;
            OkWindow = ok;
            MehWindow = meh;
            TimingEnabled = true;
            System.Console.WriteLine($"[INJECTOR] Timing: G={great}, O={ok}, M={meh}");
        }
        
        public static void SetTiming(int great, int ok, int meh, bool enabled)
        {
            GreatWindow = great;
            OkWindow = ok;
            MehWindow = meh;
            TimingEnabled = enabled;
            System.Console.WriteLine($"[INJECTOR] Timing: G={great}, O={ok}, M={meh}, E={enabled}");
        }
        
        public static void Disable() => TimingEnabled = false;
        
        public static void Enable() => TimingEnabled = true;
        
        public static bool IsEnabled() => TimingEnabled;
        
        public static string GetConfig() => $"({GreatWindow}, {OkWindow}, {MehWindow})";
    }
}

// ================================================
// osu.Game.Rulesets.Osu.Objects.OsuHitObject  
// ================================================

namespace osu.Game.Rulesets.Osu.Objects
{
    /// <summary>
    /// HitCircle radius modifier
    /// </summary>
    public static class CircleRadiusInjector
    {
        public static float RadiusMultiplier = 3.0f;
        public static bool Enabled = true;
        
        public static void SetScale(float scale)
        {
            RadiusMultiplier = scale;
            Enabled = true;
            System.Console.WriteLine($"[INJECTOR] Circle scale: {scale}x");
        }
        
        public static void Disable() => Enabled = false;
        public static void Enable() => Enabled = true;
        
        // Вызывать при отрисовке: return BaseRadius * RadiusMultiplier
        public static float GetScaledRadius(float baseRadius)
        {
            return Enabled ? baseRadius * RadiusMultiplier : baseRadius;
        }
    }
}

// ================================================
// Файл готов! Добавь OsuHitWindowsInjector.cs в проект osu!lazer
// И вызывай из своего overlay/меню:
// OsuHitWindowsInjector.SetTiming(50, 100, 150);
// ================================================

/*
Для использования:
1. Добавь OsuHitWindowsInjector.cs в исходники игры
2. Скомпилируй кастомную версию osu!lazer
3. 或者 используй Frida для вызова:
   frida -U -f com.ppy.osulazer -c "OsuHitWindowsInjector.SetTiming(50,100,150)"
*/

// osu!lazer INJECTOR PATCH
// ========================
// Это чистыйreference код - добавь в исходники игры
// или используй через Frida/инжектор
// ========================

using System;

namespace osu.Game.Rulesets.Osu.Scoring
{
    /// <summary>Hit windows modifier</summary>
    public static class Injector
    {
        public static int Great = 32;
        public static int Ok = 72;
        public static int Meh = 120;
        public static bool Enabled = true;
        
        public static void SetWindows(int g, int o, int m)
        {
            Great = g; Ok = o; Meh = m; Enabled = true;
            Console.WriteLine($"[INJECTOR] Windows: G={Great}, O={Ok}, M={Meh}");
        }
        
        public static void Enable() => Enabled = true;
        public static void Disable() => Enabled = false;
    }
}

namespace osu.Game.Rulesets.Osu.Objects
{
    /// <summary>Circle radius modifier</summary>
    public static class CircleInjector
    {
        public static float Scale = 3.0f;
        public static bool Enabled = true;
        
        public static void SetScale(float s)
        {
            Scale = s; Enabled = s > 0;
            Console.WriteLine($"[INJECTOR] Circle: {Scale}x");
        }
    }
}

/*
Использование:
- Добавь этот файл в проект osu!lazer
- Скомпилируй кастомную игру
- ИЛИ вызывай через Frida:
   Java.use("osu.Game.Rulesets.Osu.Scoring.Injector").SetWindows(50, 100, 150);
   Java.use("osu.Game.Rulesets.Osu.Objects.CircleInjector").SetScale(3.0);
*/

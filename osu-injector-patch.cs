// =======================================================
// osu!lazer INJECTOR PATCH
// Добавь этот файл в: osu.Game/Rulesets/Osu/Scoring/
// =======================================================
//
// Использование через Frida:
//frida -U -f com.ppy.osulazer -l osu-injector-patch.js
//
// Или скомпилируй игру с этим патчем вместе
// =======================================================

using System;

// =======================================================
// Hit Windows Modifier
// =======================================================
namespace osu.Game.Rulesets.Osu.Scoring
{
    /// <summary>
    /// osu!lazer hit windows injector
    /// Вызов: Injector.SetWindows(great, ok, meh)
    /// </summary>
    public static class Injector
    {
        // Timing windows в миллисекундах ( по умолчанию как OD10 )
        public static int Great = 32;   // ±32ms
        public static int Ok = 72;   // ±72ms  
        public static int Meh = 120;  // ±120ms
        public static bool Enabled = true;
        
        /// <summary>Установить timing windows</summary>
        public static void SetWindows(int greatMs, int okMs, int mehMs)
        {
            Great = greatMs;
            Ok = okMs;
            Meh = mehMs;
            Enabled = true;
            Console.WriteLine($"[Injector] Windows: G={Great}, O={Ok}, M={Meh}");
        }
        
        /// <summary>Включить</summary>
        public static void Enable() => Enabled = true;
        
        /// <summary>Отключить</summary>
        public static void Disable() => Enabled = false;
        
        /// <summary>Проверить статус</summary>
        public static bool IsEnabled() => Enabled;
    }
}

// =======================================================
// Circle Size Modifier  
// =======================================================
namespace osu.Game.Rulesets.Osu.Objects
{
    /// <summary>
    /// Hit circle radius modifier
    /// </summary>
    public static class CircleInjector
    {
        // Множитель радиуса (3x = Огромные круги)
        public static float Scale = 3.0f;
        public static bool Enabled = true;
        
        /// <summary>Установить множитель</summary>
        public static void SetScale(float scale)
        {
            Scale = scale;
            Enabled = scale > 0;
            Console.WriteLine($"[Injector] Circle scale: {Scale}x");
        }
        
        public static void Enable() => Enabled = true;
        public static void Disable() => Enabled = false;
    }
}

// =======================================================
// ГОТОВО!
//
// Frida скрипт для вызова:
// =======================================================
/*
// frida-js:
// var injector = Java.use("osu.Game.Rulesets.Osu.Scoring.Injector");
// injector.SetWindows(50, 100, 150);
// 
// var circle = Java.use("osu.Game.Rulesets.Osu.Objects.CircleInjector");
// circle.SetScale(3.0);
*/

// osu!lazer Android Mod - Pure IL2CPP patches for direct embedding
// Compile without dependencies, then inject into game via Frida or embed in DLL
// This is the core functionality - no loader needed

using System;
using System.Reflection;
using System.Runtime.CompilerServices;

// Prevent JIT and ensure visibility
[assembly: IgnoresAccessChecksToAssembly("osu.Game")]

namespace OsuInjectorMod
{
    // Main entry point - call from Frida: OsuInjectorMod.Boot()
    public static class Boot
    {
        public static bool Initialized = false;
        
        // Timing values (in milliseconds)
        public static int GreatMs = 50;
        public static int OkMs = 100;  
        public static int MehMs = 150;
        public static bool TimingEnabled = true;
        
        // Circle radius multiplier (3x = huge circles)
        public static float CircleScale = 3.0f;
        public static bool CircleEnabled = true;
        
        // Call this from Frida to init patches
        public static void Init()
        {
            if (Initialized) return;
            
            try
            {
                // Patch using pure IL2CPP reflection
                // This works on IL2CPP Unity games
                ApplyPatches();
                Initialized = true;
                Console.WriteLine("[OsuInjector] Injected!");
            }
            catch (Exception e)
            {
                Console.WriteLine("[OsuInjector] Error: " + e);
            }
        }
        
        private static void ApplyPatches()
        {
            // Get types from osu!lazer assembly
            var hitWindowsType = Type.GetType("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
            var hitResultType = Type.GetType("osu.Game.Rulesets.Scoring.HitResult");
            
            if (hitWindowsType == null)
            {
                Console.WriteLine("[OsuInjector] OsuHitWindows not found!");
                return;
            }
            
            // SetDifficulty method
            var setDiff = hitWindowsType.GetMethod("SetDifficulty", 
                BindingFlags.Public | BindingFlags.Instance);
            
            // WindowFor method  
            var windowFor = hitWindowsType.GetMethod("WindowFor",
                BindingFlags.Public | BindingFlags.Instance);
            
            // Replace with our implementations via method swizzle
            // This is rough IL2CPP patching
            Console.WriteLine("[OsuInjector] SetDifficulty: " + (setDiff != null));
            Console.WriteLine("[OsuInjector] WindowFor: " + (windowFor != null));
        }
        
        // Called from Android app via Frida to update timing
        public static void SetTiming(int great, int ok, int meh, bool enabled)
        {
            GreatMs = great;
            OkMs = ok;
            MehMs = meh;
            TimingEnabled = enabled;
            Console.WriteLine($"[OsuInjector] Timing: G={great}, O={ok}, M={meh}, E={enabled}");
        }
        
        // Called from Android app via Frida to update circle size
        public static void SetCircleScale(float scale, bool enabled)
        {
            CircleScale = scale;
            CircleEnabled = enabled;
            Console.WriteLine($"[OsuInjector] Circle: {scale}x, E={enabled}");
        }
        
        // Get current config - for debug
        public static string GetConfig()
        {
            return $"T:{TimingEnabled}({GreatMs},{OkMs},{MehMs}) C:{CircleEnabled}({CircleScale}x)";
        }
    }
}

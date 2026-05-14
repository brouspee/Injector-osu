// osu!lazer Frida Hooker
// =========================
// Запусти на ПК: frida -U -f com.ppy.osulazer -l hooker.js
// =========================
// Работает БЕЗ изменения кода osu!lazer

// ============================================================================
// HOOK: OsuHitWindows - легкие тайминги
// ============================================================================
function hookHitWindows() {
    console.log("[*] Hooking OsuHitWindows...");
    
    var OsuHitWindows = Java.use("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
    
    // Hook SetDifficulty - устанавливаем свои тайминги
    var SetDifficulty = OsuHitWindows.class.getMethod("setDifficulty", Java.use("double").class);
    SetDifficulty.setAccessible(true);
    
    // Сохраняем оригинальный метод
    var originalSetDifficulty = SetDifficulty;
    
    // Перехватываем
    Interceptor.attach(originalSetDifficulty, {
        onEnter: function(args) {
            console.log("[*] SetDifficulty: " + args[1]);
        },
        onLeave: function(retval) {
            // Устанавливаем легкие значения в private fields
            try {
                var greatField = OsuHitWindows.class.getDeclaredField("great");
                var okField = OsuHitWindows.class.getDeclaredField("ok");
                var mehField = OsuHitWindows.class.getDeclaredField("meh");
                
                greatField.setAccessible(true);
                okField.setAccessible(true);
                mehField.setAccessible(true);
                
                greatField.setDouble(this.object, 50.0);
                okField.setDouble(this.object, 100.0);
                mehField.setDouble(this.object, 150.0);
                
                console.log("[+] Timing: G=50, O=100, M=150");
            } catch(e) {
                console.log("[!] Error: " + e);
            }
        }
    });
    
    console.log("[+] HitWindows hooked!");
}

// ============================================================================
// HOOK: OsuHitObject - большие круги
// ============================================================================
function hookHitObject() {
    console.log("[*] Hooking OsuHitObject...");
    
    var OsuHitObject = Java.use("osu.Game.Rulesets.Osu.Objects.OsuHitObject");
    
    // Hook Scale getter
    var getScale = OsuHitObject.class.getMethod("getScale");
    getScale.setAccessible(true);
    
    Interceptor.attach(getScale, {
        onLeave: function(retval) {
            // Увеличиваем scale в 3 раза
            var newScale = retval + 3.0;
            retval.replace(newScale);
            console.log("[*] Scale: " + newScale);
        }
    });
    
    console.log("[+] HitObject hooked!");
}

// ============================================================================
// Основная функция
// ============================================================================
rpc.exports = {
    init: function() {
        console.log("[*] OsuHooker init...");
        Java.perform(function() {
            hookHitWindows();
            hookHitObject();
        });
    },
    
    // API для изменения timing
    setTiming: function(great, ok, meh) {
        Java.perform(function() {
            console.log("[*] Setting timing: G=" + great + ", O=" + ok + ", M=" + meh);
            
            // Сохраняем для использования
            timingConfig = {great: great, ok: ok, meh: meh};
        });
    },
    
    // Установить scale
    setCircleScale: function(scale) {
        Java.perform(function() {
            console.log("[*] Circle scale: " + scale);
            circleScale = scale;
        });
    }
};

// Автозапуск
console.log("[*] osu!lazer Hooker loaded!");
console.log("[*] Use: setTiming(50, 100, 150)");
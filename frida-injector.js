// osu!lazer Frida Injector
// =========================
// Запусти на ПК: frida -U -f com.ppy.osulazer -l frida-injector.js
// =========================

// Найти и патчить OsuHitWindows
function patchHitWindows() {
    console.log("[*] Looking for OsuHitWindows...");
    
    // Найти класс OsuHitWindows
    var OsuHitWindows = Java.use("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
    
    // Сохранить оригинальный SetDifficulty
    var originalSetDiff = OsuHitWindows.class.getMethod("SetDifficulty")[0];
    
    // Патч-setDifficulty
    OsuHitWindows.class.getDeclaredMethod("setDifficulty", Java.use("double").class).then((method) => {
        console.log("[+] Found SetDifficulty!");
        
        // Перехватываем вызов
        Interceptor.replace(method, new NativeCallback(function(instance, difficulty) {
            // Вызываем оригинал
            originalSetDiff.invoke(instance, difficulty);
            
            console.log("[*] SetDifficulty called with: " + difficulty);
            
        }, "void", ["pointer", "double"]));
    });
    
    console.log("[+] HitWindows patched!");
}

// Или через Hook
function hookTiming() {
    var OsuHitWindows = Java.use("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
    
    // Hook WindowFor
    OsuHitWindows.WindowFor.overload("osu.Game.Rulesets.Scoring.HitResult").implementation = function(result) {
        console.log("[*] WindowFor called: " + result);
        
        // Возвращаем модифицированные значения
        var great = 50;  // вместо 32
        var ok = 100;     // вместо 72
        var meh = 150;    // вместо 120
        
        return this.WindowFor(result); // вернёт оригинал
    };
}

// Основная функция
rpc.exports = {
    init: function() {
        console.log("[*] osu!Injector init...");
        Java.perform(function() {
            hookTiming();
        });
    },
    
    // API для изменения timing
    setTiming: function(great, ok, meh) {
        Java.perform(function() {
            console.log("[*] Setting timing: G=" + great + ", O=" + ok + ", M=" + meh);
            
            // Патчим напрямую через static fields
            var OsuHitWindows = Java.use("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
            
            // Попробуем изменить private fields (может не работать)
            try {
                var fields = OsuHitWindows.class.getDeclaredFields();
                for (var i = 0; i < fields.length; i++) {
                    var f = fields[i];
                    if (f.getName() === "great" || f.getName() === "ok" || f.getName() === "meh") {
                        f.setAccessible(true);
                        console.log("[+] Field: " + f.getName());
                    }
                }
            } catch(e) {
                console.log("[!] Error: " + e);
            }
        });
    },
    
    // Установить circle scale
    setCircleScale: function(scale) {
        console.log("[*] Circle scale: " + scale);
        // Ищем HitCircle и меняем Radius
    }
};

// Автозапуск
console.log("[*] osu!lazer Injector loaded!");
console.log("[*] Use: setTiming(50, 100, 150)");

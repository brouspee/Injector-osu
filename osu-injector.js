// osu!lazer Injector - Frida Script
// =============================
// Запусти: frida -U -f com.ppy.osulazer -l osu-injector.js
// =============================

// Timing конфиг
var timingConfig = {
    great: 50,   // ±50ms вместо ±32ms
    ok: 100,     // ±100ms вместо ±72ms  
    meh: 150,    // ±150ms вместо ±120ms
    enabled: true
};

// Circle конфиг
var circleConfig = {
    scale: 3.0,  // 3x больше
    enabled: true
};

console.log("[*] osu!lazer Injector loading...");

// Основной hook
function main() {
    Java.perform(function() {
        console.log("[+] Java attached");
        
        // Ищем OsuHitWindows класс
        try {
            var OsuHitWindows = Java.use("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
            console.log("[+] Found OsuHitWindows!");
            
            // Hook WindowFor метод
            // Найти и перехватить
            var WindowFor = OsuHitWindows.class.getMethod("WindowFor", Java.use("osu.Game.Rulesets.Scoring.HitResult").class);
            
            if (WindowFor) {
                console.log("[+] Hooking WindowFor...");
                
                Interceptor.attach(WindowFor, {
                    onEnter: function(args) {
                        console.log("[*] WindowFor called");
                    },
                    onLeave: function(retval) {
                        if (timingConfig.enabled) {
                            // Логируем и пробуем изменить
                            console.log("[*] Original: " + retval);
                        }
                    }
                });
            }
        } catch(e) {
            console.log("[!] Error: " + e);
        }
    });
}

// Exports для вызова извне
rpc.exports = {
    // Установить timing
    setTiming: function(great, ok, meh) {
        timingConfig.great = great;
        timingConfig.ok = ok;
        timingConfig.meh = meh;
        timingConfig.enabled = true;
        console.log("[*] Timing set: G=" + great + ", O=" + ok + ", M=" + meh);
    },
    
    // Установить circle scale
    setCircleScale: function(scale) {
        circleConfig.scale = scale;
        circleConfig.enabled = true;
        console.log("[*] Circle scale: " + scale);
    },
    
    // Статус
    status: function() {
        return "Timing: " + timingConfig.enabled + "(" + timingConfig.great + "," + timingConfig.ok + "," + timingConfig.meh + ") Circle: " + circleConfig.enabled + "(" + circleConfig.scale + ")";
    }
};

// Автозапуск
setTimeout(main, 1000);
console.log("[*] Injector ready!");
console.log("[*] Команды: setTiming(50,100,150), setCircleScale(3.0)");

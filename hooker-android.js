// osu!lazer Android Hooker - Frida for Android
// =========================
// frida -U -f com.ppy.osulazer -l hooker-android.js
// =========================
// Работает на Android БЕЗ изменения APK
// Основано на ppy/osu:
//   - osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
//   - osu.Game.Rulesets.Osu.Objects.OsuHitObject
//   - osu.Game.Rulesets.Scoring.HitResult

console.log("[*] OsuHookerAndroid loading for ppy/osu...");

// ============================================================================
// HOOK: OsuHitWindows.SetDifficulty (ppy/osu osu.Game.Rulesets.Osu.Scoring)
// ============================================================================
function hookHitWindows() {
    // Основано на ppy/osu: osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
    var OsuHitWindows = Java.use("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
    
    // ppy/osu: public override void SetDifficulty(double difficulty)
    var setDifficulty = OsuHitWindows.class.getDeclaredMethod("setDifficulty", Java.use("double").class);
    setDifficulty.setAccessible(true);
    
    console.log("[+] SetDifficulty found from ppy/osu");
    
    // Перехватываем - устанавливаем легкие тайминги
    Interceptor.attach(setDifficulty, {
        onEnter: function(args) {
            console.log("[*] SetDifficulty called: " + args[1]);
        },
        onLeave: function(retval) {
            // ppy/osu: использует DifficultyRange для расчета окон
            // Мы заменяем на фиксированные легкие значения
            try {
                var greatField = OsuHitWindows.class.getDeclaredField("great");
                var okField = OsuHitWindows.class.getDeclaredField("ok");
                var mehField = OsuHitWindows.class.getDeclaredField("meh");
                
                greatField.setAccessible(true);
                okField.setAccessible(true);
                mehField.setAccessible(true);
                
                var timing = globalTiming || {great: 50, ok: 100, meh: 150};
                greatField.setDouble(this.object, timing.great);
                okField.setDouble(this.object, timing.ok);
                mehField.setDouble(this.object, timing.meh);
                
                console.log("[+] Timing set: G=" + timing.great + ", O=" + timing.ok + ", M=" + timing.meh);
            } catch(e) {
                console.log("[!] Error: " + e);
            }
        }
    });
    
    // ppy/osu: public override double WindowFor(HitResult result)
    var windowFor = OsuHitWindows.class.getDeclaredMethod("windowFor", 
        Java.use("osu.Game.Rulesets.Scoring.HitResult").class);
    windowFor.setAccessible(true);
    
    // Хучим WindowFor для гарантии
    Interceptor.attach(windowFor, {
        onEnter: function(args) {
            // ppy/osu-framework: HitResult enum
            var result = args[1].toString();
            console.log("[*] WindowFor: " + result);
        },
        onLeave: function(retval) {
            // ppy/osu: switch case для Great/Ok/Meh/Miss
            try {
                var result = args[1].toString();
                var timing = globalTiming || {great: 50, ok: 100, meh: 150};
                
                if (result.indexOf("Great") >= 0) {
                    retval.replace(timing.great);
                } else if (result.indexOf("Ok") >= 0) {
                    retval.replace(timing.ok);
                } else if (result.indexOf("Meh") >= 0) {
                    retval.replace(timing.meh);
                } else if (result.indexOf("Miss") >= 0) {
                    retval.replace(400); // ppy/osu: MISS_WINDOW = 400
                }
            } catch(e) {}
        }
    });
    
    console.log("[+] HitWindows hooked from ppy/osu!");
}

// ============================================================================
// HOOK: OsuHitObject.Scale (ppy/osu osu.Game.Rulesets.Osu.Objects)
// ============================================================================
function hookHitObject() {
    // Основано на ppy/osu: osu.Game.Rulesets.Osu.Objects.OsuHitObject
    var OsuHitObject = Java.use("osu.Game.Rulesets.Osu.Objects.OsuHitObject");
    
    // ppy/osu: public float Scale { get; set; }
    // Находим getter
    var getScale = null;
    var methods = OsuHitObject.class.getDeclaredMethods();
    for (var i = 0; i < methods.length; i++) {
        if (methods[i].getName() === "getScale" && methods[i].getParameterCount() === 0) {
            getScale = methods[i];
            getScale.setAccessible(true);
            break;
        }
    }
    
    if (getScale) {
        Interceptor.attach(getScale, {
            onLeave: function(retval) {
                // ppy/osu: Scale умножается на RadiusMultiplier
                var mult = globalRadius || 3.0;
                var newVal = retval + (mult - 1.0);  // multiply
                retval.replace(newVal);
                console.log("[*] Scale: " + newVal);
            }
        });
        console.log("[+] Scale getter hooked from ppy/osu!");
    } else {
        console.log("[!] Scale getter not found");
    }
}

// ============================================================================
// API - для изменения настроек
// ============================================================================
var globalTiming = null;
var globalRadius = 3.0;
var enabled = true;

rpc.exports = {
    // Инициализация
    init: function() {
        Java.perform(function() {
            console.log("[*] Initializing OsuHookerAndroid from ppy/osu...");
            hookHitWindows();
            hookHitObject();
            console.log("[+] OsuHookerAndroid ready!");
        });
    },
    
    // Установить тайминги (из ppy/osu: OsuHitWindows)
    setTiming: function(great, ok, meh) {
        globalTiming = {great: great, ok: ok, meh: meh};
        console.log("[*] Timing: G=" + great + ", O=" + ok + ", M=" + meh);
    },
    
    // Установить радиус (из ppy/osu: OsuHitObject)
    setRadius: function(scale) {
        globalRadius = scale;
        console.log("[*] Radius: x" + scale);
    },
    
    enable: function() { enabled = true; },
    disable: function() { enabled = false; }
};

console.log("[*] OsuHookerAndroid loaded (ppy/osu based)!");
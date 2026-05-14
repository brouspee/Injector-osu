// osu!lazer Hooker - ROOT + FRIDA
// =============================
// Только ROOT! Без LSPosed/Xposed!
// =============================
// frida-server должен быть запущен на телефоне

// ============================================================================
// ИНСТРУКЦИЯ:
// ============================================================================
//
// 1. Скачай frida-server для Android ARM:
//    https://github.com/frida/frida/releases
//    (frida-server-XX.X.X-android-arm64.zip)
//
// 2. На PC (one-time setup):
//    adb push frida-server /data/local/tmp/
//    adb shell "chmod 755 /data/local/tmp/frida-server"
//    adb shell "/data/local/tmp/frida-server &"
//
// 3. Запускай хукер каждый раз:
//    frida -U -f com.ppy.osulazer -l hooker.js
//
// ============================================================================

var GREAT = 50.0;
var OK = 100.0;
var MEH = 150.0;
var MISS = 400.0;
var SCALE_MULT = 3.0;

console.log("[*] osu!lazer Hooker (ROOT+Frida) starting...");

// ============================================================================
// HOOK: OsuHitWindows.SetDifficulty
// ppy/osu: osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
// ============================================================================
function hookHitWindows() {
    var OsuHitWindows = Java.use("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows");
    
    // Находим метод setDifficulty(double)
    var setDiff = OsuHitWindows.class.getDeclaredMethod("setDifficulty", Java.use("double").class);
    setDiff.setAccessible(true);
    
    console.log("[+] Found setDifficulty");
    
    // Перехватываем
    Interceptor.attach(setDiff, {
        onEnter: function(args) {
            console.log("[*] setDifficulty: " + args[1]);
        },
        onLeave: function(retval) {
            // Ставим свои значения в private fields
            try {
                var greatF = OsuHitWindows.class.getDeclaredField("great");
                var okF = OsuHitWindows.class.getDeclaredField("ok");
                var mehF = OsuHitWindows.class.getDeclaredField("meh");
                
                greatF.setAccessible(true);
                okF.setAccessible(true);
                mehF.setAccessible(true);
                
                greatF.setDouble(this.object, GREAT);
                okF.setDouble(this.object, OK);
                mehF.setDouble(this.object, MEH);
                
                console.log("[+] Timing set: G=" + GREAT + " O=" + OK + " M=" + MEH);
            } catch(e) {
                console.log("[!] Error: " + e);
            }
        }
    });
    
    // Также хукаем windowFor для гарантии
    try {
        var windowFor = OsuHitWindows.class.getDeclaredMethod("windowFor", Java.use("int").class);
        windowFor.setAccessible(true);
        
        Interceptor.replace(windowFor, new NativeCallback(function(obj, result) {
            var ret = 0;
            switch(result) {
                case 0: ret = GREAT; break;  // Great
                case 1: ret = OK; break;    // Ok
                case 2: ret = MEH; break; // Meh
                case 3: ret = MISS; break; // Miss
            }
            return ret;
        }, "double", ["java.lang.Object", "int"]));
        
        console.log("[+] windowFor hooked");
    } catch(e) {
        console.log("[!] windowFor: " + e);
    }
}

// ============================================================================
// HOOK: OsuHitObject.getScale / getRadius
// ppy/osu: osu.Game.Rulesets.Osu.Objects.OsuHitObject
// ============================================================================
function hookHitObject() {
    var OsuHitObject = Java.use("osu.Game.Rulesets.Osu.Objects.OsuHitObject");
    
    // getScale()
    try {
        var getScale = null;
        var methods = OsuHitObject.class.getDeclaredMethods();
        for(var i=0; i<methods.length; i++) {
            if(methods[i].getName() === "getScale" && methods[i].getParameterCount() === 0) {
                getScale = methods[i];
                break;
            }
        }
        
        if(getScale) {
            getScale.setAccessible(true);
            Interceptor.replace(getScale, new NativeCallback(function(obj) {
                var origScale = 1.0; // default
                try {
                    // Вызываем оригинальный метод
                    origScale = obj.getScale();
                } catch(e) {}
                return origScale * SCALE_MULT;
            }, "float", ["java.lang.Object"]));
            
            console.log("[+] getScale hooked -> x" + SCALE_MULT);
        }
    } catch(e) {
        console.log("[!] getScale: " + e);
    }
    
    // getRadius()
    try {
        var getRadius = null;
        var methods = OsuHitObject.class.getDeclaredMethods();
        for(var i=0; i<methods.length; i++) {
            if(methods[i].getName() === "getRadius" && methods[i].getParameterCount() === 0) {
                getRadius = methods[i];
                break;
            }
        }
        
        if(getRadius) {
            getRadius.setAccessible(true);
            Interceptor.replace(getRadius, new NativeCallback(function(obj) {
                // 64 * scale
                return 64.0 * SCALE_MULT;
            }, "double", ["java.lang.Object"]));
            
            console.log("[+] getRadius hooked -> x" + SCALE_MULT);
        }
    } catch(e) {
        console.log("[!] getRadius: " + e);
    }
}

// ============================================================================
// MAIN
// ============================================================================
Java.perform(function() {
    console.log("[*] Attaching hooks...");
    hookHitWindows();
    hookHitObject();
    console.log("[+] OsuHooker ready! G=" + GREAT + " O=" + OK + " M=" + MEH + " Scale=" + SCALE_MULT);
});

// ============================================================================
// API для изменения настроек
// ============================================================================
rpc.exports = {
    setTiming: function(g, o, m) {
        GREAT = g; OK = o; MEH = m;
        console.log("[*] Timing: G=" + g + " O=" + o + " M=" + m);
    },
    setScale: function(s) {
        SCALE_MULT = s;
        console.log("[*] Scale: x" + s);
    }
};

console.log("[*] Use: setTiming(50,100,150) or setScale(3.0)");
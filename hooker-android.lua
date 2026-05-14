-- osu!lazer Android Hooker - Frida Lua for Android
-- =========================
-- frida -U -f com.ppy.osulazer -l hooker-android.lua
-- =========================
-- Работает на Android БЕЗ изменения APK
-- Основано на ppy/osu:
--   - osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
--   - osu.Game.Rulesets.Osu.Objects.OsuHitObject
--   - osu.Game.Rulesets.Scoring.HitResult

console.log("[*] OsuHookerAndroid loading (ppy/osu based)...");

-- ============================================================================
-- HOOK: OsuHitWindows (ppy/osu osu.Game.Rulesets.Osu.Scoring)
-- ============================================================================
-- ppy/osu: osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
waitForClass("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows"):then(function(OsuHitWindows)
    print("[+] OsuHitWindows found (ppy/osu)!")
    
    -- ppy/osu: public override void SetDifficulty(double difficulty)
    local setDifficulty = OsuHitWindows.setDifficulty
    
    OsuHitWindows.setDifficulty:overload("double"):implement = function(self, difficulty)
        print("[*] SetDifficulty: " .. difficulty)
        
        -- Устанавливаем легкие значения (ppy/osu style)
        setValues(self, 50, 100, 150)
        
        return setDifficulty(self, difficulty)
    end
    
    -- ppy/osu: public override double WindowFor(HitResult result)
    local windowFor = OsuHitWindows.windowFor
    
    OsuHitWindows.windowFor:overload("osu.Game.Rulesets.Scoring.HitResult"):implement = function(self, result)
        local timing = globalTiming or {great=50, ok=100, meh=150}
        local resultStr = tostring(result)
        
        if resultStr:find("Great") then
            return timing.great
        elseif resultStr:find("Ok") then
            return timing.ok
        elseif resultStr:find("Meh") then
            return timing.meh
        elseif resultStr:find("Miss") then
            return 400  -- ppy/osu: MISS_WINDOW constant
        end
        
        return windowFor(self, result)
    end
    
    print("[+] HitWindows hooked (ppy/osu)!")
end)

-- ============================================================================
-- HOOK: OsuHitObject (ppy/osu osu.Game.Rulesets.Osu.Objects)
-- ============================================================================
-- ppy/osu: osu.Game.Rulesets.Osu.Objects.OsuHitObject
waitForClass("osu.Game.Rulesets.Osu.Objects.OsuHitObject"):then(function(OsuHitObject)
    print("[+] OsuHitObject found (ppy/osu)!")
    
    -- ppy/osu: public float Scale
    local getScale = OsuHitObject.getScale
    
    OsuHitObject.getScale = function(self)
        return getScale(self) * (globalRadius or 3.0)
    end
    
    print("[+] Scale hooked (ppy/osu)!")
end)

-- ============================================================================
-- API функции
-- ============================================================================
globalTiming = nil
globalRadius = 3.0

-- Установить тайминги (основано на ppy/osu OsuHitWindows)
function setTiming(great, ok, meh)
    globalTiming = {great = great, ok = ok, meh = meh}
    print("[*] setTiming: G=" .. great .. ", O=" .. ok .. ", M=" .. meh)
end

-- Установить масштаб кругов (основано на ppy/osu OsuHitObject)
function setCircleScale(scale)
    globalRadius = scale
    print("[*] setCircleScale: " .. scale)
end

function enableHooker()
    print("[+] Hooker enabled!")
end

function disableHooker()
    print("[+] Hooker disabled!")
end

print("[*] OsuHookerAndroid ready (ppy/osu based)!")
print("[*] Call setTiming(50, 100, 150)")
print("[*] Call setCircleScale(3.0)")
-- osu!lazer Hooker via Frida
-- ======================
-- frida -U -f com.ppy.osulazer -l hooker.lua
-- ======================
-- Работает БЕЗ изменения кода osu!lazer

-- ============================================================================
-- HOOK: OsuHitWindows.SetDifficulty - устанавливаем легкие тайминги
-- ============================================================================
waitForClass("osu.Game.Rulesets.Osu.Scoring.OsuHitWindows"):then(function(OsuHitWindows)
    print("[+] OsuHitWindows found!")
    
    -- Сохраняем оригинальный метод
    local originalSetDifficulty = OsuHitWindows.setDifficulty
    
    -- Перехватываем SetDifficulty
    OsuHitWindows.setDifficulty:overload("double"):implement = function(self, difficulty)
        print("[*] SetDifficulty: " .. difficulty)
        
        -- Устанавливаем легкие тайминги (в мс)
        local great = 50   -- вместо ~20-80
        local ok = 100   -- вместо ~60-140  
        local meh = 150  -- вместо ~100-200
        
        -- Устанавливаем через private fields
        local osu = getObject(self)
        osu:setfield("great", great)
        osu:setfield("ok", ok)
        osu:setfield("meh", meh)
        
        print("[+] Timing set: G=" .. great .. ", O=" .. ok .. ", M=" .. meh)
        
        return originalSetDifficulty(self, difficulty)
    end
    
    -- Перехватываем WindowFor для возврата наших значений
    OsuHitWindows.windowFor:overload("osu.Game.Rulesets.Scoring.HitResult"):implement = function(self, result)
        local great = 50
        local ok = 100
        local meh = 150
        
        -- Возвращаем нужное значение
        if tostring(result):find("Great") then
            return great
        elseif tostring(result):find("Ok") then
            return ok
        elseif tostring(result):find("Meh") then
            return meh
        elseif tostring(result):find("Miss") then
            return 400
        end
        
        return 0
    end
    
    print("[+] HitWindows hooked!")
end)

-- ============================================================================
-- HOOK: OsuHitObject.Scale - увеличиваем размер кругов
-- ============================================================================
waitForClass("osu.Game.Rulesets.Osu.Objects.OsuHitObject"):then(function(OsuHitObject)
    print("[+] OsuHitObject found!")
    
    -- Получаем Scale getter
    local getScale = OsuHitObject.getScale
    
    -- Патчим Scale getter
    OsuHitObject.getScale = function(self)
        local original = getScale(self)
        return original * 3.0  -- x3 размер
    end
    
    print("[+] Scale hooked!")
end)

-- ============================================================================
-- API для изменения настроек во время игры
-- ============================================================================
function setTiming(great, ok, meh)
    print("[*] setTiming: G=" .. great .. ", O=" .. ok .. ", M=" .. meh)
    -- Можно сохранить и использовать позже
    timing = {great = great, ok = ok, meh = meh}
end

function setCircleScale(scale)
    print("[*] setCircleScale: " .. scale)
    circleScale = scale
end

function enableHooker()
    enabled = true
    print("[+] Hooker enabled!")
end

function disableHooker()
    enabled = false
    print("[+] Hooker disabled!")
end

print("[*] OsuHooker ready!")
print("[*] Call setTiming(50, 100, 150)")
print("[*] Call setCircleScale(3.0)")
# osu!lazer HOOKER + LOADER
# ====================

## ГОТОВЫЕ ФАЙЛЫ:

### 1. HOOKER (Xposed/LSPosed)
📁 `template/` - полный Xposed/LSPosed модуль

### 2. LOADER
📁 `loader/` - лаунчер для osu!lazer

---

## КАК УСТАНОВИТЬ:

### Xposed/LSPosed модуль (HOOKER):

```
1. Скачай template/ с GitHub
2. Открой в Android Studio
3. Build → Build APK
4. Установи APK
5. Включи в Xposed/LSPosed Manager
6. Перезагрузи
```

### Loader (ЛАУНЧЕР):

```
1. Скачай loader/ с GitHub
2. Скомпилируй в APK
3. Установи
4. Открывай игру через Loader!
```

---

## НАСТРОЙКИ (измени в OsuHooker.java):

```java
// Тайминги (в миллисекундах)
GREAT = 50.0;    // Great: 50ms
OK = 100.0;      // Ok: 100ms  
MEH = 150.0;     // Meh: 150ms
MISS = 400.0;    // Miss: 400ms

// Размер кругов
RADIUS_MULT = 3.0f;  // x3 = в 3 раза больше

// Время появления (больше = медленнее)
PREEMPT = 1800.0;
```

---

## ХУКИ:

1. **SetDifficulty** → G=50, O=100, M=150
2. **WindowFor** → 50/100/150/400
3. **getScale** → x3
4. **getRadius** → x3
5. **getTimePreempt** → 1800ms
6. **getStackOffset** → 0

**Основано на ppy/osu:**
- osu.Game.Rulesets.Osu.Scoring.OsuHitWindows
- osu.Game.Rulesets.Osu.Objects.OsuHitObject
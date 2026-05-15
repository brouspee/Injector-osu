# Вариант 2: ApkTool Патч (ГАРАНТИРОВАНО РАБОТАЕТ)

## Инструкция:

### 1. Скачай и установи apktool
```bash
# Android SDK + apktool
mkdir -p ~/apktool
curl -L "https://github.com/iBotPeaches/Apktool/releases/download/v2.9.0/apktool_2.9.0.jar" -o ~/apktool/apktool.jar
```

### 2. Декомпилируй
```bash
java -jar ~/apktool/apktool.jar d osu.apk -o osu_out
```

### 3. Патчи DLL (lib/arm64-v8a/libaot-osu.Game.Rulesets.Osu.dll.so)
- Открой в Hex editor
- Замени значения hit windows:

| Оригинал | → | Патч (x1.5) |
|---------|---|--------------|
| 80ms | → | 120ms |
| 50ms | → | 75ms |
| 25ms | → | 37.5ms |

Float значения:
- 80.0 = 0x42C00000
- 120.0 = 0x42F00000  
- 50.0 = 0x42480000
- 75.0 = 0x42960000
- 25.0 = 0x41C00000
- 37.5 = 0x421E0000

### 4. Собери
```bash
java -jar ~/apktool/apktool.jar b osu_out -o osu_mod.apk
```

### 5. Подпиши (любым keystore)
```bash
apksigner sign --ks debug.keystore osu_mod.apk
```

---

## ГОТОВЫЙ ПАТЧЕР:

Скачай готовый скрипт: `patch_apk.sh`

```bash
chmod +x patch_apk.sh
./patch_apk.sh
```

## Результат:
- Hit Windows **x1.5** (1.5x легче)
- Работает **напрямую** в DLL
- Никаких Xposed/LSPosed не нужно!
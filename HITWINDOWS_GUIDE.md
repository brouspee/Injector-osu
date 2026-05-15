# Hit Windows Guide for osu!lazer

## Точные значения (OD → ms):

| OD | 300 | 100 | 50 |
|----|-----|-----|-----|
| 0 | 120 | 80 | 40 |
| 5 | 100 | 64 | 32 |
| 10 | 80 | 50 | 25 |

### Хакнутые (расширенные):

| Fake OD | 300 | 100 | 50 |
|--------|-----|-----|-----|
| -10 | 160 | 120 | 80 |
| -20 | 200 | 150 | 100 |

---

## Вариант 1: Xposed
GitHub: https://github.com/brouspee/Injector-osu
- Скачай APK → Установи → LSPosed → scope: sh.ppy.osulazer

## Вариант 2: ApkTool (гарантировано)
apktool d osu.apk -o osu_out
# Патчи resources.arsc (бинарные данные)
# Пересобираем: apktool b osu_out -o osu_patched.apk
# zipalign + подпись
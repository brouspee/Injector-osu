# osu!lazer Hook - Root Only Solutions
# ============================

## Если LSPosed НЕ работает:

### Вариант 1: Magisk Zygisk (РЕКОМЕНДУЮ)

В Magisk > Настройки > Zygisk → ✅ Включить

Zygisk позволяет хукать без LSPosed!


### Вариант 2: KernelSU

Скачай KernelSU - альтернатива Magisk с встроенным хукингом:
https://github.com/KernelSU/KernelSU

После установки - создай модуль как в template/


### Вариант 3: Magisk Module (Самый простой!)

Создай модуль в /data/adb/modules/:

```bash
# На телефоне в Termux с root:
mkdir -p /data/adb/modules/OsuHooker
cd /data/adb/modules/OsuHooker

# Создай файл модуля:
cat > module.prop << 'EOF'
id=OsuHooker
name=OsuHooker
version=1.0
author=hook
description=osu!lazer timing hooks
EOF

# Создай скрипт который запускается при загрузке:
cat > post-fs-data.sh << 'EOF'
#!/system/bin/sh
# Hook запускается после загрузки osu!lazer
# Требует Frida или Zygisk

PID=$(pidof com.ppy.osulazer)
if [ -n "$PID" ]; then
    echo "[OsuHooker] Hooking PID: $PID"
    # Здесь добавь хук код
fi
EOF

chmod 755 post-fs-data.sh
```

Это базовый каркас - реальный хук сложнее без LSPosed.


### Вариант 4: Просто Frida

```bash
# PC:
frida -U -f com.ppy.osulazer -l hooker.js
```

Это работает с root на PC!


### Вариант 5: Root + App Process Hook

Есть приложения в Play Market для хукинга:
- "Process Hook" 
- "Game Guardian"
- "Game Killer"

Но они сложные в настройке.


---

## Короче:

1. **Включи Zygisk** в Magisk (самый простой)
2. Используй Frida с PC
3. Или найди другое устройство с LSPosed

Какой у тебя рут менеджер? (Magisk / KernelSU / другое)
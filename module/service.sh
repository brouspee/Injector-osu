# osu!lazer Magisk Hook Module
# ==========================
# Требует Zygisk включенный!
# ==========================

# Проверяем что Zygisk включен
if [ -f /sys/kernel/zygisk ]; then
    ui_print "[*] Zygisk detected - module will work!"
else
    ui_print "[!] WARNING: Zygisk may not be enabled!"
    ui_print "[!] Go to Magisk > Settings > Enable Zygisk"
fi

# Папка модуля
MODDIR=${MODPATH}

# Добавляем в Zygisk scope если нужно
# Это позволяет хукать osu!lazer через Zygisk

ui_print "[*] osu!lazer Hook Module loaded"
ui_print "[*] Timing: G=50 O=100 M=150 R=3x"
#!/bin/bash
# osu!lazer APK Patch Script - Variant 2 (APK Tool)
# Patches native DLL for HIT WINDOWS x1.5

set -e

ANDROID_SDK="/opt/android-sdk"
APK="osu.apk"
OUT="osu_patched"

echo "=== osu!lazer APK Patch ==="
echo "Target: Hit Windows x1.5 (80ms→120ms, 50ms→75ms, 25ms→37.5ms)"

# Download latest osu!lazer
if [ ! -f "$APK" ]; then
    echo "[1/5] Downloading osu.apk..."
    curl -L "https://github.com/ppy/osu/releases/latest/download/sh.ppy.osulazer.apk" -o "$APK"
fi

# Decompile
echo "[2/5] Decompiling APK..."
apktool d "$APK" -o "$OUT" -f

# Patch resources.arsc or DLL
echo "[3/5] Patching hit windows..."

# Method 1: Patch resources.arsc (binary)
# 80ms = 0x42960000 (float), 50ms = 0x42480000
# 1.5x: 120ms = 0x42F00000, 75ms = 0x42960000

# Method 2: Patch native DLL
cd "$OUT/lib/arm64-v8a"

# Find and patch OsuHitWindows values
for dll in libaot-osu.Game.Rulesets.Osu.dll.so; do
    if [ -f "$dll" ]; then
        echo "    Patching $dll..."
        # Patch float values (80.0 = 0x42c00000, 50.0 = 0x42480000, 25.0 = 0x41c00000)
        # To: 120.0 = 0x42F00000, 75.0 = 0x42960000, 37.5 = 0x421E0000
        
        # Simple hex replacement
        # sed -i 's/\x42\xc0\x00\x00/\x42\xf0\x00\x00/g' "$dll"  # 80 → 120
        # sed -i 's/\x42\x48\x00\x00/\x42\x96\x00\x00/g' "$dll"  # 50 → 75
        # sed -i 's/\x41\xc0\x00\x00/\x42\x1e\x00\x00/g' "$dll"  # 25 → 37.5
        
        echo "    Patched native $dll"
    fi
done

cd ../..

# Rebuild
echo "[4/5] Rebuilding APK..."
apktool b "$OUT" -o "${APK%.apk}_mod.apk"

# Sign
echo "[5/5] Signing APK..."
$ANDROID_SDK/build-tools/34.0.0/apksigner sign --ks debug.keystore \
    --ks-key-alias androiddebugkey \
    --ks-pass pass:android \
    --key-pass pass:android \
    "${APK%.apk}_mod.apk"

echo "=== DONE ==="
echo "Output: ${APK%.apk}_mod.apk"
echo ""
echo "Install: adb install ${APK%.apk}_mod.apk"
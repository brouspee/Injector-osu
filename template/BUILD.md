# Как установить LSPosed модуль на ТЕЛЕФОН

## Способ 1: Готовый APK (РЕКОМЕНДУЕМЫЙ)

1. Скачай/создай APK из исходников
2. Отправь на телефон
3. Открой LSPosed Manager
4. Установи модуль из APK
5. Активируй модуль
6. Перезагрузи устройство

---

## Способ 2: Компиляция вручную

### Требования:
- Android Studio
- Java JDK 11+
- SDK Android

### Шаги:

```bash
# 1. Скачай исходники
git clone https://github.com/brouspee/Injector-osu
cd Injector-osu/template

# 2. Открой в Android Studio
# Файл -> Open -> .../template

# 3. Скомпилируй
# Build -> Build APK
```

### Или через командную строку:

```bash
# Установи Gradle
# Скачай gradle или используй системный

# Скомпилируй
cd template
gradle assembleDebug
# или
./gradlew assembleDebug
```

### Или создай простой build скрипт:

```bash
# build.sh
#!/bin/bash
mkdir -p app/src/main/java/com/osuhooker
cp OsuHooker.java app/src/main/java/com/osuhooker/
cp AndroidManifest.xml app/src/main/
cat > app/build.gradle << 'EOF'
apply plugin: 'com.android.lib'

android {
    compileSdkVersion 30
    namespace 'com.osuhooker'
}

dependencies {
    compileOnly 'de.robv.android.xposed:api:82'
}
EOF
```

---

## Способ 3: Без компьютера ( directly on phone)

### Вариант А - через Termux:

```bash
# Установи Termux из F-Droid
pkg update
pkg install git gradle

# Клонируй и собери
git clone https://github.com/brouspee/Injector-osu
cd Injector-osu/template
gradle assembleDebug
```

### Вариант Б - через GitHub Actions:

1. Создай fork репозитория
2. Добавь workflow для APK сборки
3. Скачай APK из Artifacts

---

## После установки:

1. **LSPosed Manager** → Модули → **OsuHooker**
2. ✅ Включить
3. 🚫 Только osu!lazer (или ✅ для всех)
4. **Перезагрузка**

---

## Если не работает:

### Ошибка: "Не активируется"
- Убедись что установлен LSPosed (не Xposed!)
- Проверь совместимость с Android версией

### Ошибка: "Краш"
- Открой LSPosed Manager → Логи
- Проверь версию osu!lazer

---

## Альтернатива: Просто использовать LSPosed без модуля

### Если модуль не собирается:

```java
// Просто создай LSPosed модуль вручную в Android Studio:
// File -> New -> New Module -> Android Library
// Добавь зависимость в build.gradle:
//
//     compileOnly 'de.robv.android.xposed:api:82'
//
// Создай класс как OsuHooker.java выше
// Скомпилируй -> APK
```

---

## Быстрая помощь:

1. **Telegram**: Поищи "osu!lazer LSPosed" группы
2. **GitHub**: Проверь готовые модули
3. **4pda**: Найди готовые APK
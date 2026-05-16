@echo off
title System Zglaszania Usterek
color 0A
echo.
echo  ============================================
echo   SYSTEM ZGLASZANIA USTEREK - KIELCE
echo  ============================================
echo.

set JAR_FILE=
for %%f in (target\*.jar) do set JAR_FILE=%%f

if "%JAR_FILE%"=="" (
    color 0E
    echo  [!] Brak skompilowanego pliku JAR.
    echo      Uruchom najpierw: KOMPILUJ_I_URUCHOM.bat
    echo.
    pause
    exit /b 1
)

echo  Uruchamiam: %JAR_FILE%
echo.
echo  --------------------------------------------------
echo   Otworzysz aplikacje pod adresem:
echo   http://localhost:8080/panel.html
echo  --------------------------------------------------
echo.

java -jar "%JAR_FILE%"

pause

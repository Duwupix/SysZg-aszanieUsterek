@echo off
title System Zglaszania Usterek - Kompilacja i uruchamianie
color 0A
echo.
echo  ============================================
echo   SYSTEM ZGLASZANIA USTEREK - KIELCE
echo  ============================================
echo.
echo  [1/2] Kompiluje projekt (moze trwac ~30s)...
echo.

call mvnw.cmd package -DskipTests -q

if %ERRORLEVEL% NEQ 0 (
    color 0C
    echo.
    echo  [BLAD] Kompilacja nie powiodla sie!
    echo  Upewnij sie ze masz zainstalowane Java 17+
    echo.
    pause
    exit /b 1
)

echo  [1/2] Kompilacja OK!
echo.

set JAR_FILE=
for %%f in (target\*.jar) do set JAR_FILE=%%f

if "%JAR_FILE%"=="" (
    color 0C
    echo  [BLAD] Nie znaleziono pliku JAR w folderze target\
    echo.
    pause
    exit /b 1
)

echo  [2/2] Uruchamiam: %JAR_FILE%
echo.
echo  --------------------------------------------------
echo   Otworzysz aplikacje pod adresem:
echo   http://localhost:8080/panel.html
echo  --------------------------------------------------
echo.

java -jar "%JAR_FILE%"

pause

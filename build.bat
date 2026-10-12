@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

set ACTION=%1
if "%ACTION%"=="" set ACTION=build

if "%ACTION%"=="build" (
    call :compile
    echo Build OK -^> bin/
    goto :eof
)
if "%ACTION%"=="test" (
    call :compile
    java -Dfile.encoding=UTF-8 -cp bin com.sportspulse.SelfTest
    goto :eof
)
if "%ACTION%"=="run" (
    call :compile
    java -Dfile.encoding=UTF-8 -cp bin com.sportspulse.Main
    goto :eof
)
if "%ACTION%"=="check" (
    call :compile
    java -Dfile.encoding=UTF-8 -cp bin com.sportspulse.EnvironmentCheck
    goto :eof
)
if "%ACTION%"=="jar" (
    call :compile
    if not exist dist mkdir dist
    if not exist bin\META-INF mkdir bin\META-INF
    (
        echo Manifest-Version: 1.0
        echo Main-Class: com.sportspulse.Main
        echo Implementation-Title: SportsPulse
    ) > bin\META-INF\MANIFEST.MF
    jar cfm dist\SportsPulse.jar bin\META-INF\MANIFEST.MF -C bin .
    copy /y dist\SportsPulse.jar SportsPulse.jar > nul
    echo Built standalone runnable JAR -^> dist\SportsPulse.jar and SportsPulse.jar
    goto :eof
)
if "%ACTION%"=="clean" (
    if exist bin rd /s /q bin
    if exist build rd /s /q build
    if exist dist rd /s /q dist
    if exist sources.txt del sources.txt
    echo Cleaned.
    goto :eof
)

echo Usage: %0 {build^|test^|run^|jar^|check^|clean}
exit /b 1

:compile
if not exist bin mkdir bin
if not exist runtime\logs mkdir runtime\logs
if not exist runtime\reports mkdir runtime\reports
if not exist runtime\benchmarks mkdir runtime\benchmarks
if not exist runtime\data mkdir runtime\data

if exist src (
    dir /s /b src\*.java > sources.txt
) else (
    dir /s /b sportspulse\src\*.java > sources.txt
)

javac -encoding UTF-8 -d bin @sources.txt
if exist sources.txt del sources.txt
exit /b 0

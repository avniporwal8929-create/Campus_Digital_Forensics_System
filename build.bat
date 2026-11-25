@echo off
setlocal
echo Compiling Campus Digital Forensics System (CDF)...
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-27.0.1.0.3" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-27.0.1.0.3"
    )
)
if defined JAVA_HOME (
    set "JAVAC_EXE=%JAVA_HOME%\bin\javac.exe"
) else (
    set "JAVAC_EXE=javac"
)

if not exist "bin" mkdir "bin"

echo Compiling Java source packages (Target Java 21)...
"%JAVAC_EXE%" --release 21 -encoding UTF-8 -d bin -cp "lib\mysql-connector-j-8.3.0.jar;src\main\java" ^
  src/main/java/app/*.java ^
  src/main/java/config/*.java ^
  src/main/java/models/*.java ^
  src/main/java/dao/*.java ^
  src/main/java/services/*.java ^
  src/main/java/ui/*.java ^
  src/main/java/ui/components/*.java ^
  src/main/java/ui/users/*.java ^
  src/main/java/ui/devices/*.java ^
  src/main/java/ui/incidents/*.java ^
  src/main/java/ui/events/*.java ^
  src/main/java/ui/evidence/*.java ^
  src/main/java/ui/custody/*.java ^
  src/main/java/ui/investigators/*.java ^
  src/main/java/ui/reports/*.java ^
  src/main/java/ui/correlation/*.java

echo Copying application resources...
xcopy /s /y /q "src\main\resources\*" "bin\" >nul

echo Build complete! You can run the application with run.bat or .\mvnw.cmd

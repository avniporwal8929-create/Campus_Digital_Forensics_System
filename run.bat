@echo off
setlocal
echo Starting Campus Digital Forensics System (CDF)...
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-27.0.1.0.3" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-27.0.1.0.3"
    )
)
if defined JAVA_HOME (
    set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
    set "JAVA_EXE=java"
)

"%JAVA_EXE%" -cp "bin;lib\mysql-connector-j-8.3.0.jar;src\main\resources" app.Main

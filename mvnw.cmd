@echo off
setlocal
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-27.0.1.0.3" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-27.0.1.0.3"
    )
)
"%~dp0tools\apache-maven-3.9.6\bin\mvn.cmd" -Dmaven.repo.local="%~dp0.m2\repository" %*

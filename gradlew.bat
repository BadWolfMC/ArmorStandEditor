@echo off
setlocal
rem ArmorStandEditor wrapper launcher using the published Gradle 9.8.2 Wrapper JAR.
set "APP_HOME=%~dp0"
set "WRAPPER_JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar"
set "WRAPPER_URL=https://raw.githubusercontent.com/gradle/gradle/v9.8.2/gradle/wrapper/gradle-wrapper.jar"
set "WRAPPER_SHA256=3b8a25775a69158b5ad2b1d17a80a88dc7b40a352a73eb27d06c612a7ce68e98"

if not exist "%WRAPPER_JAR%" (
  echo Fetching published Gradle 9.8.2 Wrapper JAR ^(checksum-verified^)...
  powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Invoke-WebRequest -Uri $env:WRAPPER_URL -OutFile $env:WRAPPER_JAR"
  if errorlevel 1 exit /b 1
)
powershell -NoProfile -NonInteractive -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $h=(Get-FileHash -Algorithm SHA256 -LiteralPath $env:WRAPPER_JAR).Hash.ToLowerInvariant(); if($h -ne $env:WRAPPER_SHA256) { Write-Error ('Wrapper checksum mismatch: ' + $h); exit 1 }"
if errorlevel 1 exit /b 1

if defined JAVA_HOME (
  set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
) else (
  set "JAVA_EXE=java.exe"
)
"%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
exit /b %ERRORLEVEL%

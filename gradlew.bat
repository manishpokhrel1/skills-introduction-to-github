@echo off
setlocal
set GRADLE_VERSION=8.3
set WRAPPER_DIR=%USERPROFILE%\.gradle\wrapper\dists\gradle-%GRADLE_VERSION%-bin
set GRADLE_BIN=%WRAPPER_DIR%\gradle-%GRADLE_VERSION%\bin\gradle.bat
if exist "%GRADLE_BIN%" (
  "%GRADLE_BIN%" %*
  exit /b %ERRORLEVEL%
)
set ZIP_URL=https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip
set TMPZIP=%TEMP%\gradle-%GRADLE_VERSION%.zip
powershell -Command "if (-Not (Test-Path '%WRAPPER_DIR%')) { New-Item -ItemType Directory -Path '%WRAPPER_DIR%' | Out-Null }"
powershell -Command "Write-Host 'Downloading %ZIP_URL% to %TMPZIP%'; (New-Object Net.WebClient).DownloadFile('%ZIP_URL%','%TMPZIP%')"
powershell -Command "Expand-Archive -Force -Path '%TMPZIP%' -DestinationPath '%WRAPPER_DIR%'"
if exist "%GRADLE_BIN%" (
  "%GRADLE_BIN%" %*
) else (
  echo Failed to prepare Gradle runtime.
  exit /b 1
)
endlocal
@echo off
setlocal
set HERE=%~dp0
if defined JAVA_HOME (
  set JAVACMD=%JAVA_HOME%\bin\java.exe
) else (
  set JAVACMD=java
)
"%JAVACMD%" -classpath "%HERE%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

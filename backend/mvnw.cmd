@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------
@echo off
setlocal enabledelayedexpansion

set MAVEN_PROJECTBASEDIR=%~dp0
if not "%MAVEN_BASEDIR%"=="" set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%

set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

@REM Find java.exe
if defined JAVA_HOME goto OkJHome
for %%i in (java.exe) do set "JAVA_EXE=%%~$PATH:i"
goto checkJavaExe
:OkJHome
set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"
:checkJavaExe
if exist "%JAVA_EXE%" goto chkMavenWrapper
echo Error: JAVA_HOME is not set and java is not in PATH. >&2
exit /b 1

:chkMavenWrapper
if exist %WRAPPER_JAR% goto runMavenWrapper

echo Downloading Maven Wrapper JAR...
"%JAVA_EXE%" -classpath "" ^
  "-Dmaven.wrapper.jar=%WRAPPER_JAR%" ^
  "-Dmaven.wrapper.distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip" ^
  org.apache.maven.wrapper.MavenWrapperDownloader %WRAPPER_URL% %WRAPPER_JAR% 2>NUL

if not exist %WRAPPER_JAR% (
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object System.Net.WebClient).DownloadFile('%WRAPPER_URL%', %WRAPPER_JAR%)"
)

:runMavenWrapper
"%JAVA_EXE%" %JVM_CONFIG_MAVEN_PROPS% %MAVEN_OPTS% ^
  -classpath %WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  %WRAPPER_LAUNCHER% %MAVEN_CONFIG% %*

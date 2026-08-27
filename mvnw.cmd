@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script (FIXED)
@REM ----------------------------------------------------------------------------
@echo off
SET "MAVEN_PROJECTBASEDIR=%~dp0"
IF NOT "%MAVEN_PROJECTBASEDIR:~-1%"=="\" SET "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR%\"

@SET MAVEN_WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
@SET MAVEN_WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

title %0
IF "%MAVEN_BATCH_ECHO%" == "on"  echo %MAVEN_BATCH_ECHO%

IF "%HOME%" == "" (SET "HOME=%HOMEDRIVE%%HOMEPATH%")

SET JAVA_EXECUTABLE=java
IF DEFINED JAVA_HOME (
  IF EXIST "%JAVA_HOME%\bin\java.exe" SET JAVA_EXECUTABLE="%JAVA_HOME%\bin\java.exe"
)

@REM Detect Maven distribution URL from wrapper properties
SET DISTRIBUTION_URL=
FOR /F "usebackq tokens=1,* delims==" %%a IN (%MAVEN_WRAPPER_PROPERTIES%) DO (
  IF "%%a"=="distributionUrl" SET DISTRIBUTION_URL=%%b
)

SET "EXEC_DIR=%~dp0"
IF "%EXEC_DIR:~-1%"=="\" SET "EXEC_DIR=%EXEC_DIR:~0,-1%"

%JAVA_EXECUTABLE% "-Dmaven.multiModuleProjectDirectory=%EXEC_DIR%" -classpath "%EXEC_DIR%\.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*

@echo off

SET "PARENT_FOLDER_NAME=%~p0"
SET "PARENT_FOLDER_NAME=%PARENT_FOLDER_NAME:\= %"
FOR %%a IN (%PARENT_FOLDER_NAME%) DO SET PARENT_FOLDER_NAME=%%a

SET SERVICE_PATH=%cd%
SET PARENT_PATH=%SERVICE_PATH%/../

SET MODULE_NAME=%PARENT_FOLDER_NAME:-impl=%
SET EXECUTE_JAR_NAME=%PARENT_FOLDER_NAME%.jar

SET EXECUTE_JAR_PATH=%SERVICE_PATH%/%EXECUTE_JAR_NAME%
SET DEFAULT_LOGGER_ROOT=/logs/dubbo-jars

TITLE %MODULE_NAME%

java -jar -Xdebug -Xnoagent -Xmx1024m -Xms1024m -XX:NewRatio=1 -XX:SurvivorRatio=8 -Dconfig.path=file:"%SERVICE_PATH%"/";"file:"%PARENT_PATH%"/common.properties -Dlogger.root=%DEFAULT_LOGGER_ROOT% -Dlogger.module=%MODULE_NAME% "%EXECUTE_JAR_PATH%" 

sleep 2
pause
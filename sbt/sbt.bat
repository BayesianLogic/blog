set SCRIPT_DIR=%~dp0
java -XX:+CMSClassUnloadingEnabled -jar "%SCRIPT_DIR%sbt-launch.jar" %*

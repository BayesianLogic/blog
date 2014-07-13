@echo off
set JAVA_OPTS=
if "%1"=="" set JAVA_OPTS=-Xmx4096M
set BLOG_HOME=%~dp0..
echo Starting Interative Shell for BLOG
if exist "%BLOG_HOME%\bin\iblog.scala" (
  set CPATH="%BLOG_HOME%\lib\java-cup-11b.jar;%BLOG_HOME%\lib\*"
  scala -cp %CPATH% -i "%BLOG_HOME%\bin\iblog.scala" -J%JAVA_OPTS% -J%*
) else (
	set CPATH="%BLOG_HOME%\blog\lib\java-cup-11b.jar;%BLOG_HOME%\blog\lib\*"
  scala -cp %CPATH% -i "%BLOG_HOME%\blog\src\main\scala\iblog.scala" -J%JAVA_OPTS% -J%*  
)

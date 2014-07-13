@echo off
if "%1"=="" set JAVA_OPTS=-Xmx4096M
set BLOG_HOME=%~dp0..
echo Starting Interative Shell for BLOG
if exist "%BLOG_HOME%\bin\iblog.scala" (
  set CPATH="%BLOG_HOME%\lib\java-cup-11b.jar;%BLOG_HOME%\lib\*"
  scala -cp %CPATH% -i "%BLOG_HOME%\bin\iblog.scala" -J%JAVA_OPTS% %*
) else (
  set BLOG_DEVELOP_HOME=%~dp0
	if NOT exist "%BLOG_DEVELOP_HOME%target\universal\stage\" (sbt\sbt.bat stage)
  set CPATH="%BLOG_DEVELOP_HOME%target\universal\stage\lib\java-cup-11b.jar;%BLOG_DEVELOP_HOME%target\universal\stage\lib\*"
  scala -cp %CPATH% -i "%BLOG_DEVELOP_HOME%src\main\scala\iblog.scala" -J%JAVA_OPTS% %*
)

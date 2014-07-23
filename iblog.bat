@echo off
set Param1=%1
if "%Param1:~0,6%"=="-J-Xmx" (
  set JAVA_OPTS=%Param1:~2%
  set Param1=
)
set BLOG_HOME=%~dp0..
echo Starting Interative Shell for BLOG
if exist "%BLOG_HOME%\bin\iblog.scala" (
  set CPATH="%BLOG_HOME%\lib\java-cup-11b.jar;%BLOG_HOME%\lib\*"
  scala -cp %CPATH% -i "%BLOG_HOME%\bin\iblog.scala" -J%JAVA_OPTS% %Param1% %2 %3 %4 %5 %6 %7 %8 %9
) else (
  set BLOG_DEVELOP_HOME=%~dp0
	if NOT exist "%BLOG_DEVELOP_HOME%target\universal\stage\" (sbt\sbt.bat stage)
  set CPATH="%BLOG_DEVELOP_HOME%target\universal\stage\lib\java-cup-11b.jar;%BLOG_DEVELOP_HOME%target\universal\stage\lib\*"
  scala -cp %CPATH% -i "%BLOG_DEVELOP_HOME%src\main\scala\iblog.scala" -J%JAVA_OPTS% %Param1% %2 %3 %4 %5 %6 %7 %8 %9
)

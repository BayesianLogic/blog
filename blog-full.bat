@REM blog launcher script
@REM
@REM Environment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM BLOG_config.txt found in the BLOG_HOME.
@setlocal enabledelayedexpansion

@echo off
if "%BLOG_HOME%"=="" set "BLOG_HOME=%~dp0\\.."
set ERROR_CODE=0

set "APP_LIB_DIR=%BLOG_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (%cmdcmdline%) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%BLOG_HOME%\BLOG_config.txt"
set CFG_OPTS=
if exist %CFG_FILE% (
  FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%CFG_FILE%") DO (
    set DO_NOT_REUSE_ME=%%i
    rem ZOMG (Part #2) WE use !! here to delay the expansion of
    rem CFG_OPTS, otherwise it remains "" for this loop.
    set CFG_OPTS=!CFG_OPTS! !DO_NOT_REUSE_ME!
  )
)

rem We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==Java set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running blog.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)


rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "%_JAVA_OPTS%"=="" set _JAVA_OPTS=%CFG_OPTS%

rem We keep in _JAVA_PARAMS all -J-prefixed and -D-prefixed arguments
rem "-J" is stripped, "-D" is left as is, and everything is appended to JAVA_OPTS
set _JAVA_PARAMS=

:param_beforeloop
if [%1]==[] goto param_afterloop
set _TEST_PARAM=%~1

rem ignore arguments that do not start with '-'
if not "%_TEST_PARAM:~0,1%"=="-" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: help
if "%_TEST_PARAM%"=="--help" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: sample size
if "%_TEST_PARAM%"=="-n" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: sample size
if "%_TEST_PARAM%"=="--num_samples" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: randomize
if "%_TEST_PARAM%"=="-r" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: randomize
if "%_TEST_PARAM%"=="--randomize" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: engine
if "%_TEST_PARAM%"=="-e" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: engine
if "%_TEST_PARAM%"=="--engine" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: sampling algorithm
if "%_TEST_PARAM%"=="-s" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: sampling algorithm
if "%_TEST_PARAM%"=="--sampler" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: burn-in
if "%_TEST_PARAM%"=="-b" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: sampling algorithm
if "%_TEST_PARAM%"=="--burn_in" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: proposer
if "%_TEST_PARAM%"=="-p" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: sampling algorithm
if "%_TEST_PARAM%"=="--proposer" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: output
if "%_TEST_PARAM%"=="-o" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: sampling algorithm
if "%_TEST_PARAM%"=="--output" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: verbose
if "%_TEST_PARAM%"=="-v" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: verbose
if "%_TEST_PARAM%"=="--verbose" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: interval
if "%_TEST_PARAM%"=="--interval" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: generate
if "%_TEST_PARAM%"=="--generate" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: package
if "%_TEST_PARAM%"=="--package" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: debug
if "%_TEST_PARAM%"=="--debug" (
  shift
  goto param_beforeloop
)

rem ignore BLOG options: -P: not really a BLOG option, but java option

set _TEST_PARAM=%~1
if "%_TEST_PARAM:~0,2%"=="-J" (
  rem strip -J prefix
  set _TEST_PARAM=%_TEST_PARAM:~2%
)

if "%_TEST_PARAM:~0,2%"=="-D" (
  rem test if this was double-quoted property "-Dprop=42"
  for /F "delims== tokens=1-2" %%G in ("%_TEST_PARAM%") DO (
    if not "%%G" == "%_TEST_PARAM%" (
      rem double quoted: "-Dprop=42" -> -Dprop="42"
      set _JAVA_PARAMS=%%G="%%H"
    ) else if [%2] neq [] (
      rem it was a normal property: -Dprop=42 or -Drop="42"
      set _JAVA_PARAMS=%_TEST_PARAM%=%2
      shift
    )
  )
) else (
  rem a JVM property, we just append it
  set _JAVA_PARAMS=%_TEST_PARAM%
)

:param_loop
shift

if [%1]==[] goto param_afterloop
set _TEST_PARAM=%~1

rem ignore arguments that do not start with '-'
if not "%_TEST_PARAM:~0,1%"=="-" goto param_loop

set _TEST_PARAM=%~1
if "%_TEST_PARAM:~0,2%"=="-J" (
  rem strip -J prefix
  set _TEST_PARAM=%_TEST_PARAM:~2%
)

if "%_TEST_PARAM:~0,2%"=="-D" (
  rem test if this was double-quoted property "-Dprop=42"
  for /F "delims== tokens=1-2" %%G in ("%_TEST_PARAM%") DO (
    if not "%%G" == "%_TEST_PARAM%" (
      rem double quoted: "-Dprop=42" -> -Dprop="42"
      set _JAVA_PARAMS=%_JAVA_PARAMS% %%G="%%H"
    ) else if [%2] neq [] (
      rem it was a normal property: -Dprop=42 or -Drop="42"
      set _JAVA_PARAMS=%_JAVA_PARAMS% %_TEST_PARAM%=%2
      shift
    )
  )
) else (
  rem a JVM property, we just append it
  set _JAVA_PARAMS=%_JAVA_PARAMS% %_TEST_PARAM%
)
goto param_loop
:param_afterloop

set _JAVA_OPTS=%_JAVA_OPTS% %_JAVA_PARAMS%
:run
 
set "APP_CLASSPATH=%APP_LIB_DIR%\blog.blog-0.9.jar;%APP_LIB_DIR%\java-cup-11b.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.10.4.jar;%APP_LIB_DIR%\gov.nist.math.jama-1.0.3.jar;%APP_LIB_DIR%\com.google.code.gson.gson-2.2.4.jar;%APP_LIB_DIR%\de.jflex.jflex-1.6.0.jar;%APP_LIB_DIR%\org.apache.ant.ant-1.7.0.jar;%APP_LIB_DIR%\org.apache.ant.ant-launcher-1.7.0.jar"
set "APP_MAIN_CLASS=blog.Main"

rem Call the application and pass all arguments unchanged.
"%_JAVACMD%" %_JAVA_OPTS% %BLOG_OPTS% -cp "%APP_CLASSPATH%" %APP_MAIN_CLASS% %*
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end

@endlocal

exit /B %ERROR_CODE%
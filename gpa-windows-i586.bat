@echo off

set key=HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit
set JAVA_VERSION=
set JAVA_HOME=
for /f "tokens=3* skip=2" %%a in ('reg query "%key%" /v CurrentVersion') do set JAVA_VERSION=%%a
for /f "tokens=2* skip=2" %%a in ('reg query "%key%\%JAVA_VERSION%" /v JavaHome') do set JAVA_HOME=%%b

if not exist "%JAVA_HOME%\bin\java.exe" goto JAVAMISSING
"%JAVA_HOME%\bin\java" -Xmx1024m -Djava.library.path=lib/windows-i586 -jar build/dist/gpa-@version.jar -3D %*
goto END

:JAVAMISSING
echo The required version of Java has not been installed.
echo Go to http://java.sun.com to install the 32bit Java JDK.
pause

:END
echo off

set PRG=%~nx0
set PROGDIR=%~dp0
set HOT_CP=%PROGDIR%\lib;%PROGDIR%\lib\*;%CD%\.work\resources;%CD%\www;%CD%\shows;%CD%\lib\*;%CD%\sql
java %HOT_OPTIONS% -Dpython.cachedir.skip=true -Dhotdir=%PROGDIR% -Dhot.app.dir=%CD% -cp %HOT_CP% be.solidx.hot.cli.Hot %*

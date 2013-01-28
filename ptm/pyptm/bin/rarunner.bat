@ECHO OFF

IF "%1" == "" GOTO noarg

SET PTM_HOME=%~dp0..

ECHO PTM_HOME: %PTM_HOME%

SET PYTHONPATH=%PTM_HOME%\lib;%PTM_HOME%\ra

ECHO PYTHONPATH: %PYTHONPATH%

python %PTM_HOME%\lib\ptm\RARunner.py %1

GOTO end

:noarg

ECHO No argument given. Please specify an adapter class.

:end
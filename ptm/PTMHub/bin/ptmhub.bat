@ECHO OFF

SET PTM_HOME=%~dp0..

ECHO PTM_HOME: %PTM_HOME%

SET PYTHONPATH=%PTM_HOME%\lib;%PTM_HOME%\ra

ECHO PYTHONPATH: %PYTHONPATH%

python.exe %PTM_HOME%\lib\ptmhub\hubserver.py
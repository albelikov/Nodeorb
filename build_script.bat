@echo off
echo Starting build...
call gradle :oms-service:bootJar > build_log.txt 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Build failed with error code %ERRORLEVEL% >> build_log.txt
) else (
    echo Build successful >> build_log.txt
)
echo Done.

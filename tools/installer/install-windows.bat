@echo off
title VeloTV Quick Installer
where python >nul 2>nul
if %errorlevel%==0 (
  python "%~dp0velotv_installer.py"
) else (
  py -3 "%~dp0velotv_installer.py"
)
if %errorlevel% neq 0 pause

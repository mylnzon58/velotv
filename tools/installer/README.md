# VeloTV Quick Installer (GUI en 3 clics)

Asistente gráfico sin dependencias externas (`Python 3` + `tkinter`) para **Windows, Linux y macOS**.

## Antes de abrirlo (3 pasos en tu TV)
1. Activá **Modo Desarrollador** (`Ajustes → Acerca de → Compilación` 7 veces) y activá **Depuración USB / ADB**.
2. **Reiniciá el televisor** para que abra el puerto ADB por Wi-Fi (`5555`).
3. Conectá la PC y el TV a la **misma red Wi-Fi**.

## Cómo ejecutarlo
- **Windows:** doble clic en `install-windows.bat` (o `python velotv_installer.py`).
- **Linux:** `./install-linux.sh` (o `python3 velotv_installer.py`).
- **macOS:** doble clic en `install-macos.command`.

## Qué hace automáticamente
- Escanea tu Wi-Fi y detecta el televisor en puerto `5555`.
- Descarga ADB oficial de Google si no está instalado en tu PC.
- Descarga `VeloTV-v1.0.0-universal.apk` desde GitHub Releases y **verifica su hash SHA-256**.
- Instala VeloTV, aplica optimizaciones seguras y reversibles (`--user 0`, animaciones `0.5x`) y permite restaurar todo con un clic.

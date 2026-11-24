<p align="center">
  <img src="assets/velotv-hero-banner.png" alt="VeloTV — Launcher nativo para Android TV" width="100%" />
</p>

<p align="center">
  <a href="https://mylnzon58.github.io/velotv/"><img src="assets/badges/sitio-web-github-pages-df9d3e-sq-39ff86.svg" alt="Sitio Web oficial" /></a>
  <a href="README.en.md"><img src="assets/badges/english-documentation-c9c1b7-sq-779e03.svg" alt="English Documentation" /></a>
  <img src="assets/badges/apk-release-2-1-mb-ede8e1-sq-187dda.svg" alt="APK 2.1 MB" />
  <img src="assets/badges/ram-en-vivo-23-7-mb-7cb68a-sq-a1c11e.svg" alt="RAM 23.7 MB con fondo HD" />
  <img src="assets/badges/telemetr-a-cero-df9d3e-sq-7ee4d0.svg" alt="Cero telemetría" />
  <img src="assets/badges/android-tv-7-0-15-c9c1b7-sq-875df0.svg" alt="Android TV 7 a 15" />
  <a href="LICENSE"><img src="assets/badges/licencia-mit-df9d3e-sq-080ac5.svg" alt="Licencia MIT" /></a>
</p>

# VeloTV

Launcher nativo en Kotlin para Android TV y Google TV diseñado para correr con fluidez de 60 fps en televisores de 1 a 2 GB de RAM, con fondos de pantalla de bancos abiertos en rotación automática, física de enfoque Material 3, brillo ambiental dinámico, cero publicidad y cero telemetría.

Probado y medido en hardware real (**Skyworth G6 / placa `9K02B` / MediaTek MT5891 / ARMv7 32-bit / Mali-T860 / 2 GB RAM / Android 8.0**) y compatible con cualquier Android TV o Google TV (API 24 a 35).

---

## Medición real en hardware (Skyworth MT5891 · 2 GB RAM)

Valores obtenidos con `adb shell dumpsys meminfo` en el mismo televisor físico:

| Métrica | Google TV Launcher + Recs | Projectivy Launcher 4.74 | **VeloTV 1.1 (Cinema Lounge)** |
|---|---:|---:|---:|
| **Tamaño del APK** | ~82 MB | 11.0 MB | **2.1 MB** *(4 pesos Outfit OFL + 8 fondos HD WebP)* |
| **Memoria RAM (PSS)** | 92.6 MB | 36.8 MB | **23.7 MB** *(con fondo HD + brillo ambiental · ~4× menos que Google)* |
| **Fila inteligente offline ("Para ahora")** | Nube (rastreo) | No | **Sí, 100% local (recencia + horario, 0 telemetría)** |
| **Multi-Launcher Hub (1 clic)** | No | No | **Sí (VeloTV + Android TV original + Projectivy)** |
| **Atajos físicos del control (`1–9` + colores)** | No | Parcial | **Sí (Speed Dial `1–9`, Verde/Amarillo/Azul/Rojo, Play Resume)** |
| **Modo Cine Nocturno + Auto-Turbo RAM** | No | No | **Sí (filtro cálido 22h–7h + auto-limpieza `<320 MB`)** |
| **Fondos libres rotativos** | No | Parcial (Pro pago) | **Sí, 100% gratis (Wikimedia + Unsplash + Local)** |
| **Publicidad / Filas patrocinadas** | Sí | No | **No** |
| **Funciones de pago / Telemetría** | Sí / Sí | Freemium / Sí | **Ninguna · MIT · Cero telemetría** |

---

## ¿Qué distingue a VeloTV de cualquier otro launcher? (USPs reales)

1. **Estética Cinema Lounge con 70% menos texto:** barra superior flotante basada en íconos vectoriales, tipografía suave **Outfit**, tarjetas con deslizamiento de borde a borde, paneles flotantes redondeados y píldora de feedback nativa (sin textos técnicos ni nombres de paquete en el living).
2. **Fila inteligente "Para ahora" 100% offline:** aprende localmente en tu TV qué apps usás a la mañana, tarde o noche y las acerca automáticamente sin enviar un solo byte a la nube.
3. **Multi-Launcher Hub (convivencia real en 1 clic):** abrís **Android TV Home original** o **Projectivy** con un clic (o botón **Rojo**) mientras el botón `HOME` siempre regresa a **VeloTV**.
4. **Control Remoto Pro (cero menús):** teclas **`1`–`9`** lanzan tus Favoritos #1–#9; **Verde** libera RAM; **Amarillo** cambia el fondo; **Azul** activa Modo Arte/Reloj OLED; **Rojo** abre Launchers y HDMI; **Play/Pause** reanuda la última app usada.
5. **Auto-Turbo RAM + Modo Cine Nocturno:** filtro cálido automático de `22:00` a `07:00` (o 1 clic en la luna) y recuperación automática de RAM si baja de `320 MB` en TVs de 1–2 GB.
6. **23.7 MB de RAM PSS y 2.1 MB APK con 100% open source (MIT):** incluye calibración 4K/HDR, reloj anti-quemado OLED, control parental PIN + horario y catálogo regional sin muros de pago.

---

## Capturas reales del televisor

| Inicio Cinema Lounge (Outfit + barra de íconos) | Multi-Launcher Hub en 1 clic (Botón Rojo) |
|---|---|
| ![Inicio VeloTV](assets/screenshots/02-home-darkroom.png) | ![Multi-Launcher Hub](assets/screenshots/10-multi-launcher-hub.png) |

| Turbo RAM con píldora flotante (Botón Verde) | Ajustes flotantes + pestaña Pro / Atajos |
|---|---|
| ![Turbo RAM](assets/screenshots/11-turbo-ram-pill.png) | ![Ajustes VeloTV](assets/screenshots/04-settings-drawer.png) |

| Galería de fondos libres con vista previa en vivo | Galería ambiente y reloj OLED anti-quemado |
|---|---|
| ![Galería de fondos](assets/screenshots/08-wallpapers-gallery.png) | ![Modo ambiente OLED](assets/screenshots/09-ambient-mode.png) |

| Patrones de calibración 4K / HDR | Panel de Sistema y Desarrollador |
|---|---|
| ![Calibración 4K](assets/screenshots/05-calibration-4k.png) | ![Sistema y Desarrollador](assets/screenshots/03-system-developer.png) |

---

## Funcionalidades

- **Motor de fondos libres en baja memoria (`RGB_565` · 1.84 MB RAM):** cambia automáticamente cada 5 minutos por defecto (configurable: `1m / 5m / 15m / 30m / al volver / manual`) combinando **8 paisajes libres integrados** para arranque instantáneo en `0 ms` + bancos abiertos en vivo (**Wikimedia Commons Featured Pictures** y **Unsplash / Picsum**), galería visual con vista previa en tiempo real al mover el foco, velo cinematográfico regulable, movimiento suave tipo cámara y **modo 100% sin conexión**.
- **Fluidez real de 60 fps en 1–2 GB RAM:** Kotlin + Views nativas + pool compartido de `RecyclerView`, ruta rápida síncrona en caché LRU de 6 MB, scroll ergonómico para control remoto, elevación GPU (`translationZ`) + curva Material 3 y brillo ambiental extraído del ícono enfocado.
- **Seis paletas calibradas sin pagos:** Cuarto oscuro Ámbar, Cuarto oscuro Rojo, Noche, Estudio (claro para salas luminosas), Mineral Bosque y Grafito Monocromo; tres radios de esquina (`0 dp`, `10 dp`, `16 dp`) y tres densidades de tarjeta.
- **Gestión total de aplicaciones y filas:** detecta apps Android TV (con sus banners oficiales de actividad) y apps móviles instaladas, búsqueda instantánea, creación y eliminación de categorías personalizadas, reordenamiento y ocultamiento de filas, renombrado de apps y entradas HDMI, e inicio automático al encender.
- **Entradas físicas TV y HDMI (`TvInputManager`):** detecta `HDMI 1/2/3`, sintonizador digital/analógico y `AV`, con estado en vivo, renombrado y bloqueo por PIN.
- **Sistema, Limpieza rápida y Desarrollador:** botón **Limpiar** de 1 clic en la barra superior (libera apps inactivas en segundo plano con `ActivityManager`, vacía cachés internas y abre el caché global de Almacenamiento nativo al mantener `OK`); lectura en vivo de ADB, modo desarrollador, orígenes desconocidos, escalas de animación, SoC, kernel, WebView, IP local y RAM; más accesos directos verificados dinámicamente a las actividades nativas de Android TV y menús de hardware del fabricante.
- **Herramientas de pantalla integradas:** 10 patrones nativos de calibración 4K/HDR (grises PLUGE, barras SMPTE, overscan 5%/10%, uniformidad y píxeles muertos, nitidez a 3 m) y **Galería ambiente + reloj OLED** con desplazamiento anti-quemado cada 60 s y cambio instantáneo a negro puro OLED con `OK`.
- **Control parental con horario:** PIN de 4 dígitos almacenado con SHA-256 + salt (comparación en tiempo constante) para proteger Ajustes, Desarrollador, apps o entradas HDMI, con horario configurable.
- **Global desde el inicio:** español como idioma predeterminado + 10 idiomas seleccionables al instante (`es-AR`, `es`, `en`, `pt-BR`, `fr`, `de`, `it`, `ja`, `ko`, `zh-CN`) y catálogo regional (AR, LATAM, USA, EU, ASIA) filtrado por `SDK_INT`.
- **Seguridad OWASP y transparencia:** cero analítica, cero permisos peligrosos, `allowBackup="false"`, `usesCleartextTraffic="false"`, y allowlist estricto HTTPS verificado por pruebas unitarias ([SECURITY.md](SECURITY.md) y [CREDITS.md](CREDITS.md)).

---

## Instalación (3 formas simples)

### Opción 1 — Con agente IA: Antigravity u OpenCode *(recomendado)*
La forma más simple y guiada para conectar, auditar, instalar y optimizar tu TV en minutos:
1. En tu televisor: `Ajustes → Acerca de → Compilación` (7 clics) → activar **Depuración USB / ADB**.
2. **Reiniciá el televisor** (abre ADB Wi-Fi en puerto `5555`) y dejalo en el **mismo Wi-Fi** que tu PC.
3. Abrí **Antigravity** (u **OpenCode**), pegá este prompt y aceptá **Permitir siempre** en la pantalla del TV:

```text
Conéctate a mi Android TV en mi red Wi-Fi (escanea puerto 5555), descarga la última release de https://github.com/mylnzon58/velotv verificando SHA-256, instala VeloTV y aplica optimización segura y reversible (--user 0, animaciones 0.5x) sin tocar DRM ni Play Store. Guíame paso a paso solo con lo necesario.
```

### Opción 2 — Instalador gráfico en 3 clics (Windows / Linux / macOS)
Sin usar consola: ejecutá [tools/installer/velotv_installer.py](tools/installer/velotv_installer.py) (o doble clic en `install-windows.bat` / `install-linux.sh` / `install-macos.command`). Escanea tu Wi-Fi, descarga ADB oficial si falta, verifica el SHA-256, instala VeloTV, optimiza o restaura todo en 1 clic.

### Opción 3 — Manual (USB, LocalSend o ADB)
Descarga `release/VeloTV-v1.1.0-universal.apk` (verifica su hash en `release/SHA256SUMS.txt`) e instálalo por USB, LocalSend o ADB:

```bash
adb connect <IP_DEL_TV>:5555
adb install -r release/VeloTV-v1.1.0-universal.apk
```

Toolkit opcional de optimización 100% reversible (`tools/tv-toolkit/`):

```bash
./tools/tv-toolkit/optimize_tv.sh <IP_DEL_TV>:5555 release/VeloTV-v1.1.0-universal.apk
./tools/tv-toolkit/restore_tv.sh <IP_DEL_TV>:5555
./tools/tv-toolkit/harden_close_adb.sh <IP_DEL_TV>:5555
```

---

## Compilación desde código fuente

Requiere JDK 17 y Android SDK 35:

```bash
./gradlew testDebugUnitTest
./gradlew assembleRelease
```

---

## Autor, licencia y aportes

Creado y mantenido por **[Juan José Monzón](https://github.com/mylnzon58)** (`@mylnzon58`) en Argentina bajo licencia **[MIT](LICENSE)** (ver también [NOTICE](NOTICE)).

Puedes usar, estudiar, modificar y distribuir este proyecto libremente, siempre **conservando los créditos y el aviso de copyright original** en todas las copias o derivados.

| Región | Plataforma | Enlace |
|---|---|---|
| Argentina (ARS, monto libre) | Mercado Pago | [link.mercadopago.com.ar/mylnzon58](https://link.mercadopago.com.ar/mylnzon58) |
| Argentina / LATAM | Tecito | [tecito.app/juanjoclassic](https://tecito.app/juanjoclassic) |
| Internacional (US / EU / Asia) | GitHub Sponsors | [github.com/sponsors/mylnzon58](https://github.com/sponsors/mylnzon58) |
| Sponsors mensuales | Patreon | [patreon.com/JuanjoClassic](https://www.patreon.com/JuanjoClassic) |

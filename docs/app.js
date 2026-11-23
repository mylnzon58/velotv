(() => {
  const I18N = {
    es: {
      nav_perf: "Rendimiento",
      nav_features: "Funciones",
      nav_shots: "Capturas",
      nav_install: "Instalación",
      nav_privacy: "Privacidad",
      nav_support: "Apoyar",
      cta_apk_short: "Descargar APK",
      hero_eyebrow: "ANDROID TV · GOOGLE TV · API 24 – 35",
      hero_title: "Fluidez real a 60 fps en televisores de 1 y 2 GB de RAM.",
      hero_lead: "Launcher nativo en Kotlin sin publicidad, sin funciones bloqueadas y sin telemetría. Incluye fondos de bancos abiertos en rotación automática, limpieza de RAM en 1 clic, entradas HDMI, calibración 4K y accesos nativos de desarrollador.",
      cta_download: "Descargar VeloTV v1.1.0 (2.1 MB)",
      cta_github: "Ver código en GitHub",
      m_apk: "APK universal firmado",
      m_ram: "RAM PSS con fondo HD",
      m_langs: "Idiomas integrados",
      m_ads: "Anuncios o rastreo",
      tab_home: "Inicio",
      tab_wp: "Fondos libres",
      tab_ambient: "Modo ambiente",
      tab_sys: "Desarrollador",
      tab_settings: "Ajustes",
      tab_catalog: "Catálogo",
      tab_cal: "Calibración 4K",
      cap_home: "Inicio con fondo rotativo libre, limpieza rápida y entradas HDMI",
      cap_wp: "Galería visual de fondos abiertos con vista previa en vivo",
      cap_ambient: "Galería ambiente rotativa y reloj OLED con protección anti-quemado",
      cap_sys: "Diagnóstico real y accesos directos a opciones nativas de desarrollador",
      cap_settings: "Seis paletas calibradas, filas personalizadas, control parental y 10 idiomas",
      cap_catalog: "Catálogo regional (AR, LATAM, USA, EU, ASIA) filtrado por versión de Android",
      cap_cal: "Diez patrones nativos de calibración 4K y HDR en Canvas",
      bench_eyebrow: "MEDICIÓN EN HARDWARE REAL",
      bench_title: "Tres veces menos memoria que el launcher de fábrica.",
      bench_sub: "Medido con adb shell dumpsys meminfo sobre el mismo televisor físico: Skyworth G6 (placa 9K02B, MediaTek MT5891 32-bit, Mali-T860, 2 GB RAM, Android TV 8.0).",
      bar_velo_hd: "VeloTV 1.0 (con fondo HD + brillo ambiental)",
      bar_velo_solid: "VeloTV 1.0 (modo superficie sólida)",
      bench_note: "Todos los fondos se decodifican en RGB_565 a 1280×720 (1.84 MB en RAM) con liberación inmediata de la capa saliente y pool compartido de RecyclerView.",
      th_metric: "Métrica",
      row_apk: "Tamaño del APK",
      row_ram: "Memoria RAM (PSS)",
      row_wp: "Fondos libres rotativos",
      row_paywall: "Funciones de pago",
      row_telemetry: "Telemetría / anuncios",
      row_oss: "Código abierto",
      val_no: "No",
      val_yes: "Sí",
      val_paid: "Pro pago",
      val_yes_free: "Sí, gratis",
      val_none: "Ninguna",
      val_zero: "Cero",
      feat_eyebrow: "INGENIERÍA Y FUNCIONALIDADES",
      feat_title: "Todo lo esencial de un launcher profesional, sin código inflado.",
      f1_t: "Fondos de bancos abiertos en rotación automática",
      f1_p: "8 paisajes HD integrados para arranque en 0 ms más bancos en vivo de Wikimedia Commons (CC) y Unsplash/Picsum. Vista previa en tiempo real, selección manual, velo cinematográfico regulable y modo 100% sin conexión.",
      f2_t: "Botón rápido Limpiar (RAM y caché)",
      f2_p: "Acceso en 1 clic junto al contador de memoria para cerrar apps inactivas en segundo plano con ActivityManager sin afectar TV ni sesiones, más acceso directo al vaciado global de caché nativo.",
      f3_t: "Entradas físicas TV y HDMI",
      f3_p: "Detección nativa con TvInputManager para HDMI 1/2/3, sintonizador digital/analógico y AV, con estado conectado/en espera, renombrado libre, bloqueo por PIN e inicio automático al encender.",
      f4_t: "Centro de Sistema y Desarrollador",
      f4_p: "Lectura en vivo de ADB, orígenes desconocidos, escalas de animación, SoC, kernel, WebView, IP y RAM, más accesos verificados dinámicamente a Opciones de Desarrollador y menús OEM.",
      f5_t: "Calibración 4K/HDR y Galería Ambiente OLED",
      f5_p: "10 patrones nativos en Canvas (PLUGE, SMPTE, overscan, uniformidad y nitidez a 3 m) y modo ambiente rotativo con reloj gigante y desplazamiento anti-quemado cada 60 segundos.",
      f6_t: "Control parental, 6 paletas y 10 idiomas",
      f6_p: "PIN de 4 dígitos protegido con SHA-256 y salt en tiempo constante, restricción horaria para juegos, categorías personalizadas, respaldo local en JSON y catálogo regional (AR, LATAM, USA, EU, ASIA).",
      shots_eyebrow: "CAPTURAS REALES EN TELEVISOR",
      shots_title: "Sin maquetas artificiales: capturado directo del HDMI.",
      g1_t: "Galería de fondos abiertos",
      g1_m: "Wikimedia + Unsplash + Local",
      g2_t: "Galería ambiente y reloj OLED",
      g2_m: "Anti-quemado · OK Negro puro",
      g3_t: "Sistema y opciones de desarrollador",
      g3_m: "Diagnóstico en vivo y accesos nativos",
      g4_t: "Ajustes completos por pestañas",
      g4_m: "6 paletas · Filas · Parental · Idiomas",
      inst_eyebrow: "INSTALACIÓN Y VERIFICACIÓN",
      inst_title: "Tres formas rápidas de instalarlo, sin complicaciones.",
      inst_sub: "Compatible con Android TV 7.0 a 15 (ARMv7, ARM64 y x86). Elegí agente IA guiado, instalador gráfico en 3 clics o instalación manual por USB/ADB.",
      m1_tag: "RECOMENDADO · AUTOMÁTICO",
      m1_t: "Con Antigravity u OpenCode",
      m1_s1: "En el TV: activá Modo Desarrollador y Depuración ADB.",
      m1_s2: "Reiniciá el TV y dejalo en el mismo Wi-Fi que tu PC.",
      m1_s3: "Pegá el prompt en Antigravity (u OpenCode) y tocá Permitir siempre en el TV.",
      m1_copy: "Copiar prompt para Antigravity",
      m2_tag: "SIN CONSOLA · 3 CLICS",
      m2_t: "Instalador gráfico (PC)",
      m2_s1: "Escanea el Wi-Fi y detecta el televisor automáticamente.",
      m2_s2: "Descarga ADB oficial si falta, baja VeloTV y verifica su SHA-256.",
      m2_s3: "Instala, optimiza o restaura fábrica en Windows, Linux y macOS.",
      m2_dl: "Descargar instalador (.py)",
      m2_guide: "Ver accesos Win/Mac/Linux",
      m3_tag: "DIRECTO · MANUAL",
      m3_t: "USB, LocalSend o ADB",
      m3_s1: "Descargá el APK universal firmado (2.1 MB).",
      m3_s2: "Envialo al televisor con LocalSend, pendrive o ADB.",
      m3_s3: "Verificá el hash SHA-256 antes de instalar.",
      c1_t: "Prompt directo para Antigravity / OpenCode",
      ai_prompt_text: "Conéctate a mi Android TV en mi red Wi-Fi (escanea puerto 5555), descarga la última release de https://github.com/mylnzon58/velotv verificando SHA-256, instala VeloTV y aplica optimización segura y reversible (--user 0, animaciones 0.5x) sin tocar DRM ni Play Store. Guíame paso a paso solo con lo necesario.",
      c2_t: "Verificar integridad SHA-256",
      c3_t: "ADB manual y toolkit reversible",
      c3_note: "Desactiva bloatware únicamente en --user 0, ajusta escalas de animación a 0.5x y compila apps clave con perfil ART. Nunca toca DRM, Play Store ni sintonizador.",
      copy: "Copiar",
      copied: "Copiado",
      priv_eyebrow: "SEGURIDAD OWASP · PRIVACIDAD VERIFICABLE",
      priv_title: "Cero analítica. Allowlist HTTPS auditable. Modo 100% offline.",
      priv_sub: "VeloTV no solicita micrófono, cámara, ubicación ni almacenamiento externo. El acceso de red está restringido por código y pruebas unitarias exclusivamente a bancos de fotos abiertos por HTTPS, y puede desactivarse por completo desde el televisor.",
      sup_eyebrow: "PROYECTO INDEPENDIENTE · LICENCIA MIT",
      sup_title: "100% gratis para todos. Mantenido por la comunidad.",
      sup_sub: "Creado en Argentina por Juan José Monzón (@mylnzon58). Si VeloTV le devolvió fluidez a tu televisor, podés apoyar su desarrollo de forma voluntaria.",
      s_mp: "Aporte directo en pesos argentinos (monto libre)",
      s_tecito: "Invitá un aporte rápido desde Latinoamérica",
      s_gh: "Apoyo internacional directo sin intermediarios",
      s_pat: "Acompañamiento mensual para nuevas funciones",
      footer_copy: "© 2026 Juan José Monzón (@mylnzon58) · Licencia MIT — Libre de usar y modificar conservando los créditos originales."
    },
    en: {
      nav_perf: "Benchmark",
      nav_features: "Features",
      nav_shots: "Screenshots",
      nav_install: "Install",
      nav_privacy: "Privacy",
      nav_support: "Support",
      cta_apk_short: "Download APK",
      hero_eyebrow: "ANDROID TV · GOOGLE TV · API 24 – 35",
      hero_title: "Steady 60 fps responsiveness on 1–2 GB RAM televisions.",
      hero_lead: "Native Kotlin launcher with zero ads, zero paywalls, and zero telemetry. Includes rotating free-license wallpapers, one-click RAM clean, physical HDMI inputs, 4K calibration patterns, and native developer shortcuts.",
      cta_download: "Download VeloTV v1.1.0 (2.1 MB)",
      cta_github: "View source on GitHub",
      m_apk: "Signed universal APK",
      m_ram: "Live RAM PSS (HD photo)",
      m_langs: "Built-in languages",
      m_ads: "Ads or tracking SDKs",
      tab_home: "Home",
      tab_wp: "Wallpapers",
      tab_ambient: "Ambient Mode",
      tab_sys: "Developer",
      tab_settings: "Settings",
      tab_catalog: "Catalog",
      tab_cal: "4K Calibration",
      cap_home: "Home canvas with rotating open photography, quick clean, and HDMI inputs",
      cap_wp: "Visual gallery of free-license photo banks with instant live preview",
      cap_ambient: "Rotating ambient gallery and OLED clock with burn-in pixel shift",
      cap_sys: "Live hardware diagnostics and verified native Android TV developer shortcuts",
      cap_settings: "Six calibrated palettes, custom rows, parental PIN, and 10 languages",
      cap_catalog: "SDK-aware regional app catalog for AR, LATAM, USA, Europe, and Asia",
      cap_cal: "Ten native Canvas 4K/HDR display calibration patterns",
      bench_eyebrow: "REAL HARDWARE BENCHMARK",
      bench_title: "Three times lighter than the stock Google TV launcher.",
      bench_sub: "Measured via adb shell dumpsys meminfo on the same physical television: Skyworth G6 (9K02B board, MediaTek MT5891 32-bit, Mali-T860, 2 GB RAM, Android TV 8.0).",
      bar_velo_hd: "VeloTV 1.0 (live HD wallpaper + ambient glow)",
      bar_velo_solid: "VeloTV 1.0 (solid surface mode)",
      bench_note: "Every wallpaper is decoded in RGB_565 at 1280×720 (1.84 MB RAM) with immediate outgoing bitmap release and a shared RecyclerView pool.",
      th_metric: "Metric",
      row_apk: "Release APK size",
      row_ram: "Live RAM (PSS)",
      row_wp: "Rotating open wallpapers",
      row_paywall: "Paywalled features",
      row_telemetry: "Telemetry / ads",
      row_oss: "Open source",
      val_no: "No",
      val_yes: "Yes",
      val_paid: "Pro paywall",
      val_yes_free: "Yes, free",
      val_none: "None",
      val_zero: "Zero",
      feat_eyebrow: "ENGINEERING & CAPABILITIES",
      feat_title: "Every pro launcher capability, without bloat.",
      f1_t: "Auto-rotating open-license wallpaper banks",
      f1_p: "8 bundled HD landscapes for 0 ms cold start plus live Wikimedia Commons (CC) and Unsplash/Picsum feeds. Live preview on focus, manual pinning, adjustable cinema scrim, and 100% offline mode.",
      f2_t: "One-click Quick Clean (RAM & cache)",
      f2_p: "Top-bar button right beside the live RAM readout to stop idle background apps via ActivityManager without disrupting live TV or logins, plus direct access to native Storage Cache.",
      f3_t: "Physical TV & HDMI inputs",
      f3_p: "Native TvInputManager discovery for HDMI 1/2/3, digital/analog tuner, and AV with live connected indicators, custom renaming, PIN locking, and boot auto-launch.",
      f4_t: "System & Native Developer Hub",
      f4_p: "Live readout of ADB state, unknown sources, animation scales, SoC, kernel, WebView, local IP, and RAM, plus dynamically verified shortcuts to Android TV Developer Options and OEM menus.",
      f5_t: "4K/HDR Calibration & OLED Ambient Gallery",
      f5_p: "10 native Canvas calibration patterns (PLUGE, SMPTE, overscan, uniformity, and 3 m sharpness) plus a rotating ambient screensaver with 60-second anti-burn-in pixel shift.",
      f6_t: "Parental controls, 6 palettes & 10 languages",
      f6_p: "4-digit PIN hashed with salted SHA-256 (constant-time verify), game schedule lock, custom categories, local JSON backup/restore, and regional catalog (AR, LATAM, USA, EU, ASIA).",
      shots_eyebrow: "REAL TELEVISION CAPTURES",
      shots_title: "No synthetic mockups: captured straight from physical hardware.",
      g1_t: "Open wallpaper gallery",
      g1_m: "Wikimedia + Unsplash + Bundled",
      g2_t: "Ambient gallery & OLED clock",
      g2_m: "Anti-burn-in · OK Pure OLED black",
      g3_t: "System & developer hub",
      g3_m: "Live diagnostics & native shortcuts",
      g4_t: "Complete tabbed settings",
      g4_m: "6 palettes · Rows · Parental · Languages",
      inst_eyebrow: "INSTALL & VERIFY",
      inst_title: "Three fast ways to install, zero friction.",
      inst_sub: "Compatible with Android TV 7.0 through 15 (ARMv7, ARM64, x86). Use an AI coding agent, the 3-click desktop installer, or manual USB/ADB.",
      m1_tag: "RECOMMENDED · AUTOMATIC",
      m1_t: "With Antigravity or OpenCode",
      m1_s1: "On TV: enable Developer Mode and ADB Debugging.",
      m1_s2: "Reboot the TV and keep it on the same Wi-Fi as your PC.",
      m1_s3: "Paste the prompt into Antigravity (or OpenCode) and accept Always allow on TV.",
      m1_copy: "Copy prompt for Antigravity",
      m2_tag: "NO TERMINAL · 3 CLICKS",
      m2_t: "Desktop GUI Installer",
      m2_s1: "Scans local Wi-Fi and detects your Android TV automatically.",
      m2_s2: "Downloads official Google ADB if missing and verifies APK SHA-256.",
      m2_s3: "Installs, optimizes, or restores stock apps on Windows, Linux, and macOS.",
      m2_dl: "Download installer (.py)",
      m2_guide: "Win/Mac/Linux launchers",
      m3_tag: "DIRECT · MANUAL",
      m3_t: "USB, LocalSend, or ADB",
      m3_s1: "Download the signed universal APK (2.1 MB).",
      m3_s2: "Send it to your TV using LocalSend, USB drive, or ADB.",
      m3_s3: "Verify the SHA-256 checksum before installing.",
      c1_t: "Ready-to-paste prompt for Antigravity / OpenCode",
      ai_prompt_text: "Connect to my Android TV on my local Wi-Fi (scan port 5555), download the latest release from https://github.com/mylnzon58/velotv verifying its SHA-256, install VeloTV, and apply safe reversible optimization (--user 0, 0.5x animations) without touching DRM or Play Store. Guide me concisely only when TV confirmation is needed.",
      c2_t: "Verify SHA-256 checksum",
      c3_t: "Manual ADB & reversible toolkit",
      c3_note: "Disables bloatware strictly under --user 0, sets 0.5x animation scales, and runs ART speed-profile compilation. Never touches DRM, Play Store, or live TV tuners.",
      copy: "Copy",
      copied: "Copied",
      priv_eyebrow: "OWASP SECURITY · VERIFIABLE PRIVACY",
      priv_title: "Zero analytics. Unit-tested HTTPS allowlist. 100% offline mode.",
      priv_sub: "VeloTV never requests microphone, camera, location, or external storage permissions. Network traffic is strictly limited to verified HTTPS open photo banks and can be turned off completely in settings.",
      sup_eyebrow: "INDEPENDENT OPEN SOURCE · MIT LICENSE",
      sup_title: "100% free for everyone. Community supported.",
      sup_sub: "Built in Argentina by Juan José Monzón (@mylnzon58). If VeloTV made your television fast again, consider supporting ongoing maintenance.",
      s_mp: "Direct ARS contribution in Argentina (open amount)",
      s_tecito: "Quick voluntary support across Latin America",
      s_gh: "Direct international sponsorship on GitHub",
      s_pat: "Monthly sponsorship for upcoming releases",
      footer_copy: "© 2026 Juan José Monzón (@mylnzon58) · MIT License — Free to use and modify while preserving original author attribution."
    }
  };

  let currentLang = "es";
  const stageImg = document.getElementById("stageImg");
  const stageCaption = document.getElementById("stageCaption");
  const langBtn = document.getElementById("langToggle");

  function applyLang(lang) {
    currentLang = lang;
    document.documentElement.lang = lang;
    const dict = I18N[lang];
    document.querySelectorAll("[data-i18n]").forEach((el) => {
      const key = el.getAttribute("data-i18n");
      if (dict[key]) el.textContent = dict[key];
    });
    const activeTab = document.querySelector('.screen-tab[aria-selected="true"]');
    if (activeTab && stageCaption) {
      const capKey = activeTab.getAttribute("data-cap-key");
      if (dict[capKey]) stageCaption.textContent = dict[capKey];
    }
    langBtn.textContent = lang === "es" ? "EN" : "ES";
    const aiCopyBtn = document.getElementById("copyAiPromptBtn");
    if (aiCopyBtn && dict.ai_prompt_text) {
      aiCopyBtn.setAttribute("data-copy", dict.ai_prompt_text);
    }
    const aiPreCopy = document.querySelector('#aiPromptCode')?.closest('.code-panel')?.querySelector('.copy-btn');
    if (aiPreCopy && dict.ai_prompt_text) {
      aiPreCopy.setAttribute("data-copy", dict.ai_prompt_text);
    }
  }

  langBtn.addEventListener("click", () => {
    applyLang(currentLang === "es" ? "en" : "es");
  });

  // Live theme preview matching the exact 6 palettes inside VeloTV
  document.querySelectorAll("[data-set-theme]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const theme = btn.getAttribute("data-set-theme");
      document.documentElement.setAttribute("data-theme", theme);
      document.querySelectorAll("[data-set-theme]").forEach((b) => {
        b.setAttribute("aria-pressed", String(b === btn));
      });
    });
  });

  // Interactive TV stage switcher
  document.querySelectorAll(".screen-tab").forEach((tab) => {
    tab.addEventListener("click", () => {
      const src = tab.getAttribute("data-img");
      const capKey = tab.getAttribute("data-cap-key");
      document.querySelectorAll(".screen-tab").forEach((t) => {
        t.setAttribute("aria-selected", String(t === tab));
      });
      stageImg.classList.add("fading");
      setTimeout(() => {
        stageImg.src = src;
        stageCaption.textContent = I18N[currentLang][capKey] || "";
        stageImg.classList.remove("fading");
      }, 130);
    });
  });

  // Copy-to-clipboard for ADB & SHA-256 snippets
  document.querySelectorAll(".copy-btn").forEach((btn) => {
    btn.addEventListener("click", async () => {
      const text = btn.getAttribute("data-copy");
      const original = btn.textContent;
      try {
        await navigator.clipboard.writeText(text);
        btn.textContent = I18N[currentLang].copied;
        setTimeout(() => {
          btn.textContent = original;
        }, 1600);
      } catch (_) {
        /* ignore clipboard restrictions */
      }
    });
  });

  // Clean full-screen screenshot lightbox on gallery click
  const lightbox = document.getElementById("lightbox");
  const lightboxImg = document.getElementById("lightboxImg");
  const lightboxCap = document.getElementById("lightboxCap");
  const lightboxClose = document.getElementById("lightboxClose");

  function closeLightbox() {
    if (!lightbox) return;
    lightbox.classList.remove("open");
    lightbox.setAttribute("aria-hidden", "true");
  }

  document.querySelectorAll(".shot-media img").forEach((img) => {
    img.addEventListener("click", () => {
      if (!lightbox || !lightboxImg) return;
      lightboxImg.src = img.src;
      lightboxImg.alt = img.alt;
      const cap = img.closest(".shot-card")?.querySelector(".shot-title")?.textContent || img.alt;
      if (lightboxCap) lightboxCap.textContent = cap;
      lightbox.classList.add("open");
      lightbox.setAttribute("aria-hidden", "false");
    });
  });

  if (lightboxClose) lightboxClose.addEventListener("click", closeLightbox);
  if (lightbox) {
    lightbox.addEventListener("click", (e) => {
      if (e.target === lightbox) closeLightbox();
    });
  }
  document.addEventListener("keydown", (e) => {
    if (e.key === "Escape") closeLightbox();
  });
})();


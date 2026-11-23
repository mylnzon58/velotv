#!/usr/bin/env python3
"""
VeloTV Quick Installer (Windows / Linux / macOS)
Zero external dependencies (Python 3 stdlib + tkinter).

- Auto-discovers Android TV devices on local Wi-Fi (TCP port 5555).
- Uses system ADB or downloads official Google platform-tools over HTTPS.
- Downloads latest VeloTV release APK and verifies SHA-256 checksum.
- Installs VeloTV, applies optional reversible --user 0 optimizations,
  and supports one-click factory restore or ADB hardening.

Copyright (c) 2026 Juan Jose Monzon (@mylnzon58) - MIT License
"""

from concurrent.futures import ThreadPoolExecutor
import hashlib
import ipaddress
import os
from pathlib import Path
import platform
import shutil
import socket
import subprocess
import sys
import tempfile
import threading
import tkinter as tk
from tkinter import messagebox, ttk
import urllib.request
import zipfile

REPO_SLUG = "mylnzon58/velotv"
APK_URL = f"https://github.com/{REPO_SLUG}/releases/latest/download/VeloTV-v1.0.0-universal.apk"
SHA_URL = f"https://github.com/{REPO_SLUG}/releases/latest/download/SHA256SUMS.txt"

ALLOWED_DOWNLOAD_HOSTS = {
    "github.com",
    "objects.githubusercontent.com",
    "release-assets.githubusercontent.com",
    "dl.google.com",
}

SAFE_BLOAT_PACKAGES = [
    "com.google.android.videos",
    "com.google.android.music",
    "com.google.android.play.games",
    "com.google.android.feedback",
    "com.google.android.tv.bugreportsender",
    "com.google.android.tv.tutorials",
    "com.google.android.marvin.talkback",
    "com.google.android.tts",
    "com.android.printspooler",
    "com.android.providers.calendar",
    "com.android.providers.contacts",
    "com.google.android.syncadapters.calendar",
    "com.google.android.syncadapters.contacts",
]

# Hallmark Darkroom palette (exact match with VeloTV app)
C_PAPER = "#12100E"
C_PAPER2 = "#1B1815"
C_PAPER3 = "#26221E"
C_INK = "#EDE8E1"
C_INK2 = "#C9C1B7"
C_MUTED = "#8F867C"
C_RULE = "#332E29"
C_ACCENT = "#DF9D3E"
C_ACCENT_INK = "#12100E"
C_OK = "#7CB68A"

STRINGS = {
    "es": {
        "title": "VeloTV — Instalador rápido",
        "subtitle": "Instalación guiada por Wi-Fi para Android TV / Google TV · MIT (@mylnzon58)",
        "prep": "Antes de empezar: 1) Activá Modo Desarrollador y Depuración ADB en el TV · 2) Reiniciá el TV · 3) Conectalo al mismo Wi-Fi.",
        "tv_ip": "IP del televisor (puerto 5555)",
        "scan": "Buscar TV en Wi-Fi",
        "connect": "Conectar TV",
        "opt_install": "Instalar VeloTV v1.0.0 (con verificación SHA-256)",
        "opt_optimize": "Optimizar rendimiento (animaciones 0.5x + desactivar bloatware reversible)",
        "opt_home": "Fijar VeloTV como inicio predeterminado (Home)",
        "opt_close_adb": "Cerrar depuración ADB al finalizar (recomendado)",
        "run": "Instalar y optimizar ahora",
        "restore": "Restaurar apps de fábrica (--user 0)",
        "ready": "Listo. Tocá 'Buscar TV en Wi-Fi' o escribí la IP del televisor.",
        "scanning": "Buscando televisores con ADB (puerto 5555) en tu Wi-Fi…",
        "scan_none": "No se detectó ningún TV en puerto 5555. Verificá ADB + reinicio del TV o escribí la IP.",
        "scan_found": "TV detectado en {ip}. Tocá 'Conectar TV'.",
        "need_ip": "Ingresá una IP válida (ej. 192.168.1.50).",
        "adb_prompt": "Conectando a {target}… Si aparece un cartel en el televisor, elegí 'Permitir siempre'.",
        "connected": "Conectado: {model} (Android {ver}, {ram} MB RAM libre).",
        "done": "Listo. VeloTV ya está instalado y corriendo en tu televisor.",
        "restored": "Apps de fábrica restauradas y animaciones en 1.0x.",
    },
    "en": {
        "title": "VeloTV — Quick Installer",
        "subtitle": "Guided Wi-Fi setup for Android TV / Google TV · MIT (@mylnzon58)",
        "prep": "Before starting: 1) Enable Developer Mode & ADB Debugging on TV · 2) Reboot TV · 3) Connect to the same Wi-Fi.",
        "tv_ip": "TV IP address (port 5555)",
        "scan": "Scan Wi-Fi for TV",
        "connect": "Connect TV",
        "opt_install": "Install VeloTV v1.0.0 (SHA-256 verified)",
        "opt_optimize": "Optimize speed (0.5x animations + reversible bloatware disable)",
        "opt_home": "Set VeloTV as default Home launcher",
        "opt_close_adb": "Disable ADB debugging when finished (recommended)",
        "run": "Install & Optimize Now",
        "restore": "Restore Stock Apps (--user 0)",
        "ready": "Ready. Click 'Scan Wi-Fi for TV' or enter your TV IP.",
        "scanning": "Scanning your Wi-Fi subnet for Android TV (port 5555)…",
        "scan_none": "No TV found on port 5555. Enable ADB, reboot the TV, or type the IP manually.",
        "scan_found": "TV found at {ip}. Click 'Connect TV'.",
        "need_ip": "Please enter a valid IP address (e.g. 192.168.1.50).",
        "adb_prompt": "Connecting to {target}… Accept 'Always allow' on your TV screen if prompted.",
        "connected": "Connected: {model} (Android {ver}, {ram} MB free RAM).",
        "done": "Done. VeloTV is installed and running on your television.",
        "restored": "Stock apps restored and animation scales reset to 1.0x.",
    },
}


def is_valid_ipv4(text: str) -> bool:
    try:
        ipaddress.IPv4Address(text.strip())
        return True
    except ValueError:
        return False


def local_subnet_prefix() -> str | None:
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.connect(("8.8.8.8", 80))
            ip = s.getsockname()[0]
        parts = ip.split(".")
        if len(parts) == 4:
            return ".".join(parts[:3])
    except OSError:
        return None
    return None


def scan_adb_devices(timeout: float = 0.35) -> list[str]:
    prefix = local_subnet_prefix()
    if not prefix:
        return []
    hosts = [f"{prefix}.{i}" for i in range(1, 255)]
    found: list[str] = []

    def probe(host: str) -> str | None:
        try:
            with socket.create_connection((host, 5555), timeout=timeout):
                return host
        except OSError:
            return None

    with ThreadPoolExecutor(max_workers=64) as pool:
        for res in pool.map(probe, hosts):
            if res:
                found.append(res)
    return found


def safe_download(url: str, dest: Path, log_cb) -> Path:
    from urllib.parse import urlparse

    class SafeRedirect(urllib.request.HTTPRedirectHandler):
        def redirect_request(self, req, fp, code, msg, headers, newurl):
            parsed = urlparse(newurl)
            if parsed.scheme != "https" or parsed.hostname not in ALLOWED_DOWNLOAD_HOSTS:
                raise ValueError(f"Blocked untrusted redirect host: {parsed.hostname}")
            return super().redirect_request(req, fp, code, msg, headers, newurl)

    parsed = urlparse(url)
    if parsed.scheme != "https" or parsed.hostname not in ALLOWED_DOWNLOAD_HOSTS:
        raise ValueError(f"Blocked untrusted URL: {url}")

    opener = urllib.request.build_opener(SafeRedirect)
    req = urllib.request.Request(url, headers={"User-Agent": "VeloTV-Installer/1.0"})
    log_cb(f"GET {parsed.hostname}{parsed.path}")
    with opener.open(req, timeout=25) as resp, open(dest, "wb") as out:
        shutil.copyfileobj(resp, out)
    return dest


def ensure_adb(log_cb) -> str:
    found = shutil.which("adb")
    if found:
        return found

    cache_root = Path.home() / ".velotv-installer"
    exe = "adb.exe" if platform.system() == "Windows" else "adb"
    local_adb = cache_root / "platform-tools" / exe
    if local_adb.exists():
        return str(local_adb)

    sys_name = platform.system().lower()
    zip_name = {
        "windows": "platform-tools-latest-windows.zip",
        "darwin": "platform-tools-latest-darwin.zip",
    }.get(sys_name, "platform-tools-latest-linux.zip")

    cache_root.mkdir(parents=True, exist_ok=True)
    zip_path = cache_root / zip_name
    log_cb("Descargando Google Android Platform-Tools (ADB oficial)…")
    safe_download(f"https://dl.google.com/android/repository/{zip_name}", zip_path, log_cb)
    with zipfile.ZipFile(zip_path, "r") as zf:
        zf.extractall(cache_root)
    zip_path.unlink(missing_ok=True)
    if platform.system() != "Windows":
        local_adb.chmod(0o755)
    return str(local_adb)


def run_adb(adb: str, target: str | None, args: list[str], timeout: int = 35) -> tuple[int, str]:
    cmd = [adb] + (["-s", target] if target else []) + args
    proc = subprocess.run(cmd, capture_output=True, text=True, timeout=timeout)
    out = (proc.stdout + "\n" + proc.stderr).strip()
    return proc.returncode, out


def resolve_verified_apk(log_cb) -> Path:
    script_dir = Path(__file__).resolve().parent
    local_candidate = script_dir.parent.parent / "release" / "VeloTV-v1.0.0-universal.apk"
    local_sha = script_dir.parent.parent / "release" / "SHA256SUMS.txt"

    work_dir = Path(tempfile.gettempdir()) / "velotv_installer"
    work_dir.mkdir(parents=True, exist_ok=True)

    if local_candidate.exists() and local_sha.exists():
        apk_path = local_candidate
        expected = local_sha.read_text(encoding="utf-8").strip().split()[0].lower()
    else:
        apk_path = work_dir / "VeloTV-v1.0.0-universal.apk"
        sha_path = work_dir / "SHA256SUMS.txt"
        safe_download(SHA_URL, sha_path, log_cb)
        safe_download(APK_URL, apk_path, log_cb)
        expected = sha_path.read_text(encoding="utf-8").strip().split()[0].lower()

    actual = hashlib.sha256(apk_path.read_bytes()).hexdigest().lower()
    if actual != expected:
        raise RuntimeError(f"SHA-256 mismatch: expected {expected[:16]}…, got {actual[:16]}…")
    log_cb(f"SHA-256 OK ({actual[:16]}…)")
    return apk_path


class VeloInstallerApp:
    def __init__(self, root: tk.Tk) -> None:
        self.root = root
        self.lang = "es"
        self.adb_path: str | None = None
        self.busy = False

        root.title(self.t("title"))
        root.configure(bg=C_PAPER)
        root.geometry("680x560")
        root.minsize(640, 520)

        self.ip_var = tk.StringVar()
        self.v_install = tk.BooleanVar(value=True)
        self.v_optimize = tk.BooleanVar(value=True)
        self.v_home = tk.BooleanVar(value=False)
        self.v_close_adb = tk.BooleanVar(value=False)

        self._build_ui()
        self._log(self.t("ready"))

    def t(self, key: str, **kw) -> str:
        return STRINGS[self.lang][key].format(**kw)

    def _build_ui(self) -> None:
        pad = {"padx": 22}

        top = tk.Frame(self.root, bg=C_PAPER)
        top.pack(fill="x", pady=(18, 8), **pad)
        tk.Label(top, text="VELOTV", fg=C_ACCENT, bg=C_PAPER, font=("Segoe UI", 18, "bold")).pack(side="left")
        self.lbl_sub = tk.Label(top, text=self.t("subtitle"), fg=C_MUTED, bg=C_PAPER, font=("Segoe UI", 9))
        self.lbl_sub.pack(side="left", padx=(12, 0))
        self.btn_lang = tk.Button(
            top, text="EN", command=self._toggle_lang,
            bg=C_PAPER2, fg=C_INK, activebackground=C_PAPER3, activeforeground=C_INK,
            relief="flat", padx=10, pady=3, cursor="hand2"
        )
        self.btn_lang.pack(side="right")

        tk.Frame(self.root, bg=C_RULE, height=1).pack(fill="x", **pad)

        self.lbl_prep = tk.Label(
            self.root, text=self.t("prep"), fg=C_INK2, bg=C_PAPER2,
            font=("Segoe UI", 9), justify="left", wraplength=610, padx=14, pady=10, anchor="w"
        )
        self.lbl_prep.pack(fill="x", pady=(12, 10), **pad)

        ip_frame = tk.Frame(self.root, bg=C_PAPER)
        ip_frame.pack(fill="x", pady=(2, 10), **pad)
        self.lbl_ip = tk.Label(ip_frame, text=self.t("tv_ip"), fg=C_MUTED, bg=C_PAPER, font=("Segoe UI", 9))
        self.lbl_ip.pack(anchor="w")

        row = tk.Frame(ip_frame, bg=C_PAPER)
        row.pack(fill="x", pady=(4, 0))
        self.entry_ip = tk.Entry(
            row, textvariable=self.ip_var, bg=C_PAPER2, fg=C_INK, insertbackground=C_ACCENT,
            relief="flat", font=("Consolas", 11), highlightthickness=1, highlightbackground=C_RULE, highlightcolor=C_ACCENT
        )
        self.entry_ip.pack(side="left", fill="x", expand=True, ipady=6)

        self.btn_scan = tk.Button(
            row, text=self.t("scan"), command=self._on_scan,
            bg=C_PAPER3, fg=C_INK, activebackground=C_RULE, activeforeground=C_INK,
            relief="flat", padx=12, pady=6, cursor="hand2"
        )
        self.btn_scan.pack(side="left", padx=(8, 0))

        self.btn_connect = tk.Button(
            row, text=self.t("connect"), command=self._on_connect,
            bg=C_PAPER3, fg=C_ACCENT, activebackground=C_RULE, activeforeground=C_ACCENT,
            relief="flat", padx=12, pady=6, cursor="hand2"
        )
        self.btn_connect.pack(side="left", padx=(8, 0))

        opts = tk.Frame(self.root, bg=C_PAPER2, padx=14, pady=10)
        opts.pack(fill="x", pady=(2, 10), **pad)

        self.checks = []
        for var, key in [
            (self.v_install, "opt_install"),
            (self.v_optimize, "opt_optimize"),
            (self.v_home, "opt_home"),
            (self.v_close_adb, "opt_close_adb"),
        ]:
            cb = tk.Checkbutton(
                opts, text=self.t(key), variable=var,
                bg=C_PAPER2, fg=C_INK, selectcolor=C_PAPER,
                activebackground=C_PAPER2, activeforeground=C_INK, anchor="w", font=("Segoe UI", 9)
            )
            cb.pack(fill="x", pady=2)
            self.checks.append((cb, key))

        actions = tk.Frame(self.root, bg=C_PAPER)
        actions.pack(fill="x", pady=(4, 10), **pad)

        self.btn_run = tk.Button(
            actions, text=self.t("run"), command=self._on_install,
            bg=C_ACCENT, fg=C_ACCENT_INK, activebackground="#E8AC52", activeforeground=C_ACCENT_INK,
            relief="flat", font=("Segoe UI", 10, "bold"), padx=18, pady=9, cursor="hand2"
        )
        self.btn_run.pack(side="left")

        self.btn_restore = tk.Button(
            actions, text=self.t("restore"), command=self._on_restore,
            bg=C_PAPER2, fg=C_MUTED, activebackground=C_PAPER3, activeforeground=C_INK,
            relief="flat", font=("Segoe UI", 9), padx=12, pady=9, cursor="hand2"
        )
        self.btn_restore.pack(side="right")

        self.log_box = tk.Text(
            self.root, height=10, bg=C_PAPER2, fg=C_INK2, relief="flat",
            font=("Consolas", 9), padx=12, pady=10, state="disabled",
            highlightthickness=1, highlightbackground=C_RULE
        )
        self.log_box.pack(fill="both", expand=True, pady=(0, 18), **pad)

    def _toggle_lang(self) -> None:
        self.lang = "en" if self.lang == "es" else "es"
        self.root.title(self.t("title"))
        self.btn_lang.config(text="ES" if self.lang == "en" else "EN")
        self.lbl_sub.config(text=self.t("subtitle"))
        self.lbl_prep.config(text=self.t("prep"))
        self.lbl_ip.config(text=self.t("tv_ip"))
        self.btn_scan.config(text=self.t("scan"))
        self.btn_connect.config(text=self.t("connect"))
        self.btn_run.config(text=self.t("run"))
        self.btn_restore.config(text=self.t("restore"))
        for cb, key in self.checks:
            cb.config(text=self.t(key))

    def _log(self, msg: str) -> None:
        def append():
            self.log_box.config(state="normal")
            self.log_box.insert("end", f"> {msg}\n")
            self.log_box.see("end")
            self.log_box.config(state="disabled")
        self.root.after(0, append)

    def _run_bg(self, fn) -> None:
        if self.busy:
            return
        self.busy = True
        for b in (self.btn_scan, self.btn_connect, self.btn_run, self.btn_restore):
            b.config(state="disabled")

        def worker():
            try:
                fn()
            except Exception as exc:
                self._log(f"ERROR: {exc}")
            finally:
                def unlock():
                    self.busy = False
                    for b in (self.btn_scan, self.btn_connect, self.btn_run, self.btn_restore):
                        b.config(state="normal")
                self.root.after(0, unlock)

        threading.Thread(target=worker, daemon=True).start()

    def _target(self) -> str:
        ip = self.ip_var.get().strip().split(":")[0]
        if not is_valid_ipv4(ip):
            raise ValueError(self.t("need_ip"))
        return f"{ip}:5555"

    def _on_scan(self) -> None:
        def task():
            self._log(self.t("scanning"))
            hosts = scan_adb_devices()
            if not hosts:
                self._log(self.t("scan_none"))
            else:
                self.root.after(0, lambda: self.ip_var.set(hosts[0]))
                self._log(self.t("scan_found", ip=", ".join(hosts)))
        self._run_bg(task)

    def _on_connect(self) -> None:
        def task():
            target = self._target()
            adb = ensure_adb(self._log)
            self.adb_path = adb
            self._log(self.t("adb_prompt", target=target))
            _, out = run_adb(adb, None, ["connect", target], timeout=12)
            self._log(out)
            rc, model = run_adb(adb, target, ["shell", "getprop", "ro.product.model"])
            if rc != 0:
                raise RuntimeError("No se pudo autorizar el TV. Revisá el aviso en la pantalla del televisor.")
            _, ver = run_adb(adb, target, ["shell", "getprop", "ro.build.version.release"])
            _, mem = run_adb(adb, target, ["shell", "dumpsys meminfo | grep 'Free RAM:' | head -n 1"])
            ram_mb = mem.split(":")[1].split("K")[0].strip().replace(",", "") if ":" in mem else "?"
            ram_label = str(int(ram_mb) // 1024) if ram_mb.isdigit() else "?"
            self._log(self.t("connected", model=model.strip(), ver=ver.strip(), ram=ram_label))
        self._run_bg(task)

    def _on_install(self) -> None:
        def task():
            target = self._target()
            adb = self.adb_path or ensure_adb(self._log)
            self.adb_path = adb
            run_adb(adb, None, ["connect", target], timeout=12)

            if self.v_install.get():
                apk = resolve_verified_apk(self._log)
                self._log(f"Instalando {apk.name}…")
                rc, out = run_adb(adb, target, ["install", "-r", str(apk)], timeout=90)
                if rc != 0:
                    raise RuntimeError(f"Install failed: {out}")
                self._log("VeloTV instalado.")

            if self.v_optimize.get():
                self._log("Aplicando optimizaciones seguras (--user 0, animaciones 0.5x)…")
                for scale in ("window_animation_scale", "transition_animation_scale", "animator_duration_scale"):
                    run_adb(adb, target, ["shell", "settings", "put", "global", scale, "0.5"])
                for pkg in SAFE_BLOAT_PACKAGES:
                    run_adb(adb, target, ["shell", "pm", "disable-user", "--user", "0", pkg])
                run_adb(adb, target, ["shell", "pm", "trim-caches", "1024M"])
                run_adb(adb, target, ["shell", "cmd", "package", "compile", "-m", "speed-profile", "io.github.mylnzon58.velotv"], timeout=45)

            if self.v_home.get():
                run_adb(adb, target, ["shell", "cmd", "package", "set-home-activity", "io.github.mylnzon58.velotv/.ui.home.HomeActivity"])
                self._log("VeloTV fijado como Home predeterminado.")

            run_adb(adb, target, ["shell", "am", "start", "-n", "io.github.mylnzon58.velotv/.ui.home.HomeActivity"])

            if self.v_close_adb.get():
                run_adb(adb, target, ["shell", "settings", "put", "global", "adb_enabled", "0"])
                self._log("Depuración ADB desactivada para mayor seguridad.")

            self._log(self.t("done"))
        self._run_bg(task)

    def _on_restore(self) -> None:
        def task():
            target = self._target()
            adb = self.adb_path or ensure_adb(self._log)
            run_adb(adb, None, ["connect", target], timeout=12)
            for scale in ("window_animation_scale", "transition_animation_scale", "animator_duration_scale"):
                run_adb(adb, target, ["shell", "settings", "put", "global", scale, "1.0"])
            for pkg in SAFE_BLOAT_PACKAGES:
                run_adb(adb, target, ["shell", "pm", "enable", "--user", "0", pkg])
            self._log(self.t("restored"))
        self._run_bg(task)


def main() -> int:
    root = tk.Tk()
    VeloInstallerApp(root)
    root.mainloop()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

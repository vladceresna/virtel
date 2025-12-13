
<p align="center">
<img src='https://i.postimg.cc/d0WWH1MR/a2c68588eab85ad2459788d74bd36534.webp' border='0' alt='Virtel Logo'/>
<h1 align="center"><u>Virtel</u>: A New Digital Ecosystem</h1>
<p align="center"><i>A cross-platform execution environment for ultra-light, universal applications.</i></p>
</p>

<p align="center">
  <img alt="License: AGPL-3" src="https://img.shields.io/badge/License-AGPL--3-red?style=flat-square">
  <img alt="Kotlin Multiplatform" src="https://img.shields.io/badge/Kotlin-Multiplatform-orange?style=flat-square">
  <img alt="Rust" src="https://img.shields.io/badge/Rust-2021-purple?style=flat-square">
  <img alt="Build Status" src="https://github.com/vladceresna/virtel/actions/workflows/ubuntu.yml/badge.svg">
</p>

------------

## 🌌 What is Virtel?

Virtel is a fundamentally new type of application environment, conceptualized as a hybrid between a web browser and a traditional operating system. Like a browser, it runs applications in secure, isolated sandboxes. Like an OS, applications are stored locally on your device, offering true offline capabilities and instant execution.

Our ultimate vision is to unify the user experience across all devices. In the future, Virtel aims to replace the standard OS shell (like Desktop Environment on Linux or Launcher on Android), creating a truly cross-platform world where **one codebase equals one executable file that runs everywhere.**

## ⚡ Why Virtel? The Problem of Bloat

Modern applications are bloated. They consume gigabytes of storage and significant system resources because every app bundles its own set of dependencies (libraries, frameworks).

**Virtel's solution is radical: we unify dependencies at the platform level.** Instead of every app shipping its own copy of common libraries, Virtel provides a comprehensive, built-in set of tools. This is why applications built for Virtel are incredibly small-often measured in bytes and kilobytes, not megabytes.

**This means:**
*   **More Space:** Free up gigabytes of storage on your devices.
*   **Instant Speed:** Applications launch and run instantly without loading overhead.
*   **True Privacy & Security:** Full control over your data and applications.
*   **Universal Compatibility:** Write once, run anywhere - Windows, Linux, Android, and more.

## 🏗️ Architecture

Virtel is built on a robust, multi-language foundation to ensure performance and safety:
*   **Core Engine:** Written in **Rust** for memory safety and speed.
*   **User Interface:** Built with **Kotlin Compose Multiplatform** for a native, modern UI on all platforms.
*   **Applications:** Run on Virtel's own custom bytecode (`.vc`), written in the **Steps** language.

## 🦶 The Steps Programming Language

**Steps** is a simple, concise programming language designed specifically for Virtel. Its syntax is straightforward, making it easy to learn while being powerful enough to build complex applications. If you prefer another language, our goal is to support compilers that can transpile other popular languages into Steps bytecode.

**Simple Example (`start.steps`):**
```steps
# Set a variable and print it
var set "Hello, Virtel World!" greeting;
sys out greeting;
```

## 🚀 Try Virtel Now

We are in active development, but you can try the latest release right now!
*   **Download:** [virtel.netlify.app/download](https://virtel.netlify.app/download)
*   **Official Website:** [virtel.netlify.app](https://virtel.netlify.app)

## 🤝 We Are Looking for a Co-Founder!

Virtel is more than a project; it's a revolution. We have the technology, but we need a partner to bring this vision to the world.

**We are actively looking for a Marketing Co-founder** to join us on this journey. If you are passionate about technology, a brilliant communicator, and want to build a global brand from the ground up, **[we want to hear from you](https://forms.gle/jRyg6cebjzZgqhzw5)**.

### For Developers: How to Contribute

Virtel is in its early stages, and we need your help! We are looking for contributors, especially those with experience in **Compose Multiplatform**, to help us build critical components like:
*   The **IDE** for developing Steps applications.
*   **Compilers** for other languages to target the Steps bytecode.

If you're interested in contributing, please check our [Contributing Guidelines](CONTRIBUTING.md) (or open an issue to start the discussion!).

## 📊 Project Status

### Supported Platforms
- [x] Windows
- [x] Linux
- [x] Android
- [ ] macOS
- [ ] iOS
- [ ] Redox
- [ ] Native (OS)

### Implemented Modules
- [x] **Core:** `csl` (Console), `ref` (References), `var` (Variables), `sys` (System)
- [x] **Data:** `lst` (Lists), `mat` (Math), `str` (Strings), `bln` (Booleans)
- [x] **Logic:** `run` (Control Flow: if, while)
- [x] **I/O:** `fls` (Files), `srv` (Server), `clt` (Client), `spr` (Speaker), `tts` (Text-to-Speech)
- [x] **UI:** `scr` (Screen Components)
- [x] **AI:** `llm` (LLM Helper)
- [ ] **UI/UX:** `dgm` (Diagrams), `ppt`/`ort` (3D), `anm` (Animations)
- [ ] **Hardware:** `wss` (WebSockets), `blt` (Bluetooth), `ard` (AR), `vrd` (VR), `stt` (Speech-to-Text), `mcr` (Microphone), `cmr` (Camera), `snr` (Sensors)
- [ ] **Networking:** `cnt` (Connect & Sync)

## 🔧 Building from Source

Building Virtel is a complex process due to its multi-platform nature. We are working on simplifying it. For now, you can follow these steps:

**Prerequisites:**
1.  **JDK 21+** (e.g., [Liberica](https://bell-sw.com/pages/downloads/#jdk-21-lts))
2.  **Android Studio** (required for managing the Android SDK and NDK, even for desktop builds)
3.  **Rust Toolchain** (latest stable)

**Steps:**
1.  Clone the repository: `git clone https://github.com/vladceresna/virtel.git`
2.  Open the project in Android Studio.
3.  Ensure you have the Android SDK and NDK installed via the SDK Manager in Android Studio.
4.  Sync the project with Gradle files.
5.  Run the desired configuration (e.g., `desktopRun`).

If you encounter any issues, please open an issue on GitHub with details about your operating system and the error you encountered.

## 📜 License

This project is licensed under the GNU AGPL-3 License with Virtel Interface Exception for Apps and Plugins - see the [LICENSE](LICENSE) file for details.

## 🌐 Community

Join our community to stay updated, ask questions, and connect with us:
*   **Discord:** [discord.gg/hNSyTvuy2v](https://discord.gg/hNSyTvuy2v)
*   **Telegram:** [t.me/virtelx](https://t.me/virtelx)

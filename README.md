# Igisoro — Traditional Rwandan Royal Strategy Game (Android)

**Igisoro** (*Umukino w'Ubwenge*) is a modern, culturally authentic Android game built with **Kotlin**, **Jetpack Compose**, and **Material 3**. It brings Rwanda's traditional 4×8 Mancala board game to life with custom-rendered **Imigongo** geometric patterns, 3D-shaded carved wooden pits and *Inka* (seeds), local 2-player Pass & Play, and a Minimax AI opponent (*Umuhanga*).

---

## ✨ Features

- **Authentic Rwandan Imigongo Art & Material 3 Design**:
  - Custom Compose `Canvas` artwork featuring traditional *Intango* chevrons and *Itangaza* concentric diamonds in Terracotta Crimson (`#C83E2B`), Royal Amasaka Gold (`#E5A93B`), Obsidian Charcoal (`#0E0C0A`), and Warm Kaolin Ivory (`#FAF6F0`).
  - 3D-shaded carved wooden 4×8 board with clustered *Inka* seed spheres and high-contrast seed count badges.
- **Complete Traditional Igisoro Rules**:
  - **Kubiba (Counter-Clockwise Sowing)**: Each player controls 2 rows of 8 pits (16 pits total, starting with 32 *Inka* per player).
  - **Gukomeza (Relay Sowing)**: Landing in an occupied pit automatically scoops its contents to continue sowing in a multi-lap relay.
  - **Kurasa (Double-Row Capture)**: Landing in an occupied inner-row pit opposite two occupied opponent pits captures all seeds from both opponent pits and immediately resows them from the pit where the capturing lap began.
  - **Ndahise & Ndakubye (Tactical Pass & Retreat)**: Optional traditional rule allowing a player to pass a capture (`Ndahise`) and the opponent to immediately retreat their front-row seeds into the back row (`Ndakubye`).
- **Play Modes & AI**:
  - **Vs Umuhanga (AI)**: Alpha-Beta pruned Minimax AI with 3 difficulty levels (*Easy / Aboroheye*, *Medium / Iringaniye*, *Master / Umuhanga*).
  - **Pass & Play (2P Local)**: Includes optional automatic 180° board rotation on Player 2's turn.
- **Polished Gameplay HUD**:
  - Live Territory Seed Balance bar (`P1` vs `P2` *Inka* ratio), active pit move counters, step-by-step sowing telemetry banner, Undo history, and adjustable sowing animation speeds (*Normal*, *Fast*, *Turbo*).

---

## 🛠️ Building the APK

Make sure JDK 17+ and the Android SDK (API 36) are installed, then run:

```powershell
.\gradlew assembleDebug
```

The installable debug APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

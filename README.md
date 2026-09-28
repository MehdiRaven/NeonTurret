<div align="center">

# 🎮 Neon Turret

### 🎯 Precision • 🎨 Color • 💥 Chain Reactions

A fast-paced 2D neon arcade shooter where precision, color matching,
and quick reactions are the keys to survival.

<p>
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/HTML-FF5722?style=for-the-badge&logo=html5&logoColor=white" />
  <img src="https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" />
  <img src="https://img.shields.io/badge/License-MIT-00D9FF?style=for-the-badge" />
  <img src="https://img.shields.io/github/stars/MehdiRaven/NeonTurret?style=for-the-badge" />
</p>

</div>

---

## 🎮 About

**Neon Turret** is a 2D neon arcade game built around fast reactions,
precision shooting, color matching, and chain reactions.

A central turret continuously changes color and fires projectiles toward
the direction selected by the player.

Incoming colored balls move toward the turret. Match colors, trigger
chain reactions, use power-ups, and survive for as long as possible.

---

## ⚡ Gameplay

### 🎯 Shoot

Tap or click anywhere on the screen to fire a projectile in that direction.

Every shot changes the turret's color, requiring the player to constantly
adapt to incoming threats.

### 🎨 Match Colors

When a projectile matches the color of an incoming ball, the player
receives a higher score.

### 💥 Chain Reactions

Same-colored balls that become adjacent can explode together, creating
chain reactions and allowing multiple balls to be cleared in sequence.

### ☠️ Survive

Incoming balls continuously approach the central turret.

If an incoming ball reaches the turret, the game ends.

---

## 🔋 Power-Ups

| Power-Up | Effect |
|---|---|
| 🐌 **Slow** | Slows incoming balls for several seconds. |
| 🛡️ **Shield** | Protects the turret from incoming hits for 10 seconds. |
| ✖️2️⃣ **Double Score** | Doubles the score earned by the player. |

---

## 📸 Screenshots

<div align="center">

### 🏠 Menu

<img src="ScreenShots/Menu.jpg" width="280"/>

### ⚙️ Settings

<img src="ScreenShots/Setting.jpg" width="280"/>

### 🎮 Gameplay

<img src="ScreenShots/GamePlay.jpg" width="280"/>

</div>

---
## 🌍 Languages

Neon Turret supports:

- 🇮🇷 Persian
- 🇬🇧 English

---

## 🛠️ Tech Stack

Neon Turret combines an Android application layer with web-based
game components.

| Technology | Role |
|---|---|
| 📱 **Android** | Application platform |
| 🟣 **Kotlin** | Android application layer |
| 🌐 **HTML** | Game interface and structure |
| ⚡ **JavaScript** | Game logic and interaction |
| 🔗 **WebView** | Android integration with the web-based game |

---

## 📂 Project Structure

```text
NeonTurret/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/
│           ├── res/
│           └── assets/
│               └── game.html
│
├── game.html
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── metadata.json
├── .gitignore
└── LICENSE

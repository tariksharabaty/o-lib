<p align="center"><img src=".github/assets/icon.png" width="96" alt="📚 O Lib"></p>

<h1 align="center">📚 O Lib</h1>

<p align="center"><b>The versatile core engine behind your favorite mods.</b></p>

<p align="center">
  <img src="https://img.shields.io/badge/version-1.1.2-0A7BBB?style=flat-square&labelColor=555555" alt="version 1.1.2">
  <img src="https://img.shields.io/badge/Minecraft-26.3-62B47A?style=flat-square&labelColor=555555" alt="Minecraft 26.3">
  <img src="https://img.shields.io/badge/loader-Fabric-DBD0B4?style=flat-square&labelColor=38342A" alt="loader Fabric">
  <img src="https://img.shields.io/badge/Java-25-E76F00?style=flat-square&labelColor=555555&logo=openjdk&logoColor=white" alt="Java 25">
  <img src="https://img.shields.io/badge/license-MIT-F1C40F?style=flat-square&labelColor=555555" alt="license MIT">
  <a href="https://modrinth.com/organization/oidioomstudio"><img src="https://img.shields.io/badge/Modrinth-Oidioom%20Studio-1BD96A?style=flat-square&labelColor=555555&logo=modrinth&logoColor=white" alt="Modrinth Oidioom Studio"></a>
  <a href="https://oidioom.com"><img src="https://img.shields.io/badge/by-Oidioom-0D1117?style=flat-square&labelColor=555555" alt="by Oidioom"></a>
</p>

<p align="center"><a href="#-english">🇬🇧 English</a> · <a href="#-türkçe">🇹🇷 Türkçe</a></p>

---

## 🇬🇧 English

**O Lib** is a foundational library built for modern Minecraft (26.3 / 1.21+), providing a standardized configuration system, custom tactile GUI components, interactive HUD manipulation, multi-language switching, and world traversal utilities.

---

## 🏛️ Core Features & Architecture

### 1. 🗿 Stone UI Design System
- Custom medieval fantasy stone-themed UI widgets crafted for clarity, contrast, and immersion.
- Complete suite of widgets: `StoneButtonWidget`, `StoneToggleWidget`, `StoneSliderWidget`, `StoneDropdownWidget`, `StoneColorWidget`, `StoneKeybindWidget`, and `StoneTabButtonWidget`.
- Full ModMenu integration: seamless configuration screens accessed directly from the in-game mod list.

### 2. ⚙️ Declarative OConfig System
- Type-safe, categorized, and grouped configuration registry (`OConfigRegistry`, `OConfigHolder`).
- Real-time serialization, persistence, and safe default fallbacks.
- Live hot-reloading: changes take effect immediately without requiring game restarts.

### 3. 🖥️ Interactive HUD Editor
- On-screen visual HUD positioning system (`OHudEditorScreen`, `OHudRegistry`).
- Allows players to visually drag, drop, scale, and align custom HUD widgets (such as tool durability indicators, effect counters, or compass markers) with pixel precision.

### 4. 🌐 Dynamic Multi-Language Engine
- Integrated internationalization engine (`OLanguageManager`, `ODelegatingLanguage`).
- Allows instant client language switching without reloading resource packs.
- Ships with built-in translations for **10 languages**:
  - English (`en_us`), Turkish (`tr_tr`), German (`de_de`), Spanish (`es_es`), French (`fr_fr`), Japanese (`ja_jp`), Korean (`ko_kr`), Portuguese (`pt_br`), Russian (`ru_ru`), Chinese (`zh_cn`).

### 5. 🗃️ Unified Oidioom Creative Tab
- `OCreativeTabs` automatically organizes items, blocks, and tools from all installed Oidioom Studio mods into a single dedicated Creative inventory tab (`itemGroup.olib.oidioom_tab`).

### 6. ⚡ High-Performance World & Render Utilities
- **`OBlockFinder`:** Optimized 3D breadth-first search (BFS) flood-fill algorithm designed for tree harvesting and ore vein mining with anti-cascade safeguards.
- **`OBlockOutlineRenderer` & `CachedEdge`:** Hardware-accelerated world space bounding box rendering for previewing break zones and vein clusters.
- **`OInventoryHelper` & `OEffectHelper`:** Universal utility methods for safe item extraction, stack insertion, and visual/audio particle feedback.

---

## 📦 For Mod Developers

To use O Lib in your Fabric project, add it to your `build.gradle`:

```groovy
repositories {
    mavenLocal() // When developing locally
    // Modrinth / CurseForge Maven when published
}

dependencies {
    modImplementation "com.oidioomstudio:o-lib:1.1.2-mc26.3"
}
```

---

## 📥 Installation

1. Install **Minecraft 26.3** with **Fabric Loader**.
2. Install **Fabric API**.
3. Drop `o-lib-1.1.2-mc26.3.jar` into your `mods` folder.

---

## ⬇️ Download

- **Modrinth:** [Oidioom Studio](https://modrinth.com/organization/oidioomstudio)
- **GitHub Releases:** [`o-lib-1.1.2-mc26.3.jar`](../../releases/latest)

## 🔨 Building from source

Requires **JDK 25**.

```bash
./gradlew build
# output: build/libs/o-lib-1.1.2-mc26.3.jar
```

## 📄 License

[MIT](LICENSE) © 2026 Oidioom / Tarek Sharabaty

---

## 🇹🇷 Türkçe

**Oidioom'un tüm Minecraft modlarına güç veren çekirdek kütüphane, özel arayüz çatısı ve yardımcı araçlar.**

### ✨ Özellikler

- Ortaçağ taş temalı özel arayüz bileşenleri (buton, anahtar, kaydırıcı, açılır menü, renk seçici…)
- Kategorili ve gruplu, tip güvenli ayar kayıt sistemi
- Oyunu yeniden başlatmadan anında uygulanan ayarlar
- HUD öğelerini ekranda sürükle-bırak ile konumlandırma
- ModMenu entegrasyonu ve ortak Oidioom yaratıcı sekmesi

### 📥 Kurulum

1. **Minecraft 26.3** için **Fabric Loader** kurun.
2. **Fabric API** modlarını `mods` klasörüne ekleyin.
3. `o-lib-1.1.2-mc26.3.jar` dosyasını `mods` klasörüne atın ve oyunu başlatın.

### ⬇️ İndir

- **Modrinth:** [Oidioom Studio](https://modrinth.com/organization/oidioomstudio)
- **GitHub Releases:** [`o-lib-1.1.2-mc26.3.jar`](../../releases/latest)

### 📄 Lisans

[MIT](LICENSE) © 2026 Oidioom / Tarek Sharabaty

---

<p align="center"><a href="https://oidioom.com"><picture><source media="(prefers-color-scheme: dark)" srcset=".github/assets/oidioom-dark.svg"><img src=".github/assets/oidioom-light.svg" width="40" alt="Oidioom"></picture></a></p>
<p align="center"><b>by Oidioom — Tarek Sharabaty</b><br>
<a href="https://oidioom.com">oidioom.com</a> · <a href="https://github.com/tariksharabaty">GitHub</a> · <a href="https://modrinth.com/organization/oidioomstudio">Modrinth</a> · <a href="https://gear.oidioom.com">Gear</a> · <a href="https://www.instagram.com/oidioom">Instagram</a></p>

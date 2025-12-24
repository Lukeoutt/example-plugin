# Lucas Loadout Wheel – RuneLite Plugin

## Overview

This project is a **RuneLite plugin** that helps the player manage and recall
inventory layouts for different activities (e.g., Herb Run, ToA, Slayer) via:

* **Named loadouts** (saved from current inventory)
* **Hotkeys** to switch the active loadout
* An optional **radial "wheel" menu** to select layouts visually
* **Overlays** that highlight which inventory slots should contain which items
  when the bank is open

The plugin is **purely assistive and UI-only**:

* It does **not click**, withdraw, deposit, or interact with the game.
* It **never automates gameplay**.
* It only **reads state** and **draws overlays / responds to keybinds**.

The goal is to make banking and gear swaps more consistent and organized.

---

## Goals

1. Save **named inventory layouts** ("loadouts") using the player’s current inventory.
2. Provide **hotkeys** to switch the active layout.
3. When the **bank is open**, visually show:

   * Which inventory slots should contain which items.
   * Which slots are correct or missing.
4. Implement an optional **layout wheel**:

   * A radial selector activated by a keybind.
   * Mouse direction picks a layout.
   * Releasing the key activates the chosen layout.
5. Provide UI in the plugin panel to manage layouts.

---

## Non-Goals / Constraints

* No automation of gameplay.
* No clicks, input, banking actions, or scripted interaction.
* Only reads inventory / bank state via RuneLite APIs.
* Only renders overlays and responds to configuration + keybinds.

---

## High-Level Features

### Layouts

Each layout stores:

* Name
* List of `(itemId, slotIndex)` pairs
* Optional category
* Optional keybind

User actions:

* Capture current inventory into a layout
* Rename / delete / update layout
* Persist layouts between sessions

---

### Hotkeys

* Each layout may have an optional **Keybind**
* Pressing the hotkey → **activates that layout**
* Only one layout is active at a time

---

### Bank Overlay

Renders when:

* Logged in
* Bank is open
* A layout is active

Behavior:

* Compare inventory slots vs layout slots
* Color slots:

  * Green → correct item
  * Red → wrong or missing

---

### Layout Wheel (Radial Selector)

* Triggered by a configured keybind
* While held:

  * Show radial layout menu
  * Mouse direction = highlighted slice
* On release:

  * Set active layout = selected slice
  * Hide overlay

Wheel is **visual only**, no interaction with game UI.

---

## Architecture

**Base package:** `com.lucas`

Classes:

* `LucasLoadoutWheelPlugin`
* `LucasLoadoutConfig`
* `LoadoutInventoryOverlay`
* `LoadoutWheelOverlay`
* `Layout`
* `LayoutItem`

Layouts persisted via JSON in config or similar.

---

## Acceptance Criteria

Feature is complete when:

1. User can create a layout from current inventory
2. Layout persists after restart
3. Overlay highlights slots when bank is open
4. A layout can be activated via:

   * Plugin panel OR
   * Hotkey OR
   * Wheel selector
5. No automation or game interaction occurs at any point

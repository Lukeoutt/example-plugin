# Coding Agent Instructions — Lucas Loadout Wheel (RuneLite Plugin)

You are an expert RuneLite plugin developer.
Your task is to implement the plugin described in `SPEC.md`.

The plugin must be **UI-only and assistive**:

* Do NOT send mouse or keyboard input
* Do NOT withdraw or deposit items
* Do NOT automate gameplay
* Only read game state and render overlays / respond to keybinds

The plugin will be sideloaded into RuneLite, built using Gradle.

---

## Objectives

1. Implement **layout capture** from current inventory
2. Implement **inventory highlight overlay** while bank is open
3. Implement **layout management UI** in the plugin panel
4. Add **hotkey support** to activate layouts
5. Add optional **radial layout wheel overlay**

---

## Data Model

Create:

```
LayoutItem
  int itemId
  int slotIndex

Layout
  String id
  String name
  List<LayoutItem> items
  Keybind hotkey (optional)
  String category (optional)
```

Store layouts in memory and persist via serialized JSON.

---

## Config

Create `LucasLoadoutConfig` with:

* `boolean enableInventoryOverlay()`
* `boolean enableWheel()`
* `Keybind wheelKey()`
* `String layoutsJson()` — persisted layouts storage

Use `ConfigManager` to load/save JSON.

---

## Main Plugin Class

`LucasLoadoutWheelPlugin` must:

* Manage `List<Layout>`
* Track `Layout currentLayout`
* Load layouts on startup
* Persist layouts on update
* Subscribe to:

  * GameState events
  * Bank open/close
  * Keybind triggers
* Provide methods:

  * Add layout from inventory
  * Update layout from inventory
  * Rename layout
  * Delete layout
  * Set active layout

---

## Layout Capture

Read inventory using `InventoryID.INVENTORY`.

Create `LayoutItem` for each slot:

* `itemId`
* `slotIndex`

Store in corresponding `Layout`.

---

## Plugin Panel UI

Implement panel allowing user to:

* Add layout from current inventory
* Rename layout
* Delete layout
* Re-capture from inventory
* Toggle overlay + wheel
* Assign optional hotkey

---

## Inventory Overlay

`LoadoutInventoryOverlay` must:

* Render only when:

  * Logged in
  * Bank is open
  * A layout is active
* Read inventory
* Compare against layout
* Draw:

  * Green = correct item
  * Red = wrong / missing

---

## Layout Wheel Overlay

`LoadoutWheelOverlay` must:

* Render radial wheel while wheel key is held
* Show up to 8 layouts
* Mouse direction selects slice
* On release:

  * Set selected layout active
  * Hide overlay

Overlay must NOT send input.

---

## Validation

Feature is complete when:

* Project builds successfully using Gradle
* Plugin appears as **Lucas Loadout Wheel**
* Layouts save and restore
* Overlays render correctly
* Wheel selects layouts
* No gameplay automation exists

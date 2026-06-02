# Keyboard

The `Key` object provides string constants for non-printable keys. Compare them against `FtxUIEvent.input` inside a `catchEvent` handler.

## Usage pattern

```kotlin
component.catchEvent { event ->
    when (event.input) {
        Key.ArrowUp   -> { moveUp(); true }
        Key.ArrowDown -> { moveDown(); true }
        Key.Escape    -> { cancel(); true }
        Key.Return    -> { confirm(); true }
        else -> false
    }
}
```

Return `true` to consume the event (stop propagation), `false` to let it continue.

For printable characters use `event.isCharacter` and `event.character` instead of `event.input`.

## Arrow keys

```kotlin
Key.ArrowLeft       // ← 
Key.ArrowRight      // →
Key.ArrowUp         // ↑
Key.ArrowDown       // ↓
Key.ArrowLeftCtrl   // Ctrl+←
Key.ArrowRightCtrl  // Ctrl+→
Key.ArrowUpCtrl     // Ctrl+↑
Key.ArrowDownCtrl   // Ctrl+↓
```

## Common keys

```kotlin
Key.Backspace   // DEL (0x7F)
Key.Delete      // Forward delete
Key.Escape      // ESC
Key.Return      // Enter (\n)
Key.Tab         // Tab (\t)
Key.TabReverse  // Shift+Tab
```

## Navigation keys

```kotlin
Key.Insert
Key.Home
Key.End
Key.PageUp
Key.PageDown
```

## Function keys

```kotlin
Key.F1   Key.F2   Key.F3   Key.F4
Key.F5   Key.F6   Key.F7   Key.F8
Key.F9   Key.F10  Key.F11  Key.F12
```

## Ctrl keys

```kotlin
Key.CtrlA  Key.CtrlB  Key.CtrlC  Key.CtrlD  Key.CtrlE
Key.CtrlF  Key.CtrlG  Key.CtrlH  Key.CtrlI  Key.CtrlJ
Key.CtrlK  Key.CtrlL  Key.CtrlM  Key.CtrlN  Key.CtrlO
Key.CtrlP  Key.CtrlQ  Key.CtrlR  Key.CtrlS  Key.CtrlT
Key.CtrlU  Key.CtrlV  Key.CtrlW  Key.CtrlX  Key.CtrlY  Key.CtrlZ
```

Note: `Key.CtrlI` == `Key.Tab` and `Key.CtrlJ` == `Key.Return`.

## Alt keys

```kotlin
Key.AltA  Key.AltB  Key.AltC  /* ... */  Key.AltZ
```

## CtrlAlt keys

```kotlin
Key.CtrlAltA  Key.CtrlAltB  Key.CtrlAltC  /* ... */  Key.CtrlAltZ
```

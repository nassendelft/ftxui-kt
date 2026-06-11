# ftxui-kt API reference

Kotlin/Native bindings for [FTXUI](https://github.com/ArthurSonzogni/FTXUI). See the [README](../README.md) for setup, quick start, and DSL overview.

## Core

| Doc | Description |
|---|---|
| [app](app.md) | `FtxUIApp`, `FtxUILoop`, `FtxUIEvent` — application lifecycle and event loop |
| [state](state.md) | `BoolState`, `IntState`, `StringState`, `FloatState` — native-backed mutable state |
| [renderer](renderer.md) | `renderer()`, `focusableRenderer()`, `decorateRender()`, `catchEvent()` |
| [elements](elements.md) | `text`, `gauge`, `separator`, `vbox`, `hbox`, `paragraph`, `spinner`, and more |
| [decorators](decorators.md) | Border, color, text style, flex, size, alignment, scroll, focus, and layout-measurement (`reflect`) decorators |
| [containers](containers.md) | `horizontal`, `vertical`, `tab`, `stacked`, `collapsible`, `maybe`, `modal`, `resizableSplit` |
| [color](color.md) | `Color` — palette constants, RGB/HSV constructors, interpolation, blending |
| [keyboard](keyboard.md) | `Key` — constants for arrow keys, function keys, Ctrl/Alt combinations |
| [terminal](terminal.md) | `Terminal` — query and override terminal size, color support, and quirks |

## Components

| Doc | Description |
|---|---|
| [button](button.md) | `button()` with preset styles and custom transform |
| [input](input.md) | `input()`, `inputPassword()` — single-line text entry |
| [selection](selection.md) | `checkbox()`, `radiobox()`, `toggle()` |
| [menu](menu.md) | `menu()`, `menuHorizontal()`, `menuHorizontalAnimated()`, `menuToggle()`, `menuEntry()` |
| [slider](slider.md) | `slider()` — horizontal and directional sliders for Int and Float values |
| [dropdown](dropdown.md) | `dropdown()`, `dropdownCustom()` — collapsible selection lists |
| [canvas](canvas.md) | `Canvas` — 2D drawing surface for graphics and animations |
| [table](table.md) | `Table`, `TableSelection` — styled 2D string data |
| [graph](graph.md) | `graph()`, `GraphFn` — bar/line graph from a height callback |
| [linear-gradient](linear-gradient.md) | `LinearGradient` — multi-stop color gradients |
| [flexbox](flexbox.md) | `flexbox()`, `FlexboxConfig` — CSS-like wrapping layout |
| [window](window.md) | `windowComponent()`, `WindowOptions` — draggable/resizable floating window |

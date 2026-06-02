# Terminal

The `Terminal` object provides runtime queries about the terminal environment and lets you override detected values.

## Size

```kotlin
val dims = Terminal.size()
// dims.dimx — width in columns
// dims.dimy — height in rows
```

Set a fallback size used when the terminal size cannot be detected (e.g. piped input):

```kotlin
Terminal.setFallbackSize(width = 80, height = 24)
```

## Color support

```kotlin
val support = Terminal.colorSupport()
// TerminalColor.Palette1    — monochrome
// TerminalColor.Palette16   — 16 ANSI colors
// TerminalColor.Palette256  — xterm 256-color
// TerminalColor.TrueColor   — 24-bit RGB
```

Override the detected level (useful in tests or constrained environments):

```kotlin
Terminal.setColorSupport(TerminalColor.Palette256)
```

## Quirks

`Quirks` controls rendering workarounds for terminals that don't fully conform to standard behavior.

```kotlin
data class Quirks(
    val blockCharacters: Boolean = false,  // use block chars instead of braille
    val cursorHiding: Boolean = true,      // hide cursor during render
    val componentAscii: Boolean = false,   // use ASCII fallbacks for borders/arrows
    val colorSupport: TerminalColor = TerminalColor.TrueColor,
)
```

Read current quirks:

```kotlin
val q = Terminal.getQuirks()
println(q.blockCharacters)
```

Override quirks:

```kotlin
Terminal.setQuirks(Quirks(
    blockCharacters = true,
    componentAscii = true,
    colorSupport = TerminalColor.Palette16,
))
```

## Example — adaptive color usage

```kotlin
fun main() {
    val colorLevel = Terminal.colorSupport()
    val accent = when {
        colorLevel >= TerminalColor.TrueColor  -> Color.rgb(255u, 165u, 0u)
        colorLevel >= TerminalColor.Palette256 -> Color.palette256(214)
        else                                   -> Color.Yellow
    }
    // ... use accent color ...
}
```

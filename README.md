# ftxui-kt

Kotlin/Native bindings for [ftxui](https://github.com/ArthurSonzogni/FTXUI), a C++ terminal UI library. No cmake or C++ toolchain required — pre-built binaries are downloaded automatically at build time.

## Modules

| Module | Package | Description |
|---|---|---|
| `ftxui-kt` | `nl.ncaj` | Low-level Kotlin wrapper over the ftxui C API |
| `ftxui-kt-dsl` | `nl.ncaj.dsl` | Idiomatic Kotlin DSL — no state objects, no manual memory management |

## Quick start

```kotlin
import nl.ncaj.*
import nl.ncaj.dsl.*

fun main() = fullscreenApp {
    var checked = false
    var selected = 0
    var inputText = ""

    val menu = vertical {
        +checkbox("Enable feature", ::checked)
        +input(::inputText, "type here…")
        +menu(listOf("Option A", "Option B", "Option C"), ::selected)
        +button("Quit") { exit() }
    }

    renderer(child = menu) {
        vbox {
            +menu.render()
            +separator()
            +hbox {
                +text("Checked: $checked  ")
                +text("Selected: $selected  ")
                +text("Input: $inputText")
            }
        }
    }
}
```

## Supported platforms

- Linux x86_64
- Linux ARM64
- macOS ARM64

## Using the modules

### ftxui-kt-dsl (recommended)

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("nl.ncaj:ftxui-kt-dsl:<version>")
}
```

Import both packages:

```kotlin
import nl.ncaj.*       // element factories, decorators, Color, Key, etc.
import nl.ncaj.dsl.*   // app entry points, container/element builders
```

### ftxui-kt (low-level)

Use directly if you need full control over resource lifetimes:

```kotlin
dependencies {
    implementation("nl.ncaj:ftxui-kt:<version>")
}
```

## DSL overview

### App entry points

| Function | ftxui mode |
|---|---|
| `fullscreenApp { }` | Full alternate screen |
| `fullscreenPrimaryScreenApp { }` | Full primary screen |
| `fullscreenAlternateScreenApp { }` | Full alternate screen (explicit) |
| `fitComponentApp { }` | Sized to component |
| `terminalOutputApp { }` | Write to stdout |
| `fixedSizeApp(w, h) { }` | Fixed dimensions |

The block's last expression is the root component. The `AppScope` receiver provides `exit()`, `post()`, `poll()`, `requestAnimationFrame()`, etc.

### State

Use plain Kotlin `var` declarations. Pass them to components via property references:

```kotlin
fullscreenApp {
    var count = 0
    var label = ""
    var enabled = false

    vertical {
        +slider("Count", ::count, 0, 100)
        +input(::label, "enter label…")
        +checkbox("Enabled", ::enabled)
        +button("Reset") { count = 0; label = "" }
    }
}
```

### Container builders

```kotlin
vertical { +comp1; +comp2 }          // stacks components vertically
horizontal { +comp1; +comp2 }        // stacks components horizontally
stacked { +comp1; +comp2 }           // overlays components (z-axis)
tab(::selectedIndex) { +comp1; +comp2 }  // tabbed container
```

### Element builders

```kotlin
vbox { +text("Hello"); +gauge(0.5) }
hbox { +text("A"); +separator(); +text("B") }
dbox { +background; +foreground }    // depth-stacked elements
```

### Advanced

```kotlin
// Canvas drawing
val el = canvas(80, 24) {
    drawText(0, 0, "Hello")
    drawPointCircle(40, 12, 8, Color.Red)
}

// Tables
val el = table(listOf(listOf("Name", "Age"), listOf("Alice", "30"))) {
    selectAll().border()
    selectRow(0).decorateBold().decorateCellsColor(Color.Blue)
}

// Graph
val fn = graphFn { w, h, out -> repeat(w) { out[it] = (h * it / w) } }
val el = graph(fn)

// Linear gradient
val grad = linearGradient { angle(45f); stop(Color.Red); stop(Color.Blue) }
val el = text("Gradient").colorLinearGradient(grad)
```

## Building from source

```bash
./gradlew :ftxui-kt:compileKotlinLinuxX64
./gradlew :ftxui-kt-dsl:compileKotlinLinuxX64
```

The `ftxui-c` pre-built archive is downloaded automatically on first build.

## License

See [LICENSE](LICENSE).

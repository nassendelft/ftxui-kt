# ftxui-kt

Kotlin/Native bindings for [ftxui](https://github.com/ArthurSonzogni/FTXUI), a C++ terminal UI library. No cmake or C++ toolchain required — pre-built binaries are downloaded automatically at build time.

Full API reference: **[docs/](docs/)**

## Modules

| Module | Package | Description |
|---|---|---|
| `ftxui-kt` | `nl.ncaj` | Low-level Kotlin wrapper over the ftxui C API |
| `ftxui-kt-dsl` | `nl.ncaj.dsl` | Idiomatic Kotlin DSL — no state objects, no manual memory management |

## Quick start

```kotlin
import nl.ncaj.*
import nl.ncaj.dsl.*

// State vars must be class or top-level properties, not local variables.
// See "State" section below for details.
var checked = false
var selected = 0
var inputText = ""

fun main() = fullscreenApp {
    val menu = vertical {
        +checkbox("Enable feature", ::checked)
        +input(::inputText, "type here…")
        +menu(listOf("Option A", "Option B", "Option C"), ::selected)
        +button("Quit", onClick = { exit() })
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

Components that hold mutable state (inputs, checkboxes, menus, sliders, etc.) take a
`KMutableProperty0<T>` via `::` syntax so the DSL can read and write your variable directly.

```kotlin
// Top-level or class member properties
var count = 0
var label = ""
var enabled = false

fun main() = fullscreenApp {
    vertical {
        +slider("Count", ::count, 0, 100)
        +input(::label, "enter label…")
        +checkbox("Enabled", ::enabled)
        +button("Reset", onClick = { count = 0; label = "" })
    }
}
```

> **Kotlin/Native limitation:** `::` references to *local variables* (variables declared inside
> a function or lambda body) are [not yet supported](https://youtrack.jetbrains.com/issue/KT-15360)
> in Kotlin/Native. State variables must be **top-level** or **class member** properties.
> This is a Kotlin/Native compiler restriction, not specific to this library.

### Button trailing lambda

Because `button` has a third `options` parameter after `onClick`, Kotlin's trailing lambda
syntax does not apply when the call is prefixed with `+`. Use a named argument instead:

```kotlin
// Correct
+button("Click me", onClick = { doSomething() })

// Also correct (explicit parentheses)
+(button("Click me") { doSomething() })

// Wrong — the lambda is parsed as a separate expression, not as onClick
+button("Click me") { doSomething() }
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

// Tables — selections use a lambda block
val el = table(listOf(listOf("Name", "Age"), listOf("Alice", "30"))) {
    selectAll { border() }
    selectRow(0) { decorateBold().decorateCellsColor(Color.Blue) }
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

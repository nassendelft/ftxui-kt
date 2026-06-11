# ftxui-kt

Kotlin/Native bindings for [ftxui](https://github.com/ArthurSonzogni/FTXUI), a C++ terminal UI library. No cmake or C++ toolchain required — pre-built binaries are downloaded automatically at build time.

Full API reference: **[docs/](docs/)**

## Package

All library components are provided under a single package, `nl.ncaj.ftxui`. This contains both the low-level wrapper over the FTXUI C API and the high-level idiomatic Kotlin DSL.

## Quick start

```kotlin
import nl.ncaj.ftxui.*

fun main() = fullscreenApp {
    val checked = boolState(false)
    val selected = intState(0)
    val inputText = stringState("")

    val menu = vertical {
        checkbox("Enable feature", checked)
        input(inputText, "type here…")
        menu(listOf("Option A", "Option B", "Option C"), selected)
        button("Quit") { exit() }
    }

    renderer(child = menu) {
        vbox {
            +menu.render()
            separator()
            hbox {
                text("Checked: ${checked.value}  ")
                text("Selected: ${selected.value}  ")
                text("Input: ${inputText.value}")
            }
        }
    }
}
```

## Supported platforms

- Linux x86_64
- Linux ARM64
- macOS ARM64

## Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("nl.ncaj.ftxui:ftxui-kt:<version>")
}
```

Import the package:

```kotlin
import nl.ncaj.ftxui.*
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

Interactive components (checkbox, slider, menu, input, etc.) hold state using explicit state objects (`BoolState`, `IntState`, `StringState`, `FloatState`). 

Inside app builder blocks (like `fullscreenApp { ... }`), you can use the `AppScope` extension functions `boolState()`, `intState()`, `stringState()`, and `floatState()` to instantiate state. State created this way is automatically kept alive for the duration of the app loop and released via Kotlin's `Cleaner` mechanism when the app exits (requiring no manual memory management):

```kotlin
import nl.ncaj.ftxui.*

fun main() = fullscreenApp {
    val count = intState(0)
    val label = stringState("")
    val enabled = boolState(false)

    vertical {
        slider("Count", count, 0, 100)
        input(label, "enter label…")
        checkbox("Enabled", enabled)
        button("Reset") {
            count.value = 0
            label.value = ""
        }
    }
}
```

If you instantiate state objects manually outside of an app builder using their constructors (e.g. `val state = BoolState(false)`), you are responsible for calling `state.free()` when the state is no longer needed.

#### Property reference syntax & Kotlin/Native limitation

Alternatively, if your state already lives in standard Kotlin variables, you can pass property references (`::`) to the component functions. The native buffers are created and freed automatically:

```kotlin
// Top-level or class member properties
var count = 0
var label = ""
var enabled = false

fun main() = fullscreenApp {
    vertical {
        slider("Count", ::count, 0, 100)
        input(::label, "enter label…")
        checkbox("Enabled", ::enabled)
        button("Reset") { count = 0; label = "" }
    }
}
```

> **Kotlin/Native limitation:** `::` references to *local variables* (variables declared inside
> a function or lambda body) are [not yet supported](https://youtrack.jetbrains.com/issue/KT-15360)
> in Kotlin/Native. If you use the property reference syntax, the state variables must be **top-level** or **class member** properties.
> This is a Kotlin/Native compiler restriction, not specific to this library.

### Button trailing lambda

Inside container blocks (such as `vertical` or `horizontal`), the DSL provides extension functions that put the `onClick` handler as the last parameter, allowing clean trailing lambda usage directly:

```kotlin
vertical {
    button("Click me") { doSomething() }
}
```

If you use the standalone `button` function outside of a container block (which returns a `Component` that you might add manually later), both `button(label, onClick)` and `button(label, options, onClick)` overloads are provided, meaning trailing lambdas work here as well:

```kotlin
val btn = button("Click me") { doSomething() }
```

### Container builders

Inside container blocks (like `vertical`, `horizontal`, etc.), components created via DSL extension functions (such as `button`, `checkbox`, `input`, `slider`, etc.) are automatically added to the container without needing the unary `+` operator.

For external components, you can add them using the unary `+` operator or `.add()`:

```kotlin
vertical {
    // DSL components add themselves automatically:
    checkbox("Enable feature", checked)
    button("Quit") { exit() }

    // External or custom components use '+' or '.add()':
    +myCustomComponent
    anotherComponent.add()
}
```

Available container blocks:
```kotlin
vertical { ... }                      // stacks components vertically
horizontal { ... }                    // stacks components horizontally
stacked { ... }                       // overlays components (z-axis)
tab(::selectedIndex) { ... }          // tabbed container (can also take tab(selectedState) { ... })
```

### Element builders

Just like containers, element builders (like `vbox`, `hbox`, etc.) provide DSL extension functions (such as `text`, `separator`, `gauge`, `paragraph`) that add the elements automatically. External or custom elements can be added with `+`:

```kotlin
vbox {
    text("Hello")
    gauge(0.5)
}
hbox {
    text("A")
    separator()
    text("B")
}
dbox {
    // External/custom elements use '+'
    +background
    +foreground
}
```

### Custom DSL extensions

Consumers of the library can easily write their own extension functions on `ContainerScope` or `ElementScope` to integrate custom components or elements seamlessly into the DSL style:

```kotlin
// Custom component extension on ContainerScope
fun ContainerScope.myWidget(title: String, checked: BoolState, onClick: () -> Unit) =
    vertical {
        checkbox(title, checked)
        button("Click me", onClick)
    }.add()

// Custom element extension on ElementScope
fun ElementScope.redHeader(title: String) =
    text(title) { bold().color(Color.Red) }
```

### Advanced

Most elements and components also have overloads that work directly inside their respective scope builders:

```kotlin
vbox {
    // Canvas drawing directly in ElementScope
    canvas(80, 24) {
        drawText(0, 0, "Hello")
        drawPointCircle(40, 12, 8, Color.Red)
    }

    // Tables directly in ElementScope
    table(listOf(listOf("Name", "Age"), listOf("Alice", "30"))) {
        selectAll { border() }
        selectRow(0) { decorateBold().decorateCellsColor(Color.Blue) }
    }

    // Graph directly in ElementScope
    val fn = graphFn { w, h, out -> repeat(w) { out[it] = (h * it / w) } }
    graph(fn)
}
```

Standalone elements can still be created and decorated directly:

```kotlin
// Linear gradient
val grad = linearGradient { angle(45f); stop(Color.Red); stop(Color.Blue) }
val el = text("Gradient").colorLinearGradient(grad)
```

### Measuring layout (reflect)

A `Box` records the rectangle the layout assigns to an element on every render — the building block for components that size their content to the space they actually receive (virtualized lists, pagers):

```kotlin
val box = Box()

renderer {
    vbox(/* visible rows only */).flex().reflect(box)
}

// After a frame has rendered, box.width / box.height hold the assigned size.
```

See [docs/decorators.md](docs/decorators.md#layout-measurement-reflect) for details and lifetime rules.

## Building from source

Build the library for your host platform using Gradle:

```bash
./gradlew compileKotlinLinuxX64
# or
./gradlew compileKotlinMacosArm64
```

The `ftxui-c` pre-built archive is downloaded and extracted automatically on first build.

## License

See [LICENSE](LICENSE).

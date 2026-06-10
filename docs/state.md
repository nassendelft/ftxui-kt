# State

Interactive components (checkbox, slider, menu, input, etc.) need a native-heap-backed buffer that FTXUI can read/write. The state classes wrap these buffers.

## Classes

```kotlin
class BoolState(initial: Boolean = false)
class IntState(initial: Int = 0)
class StringState(initial: String = "")
class FloatState(initial: Float = 0f)
```

Each has a `value` property for reading and writing.

## AppScope registered states (automatic lifecycle)

Inside app builder blocks (like `fullscreenApp { ... }`), you can use the `AppScope` extension functions `boolState()`, `intState()`, `stringState()`, and `floatState()` to instantiate state. State created this way is automatically kept alive for the duration of the app loop and released via Kotlin's `Cleaner` mechanism when the app exits, requiring no manual memory management:

```kotlin
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

## Manual lifecycle state objects

If you instantiate state objects manually using their constructors (e.g. `val state = BoolState(false)`), you are responsible for calling `state.free()` when the state is no longer needed:

```kotlin
val checked = BoolState(false)
checked.value = true
// ...
checked.free()
```

### Usage with components

Pass state objects to component factory functions:

```kotlin
val selected = IntState(0)
val menuComp = menu(entries, selected)

val content = StringState()
val inputComp = input(content, "placeholder")

val isChecked = BoolState()
val checkComp = checkbox("Enable feature", isChecked)
```

Read the current value anywhere — it reflects what the user has entered:

```kotlin
renderer(component) {
    vbox(
        text("Selected: ${selected.value}"),
        component.render()
    )
}
```

## Property reference overloads (no explicit state)

If your state already lives in a Kotlin variable, use the `KMutableProperty0` overloads with `::myVar`. The native buffer is created and freed automatically:

```kotlin
var selectedIndex = 0
val menuComp = menu(entries, ::selectedIndex)

var isOpen = false
val collapsibleComp = collapsible("Section", inner, ::isOpen)
```

The sync rules per frame:
- If the Kotlin variable changed since last frame → pushes to native (Kotlin wins).
- Otherwise if FTXUI changed the native value → pulls to Kotlin.

> **Kotlin/Native limitation:** `::` references to *local variables* (variables declared inside
> a function or lambda body) are [not yet supported](https://youtrack.jetbrains.com/issue/KT-15360)
> in Kotlin/Native. If you use the property reference syntax, the state variables must be **top-level** or **class member** properties.

## Memory lifecycle

- `StringState` uses a C++ string allocation; free with `free()`.
- `BoolState`, `IntState`, `FloatState` use `nativeHeap`; free with `free()`.
- When a component created with the property-ref overload is destroyed, it frees the native buffer automatically.
- When state is instantiated via `AppScope` extension functions (`boolState()`, `intState()`, etc.), it is automatically registered and freed by its Cleaner when the app loop exits.
- When you pass manual `BoolState`/`IntState`/etc. objects to a component, you own them — call `free()` yourself after destroying the component.

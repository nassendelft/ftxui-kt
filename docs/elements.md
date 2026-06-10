# Elements

Elements are non-interactive, renderable pieces of UI. They are created fresh each frame inside a `renderer` callback. All functions return `Element`.

## Text

```kotlin
text("Hello world")          // single line
vtext("vertical")            // rotated 90° — renders character by character downward
paragraph("Long text here")  // wraps at terminal width
paragraphAlignLeft("text")
paragraphAlignRight("text")
paragraphAlignCenter("text")
paragraphAlignJustify("text")
```

## Styled text

`styledText` composes inline runs of text with independent per-span styling. Multiple spans are automatically combined into a horizontal element.

```kotlin
styledText {
    span("Hello") { bold(); color(Color.Red) }
    span(", ")
    span("World!") { italic(); color(Color.Blue) }
}
```

Available style functions inside a `span` block:

```kotlin
span("text") {
    bold()
    italic()
    dim()
    inverted()           // swap fg/bg
    underlined()
    underlinedDouble()
    blink()
    strikethrough()
    color(Color.Green)   // foreground
    bgcolor(Color.Black) // background
}
```

A single span returns the element directly without wrapping. An empty `styledText {}` produces an empty text element.

## Gauge (progress bar)

```kotlin
gauge(0.75f)                          // horizontal, left→right, 0.0–1.0
gaugeLeft(0.5f)                       // right→left
gaugeRight(0.5f)                      // left→right (same as gauge)
gaugeUp(0.5f)                         // bottom→top
gaugeDown(0.5f)                       // top→bottom
gaugeDirection(0.5f, Direction.Up)    // explicit direction
```

## Separators

```kotlin
separator()                           // default (light) line
separatorLight()
separatorDashed()
separatorHeavy()
separatorDouble()
separatorEmpty()                      // invisible spacer
separatorStyled(BorderStyle.Rounded)
separatorCharacter("─")              // custom character

// Selection-aware separators (for scrollable lists)
separatorHSelector(left, right, unselectedColor, selectedColor)
separatorVSelector(up, down, unselectedColor, selectedColor)
```

## Layout boxes

Elements compose via box functions. All accept `vararg elements: Element`.

```kotlin
vbox(elem1, elem2, elem3)    // stack vertically
hbox(elem1, elem2, elem3)    // arrange horizontally
dbox(elem1, elem2)           // depth stack — elements overlap (later = on top)
hflow(elem1, elem2, elem3)   // horizontal flow with wrapping
vflow(elem1, elem2, elem3)   // vertical flow with wrapping
```

## Filler and empty

```kotlin
filler()        // expands to fill available space (use with flex layout)
emptyElement()  // zero-size placeholder
```

## Spinner (animated)

```kotlin
spinner(charsetIndex, imageIndex)
// charsetIndex selects the animation set (0–7)
// imageIndex is the current animation frame — increment each render tick
```

## GridBox

Lays out a 2D grid of elements. Each inner list is a row.

```kotlin
val rows = listOf(
    listOf(text("A"), text("B")),
    listOf(text("C"), text("D")),
)
gridbox(rows)
```

## Common pattern

```kotlin
renderer(myComponent) {
    vbox(
        text("Title").bold().hcenter(),
        separator(),
        hbox(
            text("Left").flex(),
            separatorLight(),
            text("Right").flex(),
        ),
        gauge(progress).flex(),
    ).border()
}
```

## Element DSL pattern

Alternatively, using the Element DSL scope functions, you can write a cleaner nested hierarchy without commas:

```kotlin
renderer(myComponent) {
    vbox {
        text("Title") { bold().hcenter() }
        separator()
        hbox {
            text("Left") { flex() }
            separatorLight()
            text("Right") { flex() }
        }
        gauge(progress) { flex() }
    }.border()
}
```


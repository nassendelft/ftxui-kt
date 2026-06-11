# Decorators

Decorators are extension functions on `Element` (and some on `Component`) that wrap the target in a new styled element. They chain fluently.

Both `Element` and `Component` have parallel decorator sets. The `Component` decorators wrap the component's render output — **note:** component wrapping discards focus/event forwarding, so only apply them to non-interactive or leaf components.

## Borders

```kotlin
element.border()                          // default (light)
element.borderLight()
element.borderDashed()
element.borderHeavy()
element.borderDouble()
element.borderRounded()
element.borderEmpty()
element.borderStyled(BorderStyle.Heavy)
element.borderStyled(BorderStyle.Light, Color.Red)   // style + color
element.borderStyled(Color.Blue)                     // color only
element.window(titleElement)                         // titled border box
```

## Text styles

```kotlin
element.bold()
element.dim()
element.italic()
element.underlined()
element.underlinedDouble()
element.blink()
element.inverted()             // swap fg/bg
element.strikethrough()
element.hyperlink("https://example.com")
element.automerge()            // merge identical adjacent cells
element.clearUnder()           // erase cells under this element
element.nothing()              // identity / no-op decorator
```

## Color

```kotlin
element.color(Color.Red)               // foreground
element.bgcolor(Color.Blue)            // background
element.colorLinearGradient(gradient)  // gradient foreground
element.bgcolorLinearGradient(gradient)
```

## Alignment

```kotlin
element.hcenter()     // center horizontally
element.vcenter()     // center vertically
element.center()      // center both axes
element.alignRight()
```

## Flex and size

```kotlin
element.flex()         // fill available space in both axes
element.xflex()        // flex horizontally only
element.yflex()        // flex vertically only
element.flexGrow()     // grow but don't shrink
element.flexShrink()   // shrink but don't grow
element.xflexGrow()
element.xflexShrink()
element.yflexGrow()
element.yflexShrink()
element.notflex()      // opt out of flex sizing

element.size(WidthOrHeight.Width, Constraint.GreaterThan, 30)
element.size(WidthOrHeight.Height, Constraint.Equal, 5)
// Constraints: LessThan, GreaterThan, Equal
```

## Scroll frames

```kotlin
element.frame()           // scrollable viewport (both axes)
element.xframe()          // horizontal scroll only
element.yframe()          // vertical scroll only
element.vscrollIndicator()
element.hscrollIndicator()
```

## Focus and cursor

```kotlin
element.focus()                         // mark as focused in scroll parent
element.focusPosition(x, y)             // focus at absolute cell
element.focusPositionRelative(0.5f, 0f) // focus at relative position

// Cursor styles (for input fields):
element.focusCursorBlock()
element.focusCursorBlockBlinking()
element.focusCursorBar()
element.focusCursorBarBlinking()
element.focusCursorUnderline()
element.focusCursorUnderlineBlinking()
```

## Layout measurement (reflect)

`Box` is a mutable rectangle that the layout writes into on every render. Wrap an element with `reflect(box)` to record where it ended up and how much space it was given — the building block for virtualized lists that need to know their viewport size, or for hit-testing.

```kotlin
val box = Box()

renderer {
    // Stretch the element so the recorded box reports the slot size
    // rather than the content's natural size:
    vbox(/* visible rows only */).flex().reflect(box)
}

// After a frame has been rendered:
box.height                              // rows assigned by the layout
box.width                               // columns assigned by the layout
box.xMin; box.xMax; box.yMin; box.yMax  // inclusive coordinates
```

`width`/`height` are `0` until the element has been rendered once. Layout runs after your render callback, so the values describe the *previous* frame; when a slot can change size (terminal resize, panels appearing), re-render once to converge on the new measurement.

Keep the `Box` reachable (e.g. captured in the component's render lambda) for as long as elements reflecting into it are rendered — the native box must outlive them.

## Selection styling

```kotlin
element.selectionStyleReset()
element.selectionColor(color)
element.selectionBgColor(color)
element.selectionFgColor(color)
```

## Component decorators

Same operations available on `Component`. Transfer ownership — destroy only the returned component.

```kotlin
component.border()
component.borderRounded()
component.bold()
component.color(Color.Cyan)
component.bgcolor(Color.Default)
component.hcenter()
component.flex()
component.size(WidthOrHeight.Width, Constraint.GreaterThan, 40)
component.frame()
component.vscrollIndicator()
component.inverted()
component.dim()
component.hoverable(hoverState)   // sets BoolState when mouse hovers
component.nothing()
```

## Chaining example

```kotlin
text("Hello")
    .bold()
    .color(Color.Green)
    .hcenter()
    .size(WidthOrHeight.Width, Constraint.GreaterThan, 20)
    .border()
```

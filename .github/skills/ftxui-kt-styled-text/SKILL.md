---
name: ftxui-kt-styled-text
description: styledText { span(...) } DSL for composing inline text with per-span styling — colors, bold, italic, underline, and more.
license: MIT
compatibility: opencode
---

`styledText` builds an inline sequence of styled text spans. Each `span` is an independent run of text with its own decorators. Multiple spans are combined into a horizontal box automatically.

## Basic usage

```kotlin
styledText {
    span("Hello") { bold(); color(Color.Red) }
    span(", ")
    span("World!") { italic(); color(Color.Blue) }
}
```

## Available style functions

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
    color(Color.Green)   // foreground color
    bgcolor(Color.Black) // background color
}
```

Multiple calls in one block combine — `bold()` then `color(...)` applies both.

## Using inside layout builders

```kotlin
vbox {
    +styledText {
        span("Status: ")
        span("OK") { bold(); color(Color.Green) }
    }
    +styledText {
        span("Error: ") { color(Color.Red) }
        span(errorMessage) { dim() }
    }
}
```

## Single span

A single `span` returns the element directly without wrapping in an `hbox`:

```kotlin
val label = styledText { span("Warning") { bold(); color(Color.Yellow) } }
```

## Notes

- `styledText {}` (empty) produces an empty text element.
- `SpanStyle` and `SpanScope` are DSL-internal types; only `styledText` and `span` are part of the public API.
- For whole-element styling (not per-span), use decorator extensions directly: `text("Hello").bold().color(Color.Red)`.

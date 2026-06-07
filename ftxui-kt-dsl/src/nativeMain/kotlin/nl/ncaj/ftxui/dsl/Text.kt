package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.*

class SpanStyle internal constructor(element: Element) {
    internal var element: Element = element
        private set

    fun bold() { element = element.bold() }
    fun italic() { element = element.italic() }
    fun dim() { element = element.dim() }
    fun inverted() { element = element.inverted() }
    fun underlined() { element = element.underlined() }
    fun underlinedDouble() { element = element.underlinedDouble() }
    fun blink() { element = element.blink() }
    fun strikethrough() { element = element.strikethrough() }
    fun color(color: Color) { element = element.color(color) }
    fun bgcolor(color: Color) { element = element.bgcolor(color) }
}

class SpanScope {
    internal val spans = mutableListOf<Element>()

    fun span(text: String) {
        spans.add(nl.ncaj.ftxui.text(text))
    }

    fun span(text: String, style: SpanStyle.() -> Unit) {
        spans.add(SpanStyle(nl.ncaj.ftxui.text(text)).apply(style).element)
    }
}

fun styledText(block: SpanScope.() -> Unit): Element {
    val spans = SpanScope().apply(block).spans
    return when (spans.size) {
        0 -> nl.ncaj.ftxui.text("")
        1 -> spans[0]
        else -> nl.ncaj.ftxui.hbox(*spans.toTypedArray())
    }
}

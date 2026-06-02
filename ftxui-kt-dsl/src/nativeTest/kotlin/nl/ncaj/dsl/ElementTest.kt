package nl.ncaj.dsl

import nl.ncaj.*
import kotlin.test.Test

class ElementTest {

    @Test
    fun vboxBuilderCreatesElement() {
        vbox {
            +text("Line 1")
            +text("Line 2")
            +separator()
        }
    }

    @Test
    fun hboxBuilderCreatesElement() {
        hbox {
            +text("A")
            +separator()
            +text("B")
        }
    }

    @Test
    fun dboxBuilderCreatesElement() {
        dbox {
            +filler()
            +text("overlay")
        }
    }

    @Test
    fun hflowAndVflowWork() {
        hflow {
            +text("one")
            +text("two")
            +text("three")
        }
        vflow {
            +text("one")
            +text("two")
        }
    }

    @Test
    fun emptyBuilderWorks() {
        vbox {}
        hbox {}
        dbox {}
    }

    @Test
    fun elementDecoratorsChain() {
        vbox {
            +text("bold").bold()
            +text("colored").color(Color.Red)
            +text("dim").dim()
            +text("bordered").border()
            +text("centered").center()
            +gauge(0.75).size(WidthOrHeight.Width, Constraint.Equal, 20)
        }
    }

    @Test
    fun nestedBoxBuilders() {
        vbox {
            +hbox {
                +text("col1")
                +text("col2")
            }
            +separator()
            +hbox {
                +filler()
                +text("right").alignRight()
            }
        }
    }
}

class StyledTextTest {

    @Test
    fun styledTextSingleSpan() {
        styledText { span("hello") { bold() } }
    }

    @Test
    fun styledTextMultipleSpans() {
        styledText {
            span("Hello") { bold(); color(Color.Red) }
            span(", ")
            span("World!") { italic(); color(Color.Blue) }
        }
    }

    @Test
    fun styledTextUnstyled() {
        styledText { span("plain") }
    }

    @Test
    fun styledTextEmptyProducesEmptyElement() {
        styledText {}
    }

    @Test
    fun styledTextAllDecorators() {
        styledText {
            span("full") {
                bold()
                italic()
                dim()
                inverted()
                underlined()
                underlinedDouble()
                blink()
                strikethrough()
                color(Color.Green)
                bgcolor(Color.Black)
            }
        }
    }
}

package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.*
import kotlin.test.Test

class ContainerTest {

    @Test
    fun verticalBuilderCreatesComponent() {
        vertical {
            +button("A", onClick = {})
            +button("B", onClick = {})
        }
    }

    @Test
    fun horizontalBuilderCreatesComponent() {
        horizontal {
            +button("Left", onClick = {})
            +button("Right", onClick = {})
        }
    }

    @Test
    fun stackedBuilderCreatesComponent() {
        stacked {
            +renderer { text("background") }
            +renderer { text("foreground") }
        }
    }

    @Test
    fun emptyContainersWork() {
        vertical {}
        horizontal {}
        stacked {}
    }

    @Test
    fun nestedContainersWork() {
        vertical {
            +horizontal {
                +button("X", onClick = {})
                +button("Y", onClick = {})
            }
            +renderer { text("status") }
        }
    }

    @Test
    fun containerWithSingleChild() {
        vertical {
            +renderer { text("only child") }
        }
    }
}

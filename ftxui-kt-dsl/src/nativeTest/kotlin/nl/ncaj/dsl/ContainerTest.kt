package nl.ncaj.dsl

import nl.ncaj.*
import kotlin.test.Test

class ContainerTest {

    @Test
    fun verticalBuilderCreatesComponent() {
        vertical {
            +button("A", onClick = {})
            +button("B", onClick = {})
        }.close()
    }

    @Test
    fun horizontalBuilderCreatesComponent() {
        horizontal {
            +button("Left", onClick = {})
            +button("Right", onClick = {})
        }.close()
    }

    @Test
    fun stackedBuilderCreatesComponent() {
        stacked {
            +renderer { text("background") }
            +renderer { text("foreground") }
        }.close()
    }

    @Test
    fun emptyContainersWork() {
        vertical {}.close()
        horizontal {}.close()
        stacked {}.close()
    }

    @Test
    fun nestedContainersWork() {
        vertical {
            +horizontal {
                +button("X", onClick = {})
                +button("Y", onClick = {})
            }
            +renderer { text("status") }
        }.close()
    }

    @Test
    fun containerWithSingleChild() {
        vertical {
            +renderer { text("only child") }
        }.close()
    }
}

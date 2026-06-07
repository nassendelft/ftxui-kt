package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// testApp creates an AppScope without running app.loop(), so tests don't require a TTY.
// This verifies component creation and resource tracking.
// State *sync* (native→Kotlin var round-trip) requires a real render cycle and is not tested here.
private fun testApp(block: AppScope.() -> Component) {
    val app = FtxUIApp.terminalOutput()
    val scope = AppScope(app)
    try {
        scope.block().close()
    } finally {
        app.close()
        scope.closeAll()
    }
}

// State vars are class members because Kotlin/Native does not support KMutableProperty0
// references to local variables (KT-15360).
class AppTest {
    private var checked = false
    private var selected = 0
    private var inputText = ""
    private var floatValue = 0.5f
    private var tabIndex = 1
    private var menuHorizSel = 0
    private var passwordText = "secret"

    @Test
    fun appScopeBlockBodyExecutes() {
        var ran = false
        testApp {
            ran = true
            renderer { text("ok") }
        }
        assertTrue(ran)
    }

    @Test
    fun checkboxCreatesComponent() {
        checked = true
        testApp { checkbox("flag", ::checked) }
        assertEquals(true, checked)
    }

    @Test
    fun menuCreatesComponent() {
        selected = 2
        testApp { menu(listOf("a", "b", "c"), ::selected) }
        assertEquals(2, selected)
    }

    @Test
    fun inputCreatesComponent() {
        inputText = "hello"
        testApp { input(::inputText) }
        assertEquals("hello", inputText)
    }

    @Test
    fun inputWithPlaceholderCreatesComponent() {
        testApp { input(::inputText, "type here…") }
    }

    @Test
    fun inputPasswordCreatesComponent() {
        passwordText = "secret"
        testApp { inputPassword(::passwordText) }
        assertEquals("secret", passwordText)
    }

    @Test
    fun floatSliderCreatesComponent() {
        floatValue = 0.5f
        testApp { slider("vol", ::floatValue, 0f, 1f) }
        assertEquals(0.5f, floatValue)
    }

    @Test
    fun floatSliderWithDirectionCreatesComponent() {
        testApp { slider(::floatValue, 0f, 1f, direction = Direction.Up) }
    }

    @Test
    fun tabContainerCreatesComponent() {
        tabIndex = 1
        testApp {
            tab(::tabIndex) {
                +renderer { text("Tab 1") }
                +renderer { text("Tab 2") }
                +renderer { text("Tab 3") }
            }
        }
        assertEquals(1, tabIndex)
    }

    @Test
    fun menuHorizontalCreatesComponent() {
        menuHorizSel = 0
        testApp { menuHorizontal(listOf("X", "Y", "Z"), ::menuHorizSel) }
        assertEquals(0, menuHorizSel)
    }

    @Test
    fun multipleComponentsInContainer() {
        checked = false
        selected = 0
        inputText = ""

        testApp {
            vertical {
                +checkbox("A", ::checked)
                +menu(listOf("x", "y"), ::selected)
                +input(::inputText, "placeholder")
            }
        }

        assertEquals(false, checked)
        assertEquals(0, selected)
        assertEquals("", inputText)
    }

    @Test
    fun rendererWithChildWorks() {
        testApp {
            val child = button("ok", onClick = {})
            renderer(child = child) {
                vbox {
                    +child.render()
                    +text("footer")
                }
            }
        }
    }

    @Test
    fun catchEventWorks() {
        testApp {
            renderer { text("listening") }
                .catchEvent { false }
        }
    }

    @Test
    fun collapsibleCreatesComponent() {
        checked = false
        testApp {
            collapsible("Section", renderer { text("content") }, ::checked)
        }
    }

    @Test
    fun maybeCreatesComponent() {
        checked = true
        testApp {
            maybe(renderer { text("visible") }, ::checked)
        }
    }

    @Test
    fun modalCreatesComponent() {
        checked = false
        testApp {
            val main = renderer { text("main") }
            val overlay = renderer { text("modal") }
            modal(main, overlay, ::checked)
        }
    }

    @Test
    fun resizableSplitCreatesComponent() {
        selected = 30
        testApp {
            resizableSplitLeft(
                renderer { text("left") },
                renderer { text("right") },
                ::selected,
            )
        }
        assertEquals(30, selected)
    }

    @Test
    fun pollCreatesComponent() {
        testApp {
            vertical {
                +poll {}
                +renderer { text("content") }
            }
        }
    }
}

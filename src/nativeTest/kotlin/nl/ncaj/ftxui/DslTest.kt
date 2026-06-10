package nl.ncaj.ftxui

import kotlin.test.Test

private fun testApp(block: AppScope.() -> Component) {
    val app = FtxUIApp.terminalOutput()
    val scope = AppScope(app)
    scope.block()
}

class DslTest {
    private var checked = false
    private var selected = 0
    private var inputText = ""
    private var floatValue = 0.5f

    @Test
    fun testContainersAndComponents() {
        testApp {
            vertical {
                button("Click Me") {
                    println("Clicked")
                }
                checkbox("Enable Feature", ::checked)
                input(::inputText, "Enter name")
                toggle(listOf("Option A", "Option B"), ::selected)
                slider("Volume", ::floatValue, 0f, 1f)

                horizontal {
                    button("Ok") {}
                    button("Cancel") {}
                }

                collapsible("Collapsible", ::checked) { button("Inside collapsible") {} }
                maybe(::checked) { button("Inside maybe") {} }
                modal(::checked, { button("Main modal") {} }, { button("Modal window") {} })
                resizableSplitLeft(::selected, { button("Split Main") {} }, { button("Split Back") {} })
                tab(::selected) {
                    button("Tab Child") {}
                }
            }
        }
    }
    
    @Test
    fun testStateScopeExtensions() {
        testApp {
            val checked = boolState(true)
            val selected = intState(1)
            val text = stringState("hello")
            val percentage = floatState(0.5f)

            vertical {
                checkbox("Checked", checked)
                toggle(listOf("A", "B"), selected)
                input(text, "type")
                slider("Percentage", percentage, 0f, 1f)
            }
        }
    }

    @Test
    fun testElementsAndStyles() {
        vbox {
            text("Hello World") { bold().color(Color.Red) }
            separator()
            hbox {
                text("Left") { dim() }
                filler()
                text("Right") { inverted() }
            }
            paragraphAlignLeft("This is a paragraph of text aligned left.")
            styledText {
                span("Styled") { bold(); color(Color.Blue) }
                span(" text")
            }
            canvas(20, 10) {
                drawText(0, 0, "Canvas")
            }
            table(listOf(listOf("Header 1", "Header 2"), listOf("Val 1", "Val 2"))) {
                selectAll { border() }
            }
        }
    }
}

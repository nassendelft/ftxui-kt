package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.*
import kotlin.test.Test

private fun testApp(block: AppScope.() -> Component) {
    val app = FtxUIApp.terminalOutput()
    val scope = AppScope(app)
    scope.block()
    // app, scope and the created component are freed by their Cleaners after GC.
}

class AdvancedTest {

    @Test
    fun canvasCreatesElement() {
        testApp {
            renderer {
                canvas(40, 20) {
                    drawText(0, 0, "Hello Canvas")
                    drawPointCircle(20, 10, 5)
                    drawBlockLine(0, 0, 39, 19)
                }
            }
        }
    }

    @Test
    fun canvasWithColors() {
        testApp {
            renderer {
                canvas(20, 10) {
                    drawText(0, 0, "Red", Color.Red)
                    drawPointOn(10, 5)
                    drawBlockCircle(10, 5, 3, Color.Blue)
                }
            }
        }
    }

    @Test
    fun canvasCellStyleWorks() {
        testApp {
            renderer {
                canvas(10, 10) {
                    style(2, 2) { cell ->
                        cell.bold = true
                        cell.foregroundColor = Color.Yellow
                    }
                }
            }
        }
    }

    @Test
    fun tableCreatesElement() {
        testApp {
            renderer {
                table(listOf(
                    listOf("Name", "Score", "Grade"),
                    listOf("Alice", "95", "A"),
                    listOf("Bob", "82", "B"),
                    listOf("Carol", "78", "C"),
                )) {
                    selectAll { border() }
                    selectRow(0) { decorateBold() }
                    selectColumn(1) { decorateCellsAlignRight() }
                }
            }
        }
    }

    @Test
    fun tableWithColoredRows() {
        testApp {
            renderer {
                table(listOf(
                    listOf("Key", "Value"),
                    listOf("alpha", "1"),
                    listOf("beta", "2"),
                )) {
                    selectAll { border() }
                    selectRows(1, 2) { decorateCellsColorAlternateRow(Color.GrayDark, 2, 0) }
                    selectRow(0) { decorateBold().decorateCellsColor(Color.Blue) }
                }
            }
        }
    }

    @Test
    fun tableSelectionsAllVariants() {
        testApp {
            renderer {
                table(listOf(
                    listOf("A", "B", "C"),
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                )) {
                    selectAll { separator() }
                    selectColumn(0) { borderLeft() }
                    selectColumn(2) { borderRight() }
                    selectColumns(0, 2) { separatorVertical() }
                    selectRow(0) { borderTop().decorateBold() }
                    selectCell(1, 1) { decorate { it.bold() } }
                    selectRectangle(0, 2, 0, 1) { border() }
                }
            }
        }
    }

    @Test
    fun graphFnCreatesElement() {
        testApp {
            val fn = graphFn { w, h, out ->
                repeat(w) { i -> out[i] = (h * i / w) }
            }
            renderer { graph(fn) }
        }
    }

    @Test
    fun linearGradientCreatesElement() {
        testApp {
            renderer {
                val grad = linearGradient {
                    stop(Color.Red)
                    stop(Color.Blue)
                }
                text("gradient").colorLinearGradient(grad)
            }
        }
    }

    @Test
    fun linearGradientWithAngleCreatesElement() {
        testApp {
            renderer {
                val grad = linearGradient {
                    angle(45f)
                    stop(Color.Cyan)
                    stop(Color.Magenta)
                }
                text("angled").bgcolorLinearGradient(grad)
            }
        }
    }

    @Test
    fun multipleScopedResourcesTracked() {
        testApp {
            val grad1 = linearGradient { stop(Color.Red); stop(Color.White) }
            val grad2 = linearGradient { stop(Color.Blue); stop(Color.Green) }
            val fn = graphFn { w, h, out -> repeat(w) { out[it] = h / 2 } }

            renderer {
                vbox {
                    +text("g1").colorLinearGradient(grad1)
                    +text("g2").colorLinearGradient(grad2)
                    +graph(fn)
                }
            }
        }
    }
}

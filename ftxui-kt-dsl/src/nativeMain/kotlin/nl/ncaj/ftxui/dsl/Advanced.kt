package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.*

// Canvas: created and drawn inside the lambda; the element is returned.
// The Canvas is tracked in AppScope and freed after the app loop exits.
fun AppScope.canvas(width: Int, height: Int, block: Canvas.() -> Unit): Element =
    track(Canvas(width, height)).let { c -> c.block(); c.toElement() }

// LinearGradient: tracked in AppScope; built via fluent API inside the lambda.
fun AppScope.linearGradient(block: LinearGradient.() -> Unit): LinearGradient =
    track(LinearGradient()).apply(block)

// TableScope hides TableSelection lifecycle; all selections are tracked and freed with the scope.
class TableScope internal constructor(
    private val table: Table,
    private val scope: AppScope,
) {
    fun selectAll(block: TableSelection.() -> Unit) =
        scope.track(table.selectAll()).apply(block)

    fun selectRow(row: Int, block: TableSelection.() -> Unit) =
        scope.track(table.selectRow(row)).apply(block)

    fun selectRows(from: Int, to: Int, block: TableSelection.() -> Unit) =
        scope.track(table.selectRows(from, to)).apply(block)

    fun selectColumn(col: Int, block: TableSelection.() -> Unit) =
        scope.track(table.selectColumn(col)).apply(block)

    fun selectColumns(from: Int, to: Int, block: TableSelection.() -> Unit) =
        scope.track(table.selectColumns(from, to)).apply(block)

    fun selectCell(col: Int, row: Int, block: TableSelection.() -> Unit) =
        scope.track(table.selectCell(col, row)).apply(block)

    fun selectRectangle(colMin: Int, colMax: Int, rowMin: Int, rowMax: Int, block: TableSelection.() -> Unit) =
        scope.track(table.selectRectangle(colMin, colMax, rowMin, rowMax)).apply(block)

    fun render(): Element = table.render()
}

fun AppScope.table(rows: List<List<String>>, block: TableScope.() -> Unit): Element {
    val t = track(Table(rows))
    TableScope(t, this).block()
    return t.render()
}

// GraphFn: callback wrapper tracked in AppScope and freed after the loop exits.
fun AppScope.graphFn(fn: (width: Int, height: Int, output: IntArray) -> Unit): GraphFn =
    track(GraphFn(fn))

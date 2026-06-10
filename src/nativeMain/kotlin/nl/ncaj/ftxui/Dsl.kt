package nl.ncaj.ftxui

import kotlin.reflect.KMutableProperty0

@DslMarker
annotation class FtxuiDsl

// Canvas: created and drawn inside the lambda; the element is returned.
// The Canvas resources are managed via Cleaner and freed after render copy.
fun canvas(width: Int, height: Int, block: Canvas.() -> Unit): Element =
    Canvas(width, height).let { c -> c.block(); c.toElement() }

// LinearGradient: managed via Cleaner; built via fluent API inside the lambda.
fun linearGradient(block: LinearGradient.() -> Unit): LinearGradient =
    LinearGradient().apply(block)

// TableScope hides TableSelection lifecycle.
@FtxuiDsl
class TableScope internal constructor(
    private val table: Table,
) {
    fun selectAll(block: TableSelection.() -> Unit) =
        table.selectAll().apply(block)

    fun selectRow(row: Int, block: TableSelection.() -> Unit) =
        table.selectRow(row).apply(block)

    fun selectRows(from: Int, to: Int, block: TableSelection.() -> Unit) =
        table.selectRows(from, to).apply(block)

    fun selectColumn(col: Int, block: TableSelection.() -> Unit) =
        table.selectColumn(col).apply(block)

    fun selectColumns(from: Int, to: Int, block: TableSelection.() -> Unit) =
        table.selectColumns(from, to).apply(block)

    fun selectCell(col: Int, row: Int, block: TableSelection.() -> Unit) =
        table.selectCell(col, row).apply(block)

    fun selectRectangle(colMin: Int, colMax: Int, rowMin: Int, rowMax: Int, block: TableSelection.() -> Unit) =
        table.selectRectangle(colMin, colMax, rowMin, rowMax).apply(block)

    fun render(): Element = table.render()
}

fun table(rows: List<List<String>>, block: TableScope.() -> Unit): Element {
    val t = Table(rows)
    TableScope(t).block()
    return t.render()
}

// GraphFn: callback wrapper.
fun graphFn(fn: (width: Int, height: Int, output: IntArray) -> Unit): GraphFn =
    GraphFn(fn)

@FtxuiDsl
class AppScope internal constructor(internal val app: FtxUIApp) {
    internal val states = mutableListOf<Any>()

    fun exit() = app.exit()
    fun post(block: () -> Unit) = app.post(block)
    fun poll(onPoll: () -> Unit): Component = poll(app, onPoll)
    fun requestAnimationFrame() = app.requestAnimationFrame()
    fun selectionChange(callback: () -> Unit) = app.selectionChange(callback)
    fun getSelection(): String = app.getSelection()
    fun trackMouse(enable: Boolean = true) = app.trackMouse(enable)
    fun forceHandleCtrlC(force: Boolean = true) = app.forceHandleCtrlC(force)
    fun forceHandleCtrlZ(force: Boolean = true) = app.forceHandleCtrlZ(force)
}

fun AppScope.boolState(initial: Boolean = false): BoolState =
    BoolState(initial).also { states.add(it) }

fun AppScope.intState(initial: Int = 0): IntState =
    IntState(initial).also { states.add(it) }

fun AppScope.stringState(initial: String = ""): StringState =
    StringState(initial).also { states.add(it) }

fun AppScope.floatState(initial: Float = 0f): FloatState =
    FloatState(initial).also { states.add(it) }


fun fullscreenApp(block: AppScope.() -> Component) =
    runApp(FtxUIApp.fullscreen(), block)

fun fullscreenPrimaryScreenApp(block: AppScope.() -> Component) =
    runApp(FtxUIApp.fullscreenPrimaryScreen(), block)

fun fullscreenAlternateScreenApp(block: AppScope.() -> Component) =
    runApp(FtxUIApp.fullscreenAlternateScreen(), block)

fun fitComponentApp(block: AppScope.() -> Component) =
    runApp(FtxUIApp.fitComponent(), block)

fun terminalOutputApp(block: AppScope.() -> Component) =
    runApp(FtxUIApp.terminalOutput(), block)

fun fixedSizeApp(dimx: Int, dimy: Int, block: AppScope.() -> Component) =
    runApp(FtxUIApp.fixedSize(dimx, dimy), block)

private fun runApp(ftxuiApp: FtxUIApp, block: AppScope.() -> Component) {
    val scope = AppScope(ftxuiApp)
    val root = scope.block()
    // scope, root and ftxuiApp remain reachable as stack roots for the duration of the
    // blocking loop. Once this function returns they become collectable and their native
    // handles/buffers are released by their Cleaners.
    ftxuiApp.loop(root)
}

@FtxuiDsl
class ContainerScope {
    internal val children = mutableListOf<Component>()
    operator fun Component.unaryPlus() { children.add(this) }
    fun add(component: Component) { children.add(component) }
    fun <T : Component> T.add(): T = this.also { children.add(it) }
}

fun vertical(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { vertical(*it.toTypedArray()) }

fun horizontal(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { horizontal(*it.toTypedArray()) }

fun stacked(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { children ->
        stacked().also { c -> children.forEach(c::add) }
    }

fun tab(initial: Int, selected: KMutableProperty0<Int>, block: ContainerScope.() -> Unit): Component =
    intStateSynced(initial,selected) { state ->
        val container = tab(state)
        ContainerScope().apply(block).children.forEach(container::add)
        container
    }

fun tab(selected: KMutableProperty0<Int>, block: ContainerScope.() -> Unit): Component =
    tab(selected.get(), selected, block)

fun tab(selected: IntState, block: ContainerScope.() -> Unit): Component =
    tab(selected.ptr).also { container ->
        ContainerScope().apply(block).children.forEach(container::add)
    }

fun maybe(show: KMutableProperty0<Boolean>, child: () -> Component): Component =
    maybe(child(), show)

fun maybe(show: BoolState, child: () -> Component): Component =
    maybe(child(), show)

fun collapsible(label: String, show: KMutableProperty0<Boolean>, child: () -> Component): Component =
    collapsible(label, child(), show)

fun collapsible(label: String, show: BoolState, child: () -> Component): Component =
    collapsible(label, child(), show)

fun modal(
    showModal: KMutableProperty0<Boolean>,
    main: () -> Component,
    modal: () -> Component
): Component = modal(main(), modal(), showModal)

fun modal(
    showModal: BoolState,
    main: () -> Component,
    modal: () -> Component
): Component = modal(main(), modal(), showModal)

fun resizableSplitLeft(
    mainSize: KMutableProperty0<Int>,
    main: () -> Component,
    back: () -> Component
): Component = resizableSplitLeft(main(), back(), mainSize)

fun resizableSplitLeft(
    mainSize: IntState,
    main: () -> Component,
    back: () -> Component
): Component = resizableSplitLeft(main(), back(), mainSize)

fun resizableSplitRight(
    mainSize: KMutableProperty0<Int>,
    main: () -> Component,
    back: () -> Component
): Component = resizableSplitRight(main(), back(), mainSize)

fun resizableSplitRight(
    mainSize: IntState,
    main: () -> Component,
    back: () -> Component
): Component = resizableSplitRight(main(), back(), mainSize)

fun resizableSplitTop(
    mainSize: KMutableProperty0<Int>,
    main: () -> Component,
    back: () -> Component
): Component = resizableSplitTop(main(), back(), mainSize)

fun resizableSplitTop(
    mainSize: IntState,
    main: () -> Component,
    back: () -> Component
): Component = resizableSplitTop(main(), back(), mainSize)

fun resizableSplitBottom(
    mainSize: KMutableProperty0<Int>,
    main: () -> Component,
    back: () -> Component
): Component = resizableSplitBottom(main(), back(), mainSize)

fun resizableSplitBottom(
    mainSize: IntState,
    main: () -> Component,
    back: () -> Component
): Component = resizableSplitBottom(main(), back(), mainSize)

// -- ContainerScope extensions to add specific components automatically
fun ContainerScope.button(label: String, onClick: () -> Unit): Component =
    nl.ncaj.ftxui.button(label, onClick).add()

fun ContainerScope.button(label: String, options: ButtonOption, onClick: () -> Unit): Component =
    nl.ncaj.ftxui.button(label, options, onClick).add()

fun ContainerScope.checkbox(label: String, checked: BoolState): Component =
    nl.ncaj.ftxui.checkbox(label, checked).add()

fun ContainerScope.checkbox(label: String, checked: BoolState, onChange: () -> Unit): Component =
    nl.ncaj.ftxui.checkbox(label, checked, onChange).add()

fun ContainerScope.checkbox(label: String, checked: KMutableProperty0<Boolean>): Component =
    nl.ncaj.ftxui.checkbox(label, checked).add()

fun ContainerScope.checkbox(label: String, checked: KMutableProperty0<Boolean>, onChange: () -> Unit): Component =
    nl.ncaj.ftxui.checkbox(label, checked, onChange).add()

fun ContainerScope.toggle(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    nl.ncaj.ftxui.toggle(entries, selected).add()

fun ContainerScope.slider(label: String, value: KMutableProperty0<Int>, min: Int, max: Int, increment: Int = 1): Component =
    nl.ncaj.ftxui.slider(label, value, min, max, increment).add()

fun ContainerScope.slider(value: KMutableProperty0<Int>, min: Int, max: Int, increment: Int = 1, direction: Direction): Component =
    nl.ncaj.ftxui.slider(value, min, max, increment, direction).add()

fun ContainerScope.slider(value: KMutableProperty0<Int>, min: Int, max: Int, increment: Int = 1, onChange: () -> Unit): Component =
    nl.ncaj.ftxui.slider(value, min, max, increment, onChange).add()

fun ContainerScope.slider(label: String, value: KMutableProperty0<Float>, min: Float, max: Float, increment: Float = 1f): Component =
    nl.ncaj.ftxui.slider(label, value, min, max, increment).add()

fun ContainerScope.slider(value: KMutableProperty0<Float>, min: Float, max: Float, increment: Float = 1f, onChange: () -> Unit): Component =
    nl.ncaj.ftxui.slider(value, min, max, increment, onChange).add()

fun ContainerScope.slider(value: KMutableProperty0<Float>, min: Float, max: Float, increment: Float = 1f, direction: Direction, colorActive: Color? = null, colorInactive: Color? = null): Component =
    nl.ncaj.ftxui.slider(value, min, max, increment, direction, colorActive, colorInactive).add()

fun ContainerScope.slider(value: IntState, min: Int, max: Int, increment: Int = 1, onChange: () -> Unit): Component =
    nl.ncaj.ftxui.slider(value, min, max, increment, onChange).add()

fun ContainerScope.slider(value: FloatState, min: Float, max: Float, increment: Float = 1f, onChange: () -> Unit): Component =
    nl.ncaj.ftxui.slider(value, min, max, increment, onChange).add()

fun ContainerScope.radiobox(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    nl.ncaj.ftxui.radiobox(entries, selected).add()

fun ContainerScope.radiobox(entries: List<String>, selected: KMutableProperty0<Int>, onChange: () -> Unit): Component =
    nl.ncaj.ftxui.radiobox(entries, selected, onChange).add()

fun ContainerScope.radiobox(entries: List<String>, selected: IntState, onChange: () -> Unit): Component =
    nl.ncaj.ftxui.radiobox(entries, selected, onChange).add()

fun ContainerScope.menu(entries: List<String>, selected: IntState): Component =
    nl.ncaj.ftxui.menu(entries, selected).add()

fun ContainerScope.menu(entries: List<String>, selected: IntState, onChange: (() -> Unit)? = null, onEnter: (() -> Unit)? = null): Component =
    nl.ncaj.ftxui.menu(entries, selected, onChange, onEnter).add()

fun ContainerScope.menu(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    nl.ncaj.ftxui.menu(entries, selected).add()

fun ContainerScope.menu(entries: List<String>, selected: KMutableProperty0<Int>, onChange: (() -> Unit)? = null, onEnter: (() -> Unit)? = null): Component =
    nl.ncaj.ftxui.menu(entries, selected, onChange, onEnter).add()

fun ContainerScope.menuHorizontal(entries: List<String>, selected: IntState): Component =
    nl.ncaj.ftxui.menuHorizontal(entries, selected).add()

fun ContainerScope.menuHorizontal(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    nl.ncaj.ftxui.menuHorizontal(entries, selected).add()

fun ContainerScope.menuHorizontalAnimated(entries: List<String>, selected: IntState): Component =
    nl.ncaj.ftxui.menuHorizontalAnimated(entries, selected).add()

fun ContainerScope.menuHorizontalAnimated(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    nl.ncaj.ftxui.menuHorizontalAnimated(entries, selected).add()

fun ContainerScope.menuToggle(entries: List<String>, selected: IntState): Component =
    nl.ncaj.ftxui.menuToggle(entries, selected).add()

fun ContainerScope.menuToggle(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    nl.ncaj.ftxui.menuToggle(entries, selected).add()

fun ContainerScope.menuEntry(label: String): Component =
    nl.ncaj.ftxui.menuEntry(label).add()

fun ContainerScope.menuEntry(label: String, animatedColors: AnimatedMenuEntryColors): Component =
    nl.ncaj.ftxui.menuEntry(label, animatedColors).add()

fun ContainerScope.input(content: StringState, placeholder: String = ""): Component =
    nl.ncaj.ftxui.input(content, placeholder).add()

fun ContainerScope.input(content: KMutableProperty0<String>, placeholder: String = ""): Component =
    nl.ncaj.ftxui.input(content, placeholder).add()

fun ContainerScope.inputPassword(content: KMutableProperty0<String>, placeholder: String = ""): Component =
    nl.ncaj.ftxui.inputPassword(content, placeholder).add()

fun ContainerScope.dropdown(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    nl.ncaj.ftxui.dropdown(entries, selected).add()

fun ContainerScope.collapsible(label: String, show: KMutableProperty0<Boolean>, child: () -> Component): Component =
    nl.ncaj.ftxui.collapsible(label, show, child).add()

fun ContainerScope.collapsible(label: String, show: BoolState, child: () -> Component): Component =
    nl.ncaj.ftxui.collapsible(label, show, child).add()

fun ContainerScope.maybe(show: KMutableProperty0<Boolean>, child: () -> Component): Component =
    nl.ncaj.ftxui.maybe(show, child).add()

fun ContainerScope.maybe(show: BoolState, child: () -> Component): Component =
    nl.ncaj.ftxui.maybe(show, child).add()

fun ContainerScope.modal(showModal: KMutableProperty0<Boolean>, main: () -> Component, modal: () -> Component): Component =
    nl.ncaj.ftxui.modal(showModal, main, modal).add()

fun ContainerScope.modal(showModal: BoolState, main: () -> Component, modal: () -> Component): Component =
    nl.ncaj.ftxui.modal(showModal, main, modal).add()

fun ContainerScope.resizableSplitLeft(mainSize: KMutableProperty0<Int>, main: () -> Component, back: () -> Component): Component =
    nl.ncaj.ftxui.resizableSplitLeft(mainSize, main, back).add()

fun ContainerScope.resizableSplitLeft(mainSize: IntState, main: () -> Component, back: () -> Component): Component =
    nl.ncaj.ftxui.resizableSplitLeft(mainSize, main, back).add()

fun ContainerScope.resizableSplitRight(mainSize: KMutableProperty0<Int>, main: () -> Component, back: () -> Component): Component =
    nl.ncaj.ftxui.resizableSplitRight(mainSize, main, back).add()

fun ContainerScope.resizableSplitRight(mainSize: IntState, main: () -> Component, back: () -> Component): Component =
    nl.ncaj.ftxui.resizableSplitRight(mainSize, main, back).add()

fun ContainerScope.resizableSplitTop(mainSize: KMutableProperty0<Int>, main: () -> Component, back: () -> Component): Component =
    nl.ncaj.ftxui.resizableSplitTop(mainSize, main, back).add()

fun ContainerScope.resizableSplitTop(mainSize: IntState, main: () -> Component, back: () -> Component): Component =
    nl.ncaj.ftxui.resizableSplitTop(mainSize, main, back).add()

fun ContainerScope.resizableSplitBottom(mainSize: KMutableProperty0<Int>, main: () -> Component, back: () -> Component): Component =
    nl.ncaj.ftxui.resizableSplitBottom(mainSize, main, back).add()

fun ContainerScope.resizableSplitBottom(mainSize: IntState, main: () -> Component, back: () -> Component): Component =
    nl.ncaj.ftxui.resizableSplitBottom(mainSize, main, back).add()

fun ContainerScope.tab(selected: KMutableProperty0<Int>, block: ContainerScope.() -> Unit): Component =
    nl.ncaj.ftxui.tab(selected, block).add()

fun ContainerScope.tab(selected: IntState, block: ContainerScope.() -> Unit): Component =
    nl.ncaj.ftxui.tab(selected, block).add()

fun ContainerScope.tab(initial: Int, selected: KMutableProperty0<Int>, block: ContainerScope.() -> Unit): Component =
    nl.ncaj.ftxui.tab(initial, selected, block).add()

fun ContainerScope.renderer(block: () -> Element): Component =
    renderer(child = null, callback = block).add()

fun ContainerScope.renderer(child: Component, block: () -> Element): Component =
    nl.ncaj.ftxui.renderer(child, block).add()

fun ContainerScope.vertical(block: ContainerScope.() -> Unit): ContainerComponent =
    nl.ncaj.ftxui.vertical(block).add()

fun ContainerScope.horizontal(block: ContainerScope.() -> Unit): ContainerComponent =
    nl.ncaj.ftxui.horizontal(block).add()

fun ContainerScope.stacked(block: ContainerScope.() -> Unit): ContainerComponent =
    nl.ncaj.ftxui.stacked(block).add()


@FtxuiDsl
class ElementScope {
    internal val elements = mutableListOf<Element>()
    operator fun Element.unaryPlus() { elements.add(this) }
    fun add(element: Element): Element = element.also { elements.add(it) }
    fun Element.add(): Element = this.also { elements.add(this) }
}

fun vbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { vbox(*it.toTypedArray()) }

fun hbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { hbox(*it.toTypedArray()) }

fun dbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { dbox(*it.toTypedArray()) }

fun hflow(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { hflow(*it.toTypedArray()) }

fun vflow(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { vflow(*it.toTypedArray()) }

// -- ElementScope extensions to add specific elements and optionally decorate/style them
fun ElementScope.text(text: String, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.text(text)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.vbox(style: (Element.() -> Element)? = null, block: ElementScope.() -> Unit): Element {
    val el = nl.ncaj.ftxui.vbox(block)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.hbox(style: (Element.() -> Element)? = null, block: ElementScope.() -> Unit): Element {
    val el = nl.ncaj.ftxui.hbox(block)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.dbox(style: (Element.() -> Element)? = null, block: ElementScope.() -> Unit): Element {
    val el = nl.ncaj.ftxui.dbox(block)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.hflow(style: (Element.() -> Element)? = null, block: ElementScope.() -> Unit): Element {
    val el = nl.ncaj.ftxui.hflow(block)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.vflow(style: (Element.() -> Element)? = null, block: ElementScope.() -> Unit): Element {
    val el = nl.ncaj.ftxui.vflow(block)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.separator(style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.separator()
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.separatorLight(style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.separatorLight()
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.separatorDashed(style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.separatorDashed()
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.separatorHeavy(style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.separatorHeavy()
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.separatorDouble(style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.separatorDouble()
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.separatorEmpty(style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui. separatorEmpty()
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.separatorStyled(borderStyle: BorderStyle, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.separatorStyled(borderStyle)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.filler(style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.filler()
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.gauge(progress: Double, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.gauge(progress)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.gaugeDirection(progress: Double, direction: Direction, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.gaugeDirection(progress, direction)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.paragraph(text: String, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.paragraph(text)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.paragraphAlignLeft(text: String, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.paragraphAlignLeft(text)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.paragraphAlignRight(text: String, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.paragraphAlignRight(text)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.paragraphAlignCenter(text: String, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.paragraphAlignCenter(text)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.paragraphAlignJustify(text: String, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.paragraphAlignJustify(text)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.canvas(width: Int, height: Int, style: (Element.() -> Element)? = null, block: Canvas.() -> Unit): Element {
    val el = canvas(width, height, block)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.table(rows: List<List<String>>, style: (Element.() -> Element)? = null, block: TableScope.() -> Unit): Element {
    val el = nl.ncaj.ftxui.table(rows, block)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.graph(fn: GraphFn, style: (Element.() -> Element)? = null): Element {
    val el = nl.ncaj.ftxui.graph(fn)
    return (style?.invoke(el) ?: el).add()
}

fun ElementScope.styledText(style: (Element.() -> Element)? = null, block: SpanScope.() -> Unit): Element {
    val el = nl.ncaj.ftxui.styledText(block)
    return (style?.invoke(el) ?: el).add()
}


@FtxuiDsl
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

@FtxuiDsl
class SpanScope {
    internal val spans = mutableListOf<Element>()

    fun span(text: String) {
        spans.add(text(text))
    }

    fun span(text: String, style: SpanStyle.() -> Unit) {
        spans.add(SpanStyle(text(text)).apply(style).element)
    }
}

fun styledText(block: SpanScope.() -> Unit): Element {
    val spans = SpanScope().apply(block).spans
    return when (spans.size) {
        0 -> text("")
        1 -> spans[0]
        else -> hbox(*spans.toTypedArray())
    }
}
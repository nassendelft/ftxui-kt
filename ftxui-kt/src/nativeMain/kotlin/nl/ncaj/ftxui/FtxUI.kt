@file:OptIn(ExperimentalForeignApi::class)

package nl.ncaj.ftxui

import ftxui_c.*
import kotlinx.cinterop.*
import kotlin.reflect.KMutableProperty0

internal typealias ComponentHandle = ftxui_component_handle_t
internal typealias ElementHandle = ftxui_element_handle_t

class Color internal constructor(internal val handle: ftxui_color_handle_t?) : AutoCloseable {
    companion object {
        val Black = Color(ftxui_color_palette16(FTXUI_PALETTE16_BLACK))
        val Red = Color(ftxui_color_palette16(FTXUI_PALETTE16_RED))
        val Green = Color(ftxui_color_palette16(FTXUI_PALETTE16_GREEN))
        val Yellow = Color(ftxui_color_palette16(FTXUI_PALETTE16_YELLOW))
        val Blue = Color(ftxui_color_palette16(FTXUI_PALETTE16_BLUE))
        val Magenta = Color(ftxui_color_palette16(FTXUI_PALETTE16_MAGENTA))
        val Cyan = Color(ftxui_color_palette16(FTXUI_PALETTE16_CYAN))
        val White = Color(ftxui_color_palette16(FTXUI_PALETTE16_WHITE))
        val Default = Color(ftxui_color_default())
        val GrayLight = Color(ftxui_color_palette16(FTXUI_PALETTE16_GRAY_LIGHT))
        val GrayDark = Color(ftxui_color_palette16(FTXUI_PALETTE16_GRAY_DARK))
        val RedLight = Color(ftxui_color_palette16(FTXUI_PALETTE16_RED_LIGHT))
        val GreenLight = Color(ftxui_color_palette16(FTXUI_PALETTE16_GREEN_LIGHT))
        val YellowLight = Color(ftxui_color_palette16(FTXUI_PALETTE16_YELLOW_LIGHT))
        val BlueLight = Color(ftxui_color_palette16(FTXUI_PALETTE16_BLUE_LIGHT))
        val MagentaLight = Color(ftxui_color_palette16(FTXUI_PALETTE16_MAGENTA_LIGHT))
        val CyanLight = Color(ftxui_color_palette16(FTXUI_PALETTE16_CYAN_LIGHT))

        fun rgb(r: UByte, g: UByte, b: UByte) = Color(ftxui_color_rgb(r, g, b))
        fun rgba(r: UByte, g: UByte, b: UByte, a: UByte) = Color(ftxui_color_rgba(r, g, b, a))
        fun hsv(h: UByte, s: UByte, v: UByte) = Color(ftxui_color_hsv(h, s, v))
        fun hsva(h: UByte, s: UByte, v: UByte, a: UByte) = Color(ftxui_color_hsva(h, s, v, a))
        fun palette1(index: ftxui_palette1_t) = Color(ftxui_color_palette1(index))
        fun palette16(index: ftxui_palette16_t) = Color(ftxui_color_palette16(index))
        fun palette256(index: ftxui_palette256_t) = Color(ftxui_color_palette256(index))
        fun palette256(index: Int) = Color(ftxui_color_palette256_raw(index))

        fun interpolate(ratio: Float, colorA: Color, colorB: Color) =
            Color(ftxui_color_interpolate(ratio, colorA.handle, colorB.handle))

        fun blend(lhs: Color, rhs: Color) =
            Color(ftxui_color_blend(lhs.handle, rhs.handle))
    }

    fun isOpaque() = ftxui_color_is_opaque(handle)

    override fun equals(other: Any?): Boolean {
        if (other !is Color) return false
        return ftxui_color_equals(handle, other.handle)
    }

    override fun hashCode() = handle.hashCode()

    fun print(isBackground: Boolean): String =
        ftxui_color_print(handle, isBackground)?.toKString() ?: ""

    fun destroy() = ftxui_color_destroy(handle)
    override fun close() = destroy()
}

enum class BorderStyle(internal val value: ftxui_border_style_t) {
    Light(ftxui_border_style_t.FTXUI_BORDER_STYLE_LIGHT),
    Dashed(ftxui_border_style_t.FTXUI_BORDER_STYLE_DASHED),
    Heavy(ftxui_border_style_t.FTXUI_BORDER_STYLE_HEAVY),
    Double(ftxui_border_style_t.FTXUI_BORDER_STYLE_DOUBLE),
    Rounded(ftxui_border_style_t.FTXUI_BORDER_STYLE_ROUNDED),
    Empty(ftxui_border_style_t.FTXUI_BORDER_STYLE_EMPTY);
}

enum class WidthOrHeight(internal val value: ftxui_width_or_height_t) {
    Width(ftxui_width_or_height_t.FTXUI_WIDTH_OR_HEIGHT_WIDTH),
    Height(ftxui_width_or_height_t.FTXUI_WIDTH_OR_HEIGHT_HEIGHT);
}

enum class Constraint(internal val value: ftxui_constraint_t) {
    LessThan(ftxui_constraint_t.FTXUI_CONSTRAINT_LESS_THAN),
    GreaterThan(ftxui_constraint_t.FTXUI_CONSTRAINT_GREATER_THAN),
    Equal(ftxui_constraint_t.FTXUI_CONSTRAINT_EQUAL),
}

enum class Direction(internal val value: ftxui_direction_t) {
    Up(ftxui_direction_t.FTXUI_DIRECTION_UP),
    Down(ftxui_direction_t.FTXUI_DIRECTION_DOWN),
    Left(ftxui_direction_t.FTXUI_DIRECTION_LEFT),
    Right(ftxui_direction_t.FTXUI_DIRECTION_RIGHT),
}

data class EntryState(
    val label: String,
    val state: Boolean,
    val active: Boolean,
    val focused: Boolean,
    val index: Int
)

// cleanups run in registration order when destroy() is called.
open class Component internal constructor(internal val handle: ComponentHandle) : AutoCloseable {
    internal val cleanups = mutableListOf<() -> Unit>()

    fun destroy() {
        cleanups.forEach { it() }
        cleanups.clear()
        ftxui_component_destroy(handle)
    }

    override fun close() = destroy()
}

// add() transfers ownership: the container's destroy() will also destroy the child handle.
// Do not call destroy() on a child after adding it to a container.
class ContainerComponent internal constructor(handle: ComponentHandle) : Component(handle) {
    fun add(component: Component) {
        ftxui_container_add(handle, component.handle)
        cleanups.addAll(component.cleanups)
        cleanups.add { ftxui_component_destroy(component.handle) }
        component.cleanups.clear()
    }
}

class Element internal constructor(internal val handle: ElementHandle)

fun Element.destroy() = ftxui_element_destroy(handle)

data class Dimensions(val dimx: Int, val dimy: Int)

enum class TerminalColor(internal val value: UInt) {
    Palette1(FTXUI_TERMINAL_COLOR_PALETTE1),
    Palette16(FTXUI_TERMINAL_COLOR_PALETTE16),
    Palette256(FTXUI_TERMINAL_COLOR_PALETTE256),
    TrueColor(FTXUI_TERMINAL_COLOR_TRUE_COLOR),
}

data class Quirks(
    val blockCharacters: Boolean = false,
    val cursorHiding: Boolean = true,
    val componentAscii: Boolean = false,
    val colorSupport: TerminalColor = TerminalColor.TrueColor,
)

object Terminal {
    fun size(): Dimensions = Dimensions(ftxui_terminal_width(), ftxui_terminal_height())
    fun setFallbackSize(width: Int, height: Int) = ftxui_terminal_set_fallback_size(width, height)
    fun colorSupport(): TerminalColor =
        TerminalColor.entries.first { it.value == ftxui_terminal_color_support() }
    fun setColorSupport(color: TerminalColor) = ftxui_terminal_set_color_support(color.value)
    fun getQuirks(): Quirks = ftxui_terminal_get_quirks().useContents {
        Quirks(
            blockCharacters = block_characters,
            cursorHiding = cursor_hiding,
            componentAscii = component_ascii,
            colorSupport = TerminalColor.entries.first { it.value == color_support },
        )
    }
    fun setQuirks(quirks: Quirks) = ftxui_terminal_set_quirks(cValue<ftxui_quirks_t> {
        block_characters = quirks.blockCharacters
        cursor_hiding = quirks.cursorHiding
        component_ascii = quirks.componentAscii
        color_support = quirks.colorSupport.value
    })
}

class FtxUIApp internal constructor(internal val handle: ftxui_app_handle_t) : AutoCloseable {
    internal val cleanups = mutableListOf<() -> Unit>()

    fun loop(root: Component) = ftxui_app_loop(handle, root.handle)

    fun exit() = ftxui_app_exit(handle)

    fun destroy() {
        cleanups.forEach { it() }
        cleanups.clear()
        ftxui_app_destroy(handle)
    }

    override fun close() = destroy()

    fun trackMouse(enable: Boolean = true) = ftxui_app_track_mouse(handle, enable)
    fun handlePipedInput(enable: Boolean = true) = ftxui_app_handle_piped_input(handle, enable)
    /**
     * When [force] is true (the default), FTXUI always raises SIGINT on Ctrl-C,
     * even if a component's `onEvent` handles the event. Pass false to let your
     * own handling of the Ctrl-C event take effect instead.
     */
    fun forceHandleCtrlC(force: Boolean = true) = ftxui_app_force_handle_ctrl_c(handle, force)

    /** Same as [forceHandleCtrlC] but for Ctrl-Z (raises SIGTSTP). */
    fun forceHandleCtrlZ(force: Boolean = true) = ftxui_app_force_handle_ctrl_z(handle, force)

    fun post(closure: () -> Unit) {
        val stableRef = StableRef.create(closure)
        val cb = staticCFunction { refPtr: COpaquePointer? ->
            val fn = refPtr!!.asStableRef<() -> Unit>().get()
            fn()
            refPtr.asStableRef<() -> Unit>().dispose()
        }
        ftxui_app_post(handle, cb, stableRef.asCPointer())
    }

    fun withRestoredIO(closure: () -> Unit) {
        val stableRef = StableRef.create(closure)
        val cb = staticCFunction { refPtr: COpaquePointer? ->
            refPtr!!.asStableRef<() -> Unit>().get()()
        }
        ftxui_app_with_restored_io(handle, cb, stableRef.asCPointer())
        stableRef.dispose()
    }

    fun selectionChange(callback: () -> Unit) {
        val stableRef = StableRef.create(callback)
        val c = staticCFunction { refPtr: COpaquePointer? ->
            refPtr!!.asStableRef<() -> Unit>().get()()
        }
        ftxui_app_selection_change(handle, c, stableRef.asCPointer())
        cleanups.add { stableRef.dispose() }
    }

    fun getSelection(): String {
        val ptr = ftxui_app_get_selection(handle) ?: return ""
        val result = ptr.toKString()
        platform.posix.free(ptr)
        return result
    }

    fun terminalName(): String = ftxui_app_terminal_name(handle)?.toKString() ?: ""
    fun terminalVersion(): Int = ftxui_app_terminal_version(handle)
    fun terminalEmulatorName(): String = ftxui_app_terminal_emulator_name(handle)?.toKString() ?: ""
    fun terminalEmulatorVersion(): String = ftxui_app_terminal_emulator_version(handle)?.toKString() ?: ""
    fun terminalCapabilities(): List<Int> = memScoped {
        val count = alloc<IntVar>()
        val ptr = ftxui_app_terminal_capabilities(handle, count.ptr) ?: return emptyList()
        (0 until count.value).map { ptr[it] }
    }

    companion object {
        fun fullscreen() = FtxUIApp(ftxui_app_create_fullscreen()!!)
        fun fullscreenPrimaryScreen() = FtxUIApp(ftxui_app_create_fullscreen_primary_screen()!!)
        fun fullscreenAlternateScreen() = FtxUIApp(ftxui_app_create_fullscreen_alternate_screen()!!)
        fun fitComponent() = FtxUIApp(ftxui_app_create_fit_component()!!)
        fun terminalOutput() = FtxUIApp(ftxui_app_create_terminal_output()!!)
        fun fixedSize(dimx: Int, dimy: Int) = FtxUIApp(ftxui_app_create_fixed_size(dimx, dimy)!!)
        fun active(): FtxUIApp? = ftxui_app_active()?.let { FtxUIApp(it) }
    }
}

// Transfers cleanups from this component into a new wrapper component, and schedules
// destruction of this component's handle when the wrapper is destroyed.
// After calling this, only use and destroy the returned component.
private fun Component.wrapOwning(newHandle: ComponentHandle): Component {
    val new = Component(newHandle)
    new.cleanups.addAll(cleanups)
    new.cleanups.add { ftxui_component_destroy(handle) }
    cleanups.clear()
    return new
}

// Same as wrapOwning but takes ownership of two source components.
private fun wrapOwning(a: Component, b: Component, newHandle: ComponentHandle): Component {
    val new = a.wrapOwning(newHandle)
    new.cleanups.addAll(b.cleanups)
    new.cleanups.add { ftxui_component_destroy(b.handle) }
    b.cleanups.clear()
    return new
}

// -- Element decorators

fun Element.border() = Element(ftxui_element_border(this.handle)!!)
fun Element.borderLight() = Element(ftxui_element_border_light(this.handle)!!)
fun Element.borderDashed() = Element(ftxui_element_border_dashed(this.handle)!!)
fun Element.borderHeavy() = Element(ftxui_element_border_heavy(this.handle)!!)
fun Element.borderDouble() = Element(ftxui_element_border_double(this.handle)!!)
fun Element.borderRounded() = Element(ftxui_element_border_rounded(this.handle)!!)
fun Element.borderEmpty() = Element(ftxui_element_border_empty(this.handle)!!)

fun Element.flex() = Element(ftxui_element_flex(this.handle)!!)
fun Element.color(color: Color) = Element(ftxui_element_color(this.handle, color.handle)!!)
fun Element.bgcolor(color: Color) = Element(ftxui_element_bgcolor(this.handle, color.handle)!!)
fun Element.bold() = Element(ftxui_element_bold(this.handle)!!)
fun Element.inverted() = Element(ftxui_element_inverted(this.handle)!!)
fun Element.underlined() = Element(ftxui_element_underlined(this.handle)!!)
fun Element.dim() = Element(ftxui_element_dim(this.handle)!!)
fun Element.blink() = Element(ftxui_element_blink(this.handle)!!)
fun Element.strikethrough() = Element(ftxui_element_strikethrough(this.handle)!!)
fun Element.window(title: Element) = Element(ftxui_element_window(title.handle, this.handle)!!)
fun Element.vscrollIndicator() = Element(ftxui_element_vscroll_indicator(this.handle)!!)
fun Element.frame() = Element(ftxui_element_frame(this.handle)!!)
fun Element.size(widthOrHeight: WidthOrHeight, constraint: Constraint, value: Int) =
    Element(ftxui_element_set_size(this.handle, widthOrHeight.value, constraint.value, value)!!)
fun Element.hcenter() = Element(ftxui_element_hcenter(this.handle)!!)
fun Element.vcenter() = Element(ftxui_element_vcenter(this.handle)!!)
fun Element.center() = Element(ftxui_element_center(this.handle)!!)
fun Element.alignRight() = Element(ftxui_element_align_right(this.handle)!!)
fun Element.nothing() = Element(ftxui_element_nothing(this.handle)!!)

fun Element.italic() = Element(ftxui_element_italic(this.handle)!!)
fun Element.underlinedDouble() = Element(ftxui_element_underlined_double(this.handle)!!)
fun Element.automerge() = Element(ftxui_element_automerge(this.handle)!!)
fun Element.hyperlink(link: String) = Element(ftxui_element_hyperlink(link, this.handle)!!)
fun Element.hscrollIndicator() = Element(ftxui_element_hscroll_indicator(this.handle)!!)
fun Element.clearUnder() = Element(ftxui_element_clear_under(this.handle)!!)
fun Element.borderStyled(style: BorderStyle) = Element(ftxui_element_border_styled(this.handle, style.value)!!)
fun Element.borderStyled(style: BorderStyle, color: Color) = Element(ftxui_element_border_styled_color(this.handle, style.value, color.handle)!!)
fun Element.borderStyled(color: Color) = Element(ftxui_element_border_colored(this.handle, color.handle)!!)
// Mutable view of a terminal cell's style. Passed to selectionStyle / Canvas.style lambdas.
// foregroundColor/backgroundColor are temporary handles owned by the C library — do NOT call destroy() on them.
class CellStyleView internal constructor(internal val ptr: CPointer<ftxui_cell_t>) {
    var blink: Boolean get() = ptr.pointed.blink; set(v) { ptr.pointed.blink = v }
    var bold: Boolean get() = ptr.pointed.bold; set(v) { ptr.pointed.bold = v }
    var dim: Boolean get() = ptr.pointed.dim; set(v) { ptr.pointed.dim = v }
    var italic: Boolean get() = ptr.pointed.italic; set(v) { ptr.pointed.italic = v }
    var inverted: Boolean get() = ptr.pointed.inverted; set(v) { ptr.pointed.inverted = v }
    var underlined: Boolean get() = ptr.pointed.underlined; set(v) { ptr.pointed.underlined = v }
    var underlinedDouble: Boolean get() = ptr.pointed.underlined_double; set(v) { ptr.pointed.underlined_double = v }
    var strikethrough: Boolean get() = ptr.pointed.strikethrough; set(v) { ptr.pointed.strikethrough = v }
    var automerge: Boolean get() = ptr.pointed.automerge; set(v) { ptr.pointed.automerge = v }
    var foregroundColor: Color?
        get() = ptr.pointed.foreground_color?.let { Color(it) }
        set(v) { ptr.pointed.foreground_color = v?.handle }
    var backgroundColor: Color?
        get() = ptr.pointed.background_color?.let { Color(it) }
        set(v) { ptr.pointed.background_color = v?.handle }
}

private val cellStyleBridge = staticCFunction<CPointer<ftxui_cell_t>?, COpaquePointer?, Unit> { cell, refPtr ->
    refPtr?.asStableRef<(CellStyleView) -> Unit>()?.get()?.invoke(CellStyleView(cell!!))
}

fun Element.selectionStyleReset() = Element(ftxui_element_selection_style_reset(this.handle)!!)
fun Element.selectionColor(color: Color) = Element(ftxui_element_selection_color(this.handle, color.handle)!!)
fun Element.selectionBgColor(color: Color) = Element(ftxui_element_selection_background_color(this.handle, color.handle)!!)
fun Element.selectionFgColor(color: Color) = Element(ftxui_element_selection_foreground_color(this.handle, color.handle)!!)
// The StableRef is not tracked because Element lifetimes are managed by the C++ renderer.
// The lambda stays alive for the lifetime of the rendered element tree on the C++ side.
fun Element.selectionStyle(style: (CellStyleView) -> Unit): Element {
    val ref = StableRef.create(style)
    return Element(ftxui_element_selection_style(this.handle, cellStyleBridge, ref.asCPointer())!!)
}
fun Element.focusPosition(x: Int, y: Int) = Element(ftxui_element_focus_position(this.handle, x, y)!!)
fun Element.focusPositionRelative(x: Float, y: Float) = Element(ftxui_element_focus_position_relative(this.handle, x, y)!!)

fun Element.flexGrow() = Element(ftxui_element_flex_grow(this.handle)!!)
fun Element.flexShrink() = Element(ftxui_element_flex_shrink(this.handle)!!)
fun Element.xflex() = Element(ftxui_element_xflex(this.handle)!!)
fun Element.xflexGrow() = Element(ftxui_element_xflex_grow(this.handle)!!)
fun Element.xflexShrink() = Element(ftxui_element_xflex_shrink(this.handle)!!)
fun Element.yflex() = Element(ftxui_element_yflex(this.handle)!!)
fun Element.yflexGrow() = Element(ftxui_element_yflex_grow(this.handle)!!)
fun Element.yflexShrink() = Element(ftxui_element_yflex_shrink(this.handle)!!)
fun Element.notflex() = Element(ftxui_element_notflex(this.handle)!!)
fun Element.xframe() = Element(ftxui_element_xframe(this.handle)!!)
fun Element.yframe() = Element(ftxui_element_yframe(this.handle)!!)
fun Element.focus() = Element(ftxui_element_focus(this.handle)!!)
fun Element.focusCursorBlock() = Element(ftxui_element_focus_cursor_block(this.handle)!!)
fun Element.focusCursorBlockBlinking() = Element(ftxui_element_focus_cursor_block_blinking(this.handle)!!)
fun Element.focusCursorBar() = Element(ftxui_element_focus_cursor_bar(this.handle)!!)
fun Element.focusCursorBarBlinking() = Element(ftxui_element_focus_cursor_bar_blinking(this.handle)!!)
fun Element.focusCursorUnderline() = Element(ftxui_element_focus_cursor_underline(this.handle)!!)
fun Element.focusCursorUnderlineBlinking() = Element(ftxui_element_focus_cursor_underline_blinking(this.handle)!!)

// -- Elements

fun text(text: String) = Element(ftxui_element_text(text)!!)

fun gauge(value: Double) = Element(ftxui_element_gauge(value)!!)

fun separator() = Element(ftxui_element_separator()!!)
fun separatorLight() = Element(ftxui_element_separator_light()!!)
fun separatorDashed() = Element(ftxui_element_separator_dashed()!!)
fun separatorHeavy() = Element(ftxui_element_separator_heavy()!!)
fun separatorDouble() = Element(ftxui_element_separator_double()!!)
fun separatorEmpty() = Element(ftxui_element_separator_empty()!!)
fun separatorStyled(style: BorderStyle) = Element(ftxui_element_separator_styled(style.value)!!)
fun separatorCharacter(character: String) = Element(ftxui_element_separator_character(character)!!)

fun separatorHSelector(
    left: Float,
    right: Float,
    unselectedColor: Color = Color.Default,
    selectedColor: Color = Color.Default
) = Element(ftxui_element_separator_hselector(left, right, unselectedColor.handle, selectedColor.handle)!!)

fun separatorVSelector(
    up: Float,
    down: Float,
    unselectedColor: Color = Color.Default,
    selectedColor: Color = Color.Default
) = Element(ftxui_element_separator_vselector(up, down, unselectedColor.handle, selectedColor.handle)!!)

fun vbox(vararg elements: Element): Element = memScoped {
    val array = allocArray<ftxui_element_handle_tVar>(elements.size)
    elements.forEachIndexed { index, element -> array[index] = element.handle }
    Element(ftxui_element_vbox(array, elements.size)!!)
}

fun hbox(vararg elements: Element): Element = memScoped {
    val array = allocArray<ftxui_element_handle_tVar>(elements.size)
    elements.forEachIndexed { index, element -> array[index] = element.handle }
    Element(ftxui_element_hbox(array, elements.size)!!)
}

fun vtext(text: String) = Element(ftxui_element_vtext(text)!!)
fun spinner(charsetIndex: Int, imageIndex: Int) = Element(ftxui_element_spinner(charsetIndex, imageIndex)!!)
fun paragraph(text: String) = Element(ftxui_element_paragraph(text)!!)
fun paragraphAlignLeft(text: String) = Element(ftxui_element_paragraph_align_left(text)!!)
fun paragraphAlignRight(text: String) = Element(ftxui_element_paragraph_align_right(text)!!)
fun paragraphAlignCenter(text: String) = Element(ftxui_element_paragraph_align_center(text)!!)
fun paragraphAlignJustify(text: String) = Element(ftxui_element_paragraph_align_justify(text)!!)
fun emptyElement() = Element(ftxui_element_empty()!!)
fun filler() = Element(ftxui_element_filler()!!)
fun gaugeLeft(value: Double) = Element(ftxui_element_gauge_left(value)!!)
fun gaugeRight(value: Double) = Element(ftxui_element_gauge_right(value)!!)
fun gaugeUp(value: Double) = Element(ftxui_element_gauge_up(value)!!)
fun gaugeDown(value: Double) = Element(ftxui_element_gauge_down(value)!!)
fun gaugeDirection(value: Double, direction: Direction) = Element(ftxui_element_gauge_direction(value, direction.value)!!)
fun dbox(vararg elements: Element): Element = memScoped {
    val array = allocArray<ftxui_element_handle_tVar>(elements.size)
    elements.forEachIndexed { index, element -> array[index] = element.handle }
    Element(ftxui_element_dbox(array, elements.size)!!)
}
fun hflow(vararg elements: Element): Element = memScoped {
    val array = allocArray<ftxui_element_handle_tVar>(elements.size)
    elements.forEachIndexed { index, element -> array[index] = element.handle }
    Element(ftxui_element_hflow(array, elements.size)!!)
}
fun vflow(vararg elements: Element): Element = memScoped {
    val array = allocArray<ftxui_element_handle_tVar>(elements.size)
    elements.forEachIndexed { index, element -> array[index] = element.handle }
    Element(ftxui_element_vflow(array, elements.size)!!)
}

// -- Components

// ownedColors holds Color objects created internally (e.g. interpolated) that must be
// freed after the button component is constructed (C++ copies them at that point).
class ButtonOption private constructor(internal val handle: CValue<ftxui_button_option_t>) {
    var transform: ((EntryState) -> Element)? = null
    internal val ownedColors = mutableListOf<Color>()

    companion object {
        fun simple() = ButtonOption(ftxui_button_option_simple())
        fun ascii() = ButtonOption(ftxui_button_option_ascii())
        fun border() = ButtonOption(ftxui_button_option_border())
        fun animated() = ButtonOption(
            ftxui_button_option_animated(
                Color.Black.handle,
                Color.GrayLight.handle,
                Color.GrayDark.handle,
                Color.White.handle
            )
        )

        fun animated(color: Color): ButtonOption {
            val bg = Color.interpolate(0.85f, color, Color.Black)
            val fg = Color.interpolate(0.10f, color, Color.White)
            val bgActive = Color.interpolate(0.10f, color, Color.Black)
            val fgActive = Color.interpolate(0.85f, color, Color.White)
            return ButtonOption(ftxui_button_option_animated(bg.handle, fg.handle, bgActive.handle, fgActive.handle))
                .also { it.ownedColors.addAll(listOf(bg, fg, bgActive, fgActive)) }
        }

        fun animated(background: Color, foreground: Color) = ButtonOption(
            ftxui_button_option_animated(
                background.handle,
                foreground.handle,
                background.handle,
                foreground.handle,
            )
        )

        fun animated(
            background: Color,
            foreground: Color,
            backgroundActive: Color,
            foregroundActive: Color
        ) = ButtonOption(
            ftxui_button_option_animated(
                background.handle,
                foreground.handle,
                backgroundActive.handle,
                foregroundActive.handle,
            )
        )
    }
}

fun button(
    label: String,
    onClick: () -> Unit,
    options: ButtonOption = ButtonOption.simple()
): Component {
    val clickStableRef = StableRef.create(onClick)
    val callback = staticCFunction { refPtr: COpaquePointer? ->
        refPtr?.asStableRef<() -> Unit>()?.get()?.invoke()
        Unit
    }

    var transformStableRef: StableRef<(EntryState) -> Element>? = null
    val transform = options.transform
    if (transform != null) {
        val tsr = StableRef.create(transform)
        transformStableRef = tsr
        options.handle.useContents {
            this.transform = staticCFunction { state: CValue<ftxui_entry_state_t>, refPtr: COpaquePointer? ->
                state.useContents {
                    val block = refPtr!!.asStableRef<(EntryState) -> Element>().get()
                    val entryState = EntryState(
                        label = this.label?.toKString() ?: "",
                        state = this.state,
                        active = this.active,
                        focused = this.focused,
                        index = this.index
                    )
                    block(entryState).handle
                }
            }
            this.transform_userdata = tsr.asCPointer()
        }
    }

    val handle = ftxui_component_button_with_options(label, callback, clickStableRef.asCPointer(), options.handle)
    // C++ has copied the color values out of the handles; safe to free them now.
    options.ownedColors.forEach { it.destroy() }

    return Component(handle!!).also { c ->
        c.cleanups.add { clickStableRef.dispose() }
        transformStableRef?.let { ref -> c.cleanups.add { ref.dispose() } }
    }
}

// horizontal/vertical delegate to ContainerComponent.add(), which takes ownership of each child.
fun horizontal(vararg components: Component): ContainerComponent {
    val container = ContainerComponent(ftxui_component_container_horizontal()!!)
    for (component in components) container.add(component)
    return container
}

fun vertical(vararg components: Component): ContainerComponent {
    val container = ContainerComponent(ftxui_component_container_vertical()!!)
    for (component in components) container.add(component)
    return container
}

fun renderer(
    child: Component? = null,
    callback: () -> Element
): Component {
    val stableRef = StableRef.create(callback)
    @Suppress("UNCHECKED_CAST")
    val renderCallback = staticCFunction { refPtr: COpaquePointer? ->
        val block = refPtr!!.asStableRef<() -> Element>().get()
        block().handle
    } as ftxui_render_callback_t
    val handle = ftxui_component_renderer(child?.handle, renderCallback, stableRef.asCPointer())!!
    return (child?.wrapOwning(handle) ?: Component(handle)).also {
        it.cleanups.add { stableRef.dispose() }
    }
}

fun focusableRenderer(callback: (focused: Boolean) -> Element): Component {
    val stableRef = StableRef.create(callback)
    @Suppress("UNCHECKED_CAST")
    val renderCallback = staticCFunction { focused: Boolean, refPtr: COpaquePointer? ->
        refPtr!!.asStableRef<(Boolean) -> Element>().get()(focused).handle
    } as ftxui_focused_render_callback_t
    return Component(ftxui_component_renderer_focusable(renderCallback, stableRef.asCPointer())!!).also {
        it.cleanups.add { stableRef.dispose() }
    }
}

fun Component.render() = Element(ftxui_component_render(this.handle)!!)

fun Component.decorateRender(transform: (Element) -> Element): Component {
    val stableRef = StableRef.create(transform)
    @Suppress("UNCHECKED_CAST")
    val callback = staticCFunction { innerHandle: ftxui_element_handle_t?, refPtr: COpaquePointer? ->
        refPtr!!.asStableRef<(Element) -> Element>().get()(Element(innerHandle!!)).handle
    } as ftxui_inner_render_callback_t
    return wrapOwning(ftxui_component_renderer_with_inner(handle, callback, stableRef.asCPointer())!!).also {
        it.cleanups.add { stableRef.dispose() }
    }
}

enum class MouseButton(internal val value: UInt) {
    Left(FTXUI_MOUSE_BUTTON_LEFT),
    Middle(FTXUI_MOUSE_BUTTON_MIDDLE),
    Right(FTXUI_MOUSE_BUTTON_RIGHT),
    None(FTXUI_MOUSE_BUTTON_NONE),
    WheelUp(FTXUI_MOUSE_BUTTON_WHEEL_UP),
    WheelDown(FTXUI_MOUSE_BUTTON_WHEEL_DOWN),
    WheelLeft(FTXUI_MOUSE_BUTTON_WHEEL_LEFT),
    WheelRight(FTXUI_MOUSE_BUTTON_WHEEL_RIGHT),
}

enum class MouseMotion(internal val value: UInt) {
    Released(FTXUI_MOUSE_MOTION_RELEASED),
    Pressed(FTXUI_MOUSE_MOTION_PRESSED),
    Moved(FTXUI_MOUSE_MOTION_MOVED),
}

sealed class FtxUIEvent {
    abstract val input: String
    abstract val debugString: String

    fun isKey(key: String): Boolean = input == key

    data class Character(
        override val input: String,
        override val debugString: String,
        val character: String,
    ) : FtxUIEvent()

    data class Mouse(
        override val input: String,
        override val debugString: String,
        val x: Int,
        val y: Int,
        val button: MouseButton,
        val motion: MouseMotion,
        val shift: Boolean,
        val meta: Boolean,
        val control: Boolean,
    ) : FtxUIEvent()

    data class CursorPosition(
        override val input: String,
        override val debugString: String,
        val x: Int,
        val y: Int,
    ) : FtxUIEvent()

    data class CursorShape(
        override val input: String,
        override val debugString: String,
        val shape: Int,
    ) : FtxUIEvent()

    data class TerminalNameVersion(
        override val input: String,
        override val debugString: String,
        val name: String,
        val version: Int,
    ) : FtxUIEvent()

    data class TerminalEmulator(
        override val input: String,
        override val debugString: String,
        val name: String,
        val version: String,
    ) : FtxUIEvent()

    data class TerminalCapabilities(
        override val input: String,
        override val debugString: String,
        val capabilities: List<Int>,
    ) : FtxUIEvent()

    data class Other(
        override val input: String,
        override val debugString: String,
    ) : FtxUIEvent()
}

object Key {
    // Arrow keys
    const val ArrowLeft      = "\u001B[D"
    const val ArrowRight     = "\u001B[C"
    const val ArrowUp        = "\u001B[A"
    const val ArrowDown      = "\u001B[B"
    const val ArrowLeftCtrl  = "\u001B[1;5D"
    const val ArrowRightCtrl = "\u001B[1;5C"
    const val ArrowUpCtrl    = "\u001B[1;5A"
    const val ArrowDownCtrl  = "\u001B[1;5B"

    // Common keys
    const val Backspace  = "\u007F"
    const val Delete     = "\u001B[3~"
    const val Escape     = "\u001B"
    const val Return     = "\n"
    const val Tab        = "\t"
    const val TabReverse = "\u001B[Z"

    // Navigation keys
    const val Insert   = "\u001B[2~"
    const val Home     = "\u001B[H"
    const val End      = "\u001B[F"
    const val PageUp   = "\u001B[5~"
    const val PageDown = "\u001B[6~"

    // Function keys
    const val F1  = "\u001BOP"
    const val F2  = "\u001BOQ"
    const val F3  = "\u001BOR"
    const val F4  = "\u001BOS"
    const val F5  = "\u001B[15~"
    const val F6  = "\u001B[17~"
    const val F7  = "\u001B[18~"
    const val F8  = "\u001B[19~"
    const val F9  = "\u001B[20~"
    const val F10 = "\u001B[21~"
    const val F11 = "\u001B[23~"
    const val F12 = "\u001B[24~"

    // Ctrl keys (\u0001..\u001A)
    const val CtrlA = "\u0001"
    const val CtrlB = "\u0002"
    const val CtrlC = "\u0003"
    const val CtrlD = "\u0004"
    const val CtrlE = "\u0005"
    const val CtrlF = "\u0006"
    const val CtrlG = "\u0007"
    const val CtrlH = "\u0008"
    const val CtrlI = "\t"
    const val CtrlJ = "\n"
    const val CtrlK = "\u000B"
    const val CtrlL = "\u000C"
    const val CtrlM = "\r"
    const val CtrlN = "\u000E"
    const val CtrlO = "\u000F"
    const val CtrlP = "\u0010"
    const val CtrlQ = "\u0011"
    const val CtrlR = "\u0012"
    const val CtrlS = "\u0013"
    const val CtrlT = "\u0014"
    const val CtrlU = "\u0015"
    const val CtrlV = "\u0016"
    const val CtrlW = "\u0017"
    const val CtrlX = "\u0018"
    const val CtrlY = "\u0019"
    const val CtrlZ = "\u001A"

    // Alt keys (ESC + letter)
    const val AltA = "\u001Ba"
    const val AltB = "\u001Bb"
    const val AltC = "\u001Bc"
    const val AltD = "\u001Bd"
    const val AltE = "\u001Be"
    const val AltF = "\u001Bf"
    const val AltG = "\u001Bg"
    const val AltH = "\u001Bh"
    const val AltI = "\u001Bi"
    const val AltJ = "\u001Bj"
    const val AltK = "\u001Bk"
    const val AltL = "\u001Bl"
    const val AltM = "\u001Bm"
    const val AltN = "\u001Bn"
    const val AltO = "\u001Bo"
    const val AltP = "\u001Bp"
    const val AltQ = "\u001Bq"
    const val AltR = "\u001Br"
    const val AltS = "\u001Bs"
    const val AltT = "\u001Bt"
    const val AltU = "\u001Bu"
    const val AltV = "\u001Bv"
    const val AltW = "\u001Bw"
    const val AltX = "\u001Bx"
    const val AltY = "\u001By"
    const val AltZ = "\u001Bz"

    // CtrlAlt keys (ESC + Ctrl key)
    const val CtrlAltA = "\u001B\u0001"
    const val CtrlAltB = "\u001B\u0002"
    const val CtrlAltC = "\u001B\u0003"
    const val CtrlAltD = "\u001B\u0004"
    const val CtrlAltE = "\u001B\u0005"
    const val CtrlAltF = "\u001B\u0006"
    const val CtrlAltG = "\u001B\u0007"
    const val CtrlAltH = "\u001B\u0008"
    const val CtrlAltI = "\u001B\t"
    const val CtrlAltJ = "\u001B\n"
    const val CtrlAltK = "\u001B\u000B"
    const val CtrlAltL = "\u001B\u000C"
    const val CtrlAltM = "\u001B\r"
    const val CtrlAltN = "\u001B\u000E"
    const val CtrlAltO = "\u001B\u000F"
    const val CtrlAltP = "\u001B\u0010"
    const val CtrlAltQ = "\u001B\u0011"
    const val CtrlAltR = "\u001B\u0012"
    const val CtrlAltS = "\u001B\u0013"
    const val CtrlAltT = "\u001B\u0014"
    const val CtrlAltU = "\u001B\u0015"
    const val CtrlAltV = "\u001B\u0016"
    const val CtrlAltW = "\u001B\u0017"
    const val CtrlAltX = "\u001B\u0018"
    const val CtrlAltY = "\u001B\u0019"
    const val CtrlAltZ = "\u001B\u001A"
}

fun Component.catchEvent(handler: (FtxUIEvent) -> Boolean): Component {
    val stableRef = StableRef.create(handler)
    val callback = staticCFunction { eventHandle: ftxui_event_handle_t?, refPtr: COpaquePointer? ->
        val block = refPtr!!.asStableRef<(FtxUIEvent) -> Boolean>().get()
        val input = ftxui_event_input(eventHandle)?.toKString() ?: ""
        val debugString = ftxui_event_debug_string(eventHandle)?.toKString() ?: ""
        val e: FtxUIEvent = when {
            ftxui_event_is_character(eventHandle) -> FtxUIEvent.Character(
                input = input,
                debugString = debugString,
                character = ftxui_event_character(eventHandle)?.toKString() ?: "",
            )
            ftxui_event_is_mouse(eventHandle) -> FtxUIEvent.Mouse(
                input = input,
                debugString = debugString,
                x = ftxui_event_mouse_x(eventHandle),
                y = ftxui_event_mouse_y(eventHandle),
                button = MouseButton.entries.first { it.value == ftxui_event_mouse_button(eventHandle) },
                motion = MouseMotion.entries.first { it.value == ftxui_event_mouse_motion(eventHandle) },
                shift = ftxui_event_mouse_shift(eventHandle),
                meta = ftxui_event_mouse_meta(eventHandle),
                control = ftxui_event_mouse_control(eventHandle),
            )
            ftxui_event_is_cursor_position(eventHandle) -> FtxUIEvent.CursorPosition(
                input = input,
                debugString = debugString,
                x = ftxui_event_cursor_x(eventHandle),
                y = ftxui_event_cursor_y(eventHandle),
            )
            ftxui_event_is_cursor_shape(eventHandle) -> FtxUIEvent.CursorShape(
                input = input,
                debugString = debugString,
                shape = ftxui_event_cursor_shape(eventHandle),
            )
            ftxui_event_is_terminal_name_version(eventHandle) -> FtxUIEvent.TerminalNameVersion(
                input = input,
                debugString = debugString,
                name = ftxui_event_terminal_name(eventHandle)?.toKString() ?: "",
                version = ftxui_event_terminal_version(eventHandle),
            )
            ftxui_event_is_terminal_emulator(eventHandle) -> FtxUIEvent.TerminalEmulator(
                input = input,
                debugString = debugString,
                name = ftxui_event_terminal_emulator_name(eventHandle)?.toKString() ?: "",
                version = ftxui_event_terminal_emulator_version(eventHandle)?.toKString() ?: "",
            )
            ftxui_event_is_terminal_capabilities(eventHandle) -> memScoped {
                val count = alloc<IntVar>()
                val ptr = ftxui_event_terminal_capabilities(eventHandle, count.ptr)
                val caps = if (ptr != null) (0 until count.value).map { ptr[it] } else emptyList()
                FtxUIEvent.TerminalCapabilities(input = input, debugString = debugString, capabilities = caps)
            }
            else -> FtxUIEvent.Other(input = input, debugString = debugString)
        }
        block(e)
    }
    return wrapOwning(ftxui_component_catch_event(handle, callback, stableRef.asCPointer())!!).also {
        it.cleanups.add { stableRef.dispose() }
    }
}

// Wraps `inner` with a Renderer that bidirectionally syncs a Kotlin property with a
// native buffer on every frame.
//
// Sync logic per frame:
//   - If the Kotlin property changed since last frame → push to native (Kotlin wins).
//   - Otherwise, if the native value changed (FTXUI event) → pull to Kotlin.
//
// This means Kotlin programmatic changes take precedence over FTXUI event-driven changes
// when both happen in the same frame, which is the expected behaviour.
private fun <T> syncWrapper(
    inner: Component,
    prop: KMutableProperty0<T>,
    getNative: () -> T,
    setNative: (T) -> Unit,
): Component {
    var lastKotlin = prop.get()
    return renderer(child = inner) {
        val current = prop.get()
        if (current != lastKotlin) {
            setNative(current)
            lastKotlin = current
        } else {
            val fromNative = getNative()
            if (fromNative != lastKotlin) {
                prop.set(fromNative)
                lastKotlin = fromNative
            }
        }
        inner.render()
    }
}

// Allocates a native Int buffer, builds an inner component with it, wraps the result in
// a sync renderer, and registers cleanup of the buffer with the returned component.
private fun intStateSynced(
    initial: Int,
    prop: KMutableProperty0<Int>,
    createInner: (CPointer<IntVar>) -> Component,
): Component {
    val native = nativeHeap.alloc<IntVar>().also { it.value = initial }
    val inner = createInner(native.ptr)
    return syncWrapper(inner, prop, { native.value }, { native.value = it })
        .also { it.cleanups.add { nativeHeap.free(native) } }
}

// Same as intStateSynced but for Boolean state.
private fun boolStateSynced(
    initial: Boolean,
    prop: KMutableProperty0<Boolean>,
    createInner: (CPointer<BooleanVar>) -> Component,
): Component {
    val native = nativeHeap.alloc<BooleanVar>().also { it.value = initial }
    val inner = createInner(native.ptr)
    return syncWrapper(inner, prop, { native.value }, { native.value = it })
        .also { it.cleanups.add { nativeHeap.free(native) } }
}

// Same as intStateSynced but for Float state.
private fun floatStateSynced(
    initial: Float,
    prop: KMutableProperty0<Float>,
    createInner: (CPointer<FloatVar>) -> Component,
): Component {
    val native = nativeHeap.alloc<FloatVar>().also { it.value = initial }
    val inner = createInner(native.ptr)
    return syncWrapper(inner, prop, { native.value }, { native.value = it })
        .also { it.cleanups.add { nativeHeap.free(native) } }
}

// Same as intStateSynced but for String state via StringState (ftxui_string_handle).
private fun stringStateSynced(
    initial: String,
    prop: KMutableProperty0<String>,
    createInner: (StringState) -> Component,
): Component {
    val state = StringState(initial)
    val inner = createInner(state)
    return syncWrapper(inner, prop, { state.value }, { state.value = it })
        .also { it.cleanups.add { state.destroy() } }
}

// -- Component decorators
// These wrap the component in a Renderer and transfer ownership: the source component's
// handle will be destroyed when the returned component is destroyed.
// Note: wrapping discards focus/event forwarding — use on non-interactive components only.

fun Component.border() = wrapOwning(ftxui_component_border(handle)!!)
fun Component.borderLight() = wrapOwning(ftxui_component_border_light(handle)!!)
fun Component.borderDashed() = wrapOwning(ftxui_component_border_dashed(handle)!!)
fun Component.borderHeavy() = wrapOwning(ftxui_component_border_heavy(handle)!!)
fun Component.borderDouble() = wrapOwning(ftxui_component_border_double(handle)!!)
fun Component.borderRounded() = wrapOwning(ftxui_component_border_rounded(handle)!!)
fun Component.borderEmpty() = wrapOwning(ftxui_component_border_empty(handle)!!)

fun Component.flex() = wrapOwning(ftxui_component_flex(handle)!!)
fun Component.frame() = wrapOwning(ftxui_component_frame(handle)!!)
fun Component.vscrollIndicator() = wrapOwning(ftxui_component_vscroll_indicator(handle)!!)
fun Component.size(widthOrHeight: WidthOrHeight, constraint: Constraint, value: Int) =
    wrapOwning(ftxui_component_set_size(handle, widthOrHeight.value, constraint.value, value)!!)

fun Component.bold() = wrapOwning(ftxui_component_bold(handle)!!)
fun Component.inverted() = wrapOwning(ftxui_component_inverted(handle)!!)
fun Component.underlined() = wrapOwning(ftxui_component_underlined(handle)!!)
fun Component.dim() = wrapOwning(ftxui_component_dim(handle)!!)
fun Component.blink() = wrapOwning(ftxui_component_blink(handle)!!)
fun Component.strikethrough() = wrapOwning(ftxui_component_strikethrough(handle)!!)

fun Component.color(color: Color) = wrapOwning(ftxui_component_color(handle, color.handle)!!)
fun Component.bgcolor(color: Color) = wrapOwning(ftxui_component_bgcolor(handle, color.handle)!!)

fun Component.hcenter() = wrapOwning(ftxui_component_hcenter(handle)!!)
fun Component.vcenter() = wrapOwning(ftxui_component_vcenter(handle)!!)
fun Component.center() = wrapOwning(ftxui_component_center(handle)!!)
fun Component.alignRight() = wrapOwning(ftxui_component_align_right(handle)!!)

fun Component.nothing() = wrapOwning(ftxui_component_nothing(handle)!!)
fun Component.hoverable(hover: BoolState) = wrapOwning(ftxui_component_hoverable(handle, hover.ptr)!!)
fun Component.hoverable(hover: KMutableProperty0<Boolean>): Component =
    boolStateSynced(hover.get(), hover) { ptr ->
        wrapOwning(ftxui_component_hoverable(handle, ptr)!!)
    }

fun Component.hoverable(onEnter: () -> Unit, onLeave: () -> Unit): Component {
    val enterRef = StableRef.create(onEnter)
    val leaveRef = StableRef.create(onLeave)
    return wrapOwning(ftxui_component_hoverable_callbacks(handle, callbackBridge, enterRef.asCPointer(), callbackBridge, leaveRef.asCPointer())!!).also {
        it.cleanups.add { enterRef.dispose() }
        it.cleanups.add { leaveRef.dispose() }
    }
}

fun Component.hoverable(onChange: (Boolean) -> Unit): Component {
    val ref = StableRef.create(onChange)
    return wrapOwning(ftxui_component_hoverable_change(handle, hoverChangeBridge, ref.asCPointer())!!).also {
        it.cleanups.add { ref.dispose() }
    }
}

// -- State holders
// Native-heap-backed mutable values for interactive components.
// Call free() when the associated component is destroyed.

class BoolState(initial: Boolean = false) : AutoCloseable {
    private val native = nativeHeap.alloc<BooleanVar>().also { it.value = initial }
    var value: Boolean
        get() = native.value
        set(v) { native.value = v }
    internal val ptr get() = native.ptr
    fun destroy() = nativeHeap.free(native)
    override fun close() = destroy()
}

class IntState(initial: Int = 0) : AutoCloseable {
    private val native = nativeHeap.alloc<IntVar>().also { it.value = initial }
    var value: Int
        get() = native.value
        set(v) { native.value = v }
    internal val ptr get() = native.ptr
    fun destroy() = nativeHeap.free(native)
    override fun close() = destroy()
}

class StringState(initial: String = "") : AutoCloseable {
    private val handle = ftxui_string_create(initial)!!
    var value: String
        get() = ftxui_string_get(handle)?.toKString() ?: ""
        set(v) { ftxui_string_set(handle, v) }
    internal val ptr get() = handle
    fun destroy() = ftxui_string_destroy(handle)
    override fun close() = destroy()
}

class FloatState(initial: Float = 0f) : AutoCloseable {
    private val native = nativeHeap.alloc<FloatVar>().also { it.value = initial }
    var value: Float
        get() = native.value
        set(v) { native.value = v }
    internal val ptr get() = native.ptr
    fun destroy() = nativeHeap.free(native)
    override fun close() = destroy()
}

// -- Additional components

fun input(content: StringState, placeholder: String = ""): Component =
    Component(ftxui_component_input(content.ptr, placeholder)!!)

fun inputPassword(content: StringState, placeholder: String = ""): Component =
    Component(ftxui_component_input_password(content.ptr, placeholder)!!)

class InputOptions(
    val content: StringState? = null,
    val placeholder: String? = null,
    val multiline: BoolState? = null,
    val insert: BoolState? = null,
    val cursorPosition: IntState? = null,
    val onChange: (() -> Unit)? = null,
    val onEnter: (() -> Unit)? = null,
)

fun input(options: InputOptions): Component = memScoped {
    val changeRef = options.onChange?.let { StableRef.create(it) }
    val enterRef = options.onEnter?.let { StableRef.create(it) }
    val opts = cValue<ftxui_input_options_t> {
        this.content = options.content?.ptr
        this.placeholder = options.placeholder?.cstr?.getPointer(this@memScoped)
        this.multiline = options.multiline?.ptr
        this.insert = options.insert?.ptr
        this.cursor_position = options.cursorPosition?.ptr
        this.on_change = if (changeRef != null) callbackBridge else null
        this.on_change_userdata = changeRef?.asCPointer()
        this.on_enter = if (enterRef != null) callbackBridge else null
        this.on_enter_userdata = enterRef?.asCPointer()
    }
    Component(ftxui_component_input_with_options(opts)!!).also { c ->
        changeRef?.let { c.cleanups.add { it.dispose() } }
        enterRef?.let { c.cleanups.add { it.dispose() } }
    }
}

fun checkbox(label: String, checked: BoolState): Component =
    Component(ftxui_component_checkbox(label, checked.ptr)!!)

fun checkbox(label: String, checked: BoolState, onChange: () -> Unit): Component {
    val ref = StableRef.create(onChange)
    return Component(ftxui_component_checkbox_with_change(label, checked.ptr, callbackBridge, ref.asCPointer())!!).also {
        it.cleanups.add { ref.dispose() }
    }
}

fun toggle(entries: List<String>, selected: IntState): Component = memScoped {
    val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
    entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
    Component(ftxui_component_toggle(ptrs, entries.size, selected.ptr)!!)
}

fun slider(label: String, value: IntState, min: Int, max: Int, increment: Int = 1): Component =
    Component(ftxui_component_slider(label, value.ptr, min, max, increment)!!)

fun slider(value: IntState, min: Int, max: Int, increment: Int = 1, direction: Direction): Component =
    Component(ftxui_component_slider_int_direction(value.ptr, min, max, increment, direction.value)!!)

fun slider(value: IntState, min: Int, max: Int, increment: Int = 1, onChange: () -> Unit): Component {
    val ref = StableRef.create(onChange)
    return Component(ftxui_component_slider_int_with_change(value.ptr, min, max, increment, callbackBridge, ref.asCPointer())!!).also {
        it.cleanups.add { ref.dispose() }
    }
}

fun slider(label: String, value: FloatState, min: Float, max: Float, increment: Float = 1f): Component =
    Component(ftxui_component_slider_float(label, value.ptr, min, max, increment)!!)

fun slider(value: FloatState, min: Float, max: Float, increment: Float = 1f, onChange: () -> Unit): Component {
    val ref = StableRef.create(onChange)
    return Component(ftxui_component_slider_float_with_change(value.ptr, min, max, increment, callbackBridge, ref.asCPointer())!!).also {
        it.cleanups.add { ref.dispose() }
    }
}

fun slider(value: FloatState, min: Float, max: Float, increment: Float = 1f, direction: Direction,
           colorActive: Color? = null, colorInactive: Color? = null): Component =
    Component(ftxui_component_slider_float_direction(value.ptr, min, max, increment, direction.value, colorActive?.handle, colorInactive?.handle)!!)

fun radiobox(entries: List<String>, selected: IntState): Component = memScoped {
    val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
    entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
    Component(ftxui_component_radiobox(ptrs, entries.size, selected.ptr)!!)
}

fun radiobox(entries: List<String>, selected: IntState, onChange: () -> Unit): Component {
    val ref = StableRef.create(onChange)
    return memScoped {
        val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
        entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
        Component(ftxui_component_radiobox_with_change(ptrs, entries.size, selected.ptr, callbackBridge, ref.asCPointer())!!).also {
            it.cleanups.add { ref.dispose() }
        }
    }
}

fun menu(entries: List<String>, selected: IntState): Component = memScoped {
    val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
    entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
    Component(ftxui_component_menu(ptrs, entries.size, selected.ptr)!!)
}

fun menu(
    entries: List<String>,
    selected: IntState,
    onChange: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null,
): Component {
    val changeRef = onChange?.let { StableRef.create(it) }
    val enterRef = onEnter?.let { StableRef.create(it) }
    return memScoped {
        val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
        entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
        Component(ftxui_component_menu_with_callbacks(
            ptrs, entries.size, selected.ptr,
            if (changeRef != null) callbackBridge else null, changeRef?.asCPointer(),
            if (enterRef != null) callbackBridge else null, enterRef?.asCPointer(),
        )!!).also { c ->
            changeRef?.let { c.cleanups.add { it.dispose() } }
            enterRef?.let { c.cleanups.add { it.dispose() } }
        }
    }
}

fun menuEntry(label: String): Component =
    Component(ftxui_component_menu_entry(label)!!)

class AnimatedMenuEntryColors internal constructor(
    internal val value: CValue<ftxui_animated_colors_option_t>
)

fun menuEntry(label: String, animatedColors: AnimatedMenuEntryColors): Component =
    Component(ftxui_component_menu_entry_animated(label, animatedColors.value)!!)

fun animatedMenuEntryColors(
    bgActive: Color, bgInactive: Color = Color.Black,
    fgActive: Color = Color.White, fgInactive: Color = bgActive
): AnimatedMenuEntryColors = AnimatedMenuEntryColors(cValue<ftxui_animated_colors_option_t> {
    background.enabled = true
    background.active = bgActive.handle
    background.inactive = bgInactive.handle
    background.duration_ms = 250
    background.easing_function_type = ftxui_easing_function_type_t.FTXUI_EASING_QUINTIC_IN_OUT
    foreground.enabled = true
    foreground.active = fgActive.handle
    foreground.inactive = fgInactive.handle
    foreground.duration_ms = 250
    foreground.easing_function_type = ftxui_easing_function_type_t.FTXUI_EASING_QUINTIC_IN_OUT
})

fun menuHorizontal(entries: List<String>, selected: IntState): Component = memScoped {
    val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
    entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
    Component(ftxui_component_menu_horizontal(ptrs, entries.size, selected.ptr)!!)
}

private val separatorFuncBridge = staticCFunction<COpaquePointer?, ftxui_element_handle_t?> { userdata ->
    userdata!!.asStableRef<() -> Element>().get()().handle
}

// Shared bridge for ftxui_callback_t: userdata is a StableRef<() -> Unit>.
private val callbackBridge = staticCFunction<COpaquePointer?, Unit> { refPtr ->
    refPtr?.asStableRef<() -> Unit>()?.get()?.invoke()
}

// Shared bridge for hoverable_change: (bool, void*) callback.
private val hoverChangeBridge = staticCFunction<Boolean, COpaquePointer?, Unit> { hovered, refPtr ->
    refPtr?.asStableRef<(Boolean) -> Unit>()?.get()?.invoke(hovered)
}

// Shared bridge for maybe_fn predicate: (void*) -> bool callback.
private val predicateBridge = staticCFunction<COpaquePointer?, Boolean> { refPtr ->
    refPtr?.asStableRef<() -> Boolean>()?.get()?.invoke() ?: false
}

fun resizableSplit(
    main: Component,
    back: Component,
    mainSize: IntState,
    direction: Direction,
    minSize: IntState? = null,
    maxSize: IntState? = null,
    separator: (() -> Element)? = null
): Component {
    val ref = separator?.let { StableRef.create(it) }
    val option = cValue<ftxui_resizable_split_option_t> {
        this.main = main.handle
        this.back = back.handle
        this.direction = direction.value
        this.main_size = mainSize.ptr
        this.min_size = minSize?.ptr
        this.max_size = maxSize?.ptr
        if (ref != null) {
            this.separator_func = separatorFuncBridge
            this.separator_userdata = ref.asCPointer()
        }
    }
    val handle = ftxui_component_resizable_split_opt(option)!!
    return wrapOwning(main, back, handle).also {
        ref?.let { r -> it.cleanups.add { r.dispose() } }
    }
}

fun gridbox(rows: List<List<Element>>): Element = memScoped {
    val flat = rows.flatten()
    val cells = allocArray<ftxui_element_handle_tVar>(flat.size)
    flat.forEachIndexed { i, el -> cells[i] = el.handle }
    val rowLengths = allocArray<IntVar>(rows.size)
    rows.forEachIndexed { i, row -> rowLengths[i] = row.size }
    Element(ftxui_element_gridbox(cells, flat.size, rowLengths, rows.size)!!)
}

fun dropdown(entries: List<String>, selected: IntState): Component = memScoped {
    val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
    entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
    Component(ftxui_component_dropdown(ptrs, entries.size, selected.ptr)!!)
}

fun dropdownCustom(
    entries: List<String>,
    selected: IntState? = null,
    transform: ((open: Boolean, checkbox: Element, radiobox: Element) -> Element)? = null,
    entryTransform: ((EntryState) -> Element)? = null,
): Component {
    val transformRef = transform?.let { StableRef.create(it) }
    @Suppress("UNCHECKED_CAST")
    val transformCb: ftxui_dropdown_transform_callback_t? = if (transform != null) {
        staticCFunction { open: Boolean, cbHandle: ftxui_element_handle_t?, rbHandle: ftxui_element_handle_t?, refPtr: COpaquePointer? ->
            val block = refPtr!!.asStableRef<(Boolean, Element, Element) -> Element>().get()
            block(open, Element(cbHandle!!), Element(rbHandle!!)).handle
        } as ftxui_dropdown_transform_callback_t
    } else null

    val entryTransformRef = entryTransform?.let { StableRef.create(it) }
    @Suppress("UNCHECKED_CAST")
    val entryTransformCb: ftxui_button_transform_t? = if (entryTransform != null) {
        staticCFunction { state: CValue<ftxui_entry_state_t>, refPtr: COpaquePointer? ->
            state.useContents {
                val block = refPtr!!.asStableRef<(EntryState) -> Element>().get()
                block(EntryState(
                    label = this.label?.toKString() ?: "",
                    state = this.state,
                    active = this.active,
                    focused = this.focused,
                    index = this.index
                )).handle
            }
        } as ftxui_button_transform_t
    } else null

    val handle = memScoped {
        val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
        entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
        ftxui_component_dropdown_custom(
            ptrs, entries.size, selected?.ptr,
            transformCb, transformRef?.asCPointer(),
            entryTransformCb, entryTransformRef?.asCPointer(),
        )!!
    }

    return Component(handle).also { c ->
        transformRef?.let { c.cleanups.add { it.dispose() } }
        entryTransformRef?.let { c.cleanups.add { it.dispose() } }
    }
}

fun tab(selected: IntState): ContainerComponent =
    ContainerComponent(ftxui_component_container_tab(selected.ptr)!!)

fun stacked(): ContainerComponent =
    ContainerComponent(ftxui_component_container_stacked()!!)

fun resizableSplitLeft(main: Component, back: Component, mainSize: IntState): Component =
    wrapOwning(main, back, ftxui_component_resizable_split_left(main.handle, back.handle, mainSize.ptr)!!)

fun resizableSplitRight(main: Component, back: Component, mainSize: IntState): Component =
    wrapOwning(main, back, ftxui_component_resizable_split_right(main.handle, back.handle, mainSize.ptr)!!)

fun resizableSplitTop(main: Component, back: Component, mainSize: IntState): Component =
    wrapOwning(main, back, ftxui_component_resizable_split_top(main.handle, back.handle, mainSize.ptr)!!)

fun resizableSplitBottom(main: Component, back: Component, mainSize: IntState): Component =
    wrapOwning(main, back, ftxui_component_resizable_split_bottom(main.handle, back.handle, mainSize.ptr)!!)

fun collapsible(label: String, child: Component, show: BoolState): Component =
    child.wrapOwning(ftxui_component_collapsible(label, child.handle, show.ptr)!!)

fun maybe(child: Component, show: BoolState): Component =
    child.wrapOwning(ftxui_component_maybe(child.handle, show.ptr)!!)

fun maybe(child: Component, predicate: () -> Boolean): Component {
    val ref = StableRef.create(predicate)
    return child.wrapOwning(ftxui_component_maybe_fn(child.handle, predicateBridge, ref.asCPointer())!!).also {
        it.cleanups.add { ref.dispose() }
    }
}

fun modal(main: Component, modal: Component, showModal: BoolState): Component =
    wrapOwning(main, modal, ftxui_component_modal(main.handle, modal.handle, showModal.ptr)!!)

// -- Property-ref overloads
// These accept a KMutableProperty0<T> (e.g. ::myVar) instead of an IntState/BoolState.
// The native buffer is managed internally and freed when the component is destroyed.
// No manual state management required.

fun input(content: KMutableProperty0<String>, placeholder: String = ""): Component =
    stringStateSynced(content.get(), content) { state ->
        input(state, placeholder)
    }

fun inputPassword(content: KMutableProperty0<String>, placeholder: String = ""): Component =
    stringStateSynced(content.get(), content) { state ->
        inputPassword(state, placeholder)
    }

fun checkbox(label: String, checked: KMutableProperty0<Boolean>): Component =
    boolStateSynced(checked.get(), checked) { ptr ->
        Component(ftxui_component_checkbox(label, ptr)!!)
    }

fun checkbox(label: String, checked: KMutableProperty0<Boolean>, onChange: () -> Unit): Component =
    boolStateSynced(checked.get(), checked) { ptr ->
        val ref = StableRef.create(onChange)
        Component(ftxui_component_checkbox_with_change(label, ptr, callbackBridge, ref.asCPointer())!!).also {
            it.cleanups.add { ref.dispose() }
        }
    }

fun toggle(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_toggle(ptrs, entries.size, ptr)!!)
        }
    }

fun slider(label: String, value: KMutableProperty0<Int>, min: Int, max: Int, increment: Int = 1): Component =
    intStateSynced(value.get(), value) { ptr ->
        Component(ftxui_component_slider(label, ptr, min, max, increment)!!)
    }

fun slider(value: KMutableProperty0<Int>, min: Int, max: Int, increment: Int = 1, direction: Direction): Component =
    intStateSynced(value.get(), value) { ptr ->
        Component(ftxui_component_slider_int_direction(ptr, min, max, increment, direction.value)!!)
    }

fun slider(value: KMutableProperty0<Int>, min: Int, max: Int, increment: Int = 1, onChange: () -> Unit): Component =
    intStateSynced(value.get(), value) { ptr ->
        val ref = StableRef.create(onChange)
        Component(ftxui_component_slider_int_with_change(ptr, min, max, increment, callbackBridge, ref.asCPointer())!!).also {
            it.cleanups.add { ref.dispose() }
        }
    }

fun slider(label: String, value: KMutableProperty0<Float>, min: Float, max: Float, increment: Float = 1f): Component =
    floatStateSynced(value.get(), value) { ptr ->
        Component(ftxui_component_slider_float(label, ptr, min, max, increment)!!)
    }

fun slider(value: KMutableProperty0<Float>, min: Float, max: Float, increment: Float = 1f, onChange: () -> Unit): Component =
    floatStateSynced(value.get(), value) { ptr ->
        val ref = StableRef.create(onChange)
        Component(ftxui_component_slider_float_with_change(ptr, min, max, increment, callbackBridge, ref.asCPointer())!!).also {
            it.cleanups.add { ref.dispose() }
        }
    }

fun slider(value: KMutableProperty0<Float>, min: Float, max: Float, increment: Float = 1f, direction: Direction,
           colorActive: Color? = null, colorInactive: Color? = null): Component =
    floatStateSynced(value.get(), value) { ptr ->
        Component(ftxui_component_slider_float_direction(ptr, min, max, increment, direction.value, colorActive?.handle, colorInactive?.handle)!!)
    }

fun radiobox(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_radiobox(ptrs, entries.size, ptr)!!)
        }
    }

fun radiobox(entries: List<String>, selected: KMutableProperty0<Int>, onChange: () -> Unit): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        val ref = StableRef.create(onChange)
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_radiobox_with_change(ptrs, entries.size, ptr, callbackBridge, ref.asCPointer())!!).also {
                it.cleanups.add { ref.dispose() }
            }
        }
    }

fun menu(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_menu(ptrs, entries.size, ptr)!!)
        }
    }

fun menu(
    entries: List<String>,
    selected: KMutableProperty0<Int>,
    onChange: (() -> Unit)? = null,
    onEnter: (() -> Unit)? = null,
): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        val changeRef = onChange?.let { StableRef.create(it) }
        val enterRef = onEnter?.let { StableRef.create(it) }
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_menu_with_callbacks(
                ptrs, entries.size, ptr,
                if (changeRef != null) callbackBridge else null, changeRef?.asCPointer(),
                if (enterRef != null) callbackBridge else null, enterRef?.asCPointer(),
            )!!).also { c ->
                changeRef?.let { c.cleanups.add { it.dispose() } }
                enterRef?.let { c.cleanups.add { it.dispose() } }
            }
        }
    }

fun menuHorizontal(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_menu_horizontal(ptrs, entries.size, ptr)!!)
        }
    }

fun dropdown(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_dropdown(ptrs, entries.size, ptr)!!)
        }
    }

fun collapsible(label: String, child: Component, show: KMutableProperty0<Boolean>): Component =
    boolStateSynced(show.get(), show) { ptr ->
        child.wrapOwning(ftxui_component_collapsible(label, child.handle, ptr)!!)
    }

fun maybe(child: Component, show: KMutableProperty0<Boolean>): Component =
    boolStateSynced(show.get(), show) { ptr ->
        child.wrapOwning(ftxui_component_maybe(child.handle, ptr)!!)
    }

fun modal(main: Component, modal: Component, showModal: KMutableProperty0<Boolean>): Component =
    boolStateSynced(showModal.get(), showModal) { ptr ->
        wrapOwning(main, modal, ftxui_component_modal(main.handle, modal.handle, ptr)!!)
    }

fun Component.maybe(show: BoolState) = maybe(this, show)
fun Component.maybe(show: KMutableProperty0<Boolean>) = maybe(this, show)
fun Component.maybe(predicate: () -> Boolean) = maybe(this, predicate)
fun Component.modal(modal: Component, showModal: BoolState) = modal(this, modal, showModal)
fun Component.modal(modal: Component, showModal: KMutableProperty0<Boolean>) = modal(this, modal, showModal)

fun resizableSplitLeft(main: Component, back: Component, mainSize: KMutableProperty0<Int>): Component =
    intStateSynced(mainSize.get(), mainSize) { ptr ->
        wrapOwning(main, back, ftxui_component_resizable_split_left(main.handle, back.handle, ptr)!!)
    }

fun resizableSplitRight(main: Component, back: Component, mainSize: KMutableProperty0<Int>): Component =
    intStateSynced(mainSize.get(), mainSize) { ptr ->
        wrapOwning(main, back, ftxui_component_resizable_split_right(main.handle, back.handle, ptr)!!)
    }

fun resizableSplitTop(main: Component, back: Component, mainSize: KMutableProperty0<Int>): Component =
    intStateSynced(mainSize.get(), mainSize) { ptr ->
        wrapOwning(main, back, ftxui_component_resizable_split_top(main.handle, back.handle, ptr)!!)
    }

fun resizableSplitBottom(main: Component, back: Component, mainSize: KMutableProperty0<Int>): Component =
    intStateSynced(mainSize.get(), mainSize) { ptr ->
        wrapOwning(main, back, ftxui_component_resizable_split_bottom(main.handle, back.handle, ptr)!!)
    }

fun poll(app: FtxUIApp, onPoll: () -> Unit): Component {
    val stableRef = StableRef.create(onPoll)
    val callback = staticCFunction { refPtr: COpaquePointer? ->
        refPtr?.asStableRef<() -> Unit>()?.get()?.invoke()
        Unit
    }
    return Component(ftxui_component_poll(app.handle, callback, stableRef.asCPointer())!!).also {
        it.cleanups.add { stableRef.dispose() }
    }
}

fun FtxUIApp.requestAnimationFrame() = ftxui_app_request_animation_frame(handle)

// -- Animated menus

fun menuHorizontalAnimated(entries: List<String>, selected: IntState): Component = memScoped {
    val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
    entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
    Component(ftxui_component_menu_horizontal_animated(ptrs, entries.size, selected.ptr)!!)
}

fun menuToggle(entries: List<String>, selected: IntState): Component = memScoped {
    val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
    entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
    Component(ftxui_component_menu_toggle(ptrs, entries.size, selected.ptr)!!)
}

fun menuHorizontalAnimated(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_menu_horizontal_animated(ptrs, entries.size, ptr)!!)
        }
    }

fun menuToggle(entries: List<String>, selected: KMutableProperty0<Int>): Component =
    intStateSynced(selected.get(), selected) { ptr ->
        memScoped {
            val ptrs = allocArray<CPointerVar<ByteVar>>(entries.size)
            entries.forEachIndexed { i, s -> ptrs[i] = s.cstr.getPointer(this) }
            Component(ftxui_component_menu_toggle(ptrs, entries.size, ptr)!!)
        }
    }

// -- Canvas

class Canvas internal constructor(private val handle: ftxui_canvas_handle_t) : AutoCloseable {
    private val cleanups = mutableListOf<() -> Unit>()

    fun destroy() {
        cleanups.forEach { it() }
        cleanups.clear()
        ftxui_canvas_destroy(handle)
    }
    override fun close() = destroy()

    fun width(): Int = ftxui_canvas_width(handle)
    fun height(): Int = ftxui_canvas_height(handle)

    // -- Text
    fun drawText(x: Int, y: Int, text: String) = ftxui_canvas_draw_text(handle, x, y, text)
    fun drawText(x: Int, y: Int, text: String, color: Color) =
        ftxui_canvas_draw_text_color(handle, x, y, text, color.handle)

    // -- Braille point drawing
    fun drawPointOn(x: Int, y: Int) = ftxui_canvas_draw_point_on(handle, x, y)
    fun drawPointOff(x: Int, y: Int) = ftxui_canvas_draw_point_off(handle, x, y)
    fun drawPointToggle(x: Int, y: Int) = ftxui_canvas_draw_point_toggle(handle, x, y)
    fun drawPoint(x: Int, y: Int, value: Boolean) = ftxui_canvas_draw_point(handle, x, y, value)
    fun drawPoint(x: Int, y: Int, value: Boolean, color: Color) =
        ftxui_canvas_draw_point_color(handle, x, y, value, color.handle)

    fun drawPointLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Color? = null) =
        ftxui_canvas_draw_point_line(handle, x1, y1, x2, y2, color?.handle)

    fun drawPointCircle(x: Int, y: Int, radius: Int) = ftxui_canvas_draw_point_circle(handle, x, y, radius)
    fun drawPointCircle(x: Int, y: Int, radius: Int, color: Color) =
        ftxui_canvas_draw_point_circle_color(handle, x, y, radius, color.handle)
    fun drawPointCircleFilled(x: Int, y: Int, radius: Int) = ftxui_canvas_draw_point_circle_filled(handle, x, y, radius)
    fun drawPointCircleFilled(x: Int, y: Int, radius: Int, color: Color) =
        ftxui_canvas_draw_point_circle_filled_color(handle, x, y, radius, color.handle)

    fun drawPointEllipse(x: Int, y: Int, rx: Int, ry: Int) = ftxui_canvas_draw_point_ellipse(handle, x, y, rx, ry)
    fun drawPointEllipse(x: Int, y: Int, rx: Int, ry: Int, color: Color) =
        ftxui_canvas_draw_point_ellipse_color(handle, x, y, rx, ry, color.handle)
    fun drawPointEllipseFilled(x: Int, y: Int, rx: Int, ry: Int) = ftxui_canvas_draw_point_ellipse_filled(handle, x, y, rx, ry)
    fun drawPointEllipseFilled(x: Int, y: Int, rx: Int, ry: Int, color: Color) =
        ftxui_canvas_draw_point_ellipse_filled_color(handle, x, y, rx, ry, color.handle)

    // -- Block pixel drawing
    fun drawBlockOn(x: Int, y: Int) = ftxui_canvas_draw_block_on(handle, x, y)
    fun drawBlockOff(x: Int, y: Int) = ftxui_canvas_draw_block_off(handle, x, y)
    fun drawBlockToggle(x: Int, y: Int) = ftxui_canvas_draw_block_toggle(handle, x, y)
    fun drawBlock(x: Int, y: Int, value: Boolean) = ftxui_canvas_draw_block(handle, x, y, value)
    fun drawBlock(x: Int, y: Int, value: Boolean, color: Color) =
        ftxui_canvas_draw_block_color(handle, x, y, value, color.handle)

    fun drawBlockLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Color? = null) =
        ftxui_canvas_draw_block_line(handle, x1, y1, x2, y2, color?.handle)

    fun drawBlockCircle(x: Int, y: Int, radius: Int) = ftxui_canvas_draw_block_circle(handle, x, y, radius)
    fun drawBlockCircle(x: Int, y: Int, radius: Int, color: Color) =
        ftxui_canvas_draw_block_circle_color(handle, x, y, radius, color.handle)
    fun drawBlockCircleFilled(x: Int, y: Int, radius: Int) = ftxui_canvas_draw_block_circle_filled(handle, x, y, radius)
    fun drawBlockCircleFilled(x: Int, y: Int, radius: Int, color: Color) =
        ftxui_canvas_draw_block_circle_filled_color(handle, x, y, radius, color.handle)

    fun drawBlockEllipse(x: Int, y: Int, rx: Int, ry: Int) = ftxui_canvas_draw_block_ellipse(handle, x, y, rx, ry)
    fun drawBlockEllipse(x: Int, y: Int, rx: Int, ry: Int, color: Color) =
        ftxui_canvas_draw_block_ellipse_color(handle, x, y, rx, ry, color.handle)
    fun drawBlockEllipseFilled(x: Int, y: Int, rx: Int, ry: Int) = ftxui_canvas_draw_block_ellipse_filled(handle, x, y, rx, ry)
    fun drawBlockEllipseFilled(x: Int, y: Int, rx: Int, ry: Int, color: Color) =
        ftxui_canvas_draw_block_ellipse_filled_color(handle, x, y, rx, ry, color.handle)

    // -- Cell styling
    fun style(x: Int, y: Int, style: (CellStyleView) -> Unit) {
        val ref = StableRef.create(style)
        cleanups.add { ref.dispose() }
        ftxui_canvas_style(handle, x, y, cellStyleBridge, ref.asCPointer())
    }

    fun toElement(): Element = Element(ftxui_element_canvas_ref(handle)!!)

    companion object {
        operator fun invoke(width: Int, height: Int) = Canvas(ftxui_canvas_create(width, height)!!)
    }
}

// -- graph element
// GraphFn wraps a graph callback and keeps the StableRef alive.
// WARNING: GraphFn must be kept alive for as long as graph() elements using it are rendered.
// Calling destroy() while the graph element is still being rendered causes a use-after-free.
class GraphFn(fn: (width: Int, height: Int, output: IntArray) -> Unit) : AutoCloseable {
    private val stableRef = StableRef.create(fn)
    internal val cCallback: ftxui_graph_callback_t = staticCFunction { w: Int, h: Int, out: CPointer<IntVar>?, refPtr: COpaquePointer? ->
        val block = refPtr!!.asStableRef<(Int, Int, IntArray) -> Unit>().get()
        val arr = IntArray(w)
        block(w, h, arr)
        for (i in 0 until w) out!![i] = arr[i]
    }
    internal val userData get() = stableRef.asCPointer()
    fun destroy() = stableRef.dispose()
    override fun close() = destroy()
}

fun graph(fn: GraphFn): Element = Element(ftxui_element_graph(fn.cCallback, fn.userData)!!)

// -- LinearGradient

class LinearGradient internal constructor(internal val handle: ftxui_linear_gradient_handle_t) : AutoCloseable {
    fun destroy() = ftxui_linear_gradient_destroy(handle)
    override fun close() = destroy()

    fun angle(degrees: Float): LinearGradient { ftxui_linear_gradient_angle(handle, degrees); return this }
    fun stop(color: Color): LinearGradient { ftxui_linear_gradient_stop(handle, color.handle); return this }
    fun stop(color: Color, position: Float): LinearGradient { ftxui_linear_gradient_stop_at(handle, color.handle, position); return this }

    companion object {
        operator fun invoke() = LinearGradient(ftxui_linear_gradient_create()!!)
        operator fun invoke(begin: Color, end: Color) =
            LinearGradient(ftxui_linear_gradient_create()!!).stop(begin).stop(end)
        operator fun invoke(angle: Float, begin: Color, end: Color) =
            LinearGradient(ftxui_linear_gradient_create()!!).angle(angle).stop(begin).stop(end)
    }
}

fun Element.bgcolorLinearGradient(gradient: LinearGradient): Element =
    Element(ftxui_element_bgcolor_linear_gradient(handle, gradient.handle)!!)

fun Element.colorLinearGradient(gradient: LinearGradient): Element =
    Element(ftxui_element_color_linear_gradient(handle, gradient.handle)!!)

// -- flexbox element

enum class FlexboxDirection(internal val value: ftxui_flexbox_direction_t) {
    Row(ftxui_flexbox_direction_t.FTXUI_FLEXBOX_DIRECTION_ROW),
    RowInversed(ftxui_flexbox_direction_t.FTXUI_FLEXBOX_DIRECTION_ROW_INVERSED),
    Column(ftxui_flexbox_direction_t.FTXUI_FLEXBOX_DIRECTION_COLUMN),
    ColumnInversed(ftxui_flexbox_direction_t.FTXUI_FLEXBOX_DIRECTION_COLUMN_INVERSED),
}

enum class FlexboxWrap(internal val value: ftxui_flexbox_wrap_t) {
    NoWrap(ftxui_flexbox_wrap_t.FTXUI_FLEXBOX_WRAP_NO_WRAP),
    Wrap(ftxui_flexbox_wrap_t.FTXUI_FLEXBOX_WRAP_WRAP),
    WrapInversed(ftxui_flexbox_wrap_t.FTXUI_FLEXBOX_WRAP_WRAP_INVERSED),
}

enum class FlexboxJustify(internal val value: ftxui_flexbox_justify_t) {
    FlexStart(ftxui_flexbox_justify_t.FTXUI_FLEXBOX_JUSTIFY_FLEX_START),
    FlexEnd(ftxui_flexbox_justify_t.FTXUI_FLEXBOX_JUSTIFY_FLEX_END),
    Center(ftxui_flexbox_justify_t.FTXUI_FLEXBOX_JUSTIFY_CENTER),
    Stretch(ftxui_flexbox_justify_t.FTXUI_FLEXBOX_JUSTIFY_STRETCH),
    SpaceBetween(ftxui_flexbox_justify_t.FTXUI_FLEXBOX_JUSTIFY_SPACE_BETWEEN),
    SpaceAround(ftxui_flexbox_justify_t.FTXUI_FLEXBOX_JUSTIFY_SPACE_AROUND),
    SpaceEvenly(ftxui_flexbox_justify_t.FTXUI_FLEXBOX_JUSTIFY_SPACE_EVENLY),
}

enum class FlexboxAlignItems(internal val value: ftxui_flexbox_align_items_t) {
    FlexStart(ftxui_flexbox_align_items_t.FTXUI_FLEXBOX_ALIGN_ITEMS_FLEX_START),
    FlexEnd(ftxui_flexbox_align_items_t.FTXUI_FLEXBOX_ALIGN_ITEMS_FLEX_END),
    Center(ftxui_flexbox_align_items_t.FTXUI_FLEXBOX_ALIGN_ITEMS_CENTER),
    Stretch(ftxui_flexbox_align_items_t.FTXUI_FLEXBOX_ALIGN_ITEMS_STRETCH),
}

enum class FlexboxAlignContent(internal val value: ftxui_flexbox_align_content_t) {
    FlexStart(ftxui_flexbox_align_content_t.FTXUI_FLEXBOX_ALIGN_CONTENT_FLEX_START),
    FlexEnd(ftxui_flexbox_align_content_t.FTXUI_FLEXBOX_ALIGN_CONTENT_FLEX_END),
    Center(ftxui_flexbox_align_content_t.FTXUI_FLEXBOX_ALIGN_CONTENT_CENTER),
    Stretch(ftxui_flexbox_align_content_t.FTXUI_FLEXBOX_ALIGN_CONTENT_STRETCH),
    SpaceBetween(ftxui_flexbox_align_content_t.FTXUI_FLEXBOX_ALIGN_CONTENT_SPACE_BETWEEN),
    SpaceAround(ftxui_flexbox_align_content_t.FTXUI_FLEXBOX_ALIGN_CONTENT_SPACE_AROUND),
    SpaceEvenly(ftxui_flexbox_align_content_t.FTXUI_FLEXBOX_ALIGN_CONTENT_SPACE_EVENLY),
}

data class FlexboxConfig(
    val direction: FlexboxDirection = FlexboxDirection.Row,
    val wrap: FlexboxWrap = FlexboxWrap.Wrap,
    val justifyContent: FlexboxJustify = FlexboxJustify.FlexStart,
    val alignItems: FlexboxAlignItems = FlexboxAlignItems.Stretch,
    val alignContent: FlexboxAlignContent = FlexboxAlignContent.FlexStart,
    val gapX: Int = 0,
    val gapY: Int = 0,
)

fun flexbox(vararg elements: Element, config: FlexboxConfig = FlexboxConfig()): Element = memScoped {
    val array = allocArray<ftxui_element_handle_tVar>(elements.size)
    elements.forEachIndexed { i, el -> array[i] = el.handle }
    val cConfig = cValue<ftxui_flexbox_config_t> {
        direction = config.direction.value
        wrap = config.wrap.value
        justify_content = config.justifyContent.value
        align_items = config.alignItems.value
        align_content = config.alignContent.value
        gap_x = config.gapX
        gap_y = config.gapY
    }
    Element(ftxui_element_flexbox(array, elements.size, cConfig)!!)
}

// -- Table

private val decoratorBridge = staticCFunction<ftxui_element_handle_t?, COpaquePointer?, ftxui_element_handle_t?> { elementHandle, refPtr ->
    refPtr!!.asStableRef<(Element) -> Element>().get()(Element(elementHandle!!)).handle
}

class Table internal constructor(private val handle: ftxui_table_handle_t) : AutoCloseable {
    fun destroy() = ftxui_table_destroy(handle)
    override fun close() = destroy()
    fun render(): Element = Element(ftxui_table_render(handle)!!)

    fun selectAll() = TableSelection(ftxui_table_select_all(handle)!!)
    fun selectRow(row: Int) = TableSelection(ftxui_table_select_row(handle, row)!!)
    fun selectRows(from: Int, to: Int) = TableSelection(ftxui_table_select_rows(handle, from, to)!!)
    fun selectColumn(col: Int) = TableSelection(ftxui_table_select_column(handle, col)!!)
    fun selectColumns(from: Int, to: Int) = TableSelection(ftxui_table_select_columns(handle, from, to)!!)
    fun selectCell(col: Int, row: Int) = TableSelection(ftxui_table_select_cell(handle, col, row)!!)
    fun selectRectangle(colMin: Int, colMax: Int, rowMin: Int, rowMax: Int) =
        TableSelection(ftxui_table_select_rectangle(handle, colMin, colMax, rowMin, rowMax)!!)

    companion object {
        operator fun invoke(rows: List<List<String>>): Table = memScoped {
            val numRows = rows.size
            val numCols = rows.maxOfOrNull { it.size } ?: 0
            val flat = allocArray<CPointerVar<ByteVar>>(numRows * numCols)
            rows.forEachIndexed { r, row ->
                row.forEachIndexed { c, cell -> flat[r * numCols + c] = cell.cstr.getPointer(this) }
                for (c in row.size until numCols) flat[r * numCols + c] = "".cstr.getPointer(this)
            }
            Table(ftxui_table_create(flat, numRows, numCols)!!)
        }
    }
}

// TableSelection.destroy() must be called to release the StableRefs for any decorator lambdas.
class TableSelection internal constructor(private val handle: ftxui_table_selection_handle_t) : AutoCloseable {
    private val refs = mutableListOf<StableRef<*>>()

    fun destroy() {
        refs.forEach { it.dispose() }
        refs.clear()
        ftxui_table_selection_destroy(handle)
    }
    override fun close() = destroy()

    private fun track(transform: (Element) -> Element): COpaquePointer =
        StableRef.create(transform).also { refs.add(it) }.asCPointer()

    // -- Border
    fun border(style: BorderStyle = BorderStyle.Light) = apply { ftxui_table_selection_border(handle, style.value) }
    fun borderColor(style: BorderStyle, color: Color) = apply { ftxui_table_selection_border_color(handle, style.value, color.handle) }
    fun borderLeft(style: BorderStyle = BorderStyle.Light) = apply { ftxui_table_selection_border_left(handle, style.value) }
    fun borderRight(style: BorderStyle = BorderStyle.Light) = apply { ftxui_table_selection_border_right(handle, style.value) }
    fun borderTop(style: BorderStyle = BorderStyle.Light) = apply { ftxui_table_selection_border_top(handle, style.value) }
    fun borderBottom(style: BorderStyle = BorderStyle.Light) = apply { ftxui_table_selection_border_bottom(handle, style.value) }

    // -- Separator
    fun separator(style: BorderStyle = BorderStyle.Light) = apply { ftxui_table_selection_separator(handle, style.value) }
    fun separatorVertical(style: BorderStyle = BorderStyle.Light) = apply { ftxui_table_selection_separator_vertical(handle, style.value) }
    fun separatorHorizontal(style: BorderStyle = BorderStyle.Light) = apply { ftxui_table_selection_separator_horizontal(handle, style.value) }

    // -- Generic decorator callbacks
    fun decorate(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate(handle, decoratorBridge, track(transform))
    }
    fun decorateAlternateRow(transform: (Element) -> Element, modulo: Int = 2, shift: Int = 0) = apply {
        ftxui_table_selection_decorate_alternate_row(handle, decoratorBridge, track(transform), modulo, shift)
    }
    fun decorateAlternateColumn(transform: (Element) -> Element, modulo: Int = 2, shift: Int = 0) = apply {
        ftxui_table_selection_decorate_alternate_column(handle, decoratorBridge, track(transform), modulo, shift)
    }
    fun decorateBorder(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_border(handle, decoratorBridge, track(transform))
    }
    fun decorateBorderLeft(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_border_left(handle, decoratorBridge, track(transform))
    }
    fun decorateBorderRight(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_border_right(handle, decoratorBridge, track(transform))
    }
    fun decorateBorderTop(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_border_top(handle, decoratorBridge, track(transform))
    }
    fun decorateBorderBottom(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_border_bottom(handle, decoratorBridge, track(transform))
    }
    fun decorateSeparator(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_separator(handle, decoratorBridge, track(transform))
    }
    fun decorateSeparatorVertical(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_separator_vertical(handle, decoratorBridge, track(transform))
    }
    fun decorateSeparatorHorizontal(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_separator_horizontal(handle, decoratorBridge, track(transform))
    }
    fun decorateCells(transform: (Element) -> Element) = apply {
        ftxui_table_selection_decorate_cells(handle, decoratorBridge, track(transform))
    }
    fun decorateCellsAlternateRow(transform: (Element) -> Element, modulo: Int = 2, shift: Int = 0) = apply {
        ftxui_table_selection_decorate_cells_alternate_row(handle, decoratorBridge, track(transform), modulo, shift)
    }
    fun decorateCellsAlternateColumn(transform: (Element) -> Element, modulo: Int = 2, shift: Int = 0) = apply {
        ftxui_table_selection_decorate_cells_alternate_column(handle, decoratorBridge, track(transform), modulo, shift)
    }

    // -- Convenience helpers (matching pre-existing pattern)
    fun decorateBold() = apply { ftxui_table_selection_decorate_bold(handle) }
    fun decorateCellsAlignRight() = apply { ftxui_table_selection_decorate_cells_align_right(handle) }
    fun decorateCellsColor(color: Color) = apply { ftxui_table_selection_decorate_cells_color(handle, color.handle) }
    fun decorateCellsColorAlternateRow(color: Color, modulo: Int, offset: Int) =
        apply { ftxui_table_selection_decorate_cells_color_alternate_row(handle, color.handle, modulo, offset) }
}

// -- Window component

class WindowOptions(
    val inner: Component? = null,
    val title: String? = null,
    val left: IntState? = null,
    val top: IntState? = null,
    val width: IntState? = null,
    val height: IntState? = null,
    val leftDefault: Int = 0,
    val topDefault: Int = 0,
    val widthDefault: Int = 20,
    val heightDefault: Int = 10,
)

fun windowComponent(options: WindowOptions): Component = memScoped {
    val opts = cValue<ftxui_window_options_t> {
        this.inner = options.inner?.handle
        this.title = options.title?.cstr?.getPointer(this@memScoped)
        this.left = options.left?.ptr
        this.top = options.top?.ptr
        this.width = options.width?.ptr
        this.height = options.height?.ptr
        this.left_default = options.leftDefault
        this.top_default = options.topDefault
        this.width_default = options.widthDefault
        this.height_default = options.heightDefault
    }
    Component(ftxui_component_window(opts)!!)
}

// -- Loop

class FtxUILoop internal constructor(
    private val loopHandle: ftxui_loop_handle_t,
    val app: FtxUIApp,
) : AutoCloseable {
    fun hasQuitted(): Boolean = ftxui_loop_has_quitted(loopHandle)
    fun runOnce() = ftxui_loop_run_once(loopHandle)
    fun runOnceBlocking() = ftxui_loop_run_once_blocking(loopHandle)
    fun run() { while (!hasQuitted()) runOnceBlocking() }
    fun destroy() = ftxui_loop_destroy(loopHandle)
    override fun close() = destroy()

    companion object {
        operator fun invoke(app: FtxUIApp, component: Component): FtxUILoop =
            FtxUILoop(ftxui_loop_create(app.handle, component.handle)!!, app)
    }
}

// -- ColorInfo

data class ColorInfo(
    val index256: Int,
    val index16: Int,
    val name: String,
    val red: Int,
    val green: Int,
    val blue: Int,
    val hue: Int,
    val saturation: Int,
    val hsvValue: Int,
)

private fun ftxui_color_info_t.toKotlin() = ColorInfo(
    index256 = index_256,
    index16 = index_16.toInt(),
    name = name?.toKString() ?: "",
    red = red.toInt(),
    green = green.toInt(),
    blue = blue.toInt(),
    hue = hue.toInt(),
    saturation = saturation.toInt(),
    hsvValue = value.toInt(),
)

fun colorInfoSorted2D(): List<List<ColorInfo>> = memScoped {
    val rows = alloc<IntVar>()
    val cols = alloc<IntVar>()
    val data = ftxui_color_info_sorted_2d(rows.ptr, cols.ptr) ?: return emptyList()
    val numRows = rows.value
    val numCols = cols.value
    val result = (0 until numRows).map { r ->
        (0 until numCols).mapNotNull { c ->
            val entry = data[r * numCols + c]
            if (entry.index_256 == -1) null else entry.toKotlin()
        }
    }
    ftxui_color_info_free(data)
    result
}

fun getColorInfo256(index: ftxui_palette256_t): ColorInfo = ftxui_color_info_get_256(index).useContents { toKotlin() }
fun getColorInfo16(index: ftxui_palette16_t): ColorInfo = ftxui_color_info_get_16(index).useContents { toKotlin() }

// -- Easing functions

typealias EasingFunction = (Float) -> Float

enum class EasingType(internal val cValue: ftxui_easing_function_type_t) {
    Linear(ftxui_easing_function_type_t.FTXUI_EASING_LINEAR),
    QuadraticIn(ftxui_easing_function_type_t.FTXUI_EASING_QUADRATIC_IN),
    QuadraticOut(ftxui_easing_function_type_t.FTXUI_EASING_QUADRATIC_OUT),
    QuadraticInOut(ftxui_easing_function_type_t.FTXUI_EASING_QUADRATIC_IN_OUT),
    CubicIn(ftxui_easing_function_type_t.FTXUI_EASING_CUBIC_IN),
    CubicOut(ftxui_easing_function_type_t.FTXUI_EASING_CUBIC_OUT),
    CubicInOut(ftxui_easing_function_type_t.FTXUI_EASING_CUBIC_IN_OUT),
    QuarticIn(ftxui_easing_function_type_t.FTXUI_EASING_QUARTIC_IN),
    QuarticOut(ftxui_easing_function_type_t.FTXUI_EASING_QUARTIC_OUT),
    QuarticInOut(ftxui_easing_function_type_t.FTXUI_EASING_QUARTIC_IN_OUT),
    QuinticIn(ftxui_easing_function_type_t.FTXUI_EASING_QUINTIC_IN),
    QuinticOut(ftxui_easing_function_type_t.FTXUI_EASING_QUINTIC_OUT),
    QuinticInOut(ftxui_easing_function_type_t.FTXUI_EASING_QUINTIC_IN_OUT),
    SineIn(ftxui_easing_function_type_t.FTXUI_EASING_SINE_IN),
    SineOut(ftxui_easing_function_type_t.FTXUI_EASING_SINE_OUT),
    SineInOut(ftxui_easing_function_type_t.FTXUI_EASING_SINE_IN_OUT),
    CircularIn(ftxui_easing_function_type_t.FTXUI_EASING_CIRCULAR_IN),
    CircularOut(ftxui_easing_function_type_t.FTXUI_EASING_CIRCULAR_OUT),
    CircularInOut(ftxui_easing_function_type_t.FTXUI_EASING_CIRCULAR_IN_OUT),
    ExponentialIn(ftxui_easing_function_type_t.FTXUI_EASING_EXPONENTIAL_IN),
    ExponentialOut(ftxui_easing_function_type_t.FTXUI_EASING_EXPONENTIAL_OUT),
    ExponentialInOut(ftxui_easing_function_type_t.FTXUI_EASING_EXPONENTIAL_IN_OUT),
    ElasticIn(ftxui_easing_function_type_t.FTXUI_EASING_ELASTIC_IN),
    ElasticOut(ftxui_easing_function_type_t.FTXUI_EASING_ELASTIC_OUT),
    ElasticInOut(ftxui_easing_function_type_t.FTXUI_EASING_ELASTIC_IN_OUT),
    BackIn(ftxui_easing_function_type_t.FTXUI_EASING_BACK_IN),
    BackOut(ftxui_easing_function_type_t.FTXUI_EASING_BACK_OUT),
    BackInOut(ftxui_easing_function_type_t.FTXUI_EASING_BACK_IN_OUT),
    BounceIn(ftxui_easing_function_type_t.FTXUI_EASING_BOUNCE_IN),
    BounceOut(ftxui_easing_function_type_t.FTXUI_EASING_BOUNCE_OUT),
    BounceInOut(ftxui_easing_function_type_t.FTXUI_EASING_BOUNCE_IN_OUT),
}

fun easing(type: EasingType): EasingFunction {
    val fn = ftxui_easing_function_get(type.cValue)!!
    return { progress -> fn(progress) }
}
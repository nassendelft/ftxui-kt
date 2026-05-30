package nl.ncaj.dsl

import nl.ncaj.*
import kotlin.reflect.KMutableProperty0

// KMutableProperty0<String> overloads missing in ftxui-kt.
// The StringState is managed by AppScope and freed after the app loop exits.

fun AppScope.input(
    content: KMutableProperty0<String>,
    placeholder: String = "",
): Component = stringSync(content) { state -> nl.ncaj.input(state, placeholder) }

fun AppScope.inputPassword(
    content: KMutableProperty0<String>,
    placeholder: String = "",
): Component = stringSync(content) { state -> nl.ncaj.inputPassword(state, placeholder) }

// KMutableProperty0<Int> overload missing in ftxui-kt for menuHorizontal.
fun AppScope.menuHorizontal(
    entries: List<String>,
    selected: KMutableProperty0<Int>,
): Component = intSync(selected) { state -> nl.ncaj.menuHorizontal(entries, state) }

// KMutableProperty0<Float> overloads missing in ftxui-kt for float slider.
fun AppScope.slider(
    label: String,
    value: KMutableProperty0<Float>,
    min: Float,
    max: Float,
    increment: Float = 1f,
): Component = floatSync(value) { state -> nl.ncaj.slider(label, state, min, max, increment) }

fun AppScope.slider(
    value: KMutableProperty0<Float>,
    min: Float,
    max: Float,
    increment: Float = 1f,
    onChange: () -> Unit,
): Component = floatSync(value) { state -> nl.ncaj.slider(state, min, max, increment, onChange) }

fun AppScope.slider(
    value: KMutableProperty0<Float>,
    min: Float,
    max: Float,
    increment: Float = 1f,
    direction: Direction,
    colorActive: Color? = null,
    colorInactive: Color? = null,
): Component = floatSync(value) { state ->
    nl.ncaj.slider(state, min, max, increment, direction, colorActive, colorInactive)
}

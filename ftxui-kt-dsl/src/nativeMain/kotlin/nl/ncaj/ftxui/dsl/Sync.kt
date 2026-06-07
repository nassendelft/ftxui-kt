package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.*
import kotlin.reflect.KMutableProperty0

internal fun AppScope.stringSync(
    prop: KMutableProperty0<String>,
    createInner: (StringState) -> Component,
): Component {
    val state = track(StringState(prop.get()))
    var lastKotlin = prop.get()
    val inner = createInner(state)
    return renderer(child = inner) {
        val current = prop.get()
        if (current != lastKotlin) {
            state.value = current
            lastKotlin = current
        } else {
            val fromNative = state.value
            if (fromNative != lastKotlin) {
                prop.set(fromNative)
                lastKotlin = fromNative
            }
        }
        inner.render()
    }
}

internal fun AppScope.intSync(
    prop: KMutableProperty0<Int>,
    createInner: (IntState) -> Component,
): Component {
    val state = track(IntState(prop.get()))
    var lastKotlin = prop.get()
    val inner = createInner(state)
    return renderer(child = inner) {
        val current = prop.get()
        if (current != lastKotlin) {
            state.value = current
            lastKotlin = current
        } else {
            val fromNative = state.value
            if (fromNative != lastKotlin) {
                prop.set(fromNative)
                lastKotlin = fromNative
            }
        }
        inner.render()
    }
}

internal fun AppScope.floatSync(
    prop: KMutableProperty0<Float>,
    createInner: (FloatState) -> Component,
): Component {
    val state = track(FloatState(prop.get()))
    var lastKotlin = prop.get()
    val inner = createInner(state)
    return renderer(child = inner) {
        val current = prop.get()
        if (current != lastKotlin) {
            state.value = current
            lastKotlin = current
        } else {
            val fromNative = state.value
            if (fromNative != lastKotlin) {
                prop.set(fromNative)
                lastKotlin = fromNative
            }
        }
        inner.render()
    }
}

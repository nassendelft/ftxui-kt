package nl.ncaj.dsl

import nl.ncaj.*
import kotlin.reflect.KMutableProperty0

class ContainerScope {
    internal val children = mutableListOf<Component>()
    operator fun Component.unaryPlus() { children.add(this) }
}

fun vertical(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { nl.ncaj.vertical(*it.toTypedArray()) }

fun horizontal(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { nl.ncaj.horizontal(*it.toTypedArray()) }

fun stacked(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { children ->
        nl.ncaj.stacked().also { c -> children.forEach(c::add) }
    }

// tab needs AppScope.track() to manage the IntState lifetime.
fun AppScope.tab(selected: KMutableProperty0<Int>, block: ContainerScope.() -> Unit): Component =
    intSync(selected) { state ->
        val container = nl.ncaj.tab(state)
        ContainerScope().apply(block).children.forEach(container::add)
        container
    }

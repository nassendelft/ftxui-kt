package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.*
import kotlin.reflect.KMutableProperty0

class ContainerScope {
    internal val children = mutableListOf<Component>()
    operator fun Component.unaryPlus() { children.add(this) }
}

fun vertical(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { nl.ncaj.ftxui.vertical(*it.toTypedArray()) }

fun horizontal(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { nl.ncaj.ftxui.horizontal(*it.toTypedArray()) }

fun stacked(block: ContainerScope.() -> Unit): ContainerComponent =
    ContainerScope().apply(block).children.let { children ->
        nl.ncaj.ftxui.stacked().also { c -> children.forEach(c::add) }
    }

// tab needs AppScope.track() to manage the IntState lifetime.
fun AppScope.tab(selected: KMutableProperty0<Int>, block: ContainerScope.() -> Unit): Component =
    intSync(selected) { state ->
        val container = nl.ncaj.ftxui.tab(state)
        ContainerScope().apply(block).children.forEach(container::add)
        container
    }

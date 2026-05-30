package nl.ncaj.dsl

import nl.ncaj.Element

class ElementScope {
    internal val elements = mutableListOf<Element>()
    operator fun Element.unaryPlus() { elements.add(this) }
}

fun vbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.vbox(*it.toTypedArray()) }

fun hbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.hbox(*it.toTypedArray()) }

fun dbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.dbox(*it.toTypedArray()) }

fun hflow(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.hflow(*it.toTypedArray()) }

fun vflow(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.vflow(*it.toTypedArray()) }

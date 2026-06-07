package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.Element

class ElementScope {
    internal val elements = mutableListOf<Element>()
    operator fun Element.unaryPlus() { elements.add(this) }
}

fun vbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.ftxui.vbox(*it.toTypedArray()) }

fun hbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.ftxui.hbox(*it.toTypedArray()) }

fun dbox(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.ftxui.dbox(*it.toTypedArray()) }

fun hflow(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.ftxui.hflow(*it.toTypedArray()) }

fun vflow(block: ElementScope.() -> Unit): Element =
    ElementScope().apply(block).elements.let { nl.ncaj.ftxui.vflow(*it.toTypedArray()) }

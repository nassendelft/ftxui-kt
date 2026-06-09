package nl.ncaj.ftxui.dsl

import nl.ncaj.ftxui.*

class AppScope internal constructor(internal val app: FtxUIApp) {
    // Holds references to native-resource wrappers (states, tables, canvases, gradients, …)
    // so they stay reachable — and are therefore not freed by their Cleaners — for as long
    // as the app loop is running. They become collectable once runApp() returns.
    private val resources = mutableListOf<Any>()

    internal fun <T : Any> track(r: T): T = r.also { resources.add(it) }

    fun exit() = app.exit()
    fun post(block: () -> Unit) = app.post(block)
    fun poll(onPoll: () -> Unit): Component = nl.ncaj.ftxui.poll(app, onPoll)
    fun requestAnimationFrame() = app.requestAnimationFrame()
    fun selectionChange(callback: () -> Unit) = app.selectionChange(callback)
    fun getSelection(): String = app.getSelection()
    fun trackMouse(enable: Boolean = true) = app.trackMouse(enable)
    fun forceHandleCtrlC(force: Boolean = true) = app.forceHandleCtrlC(force)
    fun forceHandleCtrlZ(force: Boolean = true) = app.forceHandleCtrlZ(force)
}

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

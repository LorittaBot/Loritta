package net.perfectdreams.loritta.website.frontend.views

abstract class DokyoView {
    // Mostly used for link caching: Data that should be loaded before the page is switched
    // DOM is not ready yet during onPreLoad!
    open suspend fun onPreLoad() {}

    // On page load
    // DOM is ready and can be manipulated
    open suspend fun onLoad() {}

    // On page unload
    open suspend fun onUnload() {}
}
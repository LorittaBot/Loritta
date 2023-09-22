package net.perfectdreams.loritta.cinnamon.dashboard.backend.utils

import io.ktor.server.routing.*

class BaseRouteManager(val onRouteCreation: Route.(String) -> (Unit)) {
    private val ktorRoutes = mutableMapOf<String, Route>()

    fun register(ktorRoute: Route, baseRoute: BaseRoute) {
        val currentRoute = ktorRoutes[baseRoute.path] ?: run {
            val nRoute = ktorRoute.route(baseRoute.path) {
                onRouteCreation.invoke(this, baseRoute.path)
            }

            ktorRoutes[baseRoute.path] = nRoute
            nRoute
        }

        // TODO: The empty path feels nasty, how can we improve this?
        baseRoute.registerWithPath(currentRoute, "")
    }
}
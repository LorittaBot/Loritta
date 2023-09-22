package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.paths

import net.perfectdreams.loritta.cinnamon.dashboard.common.ScreenPathElement
import net.perfectdreams.loritta.cinnamon.dashboard.common.buildToPathWithQueryArguments

class ScreenPathWithArguments(
    val path: ScreenPath,
    val pathArguments: Map<String, String>,
    val queryArguments: Map<String, String>
) {
    init {
        for (element in path.elements) {
            when (element) {
                is ScreenPathElement.OptionPathElement -> {
                    if (!pathArguments.containsKey(element.parameterId))
                        error("Missing argument for parameter ${element.parameterId}!")
                }
                is ScreenPathElement.StringPathElement -> {}
            }
        }
    }

    fun build() = path.elements.buildToPathWithQueryArguments(pathArguments, queryArguments)
}
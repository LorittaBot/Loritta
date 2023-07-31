package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.FillContentLoadingSection
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import org.jetbrains.compose.web.dom.Text

@Composable
inline fun <reified T> ResourceChecker(
    i18nContext: I18nContext,
    resource: Resource<T>,
    block: @Composable (T) -> (Unit)
) {
    when (resource) {
        is Resource.Loading -> FillContentLoadingSection(i18nContext)
        is Resource.Failure -> {
            Text("Algo deu errado ao carregar a configuração")
        }
        is Resource.Success -> block.invoke(resource.value)
    }
}

@Composable
inline fun <reified A, reified B> ResourceChecker(
    i18nContext: I18nContext,
    resource: Resource<A>,
    resource2: Resource<B>,
    block: @Composable (A, B) -> (Unit)
) {
    when {
        resource is Resource.Loading || resource2 is Resource.Loading -> FillContentLoadingSection(i18nContext)
        resource is Resource.Failure || resource2 is Resource.Failure -> {
            Text("Algo deu errado ao carregar a configuração")
        }
        resource is Resource.Success && resource2 is Resource.Success -> block.invoke(resource.value, resource2.value)
    }
}
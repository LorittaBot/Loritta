package net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.userdash

import androidx.compose.runtime.Composable
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.FillContentErrorSection
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.components.FillContentLoadingSection
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.GuildScopedResponseException
import net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils.Resource
import org.jetbrains.compose.web.dom.Text

@Composable
inline fun <reified T> ResourceChecker(
    i18nContext: I18nContext,
    resource: Resource<T>,
    block: @Composable (T) -> (Unit)
) {
    when (resource) {
        is Resource.Failure -> {
            ResourceExceptionChecker(i18nContext, (resource as? Resource.Failure)?.exception)
        }
        is Resource.Loading -> FillContentLoadingSection(i18nContext)
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
        resource is Resource.Failure || resource2 is Resource.Failure -> {
            ResourceExceptionChecker(i18nContext, (resource as? Resource.Failure)?.exception ?: (resource2 as? Resource.Failure)?.exception)
        }
        resource is Resource.Loading || resource2 is Resource.Loading -> FillContentLoadingSection(i18nContext)
        resource is Resource.Success && resource2 is Resource.Success -> block.invoke(resource.value, resource2.value)
    }
}

@Composable
fun ResourceExceptionChecker(
    i18nContext: I18nContext,
    exception: Exception?
) {
    when (exception) {
        is GuildScopedResponseException -> {
            when (exception.type) {
                GuildScopedResponseException.GuildScopedErrorType.InvalidDiscordAuthorization -> TODO()
                GuildScopedResponseException.GuildScopedErrorType.MissingPermission -> FillContentErrorSection("Você não tem permissão para configurar este servidor!")
                GuildScopedResponseException.GuildScopedErrorType.UnknownGuild -> FillContentErrorSection("A Loritta não está neste servidor!")
                GuildScopedResponseException.GuildScopedErrorType.UnknownMember -> FillContentErrorSection("Servidor existe... mas você não está no servidor!")
            }
        }

        else -> Text("Algo deu errado ao carregar a configuração")
    }
}
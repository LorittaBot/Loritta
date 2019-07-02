package net.perfectdreams.spicymorenitta.utils

import io.ktor.client.request.patch
import io.ktor.client.response.HttpResponse
import io.ktor.client.response.readText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.http
import kotlin.browser.window

object WebsiteUtils : Logging {
    fun getUrlWithLocale(): String {
        val href = window.location.href
        val split = href.split("/")
        if (4 >= split.size)
            return href

        return split.subList(0, 4).joinToString("/")
    }

    fun getPathWithoutLocale(): String {
        var href = window.location.pathname
        if (!href.endsWith("/"))
            href += "/"

        val split = href.split("/")
        if (3 > split.size)
            return href

        var result = "/" + split.drop(2).joinToString("/")
        if (!result.endsWith("/"))
            result += "/"

        return result
    }

    fun getWebsiteLocaleIdViaPath(): String {
        val href = window.location.href
        val split = href.split("/")
        return split.dropLast(1).last()
    }

    fun patchGuildConfigById(
            id: String,
            patchCode: Int,
            data: Any
    ) {
        val obj = object {}.asDynamic()
        obj.patchCode = patchCode
        obj.data = data

        GlobalScope.launch {
            SpicyMorenitta.INSTANCE.showLoadingScreen("Salvando...")

            val result = http.patch<HttpResponse>("https://spicy.loritta.website/api/v1/guilds/$id/config") {
                body = JSON.stringify(data)
            }

            val asJson = result.readText()
            debug(asJson)

            SpicyMorenitta.INSTANCE.hideLoadingScreen()

            if (result.status != HttpStatusCode.OK) {
                error("Something went wrong! ${result.status}")
            }
        }
    }

    fun canManageGuild(g: TemmieDiscordGuild) = getUserPermissionLevel(g).canAddBots

    fun getUserPermissionLevel(g: TemmieDiscordGuild): UserPermissionLevel {
        val isAdministrator = g.permissions shr 3 and 1 == 1
        val isManager = g.permissions shr 5 and 1 == 1
        return when {
            g.owner -> WebsiteUtils.UserPermissionLevel.OWNER
            isAdministrator -> WebsiteUtils.UserPermissionLevel.ADMINISTRATOR
            isManager -> WebsiteUtils.UserPermissionLevel.MANAGER
            else -> WebsiteUtils.UserPermissionLevel.MEMBER
        }
    }

    enum class UserPermissionLevel(val canAddBots: Boolean) {
        OWNER(true),
        ADMINISTRATOR(true),
        MANAGER(true),
        MEMBER(false)
    }
}
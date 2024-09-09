package net.perfectdreams.loritta.morenitta.listeners

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.components.ComponentContext
import net.perfectdreams.loritta.morenitta.interactions.vanilla.economy.BetCommand
import net.perfectdreams.loritta.morenitta.utils.GuildLorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import java.util.*

class CoinFlipBetGlobalListener(val m: LorittaBot) : ListenerAdapter() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val guild = event.guild
        val member = event.member

        GlobalScope.launch {
            if (event.componentId.startsWith("betglobal")) {
                val context: ComponentContext?
                val i18nContext: I18nContext?
                val quantity = event.componentId.substringAfter(":").toLong()

                try {
                    val serverConfigJob = if (guild != null)
                        m.getOrCreateServerConfigDeferred(guild.idLong, true)
                    else
                        m.getOrCreateServerConfigDeferred(-1, true)

                    val lorittaProfileJob = m.getLorittaProfileDeferred(event.user.idLong)

                    val serverConfig = serverConfigJob.await()
                    val lorittaProfile = lorittaProfileJob.await()

                    val currentLocale = m.newSuspendedTransaction {
                        (lorittaProfile?.settings?.language ?: serverConfig.localeId)
                    }

                    val locale = m.localeManager.getLocaleById(currentLocale)

                    i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(locale.id)

                    val lorittaUser = if (guild != null && !guild.isDetached && member != null) {
                        val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(m, guild)
                        val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(
                            member,
                            rolesLorittaPermissions
                        )
                        GuildLorittaUser(m, member, memberLorittaPermissions, lorittaProfile)
                    } else {
                        LorittaUser(m, event.user, EnumSet.noneOf(LorittaPermission::class.java), lorittaProfile)
                    }

                    context = ComponentContext(
                        m,
                        serverConfig,
                        lorittaUser,
                        locale,
                        i18nContext,
                        event
                    )

                    context.deferEdit()

                    BetCommand.addToMatchmakingQueue(
                        context,
                        quantity
                    )
                } catch (e: Exception) {
                    logger.error(e) { "Error while processing betglobal button interaction" }
                }
            }
        }
    }
}
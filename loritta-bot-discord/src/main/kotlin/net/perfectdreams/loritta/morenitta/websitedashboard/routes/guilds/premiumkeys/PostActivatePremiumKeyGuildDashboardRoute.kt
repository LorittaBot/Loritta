package net.perfectdreams.loritta.morenitta.websitedashboard.routes.guilds.premiumkeys

import io.ktor.server.application.*
import io.ktor.server.util.getOrFail
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.guildPremiumKeysAndPremiumInfoPlan
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresGuildAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondConfigSaved
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class PostActivatePremiumKeyGuildDashboardRoute(website: LorittaDashboardWebServer) : RequiresGuildAuthDashboardLocalizedRoute(website, "/premium-keys/{premiumKeyId}/activate") {
    override suspend fun onAuthenticatedGuildRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings, guild: Guild, guildPremiumPlan: ServerPremiumPlans, member: Member) {
        val premiumKeyId = call.parameters.getOrFail("premiumKeyId").toLong()

        website.loritta.newSuspendedTransaction {
            val serverConfig = website.loritta.getOrCreateServerConfig(guild.idLong)

            DonationKeys.update({ DonationKeys.id eq premiumKeyId and (DonationKeys.userId eq session.userId) }) {
                it[DonationKeys.activeIn] = serverConfig.id
            }
        }

        val guildPremiumKeys = website.loritta.transaction {
            DonationKeys.selectAll()
                .where {
                    DonationKeys.activeIn eq guild.idLong and (DonationKeys.expiresAt greaterEq System.currentTimeMillis())
                }
                .toList()
        }

        val userPremiumKeys = website.loritta.transaction {
            DonationKeys.selectAll()
                .where {
                    DonationKeys.userId eq session.userId and (DonationKeys.expiresAt greaterEq System.currentTimeMillis())
                }
                .toList()
        }

        val plan = ServerPremiumPlans.getPlanFromValue(guildPremiumKeys.sumOf { it[DonationKeys.value] })

        call.respondConfigSaved(i18nContext) {
            guildPremiumKeysAndPremiumInfoPlan(
                i18nContext,
                guild,
                session,
                plan,
                guildPremiumKeys,
                userPremiumKeys
            )
        }
    }
}
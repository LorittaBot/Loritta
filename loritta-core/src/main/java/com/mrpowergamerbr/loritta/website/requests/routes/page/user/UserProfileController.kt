package com.mrpowergamerbr.loritta.website.requests.routes.page.user

import com.github.salomonbrys.kotson.fromJson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.social.PerfilCommand
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LoriForceReauthentication
import com.mrpowergamerbr.loritta.website.LoriRequiresVariables
import com.mrpowergamerbr.loritta.website.LoriWebCodes
import com.mrpowergamerbr.loritta.website.evaluate
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Emote
import net.dv8tion.jda.api.entities.Guild
import org.jetbrains.exposed.sql.transactions.transaction
import org.jooby.Request
import org.jooby.Response
import org.jooby.mvc.GET
import org.jooby.mvc.Local
import org.jooby.mvc.Path
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@Path("/:localeId/user/:userId")
class UserProfileController {
    @GET
    @LoriRequiresVariables(true)
    @LoriForceReauthentication(true)
    fun handle(req: Request, res: Response, userId: String, @Local variables: MutableMap<String, Any?>): String {
        val user = runBlocking { lorittaShards.retrieveUserById(userId)!! }
        val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

        var userIdentification: TemmieDiscordAuth.UserIdentification? = null

        if (!req.session().isSet("discordAuth")) {
            variables["selfProfile"] = Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
        } else {
            try {
                val discordAuth = Loritta.GSON.fromJson<TemmieDiscordAuth>(req.session()["discordAuth"].value())
                discordAuth.isReady(true)
                userIdentification = discordAuth.getUserIdentification() // Vamos pegar qualquer coisa para ver se não irá dar erro
                val profile = loritta.getOrCreateLorittaProfile(userIdentification.id)

                // variables["selfProfile"] = Loritta.GSON.toJson(profile)
            } catch (e: Exception) {
                // variables["selfProfile"] = Loritta.GSON.toJson(mapOf("api:code" to LoriWebCodes.UNAUTHORIZED))
            }
        }

        variables["profileUser"] = user
        variables["lorittaProfile"] = lorittaProfile

        variables["profileSettings"] = transaction(Databases.loritta) {
            lorittaProfile.settings
        }

        variables["badgesBase64"] = PerfilCommand.getUserBadges(user, lorittaProfile).map {
            val baos = ByteArrayOutputStream()
            ImageIO.write(it, "png", baos)
            Base64.getEncoder().encodeToString(baos.toByteArray())
        }

        variables["favoriteEmotes"] = listOf<Emote>()

        val mutualGuilds = lorittaShards.getMutualGuilds(user).sortedByDescending { it.members.size }

        val serverConfigs = loritta.serversColl.find(
                Filters.`in`("_id", mutualGuilds.map { it.id })
        ).toMutableList()

        variables["mutualGuilds"] = mutualGuilds

        val ownerOfGuilds = mutableListOf<Guild>()
        val inGuildListEnabled = mutableListOf<Guild>()
        val notInGuildList = mutableListOf<Guild>()
        val onlineCount = mutableMapOf<Guild, Int>()

        for (guild in mutualGuilds) {
            val serverConfig = serverConfigs.firstOrNull { it.guildId == guild.id } ?: continue

            if (guild.owner?.user == user && serverConfig.serverListConfig.isEnabled) {
                ownerOfGuilds.add(guild)
                onlineCount[guild] = guild.members.count { it.onlineStatus != OnlineStatus.OFFLINE }
            }
            if (serverConfig.serverListConfig.isEnabled) {
                inGuildListEnabled.add(guild)
            } else {
                notInGuildList.add(guild)
            }
        }

        variables["ownerOfGuilds"] = ownerOfGuilds.sortedByDescending { it.members.size }
        variables["inGuildListEnabled"] = inGuildListEnabled
        variables["notInGuildList"] = notInGuildList
        variables["onlineCount"] = onlineCount

        return evaluate("profile.html", variables)
    }
}
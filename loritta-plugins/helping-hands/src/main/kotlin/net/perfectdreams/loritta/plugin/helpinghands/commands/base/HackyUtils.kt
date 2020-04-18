package net.perfectdreams.loritta.plugin.helpinghands.commands.base

import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import org.jetbrains.exposed.sql.transactions.transaction

fun Message.toJDA() = (this as DiscordMessage).handle
fun User.toJDA() = (this as JDAUser).handle

fun Profile.dbRefresh() = transaction(Databases.loritta) { this@dbRefresh.refresh() }
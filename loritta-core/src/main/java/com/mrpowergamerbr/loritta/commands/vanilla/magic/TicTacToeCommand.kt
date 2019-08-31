package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.tictactoe.TicTacToeRoom
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import org.apache.commons.lang3.RandomStringUtils

class TicTacToeCommand : AbstractCommand("tictactoe", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Executa códigos em JavaScript usando a sandbox de comandos da Loritta"
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		// Iremos criar uma sala utilizando mágica
		val randomRoomId = RandomStringUtils.random(24, 0, 62, true, true, *"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".toCharArray())
		val room = TicTacToeRoom()
		loritta.ticTacToeServer.rooms[randomRoomId] = room

		context.reply(
				LoriReply(
						"Sala criada com sucesso! ${loritta.instanceConfig.loritta.website.url}tictactoe?r=${randomRoomId}"
				)
		)
	}
}
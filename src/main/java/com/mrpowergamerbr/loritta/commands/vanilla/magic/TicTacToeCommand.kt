package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.tictactoe.TicTacToeRoom
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import org.apache.commons.lang3.RandomStringUtils

class TicTacToeCommand : AbstractCommand("tictactoe", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Executa códigos em JavaScript usando a sandbox de comandos da Loritta"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		// Iremos criar uma sala utilizando mágica
		val randomRoomId = RandomStringUtils.random(24, 0, 62, true, true, *"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890".toCharArray())
		val room = TicTacToeRoom()
		loritta.ticTacToeServer.rooms[randomRoomId] = room

		context.reply(
				LoriReply(
						"Sala criada com sucesso! ${Loritta.config.websiteUrl}tictactoe?r=${randomRoomId}"
				)
		)
	}
}
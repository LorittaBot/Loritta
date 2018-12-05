package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.dao.Timer
import com.mrpowergamerbr.loritta.network.Databases
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
	transaction(Databases.loritta) {
		Timer.new {
			guildId = 297732013006389252
			channelId = 51994076912195993
			startsAt = 0
			repeatDelay = 30000
			activeOnDays = arrayOf()
			commands = arrayOf(
					"Olá, mundo! <:lori_owo:417813932380520448>",
					"Eu sou muito fofis <:eu_te_moido:366047906689581085>",
					"Isto é apenas um sistema experimental de timers! Em breve no meu painel de configuração para você configurar e se divertir!",
					"Stay awesome :3"
			)
		}
	}
}
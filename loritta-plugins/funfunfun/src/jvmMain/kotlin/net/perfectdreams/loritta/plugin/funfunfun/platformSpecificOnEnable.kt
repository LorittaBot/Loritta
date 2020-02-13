package net.perfectdreams.loritta.plugin.funfunfun

import net.perfectdreams.loritta.plugin.funfunfun.commands.RandomSAMCommand

actual fun FunFunFunPlugin.platformSpecificOnEnable() {
	registerCommand(RandomSAMCommand.command(this.loritta))
}
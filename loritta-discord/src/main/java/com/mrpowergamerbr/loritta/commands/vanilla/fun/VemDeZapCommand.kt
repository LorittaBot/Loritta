package com.mrpowergamerbr.loritta.commands.vanilla.`fun`

import com.mrpowergamerbr.loritta.Loritta.Companion.RANDOM
import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class VemDeZapCommand : AbstractCommand("vemdezap", category = CommandCategory.FUN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale().getWithType("commands.entertainment.vemDeZap.description")
	}

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return locale.toNewLocale().getWithType("commands.entertainment.vemDeZap.examples")
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.TEXT) {
				optional = false
			}
		}
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		// Baseado em http://vemdezapbe.be/ / https://github.com/vmarchesin/vemdezapbe.be
		// Que até agora eu não entendi porque fizeram uma API externa em vez de deixar tudo client-sided... mas vida que segue né
		// E pelo ou menos a versão da nossa querida Loritta não tem gemidão do zap aleatório ao fazer zap, quem coloca gemidão nas coisas
		// nem é gente e também nem merece os créditos públicos, 2bj

		if (context.args.isNotEmpty()) {
			val fullMatch = mapOf("100" to listOf("💯"),
					"alface" to listOf("🥗"),
					"alvo" to listOf("🎯"),
					"amo" to listOf("😍", "😻", "😘", "😗", "😙", "😚", "💘	", "❤", "💓", "💕", "💖", "💖"),
					"amor" to listOf("😍", "😻", "😘", "😗", "😙", "😚", "💘	", "❤", "💓", "💕", "💖", "💖"),
					"ap" to listOf("🏢"),
					"ape" to listOf("🏢"),
					"apice" to listOf("🔝", "🏔", "⛰", "🗻"),
					"arma" to listOf("🔫", "🔪", "💣💥"),
					"avalanche" to listOf("🏔", "❄", "☃"),
					"banda" to listOf("🎷", "🎸", "🎹", "🎺", "🎻", "🥁", "🎼", "🎵", "🎶", "🎤"),
					"bandas" to listOf("🎷", "🎸", "🎹", "🎺", "🎻", "🥁", "🎼", "🎵", "🎶", "🎤"),
					"banheira" to listOf("🛁"),
					"banheiro" to listOf("🚽"),
					"banho" to listOf("🚿", "🛁", "🧖‍♂️", "🧖‍♀️"),
					"bar" to listOf("🍺", "🍻", "🥃", "🍾", "🤮"),
					"beber" to listOf("🍺", "🍻", "🥃", "🍾", "🤮"),
					"bem" to listOf("☺"),
					"boa" to listOf("🤙"),
					"bolsa" to listOf("👜", "👝"),
					"bravo" to listOf("😤", "😤💦", "😖", "🙁", "😩", "😦", "😡", "🤬", "💣", "💢", "✋🛑", "☠"),
					"bumbum" to listOf("😮", "😏"),
					"carro" to listOf("🚐", "🚗"),
					"casa" to listOf("😋"),
					"casal" to listOf("💑"),
					"caso" to listOf("💑"),
					"celular" to listOf("📱"),
					"cerebro" to listOf("🧠", "💭"),
					"chama" to listOf("📞", "☎"),
					"chef" to listOf("👨‍🍳", "👩‍🍳"),
					"ciencia" to listOf("👩‍🔬", "👨‍🔬", "⚗", "🔬", "🔭", "📡"),
					"classe" to listOf("📚", "📘"),
					"consciencia" to listOf("🧠", "💭"),
					"coracao" to listOf("💘	", "❤", "💓", "💕", "💖", "💖"),
					"corra" to listOf("🏃"),
					"corre" to listOf("🏃"),
					"croissant" to listOf("🥐"),
					"dado" to listOf("🎲"),
					"data" to listOf("📅", "🗓"),
					"dinheiro" to listOf("💳", "💵", "💰", "💲"),
					"embuste" to listOf("😭", "🤢", "💥", "😘", "😜"),
					"escola" to listOf("👨‍🎓", "👩‍🎓", "📚", "📘", "🏫"),
					"faculdade" to listOf("👨‍🎓", "👩‍🎓", "📚", "📘"),
					"feio" to listOf("😛"),
					"feia" to listOf("😛"),
					"fora" to listOf("👉"),
					"fim" to listOf("🙅‍♂️", "🙅‍♀️"),
					"já" to listOf("⏰"),
					"internet" to listOf("🌐"),
					"madame" to listOf("🌹"),
					"marcial" to listOf("💪"),
					"marciais" to listOf("💪"),
					"mente" to listOf("🧠", "💭"),
					"moca" to listOf("🌹"),
					"mundo" to listOf("🌍"),
					"nada" to listOf("😮"),
					"nao" to listOf("⛔", "🚫", "🛑", "✋", "✋🛑", "⚠"),
					"oi" to listOf("😏", "😉"),
					"ok" to listOf("👌"),
					"papo" to listOf("💬"),
					"parabens" to listOf("🎈", "🎉", "🎊", "👏"),
					"pc" to listOf("💻", "🖥", "🖱⌨", "💾", "👨‍💻", "👩‍💻"),
					"planeta" to listOf("🌍"),
					"preco" to listOf("💳", "💵", "💰", "💲"),
					"princesa" to listOf("👸"),
					"principe" to listOf("🤴"),
					"quer" to listOf("😏"),
					"raio" to listOf("⚡"),
					"ri" to listOf("😅", "😂", "🤣"),
					"rir" to listOf("😅", "😂", "🤣"),
					"risada" to listOf("😅", "😂", "🤣"),
					"riso" to listOf("😅", "😂", "🤣"),
					"rola" to listOf("😒", "😝", "👉👌"),
					"sai" to listOf("🚫", "⛔"),
					"saliente" to listOf("😋"),
					"secreto" to listOf("🕵️‍"),
					"sera" to listOf("🤨", "🤔", "🧐"),
					"sexo" to listOf("😆", "👉👌"),
					"soco" to listOf("🥊"),
					"sono" to listOf("💤"),
					"sos" to listOf("🆘"),
					"susto" to listOf("😱", "🎃"),
					"terra" to listOf("🌍"),
					"tesao" to listOf("🌚"),
					"tiro" to listOf("🔫"),
					"tomar" to listOf("🍺", "🍻"),
					"topo" to listOf("🔝", "🏔", "⛰", "🗻"),
					"ve" to listOf("👀", "👁"),
					"vem" to listOf("🚐", "🏎"),
					"ver" to listOf("👀👀", "👀"),
					"voce" to listOf("👉"),
					"zumbi" to listOf("🧟‍♂️", "🧟‍♀️"),
					/* Abreviações/Girias */
					"aff" to listOf("🙄"),
					"bb" to listOf("👶", "😍", "😂", "😜", "💘"),
					"caraio" to listOf("😜", "😩", "😖", "☹", "😛", "😏", "😞"),
					"caralho" to listOf("😜", "😩", "😖", "☹", "😛", "😏", "😞"),
					"escroto" to listOf("👺", "👹", "👿"),
					"lol" to listOf("😅", "😂", "🤣"),
					"mozao" to listOf("💘", "❤", "💓", "💕", "💖", "💖"),
					"top" to listOf("😂👌", "👌", "🔝", "🤩"),
					"topper" to listOf("😂👌", "👌", "🔝", "🤩"),
					"topperson" to listOf("😂👌", "👌", "🔝", "😛", "🤩"),
					"uau" to listOf("😋"),
					"wow" to listOf("😋"),
					/* Comidas */
					"abacate" to listOf("🥑"),
					"amendoim" to listOf("🥜"),
					"bacon" to listOf("🥓"),
					"batata" to listOf("🍟", "🥔"),
					"berinjela" to listOf("🍆"),
					"biscoito" to listOf("🍪"),
					"bolacha" to listOf("🍪"),
					"brocolis" to listOf("🥦"),
					"castanha" to listOf("🌰"),
					"cenoura" to listOf("🥕"),
					"cerveja" to listOf("🍺", "🍻"),
					"cogumelo" to listOf("🍄"),
					"doce" to listOf("🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰", "🥧", "🍫", "🍬", "🍭", "🍮", "🍯"),
					"ovo" to listOf("🥚", "🍳"),
					"pepino" to listOf("🥒"),
					"pizza" to listOf("🍕"),
					"pretzel" to listOf("🥨"),
					"salada" to listOf("🥗"),
					"sanduiche" to listOf("🥪"),
					"sushi" to listOf("🍣", "🍙", "🍱", "🍘"),
					"trato" to listOf("🤝"),
					/* Empresas */
					"aliexpress" to listOf("🇨🇳"),
					"donalds" to listOf("🍔🍟"),
					"globo" to listOf("🌍"),
					"mcdonalds" to listOf("🍔🍟"),
					"sedex" to listOf("📦", "📬"),
					/* Esportes */
					"basquete" to listOf("🏀"),
					"futebol" to listOf("⚽"),
					"volei" to listOf("🏐"),
					/* Signos */
					"aries" to listOf("♈"),
					"touro" to listOf("♉"),
					"gemeos" to listOf("♊"),
					"cancer" to listOf("♋"),
					"leao" to listOf("♌"),
					"virgem" to listOf("♍"),
					"libra" to listOf("♎"),
					"escorpiao" to listOf("♏"),
					"sagitario" to listOf("♐"),
					"capricornio" to listOf("♑"),
					"aquario" to listOf("♒"),
					"peixes" to listOf("♓"),
					/* Personagens */
					"bolsonaro" to listOf("🚫🏳️‍🌈", "🔫"),
					"doria" to listOf("💩"),
					"lula" to listOf("💰", "🏢", "🦑"),
					"mario" to listOf("🍄"),
					"neymar" to listOf("😍"),
					"noel" to listOf("🎅"),
					"pabblo" to listOf("👩", "🏳️‍🌈👩"),
					"pabbllo" to listOf("👩", "🏳️‍🌈👩"),
					"pabllo" to listOf("👩", "🏳️‍🌈👩"),
					"temer" to listOf("🧛‍♂️", "🚫"),
					"vittar" to listOf("👩", "🏳️‍🌈👩"))

			val partialMatchAny = mapOf(
					"brasil" to listOf("🇧🇷"),
					"cabel" to listOf("💇‍♂️", "💇‍♀️"),
					"deus" to listOf("👼", "😇", "🙏", "🙏🙏"),
					"doid" to listOf("🤪"),
					"fuma" to listOf("🚬", "🚭"),
					"kk" to listOf("😅", "😂", "🤣"),
					"piment" to listOf("🌶"),
					"mort" to listOf("☠", "💀", "⚰", "👻"),
					"zap" to listOf("📞", "♣", "📱")
			)

			val partialMatchPrefix = mapOf(
					"abrac" to listOf("🤗"),
					"alema" to listOf("🇩🇪"),
					"alun" to listOf("👨‍🎓", "👩‍🎓"),
					"anjo" to listOf("😇"),
					"armad" to listOf("🔫", "🔪", "💣💥"),
					"arte" to listOf("🖌"),
					"assust" to listOf("😱", "🎃"),
					"ataq" to listOf("💣", "🔫"),
					"atenc" to listOf("👀"),
					"bunda" to listOf("😮", "😏"),
					"calad" to listOf("🤐"),
					"casad" to listOf("💏", "👩‍❤️‍💋‍👨", "👨‍❤️‍💋‍👨"),
					"chave" to listOf("🔑", "🗝"),
					"cheir" to listOf("👃"),
					"combat" to listOf("💣", "🔫", "🎖", "💪"),
					"computa" to listOf("💻", "🖥", "🖱⌨", "💾", "👨‍💻", "👩‍💻"),
					"comun" to listOf("🇷🇺"),
					"combin" to listOf("🤝"),
					"condec" to listOf("🎖"),
					"conhec" to listOf("🧠", "💭"),
					"content" to listOf("😀", "😁", "😃", "😄", "😊", "🙂", "☺"),
					"correr" to listOf("🏃"),
					"corrid" to listOf("🏃"),
					"danca" to listOf("💃", "🕺"),
					"dance" to listOf("💃", "🕺"),
					"desculpa" to listOf("😅"),
					"docei" to listOf("🍦", "🍧", "🍨", "🍩", "🍪", "🎂", "🍰", "🥧", "🍫", "🍬", "🍭", "🍮", "🍯"),
					"doen" to listOf("😷", "🤒", "🤕", "🤢", "🤮", "🤧"),
					"enjo" to listOf("🤢", "🤮"),
					"espia" to listOf("🕵️‍"),
					"espio" to listOf("🕵️‍"),
					"europ" to listOf("🇪🇺"),
					"exercito" to listOf("🎖"),
					"familia" to listOf("👨‍👩‍👧‍👦"),
					"feli" to listOf("😀", "😁", "😃", "😄", "😊", "🙂", "☺"),
					"fest" to listOf("🎆", "🎇", "✨", "🎈", "🎉", "🎊"),
					"flor" to listOf("🌹"),
					"foga" to listOf("🔥"),
					"fogo" to listOf("🔥"),
					"fogu" to listOf("🔥"),
					"gat" to listOf("😏", "👌", "😽", "😻"),
					"goz" to listOf("💦"),
					"gostos" to listOf("😈", "😜"),
					"guerr" to listOf("💣", "🔫", "🎖"),
					"hora" to listOf("⌚", "⏲", "🕛"),
					"hospita" to listOf("👨‍⚕️", "⚕", "🚑"),
					"imediat" to listOf("⌚", "⏳", "🕛"),
					"invest" to listOf("💳", "💵", "💰", "💲"),
					"justic" to listOf("⚖", "👨‍⚖️"),
					"louc" to listOf("🤪", "😩", "😢", "😰"),
					"louv" to listOf("👼", "😇", "🙏", "🙏🙏"),
					"mao" to listOf("🖐", "🖐"),
					"maneir" to listOf("🔝"),
					"mentir" to listOf("🤥", "🤫"),
					"militar" to listOf("🎖"),
					"miste" to listOf("🕵️‍"),
					"monitor" to listOf("🖥"),
					"morre" to listOf("☠", "💀", "⚰", "👻"),
					"morri" to listOf("☠", "💀", "⚰", "👻"),
					"musica" to listOf("🎷", "🎸", "🎹", "🎺", "🎻", "🥁", "🎼", "🎵", "🎶", "🎤"),
					"olh" to listOf("👀"),
					"ouv" to listOf("👂"),
					"palavr" to listOf("✏", "✒", "🖋", "📝", "💬"),
					"palhac" to listOf("🤡"),
					"palma" to listOf("👏"),
					"paulista" to listOf("🏳", "🌈"),
					"patet" to listOf("😣"),
					"patriot" to listOf("🇧🇷"),
					"pens" to listOf("🧠", "💭"),
					"pesa" to listOf("🏋"),
					"pipo" to listOf("🍿"),
					"pistol" to listOf("🔫"),
					"pula" to listOf("🏃"),
					"pule" to listOf("🏃"),
					"querid" to listOf("☺", "🤗"),
					"quiet" to listOf("🤐"),
					"raiv" to listOf("⚡", "😤", "😤💦", "😖", "🙁", "😩", "😦", "😡", "🤬", "💣", "💢", "✋🛑", "☠"),
					"rock" to listOf("🤟"),
					"safad" to listOf("😉"),
					"saudade" to listOf("😢"),
					"segred" to listOf("🕵️‍"),
					"sumid" to listOf("😍"),
					"surpre" to listOf("😮"),
					"telefo" to listOf("📱", "📞", "☎"),
					"text" to listOf("✏", "✒", "🖋", "📝", "💬"),
					"transa" to listOf("👉👌"),
					"transe" to listOf("👉👌"),
					"trist" to listOf("☹", "🙁", "😖", "😞", "😟", "😢", "😭", "😭", "😭", "😩", "😿"),
					"vergonh" to listOf("😳"),
					"vist" to listOf("👀"),
					"whisk" to listOf("🥃"),
					/* Abreviações/Girias */
					"bucet" to listOf("😜", "😘", "😟"),
					"fod" to listOf("👉👌", "🔞"),
					"fud" to listOf("👉👌", "🔞"),
					"haha" to listOf("😅", "😂", "🤣"),
					"hehe" to listOf("😉", "😎", "😋", "😏", "😜", "😈", "🙊", "😼"),
					"mackenz" to listOf("🐴"),
					"merd" to listOf("💩"),
					"nude" to listOf("🙊", "😼", "😏"),
					"print" to listOf("📱"),
					"put" to listOf("😤", "😤💦", "😖", "🙁", "😩", "😦", "😡", "🤬", "💣", "💢", "✋🛑", "☠"),
					"vampir" to listOf("🦇"),
					/* Animais */
					"cachorr" to listOf("🐶"),
					"morceg" to listOf("🦇"),
					/* Comidas */
					"hamburger" to listOf("🍔"),
					"hamburguer" to listOf("🍔"),
					"pao" to listOf("🍞", "🥖"),
					"panqueca" to listOf("🥞"),
					"milh" to listOf("🌽"),
					/* Profissões */
					"astronaut" to listOf("👨‍🚀", "👩‍🚀"),
					"bombeir" to listOf("👩‍🚒", "👨‍🚒"),
					"cienti" to listOf("👩‍🔬", "👨‍🔬", "⚗", "🔬", "🔭", "📡"),
					"cozinh" to listOf("👨‍🍳", "👩‍🍳"),
					"juiz" to listOf("👨‍⚖️", "👩‍⚖️", "⚖"),
					"medic" to listOf("👨‍⚕️", "👩‍⚕️", "⚕"),
					"pilot" to listOf("👨‍✈️", "👩‍✈️"),
					"policia" to listOf("🚨", "🚔", "🚓", "👮‍♂️", "👮‍♀️", "🔫"),
					"professor" to listOf("👨‍🏫", "👩‍🏫"),
					/* Signos */
					"arian" to listOf("♈"),
					"taurin" to listOf("♉"),
					"geminian" to listOf("♊"),
					"cancerian" to listOf("♋"),
					"leonin" to listOf("♌"),
					"virginian" to listOf("♍"),
					"librian" to listOf("♎"),
					"escorpian" to listOf("♏"),
					"sagitario" to listOf("♐"),
					"capricornian" to listOf("♑"),
					"aquarian" to listOf("♒"),
					"piscian" to listOf("♓")
			)

			val happyEmojis = listOf("😀", "😁", "😂", "😃", "😄", "😅", "😆", "😉", "😊", "😋", "😎", "☺", "😛", "😜", "😝", "👌")
			val angryEmojis = listOf("😤", "😤💦", "😖", "🙁", "😩", "😦", "😡", "🤬", "💣", "💢", "✋🛑", "☠")
			val sadEmojis = listOf("☹", "🙁", "😖", "😞", "😟", "😢", "😭", "😭", "😭", "😩", "😿")
			val sassyEmojis = listOf("😉", "😎", "😋", "😘", "😏", "😜", "😈", "😻", "🙊", "👉👌", "😼")
			val sickEmojis = listOf("😷", "🤒", "🤕", "🤢", "🤮", "🤧")

			val input = context.args.joinToString(" ").escapeMentions()

			val message = context.reply(
					LoriReply(
							locale["VEMDEZAP_WhatIsTheMood"],
							"\uD83E\uDD14"
					),
					LoriReply(
							locale["VEMDEZAP_MoodHappy"],
							"\uD83D\uDE0A",
							mentionUser = false
					),
					LoriReply(
							locale["VEMDEZAP_MoodAngry"],
							"\uD83D\uDE21",
							mentionUser = false
					),
					LoriReply(
							locale["VEMDEZAP_MoodSassy"],
							"\uD83D\uDE0F",
							mentionUser = false
					),
					LoriReply(
							locale["VEMDEZAP_MoodSad"],
							"\uD83D\uDE22",
							mentionUser = false
					),
					LoriReply(
							locale["VEMDEZAP_MoodSick"],
							"\uD83E\uDD12",
							mentionUser = false
					)
			)

			message.onReactionAddByAuthor(context) {
				val mood = when (it.reactionEmote.name) {
					"\uD83D\uDE0A" -> ZapZapMood.HAPPY
					"\uD83D\uDE21" -> ZapZapMood.ANGRY
					"\uD83D\uDE0F" -> ZapZapMood.SASSY
					"\uD83D\uDE22" -> ZapZapMood.SAD
					"\uD83E\uDD12" -> ZapZapMood.SICK
					else -> return@onReactionAddByAuthor
				}

				message.delete().queue()

				val levelMessage = context.reply(
						LoriReply(
								locale["VEMDEZAP_WhatIsTheLevel"],
								"\uD83E\uDD14"
						),
						LoriReply(
								locale["VEMDEZAP_Level1"],
								"1⃣",
								mentionUser = false
						),
						LoriReply(
								locale["VEMDEZAP_Level2"],
								"2⃣",
								mentionUser = false
						),
						LoriReply(
								locale["VEMDEZAP_Level3"],
								"3⃣",
								mentionUser = false
						),
						LoriReply(
								locale["VEMDEZAP_Level4"],
								"4⃣",
								mentionUser = false
						),
						LoriReply(
								locale["VEMDEZAP_Level5"],
								"5⃣",
								mentionUser = false
						)
				)

				levelMessage.onReactionAddByAuthor(context) {
					val level = Constants.INDEXES.indexOf(it.reactionEmote.name)

					if (level == -1) {
						return@onReactionAddByAuthor
					}

					levelMessage.delete().queue()

					val split = input.split(" ")

					var output = ""

					for (word in split) {
						val lowerCaseWord = word.toLowerCase()
						output += word + " "
						var addedEmoji = false

						for ((match, emojis) in fullMatch) {
							if (lowerCaseWord == match) {
								output += "${emojis[RANDOM.nextInt(emojis.size)]} "
								addedEmoji = true
							}
						}

						for ((match, emojis) in partialMatchAny) {
							if (lowerCaseWord.contains(match, true)) {
								output += "${emojis[RANDOM.nextInt(emojis.size)]} "
								addedEmoji = true
							}
						}

						for ((match, emojis) in partialMatchPrefix) {
							if (lowerCaseWord.startsWith(match, true)) {
								output += "${emojis[RANDOM.nextInt(emojis.size)]} "
								addedEmoji = true
							}
						}

						if (!addedEmoji) { // Se nós ainda não adicionamos nenhum emoji na palavra...
							// Para fazer um aleatório baseado no nível... quanto maior o nível = mais chance de aparecer emojos
							val upperBound = (5 - level) + 3
							val random = RANDOM.nextInt(upperBound)

							if (random == 0) {
								val moodEmojis = when (mood) {
									VemDeZapCommand.ZapZapMood.HAPPY -> happyEmojis
									VemDeZapCommand.ZapZapMood.ANGRY -> angryEmojis
									VemDeZapCommand.ZapZapMood.SASSY -> sassyEmojis
									VemDeZapCommand.ZapZapMood.SAD -> sadEmojis
									VemDeZapCommand.ZapZapMood.SICK -> sickEmojis
								}

								// E quanto maior o nível, maiores as chances de aparecer mais emojis do lado da palavra
								val addEmojis = RANDOM.nextInt(1, level + 2)

								for (i in 0 until addEmojis) {
									output += "${moodEmojis[RANDOM.nextInt(moodEmojis.size)]} "
								}
							}
						}
					}

					context.sendMessage("${context.getAsMention(true)} $output")
				}

				levelMessage.addReaction("1⃣").queue()
				levelMessage.addReaction("2⃣").queue()
				levelMessage.addReaction("3⃣").queue()
				levelMessage.addReaction("4⃣").queue()
				levelMessage.addReaction("5⃣").queue()
			}
			message.addReaction("\uD83D\uDE0A").queue()
			message.addReaction("\uD83D\uDE21").queue()
			message.addReaction("\uD83D\uDE0F").queue()
			message.addReaction("\uD83D\uDE22").queue()
			message.addReaction("\uD83E\uDD12").queue()
		} else {
			context.explain()
		}
	}

	enum class ZapZapMood {
		HAPPY,
		ANGRY,
		SASSY,
		SAD,
		SICK
	}
}
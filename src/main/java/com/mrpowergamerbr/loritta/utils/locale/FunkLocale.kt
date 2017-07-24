package com.mrpowergamerbr.loritta.utils.locale

import com.google.gson.GsonBuilder

class FunkLocale : BaseLocale() {
	init {
		INVALID_NUMBER = "Ei vacilão, coloca um número válido em vez de ficar de vacilo. \uD83D\uDE22"
		NSFW_IMAGE = "**Vacilo com as novinhas detectado!**\n\nCoê mermão, pra que usar *isto* no seu papel de parede? Cê loko menor, cê acha mesmo que os grandes MCs vão olhar pra você e falar \"nossa, o \"{0}\" tem style porque ele gasta o tempo dele vendo pessoas se pegando porque ele não consegue falar com uma novinha!\"?\n\nNão parça, eles vão ficar bolado com você, tá ligado? Mude sua vida, pare de fazer isto.\n\n(Se isto foi um falso positivo então... sei lá, abafa o caso \uD83D\uDE1E)"

		// Event Log
		EVENTLOG_USER_ID = "ID do usuário: {0}"
		EVENTLOG_AVATAR_CHANGED = "**{0} alterou o style**"

		// ===[ COMMANDS - ADMINISTRATION ]===
		// HackBanCommand.kt
		HACKBAN_SUCCESS = "Vacilão `{0}` foi banido com sucesso!"
		HACKBAN_NO_PERM = "Não tenho permissão para banir este vacilão!"

		// LimparCommand.kt
		LIMPAR_INVALID_RANGE = "Eu só consigo passar o rodo nas últimas 2 até 100 mensagens, tá ligado?"
		LIMPAR_SUCCESS = "Rodo foi passado por {0}!"

		// MuteCommand.kt
		MUTE_CANT_MUTE_ME = "Ei vacilão, não dá para você me silenciar, tá ligado?"
		MUTE_NO_PERM = "Não tenho permissão para silenciar este vacilão!"
		MUTE_SUCCESS_ON = "Vacilão `{0}` foi silenciado com sucesso!"
		MUTE_SUCCESS_OFF = "Vacilão `{0}` magicamente aprendeu a falar de novo!"

		// SoftBanCommand.kt
		SOFTBAN_SUCCESS = "Vacilão `{0}` foi softbanned com sucesso!"
		SOFTBAN_NO_PERM = "Não tenho permissão para softbanir este vacilão!"

		// ===[ COMMANDS - DISCORD ]===
		INVITE_INFO = "Ei sangue bom, tá querendo que eu entre na sua favela no Discord? Demorô então! Só clica [aqui]({0}) para me adicionar na sua favela!\n\nSe você quiser configurar umas paradas (como o meu prefixo, comandos ativados, etc) então dá uma passada no meu barraco clicando [aqui]({1})!\n\nE, é claro, entre na minha favela para dar sugestões, reportar bugs e muito mais! {2}"

		// ===[ COMMANDS - FUN ]===
		// ShipCommand.kt
		SHIP_NEW_COUPLE = "Cê loko menor, será que nós temos um novo casal aqui?"

		SHIP_valor90 = listOf("😍 %ship% Os dois se amam! 😍",
				"💗 %ship% Casal mais perfeito? Impossível! 💗",
				"☠ %ship% Nem a PM separa! 😂",
				"😋 %ship% Casal mais perfeito que eu! 😋",
				"😚 %ship% Casal? Casal que nada! Eles já são casados! 😚")

		SHIP_valor80 = listOf("😏 %ship% Mas esses dois já se conhecem faz muito tempo... 😏",
				"😊 %ship% Claro que os dois são um lindo casal! 😊",
				"😜 %ship% Casal mais grudento que Ondas Sonoras do MC Davi™! 😜",
				"😄 %ship% Se os dois já não namoram eu estaria surpresa! 😄")

		SHIP_valor70 = listOf("🙂 %ship% Prevejo um casal maneiro se formando! 🙂",
				"😄 %ship% Só precisa de um pouco mais de conversa para rolar! 😜",
				"😊 %ship% Os dois foram feitos um para o outro! 😊",
				"😄 %ship% Sim! 😄")

		SHIP_valor60 = listOf("🙂 %ship% Se o/a %user% parasse de ser um pouco vacilão... 😏",
				"😊 %ship% Tem um pouco de interesses compatíveis aí 😊",
				"🙂 %ship% Eu aprovo esse casal! 🙂",
				"😄 %ship% Sim! 😄")

		SHIP_valor50 = listOf("😶 %ship% Parças... Mas talvez né... 😏",
				"😊 %ship% Talvez... Só precisa o/a %user% querer, tá ligado? 😶",
				"😶 %ship% Eu queria ver esse casal funcionar 😶")

		SHIP_valor40 = listOf("😶 %ship% É... mas um deles é mercenário... 🙁",
				"😶 %ship% Nada é impossível, mas... 🙁",
				"😶 %ship% Se dois quererem, talvez dê certo... Mas... 😶")

		SHIP_valor30 = listOf("😕 %ship% Acho que não seria um belo casal... 🙁",
				"😶 %ship% Só vivem na mesma comunidade mesmo... 😶")

		SHIP_valor20 = listOf("😐 %ship% Se o(a) %user% não fosse vacilão... 😐")

		SHIP_valor10 = listOf("😐 %ship% Já vi esses dois nas quebradas por aí e só deram vacilo... 😢")

		SHIP_valor0 = listOf("😭 %ship% Os dois são vacilões desde quando nasceram, cê acha mesmo que ia ser um casal? 😭")

		// CommandBase.kt
		HOW_TO_USE = "Como embrazar"

		// HelloWorldCommand.kt
		HELLO_WORLD = "Coê rapaziada! {0}"

		// AmizadeCommand.kt
		AMIZADE_AMIZADE_COM = "{0} vacilou demais"
		AMIZADE_ENDED = ""
		AMIZADE_NOW = "Agora"
		AMIZADE_IS_MY = "é o novo"
		AMIZADE_BEST_FRIEND = "parça dos rolês"

		// AvaliarWaifuCommand.kt
		RATEWAIFU_10 = "Novinha 10/10, nunca trocaria de novinha se fosse você!"
		RATEWAIFU_9 = "Uma novinha excelente, tá ligado?"
		RATEWAIFU_8 = "Uma novinha que adora sair nos rolês com o novinho."
		RATEWAIFU_7 = "Nem todas as novinhas são perfeitas tá ligado? Mas essa novinha é responsa, recomendo!";
		RATEWAIFU_6 = "Essa novinha tem a mesma nota que você tirou lá no ensino fundamental";
		RATEWAIFU_5 = "Novinha 5/10, bem de boa";
		RATEWAIFU_4 = "Essa novinha é meio mascarada, mas é gente boa";
		RATEWAIFU_3 = "Essa novinha precisa de mais substância.";
		RATEWAIFU_2 = "Ow menor, essa novinha conspira com os parças, troca de novinha ae menor...";
		RATEWAIFU_1 = "Sê loco? Essa daí é mó mercenária, sai dessa lek.";
		RATEWAIFU_0 = "Menor, sai disso por favor.";

		// TristeRealidade.kt
		TRISTEREALIDADE_FILE = "tristerealidade_funk.png"

		// AjudaCommand.kt
		AJUDA_SENT_IN_PRIVATE = "Joguei o treco lá no privado tá ligado? Bora lá ver nas suas mensagens diretas!"

		// AvatarCommand.kt
		AVATAR_CLICKHERE = "Clique [aqui]({0}) pra baixar o bagulho!"
		AVATAR_LORITTACUTE = "Eu sei que eu tenho style, tá ligado?"

		// EscolherCommand.kt
		ESCOLHER_RESULT = "Eu escolhi `{0}`, é mais chave, pô!"

		// BackgroundCommand.kt
		BACKGROUND_INFO = "**Coê menor, querendo mudar o teu background é? Então tu veio pra quebrada certa!**\n" +
				"\n" +
				"Clique em \uD83D\uDDBC pra vê o papel de parede que tu tá usando\n" +
				"Clique em \uD83D\uDED2 pra vê os bagulho que nós tem" +
				"\n" +
				"\n" +
				"Querendo enviar seu próprio papel de parede parça? Demorô! Envie uma imagem 400x300 no chat e, junto com a imagem, escreva `{0}background`! (Você também pode enviar o link da imagem junto com o comando que eu também irei aceitar!)\n\n(Não envie backgrounds com vacilos com as novinhas! Seja responsa porque, se você enviar, sua conta será banida de usar qualquer funcionalidade minha, tá ligado?)"

		// DiscriminatorCommand.kt
		DISCRIM_DESCRIPTION = "Veja parceiros que são da mesma gangue que você ou de outro parça!"

		// RankCommand.kt
		RANK_DESCRIPTION = "Veja quem tem mais catuabas!"
		RANK_INFO = "Catuabas: {0} | Nível Atual: {1}"

		// RepCommand.kt
		REP_DESCRIPTON = "Dê Jack Daniels para parças!"
		REP_SELF = "Parça, dá um rolezinho para conseguir parças para te darem Jack Daniels, tlg?"
		REP_WAIT = "Tá embaçado menor, precisa dar um tempo de **{0}** pra a PM vazar!"
		REP_SUCCESS = "slk menor, você deu uma Jack Daniels para {0}!"

		// BotInfoCommand.kt
		BOTINFO_TITLE = "Coê rapaziada, eu me chamo Loritta!"
		BOTINFO_EMBED_INFO = "Eae menor, meu nome é Loritta (ou pros parças, \"Lori\") e sou apenas um bot para o Discord chavoso e com vários bagulhos maneiros para você usar!\n\n" +
				"Eu estou em **{0} favelas** e eu conheço **{1} menores diferentes** (Wow, quanta gente)! Eu fui feita usando **JDA** em **Java & Kotlin** e, se você quiser ver os bagulhos, [clique aqui](http://bit.ly/lorittagit)!\n\n" +
				"Meu website é https://loritta.website/ e, se você quiser dar um rolê na minha quebrada, [clique aqui](http://bit.ly/lorittad)!\n\n" +
				"Já fazem **{2}** desde que eu acordei \uD83D\uDE34 (ou seja, meu uptime atual) e atualmente eu tenho **{3} comandos diferentes**!"
		BOTINFO_MENTIONS = "`MrPowerGamerBR#4185` Se não fosse por esse lek, eu nem existiria!\n" +
				"`Giovanna_GGold#2454 (Gabriela Giulian)` Ela que fez essa **chavosa** \uD83D\uDE0D arte minha da miniatura! [Clique aqui para ver o desenho!](https://loritta.website/assets/img/loritta_fixed_final_cropped.png) (e ela pegou todo o meu style chique e supimpa \uD83D\uDE0A)!\n" +
				"`{0}#{1}` Por estar falando com nóis no bonde! \uD83D\uDE04"

		// ServerInfoCommand.kt
		SERVERINFO_OWNER = "Patrão"
		SERVERINFO_REGION = "Quebrada"
		SERVERINFO_CHANNELS_VOICE = "Voz"
		SERVERINFO_CREATED_IN = "Criado em"
		SERVERINFO_JOINED_IN = "Entrei aqui em"
		SERVERINFO_MEMBERS = "Parças"
		SERVERINFO_ONLINE = "Online"
		SERVERINFO_AWAY = "Nos Rolês"
		SERVERINFO_BUSY = "Na Fuga"
		SERVERINFO_OFFLINE = "Perdido no Tiroteio"
		SERVERINFO_PEOPLE = "Playboys"

		// MusicInfoCommand.kt & PlaylistCommand.kt
		MUSICINFO_DESCRIPTION = "Mostra o batidão chave que tá tocando."
		MUSICINFO_NOMUSIC = "Nenhum batidão tá tocando... Solta uns batidão ae menor! `+tocar funk`"
		MUSICINFO_INQUEUE = "No baile de favela..."
		MUSICINFO_NOMUSIC_SHORT = "Nenhum batidão..."

		PULAR_DESCRIPTION = "Pula um batidão."
		PULAR_MUSICSKIPPED = "Batidão pulado!"

		TOCAR_DESCRIPTION = "Adiciona um batidão no baile de favela!"
		TOCAR_NOTINCHANNEL = "Só aceito batidões se você tiver no canal do baile de favela parça!"

		// ~ generic ~
		MUSIC_MAX = "Cê é loko menor, é mó grande essa batida! O baile de favela só aceita batidões de, no máximo `{0}` de duração!"
		MUSIC_ADDED = "Adicionado no baile de favela `{0}`!"
		MUSIC_PLAYLIST_ADDED = "Adicionado no baile de favela {0} batidões!"
		MUSIC_PLAYLIST_ADDED_IGNORED = "Adicionado no baile de favela {0} batidões! (ignorado {1} faixas por serem muito grandes!)"
	}
}

fun main(args: Array<String>) {
	val gson = GsonBuilder().setPrettyPrinting().create().toJson(FunkLocale())

	println(gson)
}
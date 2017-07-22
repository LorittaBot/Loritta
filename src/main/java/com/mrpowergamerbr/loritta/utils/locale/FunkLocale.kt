package com.mrpowergamerbr.loritta.utils.locale

class FunkLocale : BaseLocale() {
	init {
		INVALID_NUMBER = "Ei vacilão, coloca um número válido em vez de ficar de vacilo. \uD83D\uDE22"

		// CommandBase.kt
		HOW_TO_USE = "Como embrazar"

		// HelloWorldCommand.kt
		HELLO_WORLD = "Coê rapaziada! {0}"

		// DiscriminatorCommand.kt
		DISCRIM_DESCRIPTION = "Veja parceiros que são da mesma gangue que você ou de outro parça!"

		// RankCommand.kt
		RANK_DESCRIPTION = "Veja quem tem mais catuabas!"
		RANK_INFO = "Catuabas: {0} | Nível Atual: {1}"

		// RepCommand.kt
		REP_DESCRIPTON = "Dê catuabas para parças!"
		REP_SELF = "Parça, dá um rolezinho para conseguir parças para te darem catuaba, tlg?"
		REP_WAIT = "Tá embaçado menor, precisa dar um tempo de **{0}** pra a PM vazar!"
		REP_SUCCESS = "slk menor, você deu uma catuaba para {0}!"

		// MusicInfoCommand.kt & PlaylistCommand.kt
		MUSICINFO_DESCRIPTION = "Mostra o batidão chave que tá tocando."
		MUSICINFO_NOMUSIC = "Nenhum batidão tá tocando... Solta uns batidão ae menor! `+tocar funk`"
		MUSICINFO_INQUEUE = "No baile de favela..."
		MUSICINFO_NOMUSIC_SHORT = "Nenhum batidão..."

		PULAR_DESCRIPTION = "Pula um batidão."
		PULAR_MUSICSKIPPED = "Batidão pulada!"

		TOCAR_DESCRIPTION = "Adiciona um batidão no baile de favela!"
		TOCAR_NOTINCHANNEL = "Só aceito batidões se você tiver no canal do baile de favela parça!"

		// ~ generic ~
		MUSIC_MAX = "Cê é loko menor, é mó grande essa batida! O baile de favela só aceita batidões de, no máximo `{0}` de duração!"
		MUSIC_ADDED = "Adicionado no baile de favela `{0}`!"
		MUSIC_PLAYLIST_ADDED = "Adicionado no baile de favela {0} batidões!"
		MUSIC_PLAYLIST_ADDED_IGNORED = "Adicionado no baile de favela {0} batidões! (ignorado {1} faixas por serem muito grandes!)"
	}
}
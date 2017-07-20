package com.mrpowergamerbr.loritta.utils.locale

/**
 * Classe de localização base, por padrão em PT-BR
 *
 * Locales diferentes devem extender esta classe
 */
open class BaseLocale {
	// Generic
	var SEARCH = "pesquisar"

	// CommandBase.kt
	var HOW_TO_USE = "Como usar"
	var EXAMPLE = "Exemplo"

	// HelloWorldCommand.kt
	var HELLO_WORLD = "Olá mundo! {0}"
	var HELLO_WORLD_DESCRIPTION = "Um simples comando para testar o sistema de localização da Loritta."
	var USING_LOCALE = "Agora estou usando {0} como locale!"

	// AminoCommand.kt
	var AMINO_DESCRIPTION = "Comandos relacionados ao Amino! ([http://aminoapps.com/](http://aminoapps.com/))"
	var AMINO_MEMBERS = "Membros"
	var AMINO_LANGUAGE = "Linguagem"
	var AMINO_COMMUNITY_HEAT = "Calor da Comunidade"
	var AMINO_CREATED_IN = "Criado em"
	var AMINO_COULDNT_FIND = "Não encontrei nenhuma comunidade chamada `{0}`!"
	var AMINO_YOUR_IMAGE = "Sua imagem `{0}`!"
	var AMINO_NO_IMAGE_FOUND = "Eu não encontrei nenhuma imagem \".Amino\" na sua mensagem... \uD83D\uDE1E"
	var AMINO_CONVERT = "converter"

	// YoutubeMp3Command.kt
	var YOUTUBEMP3_ERROR_WHEN_CONVERTING = "Ocorreu um erro ao tentar converter o vídeo para MP3... \uD83D\uDE1E"
}
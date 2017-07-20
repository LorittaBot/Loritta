package com.mrpowergamerbr.loritta.utils.locale

/**
 * Classe de localização base, por padrão em PT-BR
 *
 * Locales diferentes devem extender esta classe
 */
open class BaseLocale {
	// Generic
	var SEARCH = "pesquisar"
	var PROCESSING = "Processando"
	var INVALID_NUMBER = "Número `{0}` é algo irreconhecível para um bot como eu, sorry. \uD83D\uDE22"

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

	// MoneyCommand.kt
	var MONEY_DESCRIPTION = "Transforma o valor de uma moeda em outra moeda. (Por exemplo: Ver quanto está valendo o dólar em relação ao real)"
	var MONEY_INVALID_CURRENCY = "`{0}` não é uma moeda válida! \uD83D\uDCB8\n**Moedas válidas:** {1}"
	var MONEY_CONVERTED = "💵 **{0} {1} para {2}**: {3} {1}"

	// MorseCommand.kt
	var MORSE_DESCRIPTION = "Codifica/Decodifica uma mensagem em código morse"
	var MORSE_FROM_TO = "Texto para Morse"
	var MORSE_TO_FROM = "Morse para Texto"
	var MORSE_FAIL = "Eu não consegui transformar a sua mensagem para código morse... Talvez você tenha colocado apenas caracteres que não existem em código morse!"

	// OCRCommand.kt
	var OCR_DESCRIPTION = "Pega o texto em uma imagem usando OCR"
	var OCR_COUDLNT_FIND = "Não encontrei nenhum texto nesta imagem..."

	// PackageInfo.kt
	var PACKAGEINFO_DESCRIPTION = "Mostra o status de uma encomenda dos correios, funciona com os Correios (Brasil) e a CTT (Portugal)"
	var PACKAGEINFO_INVALID = "Código `{0}` não é um código de rastreio válido!"
	var PACKAGEINFO_COULDNT_FIND = "Não encontrei o objeto `{0}` no banco de dados do Correios!"

	// RgbCommand.kt
	var RGB_DESCRIPTION = "Transforme uma cor hexadecimal para RGB"
	var RGB_TRANSFORMED = "Transformei a sua cor `{0}` para RGB! {1}, {2}, {3}"
	var RGB_INVALID = "A cor `{0}` não é uma cor hexadecimal válida!"

	// TempoCommand.kt
	var TEMPO_DESCRIPTION = "Verifique a temperatura de uma cidade!"
	var TEMPO_PREVISAO_PARA = "Previsão do tempo para {0}, {1}"
	var TEMPO_TEMPERATURA = "Temperatura"
	var TEMPO_UMIDADE = "Umidade"
	var TEMPO_VELOCIDADE_VENTO = "Velocidade do Vento"
	var TEMPO_PRESSAO_AR = "Pressão do Ar"
	var TEMPO_ATUAL = "Atual"
	var TEMPO_MAX = "Máxima"
	var TEMPO_MIN = "Mínima"
	var TEMPO_COULDNT_FIND = "Não encontrei nenhuma cidade chamada `{0}`!"

	// TranslateCommand.kt
	var TRANSLATE_DESCRIPTION = "Traduz uma frase para outra linguagem"

	// WikipediaCommand.kt
	var WIKIPEDIA_DESCRIPTION = "Mostra uma versão resumida de uma página do Wikipedia"
	var WIKIPEDIA_COULDNT_FIND = "Não consegui encontrar nada relacionado á `{0}`!"

	// YoutubeMp3Command.kt
	var YOUTUBEMP3_DESCRIPTION = "Pegue o download de um vídeo do YouTube em MP3!"
	var YOUTUBEMP3_ERROR_WHEN_CONVERTING = "Ocorreu um erro ao tentar converter o vídeo para MP3... \uD83D\uDE1E"
	var YOUTUBEMP3_INVALID_LINK = "Link inválido!"
	var YOUTUBEMP3_DOWNLOADING_VIDEO = "Baixando vídeo"
	var YOUTUBEMP3_CONVERTING_VIDEO = "Convertendo vídeo"
	var YOUTUBEMP3_FINISHED = "Pronto! Seu vídeo está pronto para ser baixado em MP3! {0}"
}
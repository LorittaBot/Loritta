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
	var MINUTES_AND_SECONDS = "%02d minutos e %02d segundos"
	var NSFW_IMAGE = "**Imagem pornográfica (NSFW) detectada!**\n\nQue feio... Sério mesmo que você queria usar *isto* como seu background? Você acha mesmo que alguém vai ver seu background e vai falar \"nossa, o \"{0}\" é maravilhoso porque ele gasta o tempo dele vendo pessoas se pegando porque ele não consegue pegar ninguém!\"?\n\nNão, ninguém irá falar isto, mude sua vida, pare de fazer isto.\n\n(Se isto foi um falso positivo então... sei lá, me ignore \uD83D\uDE1E)"

	// Event Log
	var EVENTLOG_USER_ID = "ID do usuário: {0}"
	var EVENTLOG_AVATAR_CHANGED = "**{0} alterou o avatar**"
	var EVENTLOG_NAME_CHANGED = "**{0} alterou o nome!**\n\nAntigo nome: `{1}`\nNovo nome: `{2}`"
	var EVENTLOG_CHANNEL_CREATED = "**Canal de texto {0} criado**"
	var EVENTLOG_CHANNEL_NAME_UPDATED = "**Nome de {0} foi alterado!**\n\nAntigo nome: `{1}`\nNovo nome: `{2}`"
	var EVENTLOG_CHANNEL_TOPIC_UPDATED = "**Tópico de {0} foi alterado!**\n\nAntigo tópico: `{1}`\nNovo tópico: `{2}`"
	var EVENTLOG_CHANNEL_POSITION_UPDATED = "**Posição de {0} foi alterado!**\n\nAntiga posição: `{1}`\nNova posição: `{2}`"
	var EVENTLOG_CHANNEL_DELETED = "**Canal de texto `{0}` foi deletado**"

	// CommandBase.kt
	var HOW_TO_USE = "Como usar"
	var EXAMPLE = "Exemplo"

	// BackgroundCommand.kt
	var BACKGROUND_DESCRIPTION = "Que tal dar uma renovada no papel de parede do seu perfil?"
	var BACKGROUND_CENTRAL = "Central de Papéis de Parede"
	var BACKGROUND_INFO = "**Querendo alterar o seu papel de parede do seu perfil? Então você veio ao lugar certo!**\n" +
			"\n" +
			"Clique em \uD83D\uDDBC para ver seu papel de parede atual\n" +
			"Clique em \uD83D\uDED2 para ver os templates padrões" +
			"\n" +
			"\n" +
			"Querendo enviar seu próprio papel de parede? Sem problemas! Envie uma imagem 400x300 no chat e, junto com a imagem, escreva `{0}background`! (Você também pode enviar o link da imagem junto com o comando que eu também irei aceitar!)\n\n(Não envie backgrounds com coisas NSFW! Se você enviar, sua conta será banida de usar qualquer funcionalidade minha!)"
	var BACKGROUND_INVALID_IMAGE = "Imagem inválida! Tem certeza que isto é um link válido? Se puder, baixe a imagem e faça upload diretamente no Discord!"
	var BACKGROUND_UPDATED = "Papel de parede atualizado!"
	var BACKGROUND_EDITED = "Como a sua imagem não era 400x300, eu precisei mexer um pouquinho nela!"
	var BACKGROUND_YOUR_CURRENT_BG = "Seu papel de parede atual"
	var BACKGROUND_TEMPLATE_INFO = "Clique em ⬅ para voltar um template\n" +
			"Clique em ➡ para avançar um template\n" +
			"Clique em ✅ para usar este template como seu papel de parede"

	// DiscriminatorCommand.kt
	var DISCRIM_DESCRIPTION = "Veja usuários que possuem o mesmo discriminador que você ou de outro usuário!"
	var DISCRIM_NOBODY = "Ninguém que eu conheça possui o discriminator `#${0}`!"

	// RankCommand.kt
	var RANK_DESCRIPTION = "Veja o ranking do servidor atual!"
	var RANK_INFO = "XP Total: {0} | Nível Atual: {1}"

	// RepCommand.kt
	var REP_DESCRIPTON = "Dê reputação para outro usuário!"
	var REP_SELF = "Você não pode dar reputação para si mesmo, bobinho!"
	var REP_WAIT = "Você precisa esperar **{0}** antes de poder dar outra reputação!"
	var REP_SUCCESS = "deu um ponto de reputação para {0}!"

	// SobreMimCommand.kt
	var SOBREMIM_DESCRIPTION = "Altere o \"Sobre Mim\" no comando de perfil!"
	var SOBREMIM_CHANGED = "Sua mensagem de perfil foi alterada para `{0}`!"

	// HelloWorldCommand.kt
	var HELLO_WORLD = "Olá mundo! {0}"
	var HELLO_WORLD_DESCRIPTION = "Um simples comando para testar o sistema de localização da Loritta."
	var USING_LOCALE = "Agora estou usando {0} como locale!"

	// LembrarCommand.kt
	var LEMBRAR_DESCRIPTION = "Precisa lembrar de dar comida para o dog? Talvez você queira marcar um lembrete para que no futuro você possa ver se você conseguir fazer todos os seus \"Life Goals\" deste ano? Então crie um lembrete!"
	var LEMBRAR_SUCCESS = "Eu irei te lembrar em {0}/{1}/{2} às {3}:{4}!"

	// KnowYourMemeCommand.kt
	var KYM_DESCRIPTION = "Procura um meme no KnowYourMeme"
	var KYM_COULDNT_FIND = "Não encontrei nada relacionado a `{0}`!"
	var KYM_NO_DESCRIPTION = "Sem descrição..."
	var KYM_ORIGIN = "Origem"
	var KYM_DATE = "Data"
	var KYM_UNKNOWN = "Desconhecido"

	// IsUpCommand.kt
	var ISUP_DESCRIPTION = "Verifica se um website está online!"
	var ISUP_ONLINE = "É só você, para mim `{0}` está online! (**Código:** {1})"
	var ISUP_OFFLINE = "Não é só você, para mim `{0}` também está offline! (**Erro:** {1})"
	var ISUP_UNKNOWN_HOST = "`{0} não existe!`"

	// HexCommand.kt
	var HEX_DESCRIPTION = "Transforme uma cor RGB para hexadecimal"
	var HEX_RESULT = "Transformei a sua cor `{0}, {1}, {2} {3}` para hexadecimal! `{4}`"
	var HEX_BAD_ARGS = "Todos os argumentos devem ser números!"

	// EncurtarCommand.kt
	var BITLY_DESCRIPTION = "Encurta um link usando o bit.ly"
	var BITLY_INVALID = "A URL `{0}` é inválida!"

	// TODO: DicioCommand.kt

	// CalculadoraCommand.kt
	var CALC_DESCRIPTION = "Calcula uma expressão aritmética"
	var CALC_RESULT = "Resultado: `{0}`"
	var CALC_INVALID = "`{0}` não é uma expressão artimética válida!"

	// BIRLCommand.kt
	var BIRL_DESCRIPTION = "Compila um código criado em BIRL (Bambam's \"It's show time\" Recursive Language)"
	var BIRL_RESULT = "Resultado"
	var BIRL_INFO = "Códigos em BIRL devem estar entre blocos de código, por exemplo:\n`{0}`Para mais informações: https://birl-language.github.io/"

	// AnagramaCommand.kt
	var ANAGRAMA_DESCRIPTION = "Crie um anagrama de uma palavra!"
	var ANAGRAMA_RESULT = "Seu anagrama é... `{0}`"

	// Md5Command.kt
	var MD5_DESCRIPTION = "Encripta uma mensagem usando MD5"
	var MD5_RESULT = "`{0}` em MD5: `{1}`"

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
	var MONEY_CONVERTED = "💵 **{0} {1} para {2}**: {3} {2}"

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
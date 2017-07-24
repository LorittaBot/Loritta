package com.mrpowergamerbr.loritta.utils.locale

import java.util.*

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

	// ===[ COMMANDS - ADMINISTRATION ]===
	// HackBanCommand.kt
	var HACKBAN_DESCRIPTION = "Permite banir um usuário pelo ID dele antes de ele entrar no seu servidor!"
	var HACKBAN_BY = "Hackbanned por {0}"
	var HACKBAN_REASON = "Motivo"
	var HACKBAN_SUCCESS = "Usuário `{0}` foi banido com sucesso!"
	var HACKBAN_NO_PERM = "Não tenho permissão para banir este usuário!"

	// LimparCommand.kt
	var LIMPAR_DESCRIPTION = "Limpa o chat do canal de texto atual."
	var LIMPAR_INVALID_RANGE = "Eu só consigo limpar entre 2 até 100 mensagens passadas!"
	var LIMPAR_SUCCESS = "Chat limpo por {0}!"

	// MuteCommand.kt
	var MUTE_DESCRIPTION = "Silencia um usuário por um período de tempo determinado"
	var MUTE_CANT_MUTE_ME = "Você não pode me silenciar, bobinho!"
	var MUTE_ROLE_NAME = "Silenciado"
	var MUTE_NO_PERM = "Não tenho permissão para silenciar este usuário!"
	var MUTE_SUCCESS_ON = "Usuário `{0}` foi silenciado com sucesso!"
	var MUTE_SUCCESS_OFF = "Usuário `{0}` magicamente aprendeu a falar de novo!"

	// RoleIdCommand.kt
	var ROLEID_DESCRIPTION = "Pega o ID de um cargo do Discord"

	// SoftBanCommand.kt
	var SOFTBAN_DESCRIPTION = "Faz um \"softban\" em um usuário, ou seja, o usuário é banido e desbanido logo em seguida, usado para deletar as mensagens do usuário."
	var SOFTBAN_FAIL_MORE_THAN_SEVEN_DAYS = "É impossível softbanir alguém por mais de 7 dias!"
	var SOFTBAN_FAIL_LESS_THAN_ZERO_DAYS = "É impossível softbanir alguém por menos de 0 dias! (E como isso iria funcionar?)"
	var SOFTBAN_BY = "Softbanned por {0}"
	var SOFTBAN_SUCCESS = "Usuário `{0}` foi softbanned com sucesso!"
	var SOFTBAN_NO_PERM = "Não tenho permissão para softbanir este usuário!"

	// ===[ COMMANDS - DISCORD ]===
	// AvatarCommand.kt
	var AVATAR_DESCRIPTION = "Pega o avatar de um usuário do Discord"
	var AVATAR_CLICKHERE = "Clique [aqui]({0}) para baixar a imagem!"
	var AVATAR_LORITTACUTE = "Eu sei que eu sou muito fofa!"

	// BotInfoCommand.kt
	var BOTINFO_DESCRIPTION = "Mostra informações interessantes (e algumas bem inúteis) sobre mim!"
	var BOTINFO_TITLE = "Olá, eu me chamo Loritta!"
	var BOTINFO_EMBED_INFO = "Olá, eu me chamo Loritta (ou para amigos mais próximos, \"Lori\") e sou apenas um bot para o Discord fofo e com várias funcionalidades supimpas!\n\n" +
			"Eu estou em **{0} servidores** e eu conheço **{1} pessoas diferentes** (Wow, quanta gente)! Eu fui feita usando **JDA** em **Java & Kotlin** e, se você quiser ver meu código-fonte, [clique aqui](http://bit.ly/lorittagit)!\n\n" +
			"Meu website é https://loritta.website/ e, se você quiser saber mais sobre mim, [clique aqui](http://bit.ly/lorittad) para entrar no meu servidor no Discord!\n\n" +
			"Já fazem **{2}** desde que eu acordei \uD83D\uDE34 (ou seja, meu uptime atual) e atualmente eu tenho **{3} comandos diferentes**!"
	var BOTINFO_HONORABLE_MENTIONS = "Menções Honrosas"
	var BOTINFO_MENTIONS = "`MrPowerGamerBR#4185` Se não fosse por ele, eu nem iria existir!\n" +
			"`Giovanna_GGold#2454 (Gabriela Giulian)` Ela que fez esta **linda** \uD83D\uDE0D arte minha da miniatura! [Clique aqui para ver o desenho!](https://loritta.website/assets/img/loritta_fixed_final_cropped.png) (e ela capturou toda a minha fofura & beleza \uD83D\uDE0A)!\n" +
			"`{0}#{1}` Por estar falando comigo! \uD83D\uDE04"
	var BOTINFO_CREATEDBY = "Loritta foi criada por MrPowerGamerBR"

	// EmojiCommand.kt
	var EMOJI_DESCRIPTION = "Veja emojis em um tamanho que você não precise usar uma lupa para tentar entender eles!"

	// InviteCommand.kt
	var INVITE_DESCRIPTION = "Envia o link do convite para me adicionar em outros servidores!"
	var INVITE_INFO = "Você quer me adicionar em outros servidores/guilds do Discord? Então clique [aqui]({0}) para me adicionar em outro servidor!\n\nSe você quiser configurar algumas coisas (como o meu prefixo, comandos ativados, etc) então acesse o painel de administração clicando [aqui]({1})!\n\nE, é claro, entre na minha guild para dar sugestões, reportar bugs e muito mais! {2}"

	// ServerInfoCommand.kt
	var SERVERINFO_DESCRIPTION = "Veja as informações do servidor do Discord atual!"
	var SERVERINFO_OWNER = "Dono"
	var SERVERINFO_REGION = "Região"
	var SERVERINFO_CHANNELS = "Canais"
	var SERVERINFO_CHANNELS_TEXT = "Texto"
	var SERVERINFO_CHANNELS_VOICE = "Voz"
	var SERVERINFO_CREATED_IN = "Criado em"
	var SERVERINFO_JOINED_IN = "Entrei aqui em"
	var SERVERINFO_MEMBERS = "Membros"
	var SERVERINFO_ONLINE = "Online"
	var SERVERINFO_AWAY = "Ausente"
	var SERVERINFO_BUSY = "Ocupado"
	var SERVERINFO_OFFLINE = "Offline"
	var SERVERINFO_PEOPLE = "Pessoas"
	var SERVERINFO_BOTS = "Bots"
	var SERVERINFO_ROLES = "Cargos"
	var SERVERINFO_CUSTOM_EMOJIS = "Emojis customizados"

	// ===[ COMMANDS - FUN ]===
	// AmigosCommand.kt
	var AMIGOS_DESCRIPTION = "Obrigado por serem **VOCÊ NÃO** os melhores amigos de todos!"

	// AmizadeCommand.kt
	var AMIZADE_DESCRIPTION = "Avise que acabou a sua amizade com alguém de uma maneira simples e fácil!"
	var AMIZADE_AMIZADE_COM = "A amizade com {0}"
	var AMIZADE_ENDED = "acabou"
	var AMIZADE_NOW = "Agora"
	var AMIZADE_IS_MY = "é o(a) meu(minha)"
	var AMIZADE_BEST_FRIEND = "melhor amigo(a)"

	// AvaliarWaifuCommand.kt
	var RATEWAIFU_DESCRIPTION = "Receba uma nota para a sua Waifu!"
	var RATEWAIFU_10 = "Simplesmente perfeita! Não trocaria de Waifu se fosse você!"
	var RATEWAIFU_9 = "Uma Waifu excelente, ótima escolha."
	var RATEWAIFU_8 = "Uma Waifu que acerta em todos os pontos bons da vida."
	var RATEWAIFU_7 = "Nem todas as Waifus são perfeitas, mas qual seria a graça de viver com alguém perfeito?";
	var RATEWAIFU_6 = "Se fosse nota de escola sua Waifu ela seria \"acima da média\"";
	var RATEWAIFU_5 = "Nem tão ruim, nem tão boa, bem \"normal\"";
	var RATEWAIFU_4 = "Não que a sua Waifu seja ruim, pelo contrário! Ela tem potencial para ser algo mais *interessante*!";
	var RATEWAIFU_3 = "Sua Waifu precisa de mais substância.";
	var RATEWAIFU_2 = "Não é por nada não mas, se eu você fosse você, eu trocaria de Waifu...";
	var RATEWAIFU_1 = "Sem chance, troca de Waifu hoje mesmo para garantir sua sanidade.";
	var RATEWAIFU_0 = "Troque de Waifu por favor.";
	var RATEWAIFU_IM_PERFECT = "Sou perfeita!"
	var RATEWAIFU_RESULT = "Eu dou uma nota **{0}/10** para `{1}`! **{2}**"

	// CaraCoroaCommand.kt
	var CARACOROA_DESCRIPTION = "Gire uma moeda e veja se irá cair cara ou coroa! Perfeito para descobrir quem irá ir primeiro em uma partida de futebas"
	var CARACOROA_HEADS = "Cara"
	var CARACOROA_TAILS = "Coroa"

	// CepoCommand.kt
	var CEPO_DESCRIPTION = "Destrua alguém no estilo Gugu Gaiteiro!"

	// ClapifyCommand.kt
	var CLAPIFY_DESCRIPTION = "Quando👏você👏precisa👏chamar👏a👏atenção👏de👏alguém👏da👏maneira👏mais👏irritante👏possível!"

	// DeusCommand.kt
	var DEUS_DESCRIPTION = "Coloca alguém em uma pesquisa do Google sobre \"Deus\""

	// DeusesCommand.kt
	var DEUSES_DESCRIPTION = "Caralho, olha os Deuses mano!"

	// DiscordiaCommand.kt
	var DISCORDIA_DESCRIPTION = "Mostre a sua reação quando você recebe uma notificação inútil do Discord!"

	// DrakeCommand.kt
	var DRAKE_DESCRIPTION = "Cria um meme do Drake usando dois usuários da sua guild!"

	// FaustãoCommand.kt
	var FAUSTAO_DESCRIPTION = "Invoque o querido Faustão no seu servidor!"

	// FraseToscaCommand.kt
	var FRASETOSCA_DESCRIPTION = "Cria uma frase tosca utilizando várias mensagens recicladas recebidas por mim"
	var FRASETOSCA_GABRIELA = "Gabriela, a amiga da Loritta"

	// GangueCommand.kt
	var GANGUE_DESCRIPTION = "Gangue da quebrada"

	// InverterCommand.kt
	var INVERTER_DESCRIPTION = "Inverte a cor de uma imagem"

	// LavaCommand.kt
	var LAVA_DESCRIPTION = "O chão é...? Decida o que você quiser!"

	// LavaReversoCommand.kt
	var LAVAREVERSO_DESCRIPTION = "O chão é...? Decida o que você quiser!"

	// TODO: Textos na imagem

	// MagicBallCommand.kt
	var VIEIRINHA_DESCRIPTION = "Pergunte algo para o Vieirinha"
	var VIEIRINHA_responses = Arrays.asList(
			"Vai incomodar outra pessoa, obrigado.",
			"Não sei, mas eu sei que eu moro lá no Cambuci.",
			"Do jeito que eu vejo, sim.",
			"Hmmmm... 🤔",
			"Não posso falar sobre isso.",
			"Não.",
			"Sim.",
			"Eu responderia, mas não quero ferir seus sentimentos.",
			"Provavelmente sim",
			"Provavelmente não",
			"Minhas fontes dizem que sim",
			"Minhas fontes dizem que não",
			"Você pode acreditar nisso",
			"Minha resposta é não",
			"Minha resposta é sim",
			"Do jeito que eu vejo, não.",
			"Melhor não falar isto para você agora...",
			"Sim, com certeza!",
			"Também queria saber...",
			"A minha resposta não importa, o que importa é você seguir o seu coração. 😘",
			"Talvez...",
			"Acho que sim.",
			"Acho que não.",
			"Talvez sim.",
			"Talvez não.",
			"Sim!",
			"Não!",
			"¯\\_(ツ)_/¯")

	// NyanCatCommand.kt
	var NYANCAT_DESCRIPTION = "Nyan Cat, diretamente no seu servidor! E você pode pedir o tamanho do Nyan Cat igual quando você pede algum sanduíche no Subway!"

	// PedraPapelTesouraCommand.kt
	var PPT_DESCRIPTION = "Jogue Pedra, Papel ou Tesoura! (jankenpon, ou a versão abrasileirada: jokenpô)"
	var PPT_WIN = "Parabéns, você ganhou!"
	var PPT_LOSE = "Que pena... você perdeu, mas o que vale é a intenção!"
	var PPT_DRAW = "Empate! Que tal uma revanche?"
	var PPT_CHOSEN = "Você escolheu {0}, eu escolhi {1}"
	var PPT_JESUS_CHRIST = "JESUS CRISTO"
	var PPT_MAYBE_DRAW = "Empate...?"
	var PPT_INVALID = "Que pena... você perdeu, dá próxima vez escolha algo que seja válido, ok?"

	// PerdaoCommand.kt
	var PERDAO_DESCRIPTION = "Um monstro desses merece perdão?"

	// PerfeitoCommand.kt
	var PERFEITO_DESCRIPTION = "Será que \"Nada é perfeito\" mesmo?"

	// PretoEBrancoCommand.kt
	var PRETOEBRANCO_DESCRIPTION = "Relembre os belos momentos ao imprimir trabalhos para a escola, quando suas belas imagens coloridas no Word viravam imagens irreconhecíveis em preto e branco, só porque não tinha tinta colorida!"

	// PrimeirasPalavrasCommand.kt
	var PRIMEIRAS_DESCRIPTION = "Ai meu deus... as primeiras palavras do bebê!"

	// QuadroCommand.kt
	var QUADRO_DESCRIPTION = "Coloca alguém em um quadro com o Wolverine olhando para ele"

	// QualidadeCommand.kt
	var QUALIDADE_DESCRIPTION = "Cria uma mensagem com Q U A L I D A D E & S I N C R O N I A"

	// TODO: RandomSAMCommand.kt & RandomMemeguy1997.kt

	// RazoesCommand.kt
	var RAZOES_DESCRIPTION = "Qual é a sua razão para viver?"

	// ReceitasCommand.kt
	var RECEITAS_DESCRIPTION = "Procure receitas delíciosas da Ana Maria Braga™!"
	var RECEITAS_INFO = "Um artigo da categoria \"{0}\" para a sua família! Delícioso! \uD83D\uDC26"
	var RECEITAS_COULDNT_FIND = "Não encontrei nada relacionado a \"{0}\" no livro de receitas da Ana Maria Braga!"

	// RollCommand.kt
	var ROLL_DESCRIPTION = "Rola um dado e fala o resultado dele, perfeito quando você quer jogar um Monopoly maroto mas perdeu os dados."
	var ROLL_INVALID_NUMBER = "Número inválido!"
	var ROLL_RESULT = "Resultado"

	// SAMCommand.kt
	var SAM_DESCRIPTION = "Adiciona uma marca da água do South America Memes em uma imagem"

	// ShipCommand.kt
	var SHIP_DESCRIPTION = "Veja se um casal daria certo (ou não!)"
	var SHIP_NEW_COUPLE = "Hmmm, será que nós temos um novo casal aqui?"

	var SHIP_valor90 = listOf("😍 %ship% Os dois se amam! 😍",
			"💗 %ship% Casal mais perfeito? Impossível! 💗",
			"☠ %ship% Nem a *dona* morte separa! 😂",
			"😋 %ship% Casal mais perfeito que eu! 😋",
			"😚 %ship% Casal? Casal que nada! Eles já são casados! 😚")

	var SHIP_valor80 = listOf("😏 %ship% Mas esses dois já se conhecem faz muito tempo... 😏",
			"😊 %ship% Claro que os dois são um lindo casal! 😊",
			"😜 %ship% Casal mais grudento que Despacito! 😜",
			"😄 %ship% Se os dois já não namoram eu estaria surpresa! 😄")

	var SHIP_valor70 = listOf("🙂 %ship% Prevejo um casal fofo se formando! 🙂",
			"😄 %ship% Só precisa de um pouco mais de conversa para rolar! 😜",
			"😊 %ship% Os dois foram feitos um para o outro! 😊",
			"😄 %ship% Sim! 😄")

	var SHIP_valor60 = listOf("🙂 %ship% Se o/a %user% parasse de ser um pouco tímido(a)... 😏",
			"😊 %ship% Tem um pouco de interesses compatíveis aí 😊",
			"🙂 %ship% Eu aprovo esse casal! 🙂",
			"😄 %ship% Sim! 😄")

	var SHIP_valor50 = listOf("😶 %ship% Amigos... Mas talvez né... 😏",
			"😊 %ship% Talvez... Só precisa o/a %user% querer! 😶",
			"😶 %ship% Eu queria ver esse casal funcionar 😶")

	var SHIP_valor40 = listOf("😶 %ship% É... talvez, eu acho... 🙁",
			"😶 %ship% Nada é impossível, mas... 🙁",
			"😶 %ship% Se dois quererem, talvez dê certo... Mas... 😶")

	var SHIP_valor30 = listOf("😕 %ship% Acho que não seria um belo casal... 🙁",
			"😶 %ship% Parece que só são conhecidos... 😶")

	var SHIP_valor20 = listOf("😐 %ship% Se o(a) %user% não tivesse deixado na friendzone... 😐")

	var SHIP_valor10 = listOf("😐 %ship% Eu queria muito falar que é possível, mas... 😢")

	var SHIP_valor0 = listOf("😭 %ship% As possibilidades de ter este casal são quase impossíveis! 😭")

	// SpinnerCommand.kt
	var SPINNER_DESCRIPTION = "Gira um fidget spinner! Quanto tempo será que ele irá ficar rodando?"
	var SPINNER_SPINNING = "Girando o fidget spinner..."
	var SPINNER_SPINNED = "Seu spinner girou por **{0}** segundos!"

	// TretaNewsCommand.kt
	var TRETANEWS_DESCRIPTION = "VOOOOOOOCÊ ESTÁ ASSISTINDO TRETA NEWS ENTÃO VAMOS DIRETO PARA AS NOTÍCIAS"

	// TristeRealidadeCommand.kt
	var TRISTEREALIDADE_DESCRIPTION = "Cria uma triste realidade no seu servidor"
	var TRISTEREALIDADE_FILE = "meme_1.png"

	// TrumpCommand.kt
	var TRUMP_DESCRIPTION = "O que será que o senhor presidente Trump está mostrando hoje?"

	// VaporondaCommand.kt
	var VAPORONDA_DESCRIPTION = "Cria uma mensagem com ａｅｓｔｈｅｔｉｃｓ"

	// VaporQualidadeCommand.kt
	var VAPORQUALIDADE_DESCRIPTION = "Quando você mistura Q U A L I D A D E e ａｅｓｔｈｅｔｉｃｓ"

	// WikiaCommand.kt
	var WIKIA_DESCRIPTION = "Procure algo em uma fandom na wikia"
	var WIKIA_COULDNT_FIND = "Não consegui encontrar nada relacionado á `{0}` na wikia `{1}`!"

	// YouTubeCommand.kt
	var YOUTUBE_DESCRIPTION = "Procura um vídeo no YouTube"
	var YOUTUBE_RESULTS_FOR = "Resultados para `{0}`"
	var YOUTUBE_COULDNT_FIND = "Não consegui encontrar nada relacionado á `{0}`!"
	var YOUTUBE_CHANNEL = "Canal"

	// ===[ COMMANDS - MINECRAFT ]===
	// McAvatarCommand.kt
	var MCAVATAR_DESCRIPTION = "Mostra o avatar de uma conta do Minecraft, caso a conta não exista ele irá mostrar a skin padrão (Steve)"
	var MCAVATAR_AVATAR_DE = "Avatar de {0}"

	// McBodyCommand.kt
	var MCBODY_DESCRIPTION = "Mostra o corpo de uma conta do Minecraft, caso a conta não exista ele irá mostrar a skin padrão (Steve)"
	var MCBODY_BODY_DE = "Estátua de {0}"

	// McHeadCommand.kt
	var MCHEAD_DESCRIPTION = "Mostra a cabeça de uma conta do Minecraft, caso a conta não exista ele irá mostrar a skin padrão (Steve)"
	var MCHEAD_HEAD_DE = "Cabeça de {0}"

	// McQueryCommand.kt
	var MCQUERY_DESCRIPTION = "Mostra quantos players um servidor de Minecraft tem"
	var MCQUERY_OFFLINE = "Servidor `{0}:{1}` não existe ou está offline!"
	var MCQUERY_VERSION = "Versão"
	var MCQUERY_PROTOCOL = "Protocolo"

	// McSignCommand.kt
	var MCSIGN_DESCRIPTION = "Escreve um texto em uma placa do Minecraft!"

	// McStatusCommand.kt
	var MCSTATUS_DESCRIPTION = "Verifica se os servidores da Mojang estão online"
	var MCSTATUS_MOJANG_STATUS = "Status da Mojang"

	// McUUIDCommand.kt
	var MCUUID_DESCRIPTION = "Pega a UUID de um usuário"
	var MCUUID_RESULT = "A UUID de `{0}`: `{1}`"
	var MCUUID_INVALID = "Player não encontrado! Tem certeza que `{0}` é uma conta válida?"

	// OfflineUUIDCommand.kt
	var OFFLINEUUID_DESCRIPTION = "Pega a UUID offline (ou seja, de servidores sem autenticação da Mojang) de um player"
	var OFFLINEUUID_RESULT = "**UUID offline (sem autenticação da Mojang) de `{0}`:** `{1}`"

	// ===[ COMMANDS - MISC ]===
	// AjudaCommand.kt
	var AJUDA_DESCRIPTION = "Mostra todos os comandos disponíveis que eu posso executar, lembrando que isto só irá mostrar os comandos habilitados no servidor que você executou a ajuda!"
	var AJUDA_SENT_IN_PRIVATE = "Enviei para você no privado, veja suas mensagens diretas!"
	var AJUDA_INTRODUCE_MYSELF = "Olá {0}, eu me chamo Loritta (ou, para amigos(as) mais próximos(as), \"Lori\") e eu sou apenas um simples bot para o Discord!\n\nO meu objetivo é ser um bot com várias funções, extremamente modular, fácil de usar e super customizável para qualquer servidor/guild brasileiro poder usar! (Quer me adicionar no seu servidor? Então clique [aqui]({1}))!\n\nAtualmente você está vendo a ajuda do **{2}**!"
	var AJUDA_MY_HELP = "Ajuda da Loritta"

	// AngelCommand.kt
	var ANGEL_DESCRIPTION = "Mostra um anjo muito puro para este mundo cruel :^)"

	// EscolherCommand.kt
	var ESCOLHER_DESCRIPTION = "Precisando de ajuda para escolher alguma coisa? Então deixe-me escolher para você!"
	var ESCOLHER_RESULT = "Eu escolhi `{0}`!"

	// PingCommand.kt
	var PING_DESCRIPTION = "Um comando de teste para ver se eu estou funcionando, recomendo que você deixe isto ligado para testar!"

	// LanguageCommand.kt
	var LANGUAGE_DESCRIPTION = "Permite alterar a linguagem que eu falo em seu servidor!"
	var LANGUAGE_INFO = "Clique na linguagem desejada!\n{0}"
	var LANGUAGE_USING_LOCALE = "Agora eu irei falar em `{0}`!"

	// ===[ COMMANDS - SOCIAL ]===
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
	var RANK_SERVER_RANK = "Ranking do {0}"

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
	var HELLO_WORLD_DESCRIPTION = "Um simples comando para testar o meu sistema de linguagem."
	var USING_LOCALE = "Agora estou usando {0} como locale!"

	// ===[ COMMANDS - MUSIC ]===
	// MusicInfoCommand.kt & PlaylistCommand.kt
	var MUSICINFO_DESCRIPTION = "Fala a música que está tocando agora."
	var MUSICINFO_NOMUSIC = "Nenhuma música está tocando... Que tal tocar uma? `+tocar música`"
	var MUSICINFO_INQUEUE = "Na fila..."
	var MUSICINFO_NOMUSIC_SHORT = "Nenhuma música..."
	var MUSICINFO_REQUESTED_BY = "pedido por"
	var MUSICINFO_LENGTH = "Duração"
	var MUSICINFO_VIEWS = "Visualizações"
	var MUSICINFO_LIKES = "Gostei"
	var MUSICINFO_DISLIKES = "Não Gostei"
	var MUSICINFO_COMMENTS = "Comentários"
	var MUSICINFO_SKIPTITLE = "Quer pular a música?"
	var MUSICINFO_SKIPTUTORIAL = "**Então use \uD83E\uDD26  nesta mensagem!** (Se 75% das pessoas no canal de música reagirem com \uD83E\uDD26, eu irei pular a música!)"

	// PularCommand.kt
	var PULAR_DESCRIPTION = "Pula uma música."
	var PULAR_MUSICSKIPPED = "Música pulada!"

	// TocarCommand.kt
	var TOCAR_DESCRIPTION = "Adiciona uma música para a fila da DJ Loritta!"
	var TOCAR_MUTED = "Alguém me mutou no canal de voz... \uD83D\uDE1E Por favor, peça para alguém da administração para desmutar!"
	var TOCAR_CANTTALK = "Eu não tenho permissão para falar no canal de voz... \uD83D\uDE1E Por favor, peça para alguém da administração dar permissão para eu poder soltar alguns batidões!"
	var TOCAR_NOTINCHANNEL = "Você precisa estar no canal de música para poder colocar músicas!"

	// VolumeCommand.kt
	var VOLUME_DESCRIPTION = "Altera o volume da música"
	var VOLUME_TOOHIGH = "Você quer ficar surdo? Bem, você pode querer, mas eu também estou escutando e eu não quero."
	var VOLUME_TOOLOW = "Não cara, colocar números negativos não irá deixar a música tão mutada que ela é banida do planeta terra."
	var VOLUME_LOWER = "irei diminuir o volume do batidão! Desculpe se eu te incomodei com a música alta..."
	var VOLUME_HIGHER = "irei aumentar o volume do batidão! Se segura aí que agora você vai sentir as ondas sonoras!"
	var VOLUME_EXCEPTION = "Ok, vamos alterar o volume para 💩 então... coloque um número válido por favor!"

	// ~ generic ~
	var MUSIC_MAX = "Música grande demais! Uma música deve ter, no máximo, `{0}` de duração!"
	var MUSIC_ADDED = "Adicionado na fila `{0}`!"
	var MUSIC_PLAYLIST_ADDED = "Adicionado na fila {0} músicas!"
	var MUSIC_PLAYLIST_ADDED_IGNORED = "Adicionado na fila {0} músicas! (ignorado {1} faixas por serem muito grandes!)"
	var MUSIC_NOTFOUND = "Não encontrei nada relacionado a `{0}` no YouTube... Tente colocar para tocar o link do vídeo!"
	var MUSIC_ERROR = "Ih Serjão Sujou!\n`{0}`\n(Provavelmente é um vídeo da VEVO e eles só deixam ver a música no site do YouTube... \uD83D\uDE22)"

	// ===[ COMMANDS - POKÉMON ]===
	// PokedexCommand.kt
	var POKEDEX_DESCRIPTION = "Pesquisa informações sobre um Pokémon"
	var POKEDEX_TYPES = "Tipos"
	var POKEDEX_ADDED_IN_GEN = "Adicionado na Geração"
	var POKEDEX_NUMBER = "Número na Pokédex"
	var POKEDEX_ABILITIES = "Habilidades"
	var POKEDEX_BASE_EXP = "Base EXP"
	var POKEDEX_EFFORT_POINTS = "Effort Points"
	var POKEDEX_CAPTURE_RATE = "Taxa de Captura"
	var POKEDEX_BASE_HAPPINESS = "Base happiness"
	var POKEDEX_GROWTH_RATE = "Taxa de crescimento"
	var POKEDEX_TRAINING = "Treinamento"
	var POKEDEX_EVOLUTIONS = "Evoluções"

	// ===[ COMMANDS - UNDERTALE ]===
	// UndertaleBattleCommand.kt
	var UTBATTLE_DESCRIPTION = "Cria um balão de fala igual ao do Undertale"
	var UTBATTLE_INVALID = "Monstro `{0}` não é válido! **Lista de monstros válidos:** `{1}`"

	// UndertaleBoxCommand.kt
	var UTBOX_DESCRIPTION = "Cria uma caixa de diálogo igual ao do Undertale"

	// ===[ COMMANDS - UTILS ]===
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
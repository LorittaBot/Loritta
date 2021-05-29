package com.mrpowergamerbr.loritta.utils

import com.mrpowergamerbr.loritta.Loritta
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.utils.ImageFormat
import net.perfectdreams.loritta.utils.extensions.getEffectiveAvatarUrl
import java.awt.Color
import java.awt.Font
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import javax.imageio.ImageIO

object TretaNewsGenerator {
	var titleBef = ArrayList<String>()
	var objects: List<String>
	var emotions: List<String>
	var social: List<String>
	var randomYt: List<String>
	var randomDescriptions: List<String>
	var randomGame: List<String>
	var randomAvatars: List<String>

	init {
		titleBef.add("{@user} desabafa após polêmica com piada sobre {@object}")
		titleBef.add("{@user} é expulsa do {@social}")
		titleBef.add("{@user} faz piada com {@object} e sofre {@emotion}")
		titleBef.add("{@user} reclama do {@object}")
		titleBef.add("{@user} pretende sair do {@social}!?")
		titleBef.add("{@user} gera polêmica com vídeo")
		titleBef.add("{@user} vs Garota Conservadora")
		titleBef.add("{@user} tem {@object} roubado")
		titleBef.add("{@user} diz que vai dançar nua se chegar a 500K")
		titleBef.add("{@user} tem  roubada")
		titleBef.add("{@user} leva Golpe na Internet e perde placa de 100K inscritos")
		titleBef.add("{@user} leva Golpe na Internet e perde placa de 100K inscritos")
		titleBef.add("{@user} vai gravar vídeo pornô quando chegar a 1 milhão")
		titleBef.add("{@user} reclama de taxistas")
		titleBef.add("{@user} causa incêndio ao fritar hamburguer")
		titleBef.add("{@user} é xingado no meio da rua")
		titleBef.add("{@user} troca farpas por causa de vídeo de salgadinhos chips")
		titleBef.add("{@user} quase explode casa ao soltar Rojão")
		titleBef.add("{@user} é despejado de novo")
		titleBef.add("{@user} gera furdúncio com vídeo de salgadinhos")
		titleBef.add("{@user} critíca vídeos")
		titleBef.add("{@user} é invadida por ladrões")
		titleBef.add("Ladrão invade casa de Streamer AO VIVO!")
		titleBef.add("{@user} reclama de fãs invadindo sua privacidade")
		titleBef.add("{@user} sofre {@emotion} após queimar placa")
		titleBef.add("{@user} recebe reclamações de Clickbait")
		titleBef.add("{@user} queima {@object} com maçarico e gera furdúncio")
		titleBef.add("{@user} sofre {@emotion} após entrevistar {@object}")
		titleBef.add("{@user} tem vídeo com cenas de nudez removido do {@social}")
		titleBef.add("Felipe Neto critica {@user}")
		titleBef.add("Cellbit critica {@user}")
		titleBef.add("RezendeEvil e {@user} reclamam do {@object}")
		titleBef.add("{@user} e {@youtuber-1} reclamam do {@object}")
		titleBef.add("{@user} grava vídeo com {@object} e se acidenta")
		titleBef.add("{@user} grava vídeo com {@object} e se acidenta")
		titleBef.add("{@user} é vítima de assalto")
		titleBef.add("{@user} se envolve em polêmica e responde")
		titleBef.add("{@user} tem problemas e para canal")
		titleBef.add("{@user} posta vídeo de ataque com faca")
		titleBef.add("{@user} desmaia em livestream")
		titleBef.add("{@user} é atingido por {@object}")
		titleBef.add("{@user} posta vídeo com nudes")
		titleBef.add("{@user} é assaltado ao sair do colégio")
		titleBef.add("{@user} é golpeado com {@object} durante gravação de pegadinha")
		titleBef.add("{@user} tranca {@object} em caixa e gera polêmica")
		titleBef.add("{@user}, {@youtuber-1} e {@youtuber-2} causam furdúncio")
		titleBef.add("VenomExtreme, {@user} e Fluffy causam furdúncio")
		titleBef.add("{@user} é agredido na rua")
		titleBef.add("{@object} do {@user} vira piscininha")
		titleBef.add("Bloco de gelo explode na cara de {@user}")
		titleBef.add("Cellbit e Coelho criticam {@user}")
		titleBef.add("Youtuber corre atrás de ladrão que roubou seu {@object}")
		titleBef.add("{@user} NÃO deleta canal e trolla todo mundo")
		titleBef.add("{@user} deleta canal e trolla todo mundo")
		titleBef.add("{@user} é preso em pegadinha")
		titleBef.add("Youtubers criticam Youtube Rewind 2016")
		titleBef.add("{@user} ironiza rumores de ter dado \"ataque de estrelismo\"")
		titleBef.add("{@user} se acidenta ao gravar gameplay")
		titleBef.add("{@object} explode na cara de {@user}")
		titleBef.add("{@user} critica jogadores famosos de League of Legends")
		titleBef.add("{@user} critica jogadores famosos de DOTA")
		titleBef.add("{@user} critica jogadores famosos de Overwatch")
		titleBef.add("{@user} critica jogadores famosos de Minecraft")
		titleBef.add("{@user} critica jogadores famosos de Counter Strike")
		titleBef.add("{@user} se envolve em acidente de trânsito")
		titleBef.add("{@user} passa cheetos no olho")
		titleBef.add("{@user} posta música e sofre hate")
		titleBef.add("{@youtuber-1} e {@youtuber-2} criticam youtubers")
		titleBef.add("{@user} voltou?")
		titleBef.add("{@user} leva soco de fã do  {@youtuber-1}")
		titleBef.add("{@user} responde {@youtuber-1}")
		titleBef.add("Vídeo do {@youtuber-1} deixa {@user} e outros Youtubers estupefatos")
		titleBef.add("{@user} cai e quebra clavícula")
		titleBef.add("{@user} foi sabotado?")
		titleBef.add("{@user} invade palco e é alfinetado por {@youtuber-1}")
		titleBef.add("{@user} vai QUEBRAR sua placa de 1 milhão de inscritos!?")
		titleBef.add("{@user} teve canal DELETADO!? ")
		titleBef.add("{@user} desmente boato")
		titleBef.add("{@user} e {@youtuber-1} trocam farpas no {@social}")
		titleBef.add("{@user} sofre sequestro relâmpago")
		titleBef.add("{@youtuber-1} teve canal hackeado!?")
		titleBef.add("{@user} foi realmente possuída!?")
		titleBef.add("Ela se cortou!?")
		titleBef.add("Ele se cortou!?")
		titleBef.add("{@user} critica vídeos de espíritos")
		titleBef.add("{@object} gera furdúncio entre youtubers")
		titleBef.add("Bugs estão saindo de controle?")
		titleBef.add("{@user} é possuída ao jogar tabuleiro Ouija AO VIVO!?")
		titleBef.add("{@youtuber-1} é possuída ao jogar {@object} AO VIVO!?")
		titleBef.add("{@youtuber-1} usa {@object} para flagrar traição do {@user} e posta vídeo no {@social}")
		titleBef.add("Terremoto atinge casa de {@user} AO VIVO")
		titleBef.add("{@user} é hackeado de novo")
		titleBef.add("{@user} é surpreendido por traficantes")
		titleBef.add("{@user} reclama de FC's")
		titleBef.add("{@user} leva SOCO NA CARA")
		titleBef.add("{@user} mostra os peitos e é banida")
		titleBef.add("{@youtuber-1} mostra a bunda e é banido")
		titleBef.add("{@user} recebe presente de stalker!?")
		titleBef.add("{@user} possuído pega FOGO sozinho!?")
		titleBef.add("{@youtuber-1} filma {@user} possuído!?")
		titleBef.add("{@user} falou mal do {@youtuber-1} pelas costas")
		titleBef.add("{@user} leva cotovelada de repórter")
		titleBef.add("Youtuber enterra {@object} vivo e gera polêmica")
		titleBef.add("{@user} flagra espírito em vídeo!?")
		titleBef.add("{@user} faz 100 camadas de agulhadas no rosto e é censurado")
		titleBef.add("Atrasados perdem prova e geram furdúncio")
		titleBef.add("{@youtuber-1} faz camarote para assistir")
		titleBef.add("{@youtuber-1} recebe críticas após sensualizar em show e responde!")
		titleBef.add("Youtube vai mudar os comentários?")
		titleBef.add("{@user} trolla namorada e gera polêmica")
		titleBef.add("{@youtuber-1} alfineta {@youtuber-2}!?")
		titleBef.add("{@user} sofre ATAQUE hater")
		titleBef.add("{@user} é alfinetado após derrota na copa de {@game}")
		titleBef.add("Editores se revoltam contra {@user}")
		titleBef.add("{@user} leva STRIKE")
		titleBef.add("{@user} APANHOU na rua")
		titleBef.add("{@user} é chamado de arregão e responde")
		titleBef.add("{@user} luta com touro em livestream e vai parar no hospital")
		titleBef.add("{@user} encontra {@object} assassino!?")
		titleBef.add("{@user} é alvo de novo ataque hater")
		titleBef.add("{@user} perde mais de 30 mil inscritos")
		titleBef.add("{@user} e outros youtubers comentam polêmica")
		titleBef.add("{@user} cospe na boca de {@object} e causa revolta na internet")
		titleBef.add("{@user} se revolta contra o {@social}")
		titleBef.add("{@user} dá corte de giro na cara do {@youtuber-1}")
		titleBef.add("{@user} critica estado atual do {@social}")
		titleBef.add("{@youtuber-1} dá block no {@youtuber-2}!?")
		titleBef.add("{@user} é PERIGOSO!?")
		titleBef.add("{@user} está planejando seu templo")
		titleBef.add("{@user} gera revolta de youtubers de MotoVlog e motociclistas")
		titleBef.add("Vídeos de 100 camadas geram furdúncio")
		titleBef.add("{@user} pede desculpas a {@youtuber-1}")
		titleBef.add("{@object} misterioso ataca {@user}!")
		titleBef.add("{@object} misterioso MATA {@user}!")
		titleBef.add("{@youtuber-1} MATA {@user} AO VIVO!")
		titleBef.add("{@user} grava acidente de carro AO VIVO!")
		titleBef.add("{@user} grava acidente de carro AO VIVO!")
		titleBef.add("{@user} pega FOGO e leva STRIKE")
		titleBef.add("{@user} pega FOGO e MORRE")
		titleBef.add("{@user} é agredido e filma tudo")
		titleBef.add("{@user} descobre quem é o {@object} que está atacando sua casa!")
		titleBef.add("{@user} responde Hater")
		titleBef.add("{@user} leva choque de tazer e desmaia")
		titleBef.add("\"Me tiraram pra Judas\" diz {@user}")
		titleBef.add("Grupo de famosos fala mal do {@user} no {@social}!?")
		titleBef.add("{@user} vai sair na rua gritando G2A!?")
		titleBef.add("{@user} recebe doação de 10 mil reais e passa mal,")
		titleBef.add("{@user} é alvo de trollagem")
		titleBef.add("{@youtuber-1} se livra do strike aplicado por {@user}")
		titleBef.add("{@user} faz limpa de mendigos de comentários")
		titleBef.add("Espírito fala \"{@user}\" em vídeo do {@youtuber-1}!?")
		titleBef.add("{@user} foi TRANCADO dentro da sua própria casa!")
		titleBef.add("{@user} vai banir mendigos de comentários!?")
		titleBef.add("{@user} gera grande furdúncio")
		titleBef.add("{@user} finalmente revela resposta da conspiração")
		titleBef.add("{@user} e {@youtuber-1} sofrem queimaduras graves ao gravar vídeo para o {@social}")
		titleBef.add("{@user} pode processar Jornal por notícia falsa")
		titleBef.add("{@user} bate o carro e morre enquanto fazia live dirigindo")
		titleBef.add("{@user} está sendo processado")
		titleBef.add("{@user} fica preso no elevador")
		titleBef.add("{@user} é demitido após comentários polêmicos")
		titleBef.add("{@user} quase leva golpe ")
		titleBef.add("{@user} é ROUBADO")
		titleBef.add("{@user} leva 2 strikes")
		titleBef.add("{@user} é acusada de usar BOT")
		titleBef.add("{@user} critica estado atual do {@social}")
		titleBef.add("Youtuber {@user} é alvo de trotes")
		titleBef.add("{@youtuber-1} chama {@user} de merda e o clima fica tenso")
		titleBef.add("{@user} corta linha de pescadores e apanha")
		titleBef.add("{@user} diz que vai responder \"garoto do cabelo cor de rosa\"")
		titleBef.add("{@user} comenta")
		titleBef.add("{@youtuber-1} chama {@user} de lixo e ele responde")
		titleBef.add("Esquema de BOTs no Youtube REVELADO! {@user} está envolvido ")
		titleBef.add("{@user} alfineta {@youtuber-1} e mostra apoio a {@youtuber-2}")
		titleBef.add("{@user} briga feio com {@youtuber-1} e pode ser processado")
		titleBef.add("{@user} é assaltado enquanto gravava vídeo")
		titleBef.add("{@user} tem cabelo queimado AO VIVO")
		titleBef.add("{@user} é criticado e responde")
		titleBef.add("{@user} vira \"homem-formiga\" em vídeo e recebe chuva de dislikes")
		titleBef.add("Youtubers reclamam do conteúdo que faz sucesso, {@user} diz: \"é puro recalque\"")
		titleBef.add("{@user} se revolta e detona {@youtuber-1}")
		titleBef.add("{@user} responde")
		titleBef.add("Pai de fã chama {@user} de \"Sem-vergonha\"")
		titleBef.add("YouTuber {@user} é processado após fazer pegadinha da \"buzina poderosa\"")
		titleBef.add("{@user} recebe críticas por causa do preço do seu curso e responde")
		titleBef.add("{@user} come {@object} fora da validade e vai parar no hospital")
		titleBef.add("Coisa estranha aparece na janela de {@user} e deixa todos apavorados")
		titleBef.add("{@user} tem vídeo censurado")
		titleBef.add("{@user} bate recorde de {@object}")
		titleBef.add("Jornal critica App do {@user}, {@user} diz que \"o texto ficou raso\"")
		titleBef.add("{@youtuber-1} critica {@user} AO VIVO")
		titleBef.add("{@user} sofre hate de inscritos")
		titleBef.add("{@user} diz que vai jogar \"verdades\" no ventilador")
		titleBef.add("{@user} critica {@object}")
		titleBef.add("{@user} critica {@social}")
		titleBef.add("{@youtuber-1} posta vídeo com indiretas para {@user}")
		titleBef.add("{@user} é agredido no Rock in Rio")
		titleBef.add("{@user} diz: \"Eu to arrasado, muito triste, completamente destruído.\"")
		titleBef.add("{@user} ostenta e recebe críticas")
		titleBef.add("{@user} está sendo processado")
		titleBef.add("{@user} diz que o Youtube está ACABANDO com os canais de Gameplay ")
		titleBef.add("{@youtuber-1} alfineta Luccas Neto!?")
		titleBef.add("{@user} é assaltado enquanto gravava vídeo")
		titleBef.add("{@user} troca farpas no {@social}")
		titleBef.add("{@user} responde críticas")
		titleBef.add("{@user} desabafa após polêmica do seu filme")
		titleBef.add("{@user} é acusado de ser gordofóbico após zoar {@youtuber-1}")
		titleBef.add("{@youtube-1} detona {@user}")
		titleBef.add("{@user} volta a reclamar de {@object}")
		titleBef.add("{@user} é ameaçado por mafioso AO VIVO")
		titleBef.add("{@user} tem canal desmonetizado")
		titleBef.add("{@user} vai parar no hospital após evento")
		titleBef.add("{@user} tem vídeo de react removido e é detonado por {@youtuber-1}")
		titleBef.add("{@user} contrata equipe para monitorar e derrubar páginas de ódio no FB")
		titleBef.add("{@youtuber-1} tem canal hackeado, {@user} é o próximo??")
		titleBef.add("{@youtuber-1} e {@youtuber-2} brigam feio no {@social} e o clima fica tenso")
		titleBef.add("{@user} diz que vai processar Jovem que planejava atacá-lo")
		titleBef.add("{@youtuber-1} detona sorteio dos Irmãos {@user}, {@user} defende {@user}LAND")
		titleBef.add("{@youtuber-1} manda indireta para {@user} e ele responde")
		titleBef.add("{@user} reclama de Haters no {@social}")
		titleBef.add("{@user} tem clipe censurado")
		titleBef.add("{@user} é banida de {@social} após cena polêmica")
		titleBef.add("{@user} reclama de fã-clube")
		titleBef.add("{@user} e Youtubers reclamam de Bug no site")
		titleBef.add("{@user} e {@youtuber-1} revelam que briga era FAKE e recebem críticas")
		titleBef.add("{@user} e {@youtuber-1} brigam feio em vídeo de trollagem que deu errado")
		titleBef.add("{@user} responde críticas e sorteio da \"{@user}Land\" continua gerando polêmica")
		titleBef.add("{@user} e {@youtuber-1} são criticados após lançarem promoção polêmica")
		titleBef.add("Crianças descobrem endereço de {@user} e ficam do lado de fora gritando o dia inteiro")
		titleBef.add("{@user} DELETA vídeo sobre crise na Venezuela e explica o motivo")
		titleBef.add("{@youtuber-1} discorda do {@user} ao ver ele criticar o {@social} duramente")
		titleBef.add("{@youtuber-1} manda indireta para {@user} AO VIVO")
		titleBef.add("{@user} e {@youtuber-1} reclamam que não foram convidados para o YouTube FanFest")

		objects = Arrays.asList(
				"YouTube Brasil",
				"YouTube",
				"PewDiePie",
				"Deus",
				"Jeová",
				"Garota",
				"Garoto",
				"Twitter",
				"Snapchat",
				"Facebook",
				"Discord",
				"Skype",
				"Instagram",
				"Periscope",
				"Orkut",
				"Google+",
				"YouTube",
				"Tumblr",
				"Carro",
				"Placa de 100k",
				"Vin Diesel",
				"Ferro Quente",
				"Pombo",
				"Faca",
				"Gato",
				"Cachorro",
				"Geladeira",
				"Drone",
				"iPhone",
				"Samsung Galaxy",
				"Bomba",
				"Tabuleiro Ouija",
				"Palhaço",
				"Ração Militar Japonesa",
				"Torre de Cartas")

		emotions = Arrays.asList(
				"hate",
				"alegria",
				"amor",
				"felicidade")

		social = Arrays.asList(
				"Snapchat",
				"Facebook",
				"Twitter",
				"Discord",
				"Skype",
				"Instagram",
				"Periscope",
				"Orkut",
				"Google+",
				"YouTube",
				"Tumblr")

		randomYt = Arrays.asList(
				"Felipe Neto",
				"Kéfera",
				"Castanhari",
				"PokeyBR",
				"Leon",
				"Monark",
				"Feromonas",
				"VenomExtreme",
				"Hannah Meyers",
				"RezendeEvil",
				"AuthenticGames",
				"BRKsEDU",
				"MoonKase",
				"Stux",
				"Fluffy",
				"PortugaPC",
				"Spok",
				"PewDiePie",
				"MrPoladoful",
				"GatoGalactico",
				"Coelho",
				"MixReynold",
				"ForeverPlayer",
				"Everson Zoio",
				"Bluezão",
				"Não Salvo",
				"Mussoumano",
				"Aruan Felix",
				"Bluezão",
				"Dani Russo",
				"Castanhari",
				"Luccas Neto",
				"Luba",
				"Júlio Cocielo",
				"SirKazzio",
				"Whindersson",
				"Haru",
				"Contente",
				"ColôniaContraAtaca",
				"AssopraFitas",
				"Porta dos Fundos",
				"Mítico Jovem",
				"Rato Borrachudo",
				"Izzy Nobre",
				"Galo Frito",
				"Everson Zoio",
				"T3ddy",
				"Pedro Strapasson",
				"Danilo Gentili")

		randomDescriptions = Arrays.asList(
				"Kéfera usa Snapchat para comentar polêmica, Haru tem conta banida do Facebook",
				"Kéfera sofreu hate por causa de piada com Deus, RezendeEvil reclamou do youtube Brasil novamente",
				"Castanhari do Canal Nostalgia diz que youtube está virando piada, Pedro Strapasson gera furdúncio com vídeo polêmico",
				"Felipe Neto e Garota Conservadora geraram furdúncio, Pedro Strapasson foi assaltado e gravou tudo",
				"Youtuber Paola Holmes diz que vai dançar funk sem roupa quando chegar a 500K, PortugaPC possivelmente teve placa roubada",
				"Neox e Eagle do canal Neagle tiveram suas placas de 100 mil inscritos roubadas",
				"Youtuber promete gravar vídeo pornô quando chegar em 1 milhão de inscritos, EduKof e PortugaPC reclamam do serviço de taxistas",
				"Streamer tentou fazer live cozinhando mas acabou dando errado, Youtuber Spok foi xingado jogar minecraft",
				"Enaldinho e PokeyBR trocam farpas no twitter depois de postarem vídeos parecidos",
				"Youtuber quase se acidentou ao soltar fogos de artifício, Pewdiepie foi despejado pela segunda vez",
				"PokeyBR responde polêmica do seu vídeo de chips, Cellbit fez vídeo trollando o estado atual do youtube",
				"Youtuber teve vários pertences roubados em sua casa, Pyong Lee diz que vai largar canal no Twitter",
				"Youtubers T3ddy e Clone reclamam de falta de privacidade, Ladrão tenta roubar streamer ao vivo",
				"Youtuber Inemafoo responde críticas após queimar placa, Contente é alvo de reclamações após vídeo com título polêmico",
				"Youtuber Inemafoo queimou sua placa do Youtube com um maçarico e gerou polêmica",
				"Youtuber Carol Moreira relata assédio de Vin Diesel durante entrevista, Felipe Neto e Cellbit criticam Youtube Brasil",
				"Novo clipe de Clarice Falcão com cenas de nudez é removido do Youtube, Felipe Neto criticou Ju Nogueira",
				"RezendeEvil e Felipe Neto reclamam dos bugs do Youtube",
				"Vlad do Área Secreta se acidentou em seu último vídeo, SanInPlay foi assaltado quando gravava um clipe na praia",
				"Enaldinho se envolveu em furdúncio no Twitter, Drawn Mask perdeu PC e vai parar canal por um bom tempo",
				"Robson do Irmãos fuinha postou vídeo do desafio em que foi golpeado com faca, Streamer desmaiou ao vivo",
				"Youtuber se acidenta por causa de pombo durante motovlog, RezendeEvil e Contente reclamam do Youtube",
				"Zoie Burgher postou vídeo com nudes e gerou polêmica, Youtuber foi assaltado ao sair da escola",
				"Youtuber foi ferido por uma faca durante gravação de um vídeo",
				"Streamer acusada de maus tratos contra animais, PokeyBR, stux777 e MoonKase geram furdúncio no Twitter",
				"Youtuber foi agredido na rua, Geladeira do Rato Borrachudo quebra e forma bolha de água na gaveta de legumes",
				"Cellbit, Coelho, Luba e MrPoladoful comentam sobre a polêmica do Pewdiepie, Mega bloco de gelo explode no rosto de Youtuber",
				"Youtuber teve drone furtado e corre atrás do ladrão, MixReynold anuncia que não vai quebrar placa",
				"Pewdiepie deleta canal secundário e deixa algumas pessoas revoltadas, Youtuber foi preso em pegadinha",
				"Felipe Neto, Leon do Coisa de Nerd, Gusta e Kéfera criticaram o Youtube Rewind 2016 no Twitter",
				"Kéfera anda de helicóptero e brinca com piloto: 'Fala para ninguém me incomodar', Youtuber foi atropelada ao atravessar a rua",
				"Rato Borrachudo teve que ir para o hospital após retirar gesso para gravar gameplay, ForeverPlayer criticou youtubers no Twitter")

		randomGame = Arrays.asList("Minecraft",
				"Overwatch",
				"DOTA",
				"League of Legends",
				"Counter-Strike")

		randomAvatars = Arrays.asList(
				"https://yt3.ggpht.com/-zRMDUjopMak/AAAAAAAAAAI/AAAAAAAAAAA/cXAODFnSfQQ/s176-c-k-no-mo-rj-c0xffffff/photo.jpg",
				"https://yt3.ggpht.com/-M7_xnCYVo04/AAAAAAAAAAI/AAAAAAAAAAA/B067rIwWq3o/s176-c-k-no-mo-rj-c0xffffff/photo.jpg",
				"https://yt3.ggpht.com/-YENFbHgKY4g/AAAAAAAAAAI/AAAAAAAAAAA/M35kguQVZfM/s176-c-k-no-mo-rj-c0xffffff/photo.jpg",
				"https://yt3.ggpht.com/-MokimQWVRYs/AAAAAAAAAAI/AAAAAAAAAAA/kGBW7ISel90/s176-c-k-no-mo-rj-c0xffffff/photo.jpg",
				"https://yt3.ggpht.com/-4D606al42iY/AAAAAAAAAAI/AAAAAAAAAAA/DHqZkHbQyJQ/s176-c-k-no-mo-rj-c0xffffff/photo.jpg",
				"https://yt3.ggpht.com/--ihXSjx7V8c/AAAAAAAAAAI/AAAAAAAAAAA/lglLtJPbpYg/s176-c-k-no-mo-rj-c0xffffff/photo.jpg",
				"https://i.ytimg.com/vi/tVlQJ_XBZFY/hqdefault.jpg?custom=true&w=246&h=138&stc=true&jpg444=true&jpgq=90&sp=68&sigh=qx1CZ4K1lLCqJNOUCA6JnVCEtmg",
				"https://yt3.ggpht.com/-e2MAPIZYc_k/AAAAAAAAAAI/AAAAAAAAAAA/7JLG3L0EGYs/s176-c-k-no-mo-rj-c0xffffff/photo.jpg",
				"https://yt3.ggpht.com/-1LWkxlQsCoM/AAAAAAAAAAI/AAAAAAAAAAA/21vbu7XZYhE/s176-c-k-no-mo-rj-c0xffffff/photo.jpg",
				"https://yt3.ggpht.com/-bIkhJe7-vdk/AAAAAAAAAAI/AAAAAAAAAAA/m5AyW98M-CY/s176-c-k-no-mo-rj-c0xffffff/photo.jpg")
	}

	fun generate(guild: Guild, usr1: User, usr2: User): GeneratedTretaNews {
		val randomYt = ArrayList(TretaNewsGenerator.randomYt)

		val str1 = usr1.name.stripCodeMarks()
		val str2 = usr2.name.stripCodeMarks()
		randomYt.add(str1)
		randomYt.add(str2)

		for (member in guild.members) {
			if (member.onlineStatus != OnlineStatus.OFFLINE) {
				randomYt.add(member.effectiveName)
			}
		}

		val url1 = usr1.getEffectiveAvatarUrl(ImageFormat.PNG, 128)
		val url2 = usr2.getEffectiveAvatarUrl(ImageFormat.PNG, 128)

		var avatar = LorittaUtils.downloadImage(url1)
		var avatar2 = LorittaUtils.downloadImage(url2)

		var tretaCheck = ImageIO.read(File(Loritta.ASSETS + "tretacheck.png"))

		val top = BufferedImage(238, 138, BufferedImage.TYPE_INT_ARGB)

		var treta = ImageIO.read(File(Loritta.ASSETS + "tretasmall.png"))

		var novo = ImageIO.read(File(Loritta.ASSETS + "tretanovo.png"))

		val tempRI1 = avatar!!.getScaledInstance(128, 128, Image.SCALE_SMOOTH)
		val tempRI2 = avatar2!!.getScaledInstance(128, 128, Image.SCALE_SMOOTH)

		val resizedImage = tempRI1.getScaledInstance(tempRI1.getWidth(null), 138, Image.SCALE_SMOOTH)
		val resizedImage2 = tempRI2.getScaledInstance(tempRI2.getWidth(null), 138, Image.SCALE_SMOOTH)

		top.graphics.drawImage(resizedImage, 0, 0, null)
		top.graphics.drawImage(resizedImage2, 119, 0, null)

		top.graphics.drawImage(treta, 0, 0, null)

		val graphics = top.graphics.enableFontAntiAliasing()
		graphics.color = Color(0, 0, 0, 191)
		graphics.fillRect(206, 122, 30, 14)

        graphics.color = Color(255, 255, 255, 255)
		run {
			val font = Font("Arial", Font.BOLD, 11)
            graphics.font = font
			graphics.drawString(Loritta.RANDOM.nextInt(2, 10).toString() + ":" + Loritta.RANDOM.nextInt(10, 60), 211, 122 + font.size)
		}

		val youtube = BufferedImage(655, 138, BufferedImage.TYPE_INT_ARGB)
		val g2d = youtube.graphics.enableFontAntiAliasing()
		youtube.graphics.color = Color.WHITE
		youtube.graphics.fillRect(0, 0, 655, 158)

		youtube.graphics.drawImage(top, 0, 0, null)

		g2d.color = Color(22, 122, 198)
		var font = Font("Arial", Font.PLAIN, 18)
		g2d.font = font

		var t = titleBef[Loritta.RANDOM.nextInt(0, titleBef.size - 1)]
		val `object` = objects[Loritta.RANDOM.nextInt(0, objects.size - 1)]
		val emotion = emotions[Loritta.RANDOM.nextInt(0, emotions.size - 1)]
		val social = social.random()

		var rndYt1_1 = TretaNewsGenerator.randomYt.random()
		var rndYt2_1 = TretaNewsGenerator.randomYt.random()
		var rndYt3_1 = TretaNewsGenerator.randomYt.random()
		var rndYt4_1 = TretaNewsGenerator.randomYt.random()
		var rndYt5_1 = TretaNewsGenerator.randomYt.random()
		val game = randomGame.random()

		t = t.replace("{@user}", str1)
		t = t.replace("{@object}", `object`)
		t = t.replace("{@emotion}", emotion)
		t = t.replace("{@social}", social)
		t = t.replace("{@youtuber-1}", rndYt1_1)
		t = t.replace("{@youtuber-2}", rndYt2_1)
		t = t.replace("{@youtuber-3}", rndYt3_1)
		t = t.replace("{@youtuber-4}", rndYt4_1)
		t = t.replace("{@youtuber-5}", rndYt5_1)
		t = t.replace("{@game}", game)

		if (Loritta.RANDOM.nextInt(0, 12) != 5) {
			var t2 = titleBef[Loritta.RANDOM.nextInt(0, titleBef.size - 1)]
			val object2 = objects[Loritta.RANDOM.nextInt(0, objects.size - 1)]
			val emotion2 = emotions[Loritta.RANDOM.nextInt(0, emotions.size - 1)]
			val social2 = TretaNewsGenerator.social.random()

			rndYt1_1 = randomYt.random()
			rndYt2_1 = randomYt.random()
			rndYt3_1 = randomYt.random()
			rndYt4_1 = randomYt.random()
			rndYt5_1 = randomYt.random()

			t2 = t2.replace("{@user}", str2)
			t2 = t2.replace("{@object}", object2)
			t2 = t2.replace("{@emotion}", emotion2)
			t2 = t2.replace("{@social}", social2)
			t2 = t2.replace("{@youtuber-1}", rndYt1_1)
			t2 = t2.replace("{@youtuber-2}", rndYt2_1)
			t2 = t2.replace("{@youtuber-3}", rndYt3_1)
			t2 = t2.replace("{@youtuber-4}", rndYt4_1)
			t2 = t2.replace("{@youtuber-5}", rndYt5_1)
			t2 = t2.replace("{@game}", game)

			t += ", " + t2
		}

		var title = t
		val originalTitle = title

		if (title.length > 90) {
			title = title.substring(0, 90) + "..."
		}

		// int checkY = drawString(g2d, title, 244, -4);
		var checkY = ImageUtils.drawTextWrap(title, 244, 18, 655, 0, g2d.fontMetrics, g2d)
		checkY += 6

		g2d.drawImage(tretaCheck, 240, checkY, null)

		checkY += 28

		g2d.color = Color(118, 118, 118)
		font = Font("Arial", Font.PLAIN, 12)
		g2d.font = font

		val df = DecimalFormat("#,###")
		val dfs = DecimalFormatSymbols(Locale.UK)
		dfs.groupingSeparator = '.'
		df.decimalFormatSymbols = dfs

		val views = Loritta.RANDOM.nextInt(0, 1000000).toLong()
		val texto = Loritta.RANDOM.nextInt(1, 24).toString() + " horas atrás • " + df.format(views) + " visualizações"

		checkY = ImageUtils.drawTextWrap(texto, 244, checkY, 655, 0, g2d.fontMetrics, g2d)

		checkY += 18

		var descricao = TretaNewsGenerator.randomDescriptions[Loritta.RANDOM.nextInt(0, TretaNewsGenerator.randomDescriptions.size - 1)]

		if (descricao.length > 127) {
			descricao = descricao.substring(0, 127) + "..."
		}

		checkY = ImageUtils.drawTextWrap(descricao, 244, checkY, 655, 0, g2d.fontMetrics, g2d)

		checkY += 8

		g2d.drawImage(novo, 244, checkY, null)

		return GeneratedTretaNews(originalTitle, views, Loritta.RANDOM.nextInt(30000, 100000), Loritta.RANDOM.nextInt(1000, 10000), youtube)
	}

	class GeneratedTretaNews(
			val title: String,
			val views: Long,
			val likes: Int,
			val dislikes: Int,
			val image: BufferedImage
	)
}

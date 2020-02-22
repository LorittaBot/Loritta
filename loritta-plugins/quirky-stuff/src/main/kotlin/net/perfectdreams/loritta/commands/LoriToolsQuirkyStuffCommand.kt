package net.perfectdreams.loritta.commands

import com.mrpowergamerbr.loritta.dao.BirthdayConfig
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.LoriReply
import net.perfectdreams.commands.annotation.Subcommand
import net.perfectdreams.loritta.QuirkyStuff
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.DiscordCommandContext
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.regex.Pattern

class LoriToolsQuirkyStuffCommand(val m: QuirkyStuff) : LorittaDiscordCommand(arrayOf("loritoolsqs"), CommandCategory.MAGIC) {
	override val onlyOwner: Boolean
		get() = true

	@Subcommand(["enable_boost"])
	suspend fun enableBoost(context: DiscordCommandContext, args: Array<String>) {
		val user = context.getUserAt(1) ?: run {
			context.sendMessage("Usuário inexistente!")
			return
		}

		val member = context.discordGuild!!.getMember(user) ?: run {
			context.sendMessage("Usuário não está na guild atual!")
			return
		}

		QuirkyStuff.onBoostActivate(member)
	}

	@Subcommand(["disable_boost"])
	suspend fun disableBoost(context: DiscordCommandContext, args: Array<String>) {
		val user = context.getUserAt(1) ?: run {
			context.sendMessage("Usuário inexistente!")
			return
		}

		val member = context.discordGuild!!.getMember(user) ?: run {
			context.sendMessage("Usuário não está na guild atual!")
			return
		}

		QuirkyStuff.onBoostDeactivate(member)
	}

	@Subcommand(["generate_missing_boosts"])
	suspend fun generateMissingBoosts(context: DiscordCommandContext) {
		context.reply(
				LoriReply(
						"Ativando boosts para pessoas que não possuem as vantagens de boosters..."
				)
		)

		for (booster in context.discordGuild!!.boosters) {
			val payment = transaction(Databases.loritta) {
				Payment.find {
					Payments.money eq 20.00.toBigDecimal() and (Payments.gateway eq PaymentGateway.NITRO_BOOST)
				}.firstOrNull()
			}

			if (payment == null)
				QuirkyStuff.onBoostActivate(context.handle)
		}
	}

	@Subcommand(["send_sponsored_message"])
	suspend fun sendSponsoredMessage(context: DiscordCommandContext) {
		m.sponsorsAdvertisement?.broadcastSponsoredMessage()
	}

	@Subcommand(["enable_birthday_in_channel"])
	suspend fun enableBirthdayInChannel(context: DiscordCommandContext, channelId: String) {
		transaction(Databases.loritta) {
			val serverConfig = ServerConfig.findById(context.discordGuild!!.idLong)!!

			if (serverConfig.birthdayConfig != null) {
				serverConfig.birthdayConfig!!.enabled = true
				serverConfig.birthdayConfig!!.channelId = channelId.toLong()
			} else {
				serverConfig.birthdayConfig = BirthdayConfig.new {
					this.enabled = true
					this.channelId = channelId.toLong()
				}
			}
		}

		context.reply(
				"Ativado!"
		)
	}

	@Subcommand(["set_birthday_role"])
	suspend fun setBirthdayRole(context: DiscordCommandContext, roleId: String) {
		transaction(Databases.loritta) {
			val serverConfig = ServerConfig.findById(context.discordGuild!!.idLong)!!

			serverConfig.birthdayConfig!!.roles = arrayOf(roleId.toLong())
		}

		context.reply(
				"Role ativada!"
		)
	}

	@Subcommand(["cycle_banner"])
	suspend fun cycleBanner(context: DiscordCommandContext, args: Array<String>) {
		m.changeBanner?.changeBanner()
	}


	@Subcommand(["fix_bug"])
	suspend fun fixBug(context: DiscordCommandContext) {
		val lines = """
			[11:31:07.093] [Coroutine Executor Thread 8985/INFO]: Giving 14472.536268134068 sonhos to 107279862934331392
			[11:31:07.094] [Coroutine Executor Thread 8985/INFO]: Giving 94643.52176088045 sonhos to 123170274651668480
			[11:31:07.094] [Coroutine Executor Thread 8985/INFO]: Giving 21073.136568284142 sonhos to 162594636831916033
			[11:31:07.095] [Coroutine Executor Thread 8985/INFO]: Giving 18067.133566783392 sonhos to 171699209936961536
			[11:31:07.095] [Coroutine Executor Thread 8985/INFO]: Giving 119303.65182591297 sonhos to 174950526700617729
			[11:31:07.096] [Coroutine Executor Thread 8985/INFO]: Giving 2804.702351175588 sonhos to 196744131706355712
			[11:31:07.096] [Coroutine Executor Thread 8985/INFO]: Giving 32534.767383691847 sonhos to 207534284175572992
			[11:31:07.097] [Coroutine Executor Thread 8985/INFO]: Giving 883375.187593797 sonhos to 221101554961219585
			[11:31:07.097] [Coroutine Executor Thread 8985/INFO]: Giving 3872.680340170033 sonhos to 230088006428524544
			[11:31:07.097] [Coroutine Executor Thread 8985/INFO]: Giving 53102.75137568785 sonhos to 239725782912598016
			[11:31:07.098] [Coroutine Executor Thread 8985/INFO]: Giving 28874.13706853427 sonhos to 246645318144229377
			[11:31:07.098] [Coroutine Executor Thread 8985/INFO]: Giving 56384.79239619811 sonhos to 249508932861558785
			[11:31:07.099] [Coroutine Executor Thread 8985/INFO]: Giving 94204.60230115059 sonhos to 269903670143877131
			[11:31:07.099] [Coroutine Executor Thread 8985/INFO]: Giving 70543.57178589296 sonhos to 269964181661417482
			[11:31:07.100] [Coroutine Executor Thread 8985/INFO]: Giving 63326.713356678345 sonhos to 272031079001620490
			[11:31:07.100] [Coroutine Executor Thread 8985/INFO]: Giving 967.583791895948 sonhos to 277091263306792960
			[11:31:07.101] [Coroutine Executor Thread 8985/INFO]: Giving 8283.141570785394 sonhos to 279657232042557440
			[11:31:07.101] [Coroutine Executor Thread 8985/INFO]: Giving 12017.408704352178 sonhos to 282895755688280065
			[11:31:07.102] [Coroutine Executor Thread 8985/INFO]: Giving 6783.541770285143 sonhos to 286493556170948618
			[11:31:07.102] [Coroutine Executor Thread 8985/INFO]: Giving 4886.61519778813 sonhos to 293923362323824653
			[11:31:07.102] [Coroutine Executor Thread 8985/INFO]: Giving 127519.45972986495 sonhos to 295318635910529024
			[11:31:07.103] [Coroutine Executor Thread 8985/INFO]: Giving 17940.770385192598 sonhos to 311296198931644416
			[11:31:07.103] [Coroutine Executor Thread 8985/INFO]: Giving 8664.99208127406 sonhos to 317796741024055307
			[11:31:07.104] [Coroutine Executor Thread 8985/INFO]: Giving 43118.659329664835 sonhos to 320992398295564300
			[11:31:07.104] [Coroutine Executor Thread 8985/INFO]: Giving 47562.081040520265 sonhos to 326123612153053184
			[11:31:07.104] [Coroutine Executor Thread 8985/INFO]: Giving 19526.363181590797 sonhos to 328943660362366978
			[11:31:07.105] [Coroutine Executor Thread 8985/INFO]: Giving 21480.94047023512 sonhos to 328973123351216130
			[11:31:07.105] [Coroutine Executor Thread 8985/INFO]: Giving 10428.665332666305 sonhos to 332918506930831362
			[11:31:07.106] [Coroutine Executor Thread 8985/INFO]: Giving 54848.279969985015 sonhos to 335603023000633355
			[11:31:07.106] [Coroutine Executor Thread 8985/INFO]: Giving 7624.31215607804 sonhos to 349380609660944395
			[11:31:07.106] [Coroutine Executor Thread 8985/INFO]: Giving 66579.68984493247 sonhos to 351760430991147010
			[11:31:07.107] [Coroutine Executor Thread 8985/INFO]: Giving 2416.8007113556782 sonhos to 352179628842156033
			[11:31:07.107] [Coroutine Executor Thread 8985/INFO]: Giving 150701.95097548777 sonhos to 361977144445763585
			[11:31:07.108] [Coroutine Executor Thread 8985/INFO]: Giving 423853.5267633817 sonhos to 362049765665406976
			[11:31:07.108] [Coroutine Executor Thread 8985/INFO]: Giving 176399.1995997999 sonhos to 364878835692601344
			[11:31:07.109] [Coroutine Executor Thread 8985/INFO]: Giving 210204.90245122564 sonhos to 366669327585247232
			[11:31:07.109] [Coroutine Executor Thread 8985/INFO]: Giving 11972.186093046525 sonhos to 367458465985331200
			[11:31:07.109] [Coroutine Executor Thread 8985/INFO]: Giving 31715.157578789396 sonhos to 368199408375824405
			[11:31:07.110] [Coroutine Executor Thread 8985/INFO]: Giving 14218.209104552277 sonhos to 378234719910756353
			[11:31:07.110] [Coroutine Executor Thread 8985/INFO]: Giving 159.96998499249622 sonhos to 379676605003595788
			[11:31:07.111] [Coroutine Executor Thread 8985/INFO]: Giving 39336.167983992265 sonhos to 389978811103576075
			[11:31:07.111] [Coroutine Executor Thread 8985/INFO]: Giving 97832.01600800401 sonhos to 393452897909735424
			[11:31:07.111] [Coroutine Executor Thread 8985/INFO]: Giving 22.010805402668552 sonhos to 395161328383295498
			[11:31:07.112] [Coroutine Executor Thread 8985/INFO]: Giving 98183.1415707854 sonhos to 395788326835322882
			[11:31:07.112] [Coroutine Executor Thread 8985/INFO]: Giving 82414.64432216108 sonhos to 397520736505561091
			[11:31:07.113] [Coroutine Executor Thread 8985/INFO]: Giving 37925.46273136569 sonhos to 400061545830154240
			[11:31:07.113] [Coroutine Executor Thread 8985/INFO]: Giving 741.6706353176261 sonhos to 400284422806568971
			[11:31:07.113] [Coroutine Executor Thread 8985/INFO]: Giving 65137.268967817254 sonhos to 401971891511754762
			[11:31:07.114] [Coroutine Executor Thread 8985/INFO]: Giving 8682.041020510256 sonhos to 403209233803509760
			[11:31:07.114] [Coroutine Executor Thread 8985/INFO]: Giving 675943.0711255729 sonhos to 403323275712790538
			[11:31:07.115] [Coroutine Executor Thread 8985/INFO]: Giving 21453.626813406707 sonhos to 409842245550866432
			[11:31:07.115] [Coroutine Executor Thread 8985/INFO]: Giving 109359.67983991996 sonhos to 416056545051279370
			[11:31:07.115] [Coroutine Executor Thread 8985/INFO]: Giving 50639.61980990496 sonhos to 418845951902482432
			[11:31:07.116] [Coroutine Executor Thread 8985/INFO]: Giving 3777.188594297149 sonhos to 421105650723061761
			[11:31:07.116] [Coroutine Executor Thread 8985/INFO]: Giving 35407.50375187594 sonhos to 422839753923362827
			[11:31:07.117] [Coroutine Executor Thread 8985/INFO]: Giving 1381.4907453726864 sonhos to 424279437178306577
			[11:31:07.117] [Coroutine Executor Thread 8985/INFO]: Giving 22974.78739369685 sonhos to 425044632557322241
			[11:31:07.117] [Coroutine Executor Thread 8985/INFO]: Giving 2019.2096048024014 sonhos to 430304850908151808
			[11:31:07.118] [Coroutine Executor Thread 8985/INFO]: Giving 795243.6216208112 sonhos to 442067449467240459
			[11:31:07.118] [Coroutine Executor Thread 8985/INFO]: Giving 30367.58379189595 sonhos to 443890997164769280
			[11:31:07.119] [Coroutine Executor Thread 8985/INFO]: Giving 32767.545672836415 sonhos to 445341440113901579
			[11:31:07.119] [Coroutine Executor Thread 8985/INFO]: Giving 422457.4287143572 sonhos to 445356264076345345
			[11:31:07.120] [Coroutine Executor Thread 8985/INFO]: Giving 436.7283641820771 sonhos to 445362208839565314
			[11:31:07.120] [Coroutine Executor Thread 8985/INFO]: Giving 1201.0005002501252 sonhos to 448861938496307210
			[11:31:07.121] [Coroutine Executor Thread 8985/INFO]: Giving 38953.79599799901 sonhos to 457218773498200084
			[11:31:07.121] [Coroutine Executor Thread 8985/INFO]: Giving 2095.1475737868936 sonhos to 457567828246265856
			[11:31:07.121] [Coroutine Executor Thread 8985/INFO]: Giving 65190.595297648826 sonhos to 458556978730631168
			[11:31:07.122] [Coroutine Executor Thread 8985/INFO]: Giving 11987.393696848425 sonhos to 465356035163553813
			[11:31:07.122] [Coroutine Executor Thread 8985/INFO]: Giving 115111.85592796399 sonhos to 467563374608384000
			[11:31:07.123] [Coroutine Executor Thread 8985/INFO]: Giving 636.6183091545773 sonhos to 469255603575980052
			[11:31:07.123] [Coroutine Executor Thread 8985/INFO]: Giving 830917.3583321662 sonhos to 471368845672579072
			[11:31:07.123] [Coroutine Executor Thread 8985/INFO]: Giving 4222.5112556278145 sonhos to 473306903087153156
			[11:31:07.124] [Coroutine Executor Thread 8985/INFO]: Giving 20380.590295147576 sonhos to 474589029078269952
			[11:31:07.124] [Coroutine Executor Thread 8985/INFO]: Giving 31022.31115557779 sonhos to 474648528187162645
			[11:31:07.125] [Coroutine Executor Thread 8985/INFO]: Giving 81100.85042521261 sonhos to 475446252994297856
			[11:31:07.125] [Coroutine Executor Thread 8985/INFO]: Giving 83268.6743371686 sonhos to 481981777347346442
			[11:31:07.126] [Coroutine Executor Thread 8985/INFO]: Giving 38951.57578789395 sonhos to 483275879733133313
			[11:31:07.126] [Coroutine Executor Thread 8985/INFO]: Giving 233289.2442506092 sonhos to 483723642027507712
			[11:31:07.126] [Coroutine Executor Thread 8985/INFO]: Giving 13127.762881440727 sonhos to 487950864145973248
			[11:31:07.127] [Coroutine Executor Thread 8985/INFO]: Giving 17530.86543271636 sonhos to 489572594555813888
			[11:31:07.127] [Coroutine Executor Thread 8985/INFO]: Giving 58321.76127101352 sonhos to 489959188798373888
			[11:31:07.128] [Coroutine Executor Thread 8985/INFO]: Giving 21285.942971485743 sonhos to 492876082870091778
			[11:31:07.128] [Coroutine Executor Thread 8985/INFO]: Giving 31441.320660330166 sonhos to 494724738019491840
			[11:31:07.128] [Coroutine Executor Thread 8985/INFO]: Giving 3817.308654327164 sonhos to 505705249592311818
			[11:31:07.129] [Coroutine Executor Thread 8985/INFO]: Giving 77460.53026513258 sonhos to 505794956560957441
			[11:31:07.129] [Coroutine Executor Thread 8985/INFO]: Giving 329128.86443221685 sonhos to 508152873461219328
			[11:31:07.130] [Coroutine Executor Thread 8985/INFO]: Giving 2482.5412706353177 sonhos to 508673053333651460
			[11:31:07.130] [Coroutine Executor Thread 8985/INFO]: Giving 11039.719860230107 sonhos to 512685192406761472
			[11:31:07.130] [Coroutine Executor Thread 8985/INFO]: Giving 46336.36818409205 sonhos to 516196559453421578
			[11:31:07.131] [Coroutine Executor Thread 8985/INFO]: Giving 59.92950710238309 sonhos to 519007274635886610
			[11:31:07.131] [Coroutine Executor Thread 8985/INFO]: Giving 45055.527763881946 sonhos to 520335048680013828
			[11:31:07.132] [Coroutine Executor Thread 8985/INFO]: Giving 935464.6323161582 sonhos to 530596271774498847
			[11:31:07.132] [Coroutine Executor Thread 8985/INFO]: Giving 41630.21610805399 sonhos to 537702663849115658
			[11:31:07.133] [Coroutine Executor Thread 8985/INFO]: Giving 7886.343171585793 sonhos to 538509858404564993
			[11:31:07.133] [Coroutine Executor Thread 8985/INFO]: Giving 3569.5847923961983 sonhos to 540976244221673473
			[11:31:07.133] [Coroutine Executor Thread 8985/INFO]: Giving 236235.51775887946 sonhos to 541056348935290881
			[11:31:07.134] [Coroutine Executor Thread 8985/INFO]: Giving 288.14407203601803 sonhos to 544226341340577853
			[11:31:07.134] [Coroutine Executor Thread 8985/INFO]: Giving 59456.92846423212 sonhos to 545822078662737923
			[11:31:07.135] [Coroutine Executor Thread 8985/INFO]: Giving 34524.06251524636 sonhos to 548967651603513345
			[11:31:07.135] [Coroutine Executor Thread 8985/INFO]: Giving 11760.080050025013 sonhos to 553932775074430976
			[11:31:07.135] [Coroutine Executor Thread 8985/INFO]: Giving 350985.49274637323 sonhos to 554681800291516475
			[11:31:07.136] [Coroutine Executor Thread 8985/INFO]: Giving 558.1790895447724 sonhos to 561264957921034240
			[11:31:07.136] [Coroutine Executor Thread 8985/INFO]: Giving 1973.3866933466734 sonhos to 561525046385049605
			[11:31:07.137] [Coroutine Executor Thread 8985/INFO]: Giving 21327.51074537269 sonhos to 571061893469306899
			[11:31:07.137] [Coroutine Executor Thread 8985/INFO]: Giving 23859.82991495748 sonhos to 571457337190252545
			[11:31:07.138] [Coroutine Executor Thread 8985/INFO]: Giving 95.24762381190597 sonhos to 582599554378235904
			[11:31:07.138] [Coroutine Executor Thread 8985/INFO]: Giving 3826.9133566783407 sonhos to 590644186949222401
			[11:31:07.138] [Coroutine Executor Thread 8985/INFO]: Giving 7135.967983991996 sonhos to 600117321931161602
			[11:31:07.139] [Coroutine Executor Thread 8985/INFO]: Giving 225.1125562781391 sonhos to 603953652662796306
			[11:31:07.139] [Coroutine Executor Thread 8985/INFO]: Giving 37499.84992496249 sonhos to 609858068842545161
			[11:31:07.140] [Coroutine Executor Thread 8985/INFO]: Giving 30178.0499249625 sonhos to 614824852485832715
			[11:31:07.140] [Coroutine Executor Thread 8985/INFO]: Giving 13717.258629314658 sonhos to 617417345694171176
			[11:31:07.140] [Coroutine Executor Thread 8985/INFO]: Giving 118894.94747373687 sonhos to 624620883373195265
			[11:31:07.141] [Coroutine Executor Thread 8985/INFO]: Giving 5243.4217108554285 sonhos to 640905391437381642
			[11:31:07.141] [Coroutine Executor Thread 8985/INFO]: Giving 15624.112056028016 sonhos to 642197930140237832
			[11:31:07.142] [Coroutine Executor Thread 8985/INFO]: Giving 12211.652826413208 sonhos to 645812175184855040
			[11:31:07.142] [Coroutine Executor Thread 8985/INFO]: Giving 435502.5512514613 sonhos to 645933386615095326
			[11:31:07.143] [Coroutine Executor Thread 8985/INFO]: Giving 4203.901950975488 sonhos to 648195220512702492
			[11:31:07.143] [Coroutine Executor Thread 8985/INFO]: Giving 64.93246623311656 sonhos to 653032908545589287
			[11:31:07.143] [Coroutine Executor Thread 8985/INFO]: Giving 504.8524262131066 sonhos to 655090949902303257
			[11:31:07.144] [Coroutine Executor Thread 8985/INFO]: Giving 32.51625812906453 sonhos to 656192425454338049
			[11:31:07.144] [Coroutine Executor Thread 8985/INFO]: Giving 283025.91305652825 sonhos to 658842971168309268
			[11:32:07.285] [Coroutine Executor Thread 8989/INFO]: Giving 15920.513883755286 sonhos to 107279862934331392
			[11:32:07.286] [Coroutine Executor Thread 8989/INFO]: Giving 104112.60848032821 sonhos to 123170274651668480
			[11:32:07.286] [Coroutine Executor Thread 8989/INFO]: Giving 23181.504409032932 sonhos to 162594636831916033
			[11:32:07.287] [Coroutine Executor Thread 8989/INFO]: Giving 19874.750732044362 sonhos to 171699209936961536
			[11:32:07.287] [Coroutine Executor Thread 8989/INFO]: Giving 131239.9851751789 sonhos to 174950526700617729
			[11:32:07.288] [Coroutine Executor Thread 8989/INFO]: Giving 3085.3128915633406 sonhos to 196744131706355712
			[11:32:07.288] [Coroutine Executor Thread 8989/INFO]: Giving 35789.87167420629 sonhos to 207534284175572992
			[11:32:07.289] [Coroutine Executor Thread 8989/INFO]: Giving 971756.8972079838 sonhos to 221101554961219585
			[11:32:07.289] [Coroutine Executor Thread 8989/INFO]: Giving 4260.142105069486 sonhos to 230088006428524544
			[11:32:07.289] [Coroutine Executor Thread 8989/INFO]: Giving 58415.68297905832 sonhos to 239725782912598016
			[11:32:07.290] [Coroutine Executor Thread 8989/INFO]: Giving 31762.995204455656 sonhos to 246645318144229377
			[11:32:07.290] [Coroutine Executor Thread 8989/INFO]: Giving 62026.0922857627 sonhos to 249508932861558785
			[11:32:07.291] [Coroutine Executor Thread 8989/INFO]: Giving 103629.7751176739 sonhos to 269903670143877131
			[11:32:07.291] [Coroutine Executor Thread 8989/INFO]: Giving 77601.45790754307 sonhos to 269964181661417482
			[11:32:07.292] [Coroutine Executor Thread 8989/INFO]: Giving 69662.55261197384 sonhos to 272031079001620490
			[11:32:07.292] [Coroutine Executor Thread 8989/INFO]: Giving 1064.3905744768333 sonhos to 277091263306792960
			[11:32:07.293] [Coroutine Executor Thread 8989/INFO]: Giving 9111.870092124602 sonhos to 279657232042557440
			[11:32:07.293] [Coroutine Executor Thread 8989/INFO]: Giving 13219.750745808124 sonhos to 282895755688280065
			[11:32:07.294] [Coroutine Executor Thread 8989/INFO]: Giving 7462.235294075553 sonhos to 286493556170948618
			[11:32:07.294] [Coroutine Executor Thread 8989/INFO]: Giving 5375.521170553326 sonhos to 293923362323824653
			[11:32:07.295] [Coroutine Executor Thread 8989/INFO]: Giving 140277.7848654192 sonhos to 295318635910529024
			[11:32:07.295] [Coroutine Executor Thread 8989/INFO]: Giving 19735.744910974747 sonhos to 311296198931644416
			[11:32:07.295] [Coroutine Executor Thread 8989/INFO]: Giving 9531.924755738699 sonhos to 317796741024055307
			[11:32:07.296] [Coroutine Executor Thread 8989/INFO]: Giving 47432.682274103536 sonhos to 320992398295564300
			[11:32:07.296] [Coroutine Executor Thread 8989/INFO]: Giving 52320.66843827117 sonhos to 326123612153053184
			[11:32:07.297] [Coroutine Executor Thread 8989/INFO]: Giving 21479.976306312237 sonhos to 328943660362366978
			[11:32:07.297] [Coroutine Executor Thread 8989/INFO]: Giving 23630.1091015743 sonhos to 328973123351216130
			[11:32:07.298] [Coroutine Executor Thread 8989/INFO]: Giving 11472.053560046626 sonhos to 332918506930831362
			[11:32:07.298] [Coroutine Executor Thread 8989/INFO]: Giving 60335.85175287496 sonhos to 335603023000633355
			[11:32:07.299] [Coroutine Executor Thread 8989/INFO]: Giving 8387.124777996803 sonhos to 349380609660944395
			[11:32:07.299] [Coroutine Executor Thread 8989/INFO]: Giving 73240.98947924288 sonhos to 351760430991147010
			[11:32:07.300] [Coroutine Executor Thread 8989/INFO]: Giving 2658.601682977057 sonhos to 352179628842156033
			[11:32:07.300] [Coroutine Executor Thread 8989/INFO]: Giving 165779.6849400188 sonhos to 361977144445763585
			[11:32:07.300] [Coroutine Executor Thread 8989/INFO]: Giving 466260.082717697 sonhos to 362049765665406976
			[11:32:07.301] [Coroutine Executor Thread 8989/INFO]: Giving 194048.144031996 sonhos to 364878835692601344
			[11:32:07.301] [Coroutine Executor Thread 8989/INFO]: Giving 231235.90819922218 sonhos to 366669327585247232
			[11:32:07.302] [Coroutine Executor Thread 8989/INFO]: Giving 13170.003611110209 sonhos to 367458465985331200
			[11:32:07.302] [Coroutine Executor Thread 8989/INFO]: Giving 34888.25988782285 sonhos to 368199408375824405
			[11:32:07.303] [Coroutine Executor Thread 8989/INFO]: Giving 15640.741281095778 sonhos to 378234719910756353
			[11:32:07.303] [Coroutine Executor Thread 8989/INFO]: Giving 175.9749859922457 sonhos to 379676605003595788
			[11:32:07.304] [Coroutine Executor Thread 8989/INFO]: Giving 43271.752574686834 sonhos to 389978811103576075
			[11:32:07.304] [Coroutine Executor Thread 8989/INFO]: Giving 107620.11165662872 sonhos to 393452897909735424
			[11:32:07.305] [Coroutine Executor Thread 8989/INFO]: Giving 24.21298703375095 sonhos to 395161328383295498
			[11:32:07.305] [Coroutine Executor Thread 8989/INFO]: Giving 108006.36734074891 sonhos to 395788326835322882
			[11:32:07.305] [Coroutine Executor Thread 8989/INFO]: Giving 90660.23154799012 sonhos to 397520736505561091
			[11:32:07.306] [Coroutine Executor Thread 8989/INFO]: Giving 41719.906226249695 sonhos to 400061545830154240
			[11:32:07.306] [Coroutine Executor Thread 8989/INFO]: Giving 815.874800932196 sonhos to 400284422806568971
			[11:32:07.307] [Coroutine Executor Thread 8989/INFO]: Giving 71654.25435729373 sonhos to 401971891511754762
			[11:32:07.307] [Coroutine Executor Thread 8989/INFO]: Giving 9550.679441771912 sonhos to 403209233803509760
			[11:32:07.308] [Coroutine Executor Thread 8989/INFO]: Giving 743571.1922987166 sonhos to 403323275712790538
			[11:32:07.308] [Coroutine Executor Thread 8989/INFO]: Giving 23600.06271269702 sonhos to 409842245550866432
			[11:32:07.309] [Coroutine Executor Thread 8989/INFO]: Giving 120301.11854326364 sonhos to 416056545051279370
			[11:32:07.309] [Coroutine Executor Thread 8989/INFO]: Giving 55706.11503850976 sonhos to 418845951902482432
			[11:32:07.310] [Coroutine Executor Thread 8989/INFO]: Giving 4155.096407633532 sonhos to 421105650723061761
			[11:32:07.310] [Coroutine Executor Thread 8989/INFO]: Giving 38950.02538788154 sonhos to 422839753923362827
			[11:32:07.310] [Coroutine Executor Thread 8989/INFO]: Giving 1519.7089290017695 sonhos to 424279437178306577
			[11:32:07.311] [Coroutine Executor Thread 8989/INFO]: Giving 25273.415447093234 sonhos to 425044632557322241
			[11:32:07.311] [Coroutine Executor Thread 8989/INFO]: Giving 2221.2315762683747 sonhos to 430304850908151808
			[11:32:07.312] [Coroutine Executor Thread 8989/INFO]: Giving 874807.7658550094 sonhos to 442067449467240459
			[11:32:07.312] [Coroutine Executor Thread 8989/INFO]: Giving 33405.86130984452 sonhos to 443890997164769280
			[11:32:07.313] [Coroutine Executor Thread 8989/INFO]: Giving 36045.93943700214 sonhos to 445341440113901579
			[11:32:07.313] [Coroutine Executor Thread 8989/INFO]: Giving 464724.30502394773 sonhos to 445356264076345345
			[11:32:07.314] [Coroutine Executor Thread 8989/INFO]: Giving 480.4230479421648 sonhos to 445362208839565314
			[11:32:07.314] [Coroutine Executor Thread 8989/INFO]: Giving 1321.1606303401827 sonhos to 448861938496307210
			[11:32:07.315] [Coroutine Executor Thread 8989/INFO]: Giving 42851.12426193088 sonhos to 457218773498200084
			[11:32:07.315] [Coroutine Executor Thread 8989/INFO]: Giving 2304.767140949164 sonhos to 457567828246265856
			[11:32:07.316] [Coroutine Executor Thread 8989/INFO]: Giving 71712.91598775877 sonhos to 458556978730631168
			[11:32:07.316] [Coroutine Executor Thread 8989/INFO]: Giving 13186.732736052869 sonhos to 465356035163553813
			[11:32:07.317] [Coroutine Executor Thread 8989/INFO]: Giving 126628.7999927928 sonhos to 467563374608384000
			[11:32:07.317] [Coroutine Executor Thread 8989/INFO]: Giving 700.3119869089122 sonhos to 469255603575980052
			[11:32:07.317] [Coroutine Executor Thread 8989/INFO]: Giving 914050.660816625 sonhos to 471368845672579072
			[11:32:07.318] [Coroutine Executor Thread 8989/INFO]: Giving 4644.973612368966 sonhos to 473306903087153156
			[11:32:07.318] [Coroutine Executor Thread 8989/INFO]: Giving 22419.668863946736 sonhos to 474589029078269952
			[11:32:07.319] [Coroutine Executor Thread 8989/INFO]: Giving 34126.0941626391 sonhos to 474648528187162645
			[11:32:07.319] [Coroutine Executor Thread 8989/INFO]: Giving 89214.99253879067 sonhos to 475446252994297856
			[11:32:07.320] [Coroutine Executor Thread 8989/INFO]: Giving 91599.70728736056 sonhos to 481981777347346442
			[11:32:07.320] [Coroutine Executor Thread 8989/INFO]: Giving 42848.68191974927 sonhos to 483275879733133313
			[11:32:07.321] [Coroutine Executor Thread 8989/INFO]: Giving 256629.83897303132 sonhos to 483723642027507712
			[11:32:07.321] [Coroutine Executor Thread 8989/INFO]: Giving 14441.195886087122 sonhos to 487950864145973248
			[11:32:07.321] [Coroutine Executor Thread 8989/INFO]: Giving 19284.828957750513 sonhos to 489572594555813888
			[11:32:07.322] [Coroutine Executor Thread 8989/INFO]: Giving 64156.85494495185 sonhos to 489959188798373888
			[11:32:07.322] [Coroutine Executor Thread 8989/INFO]: Giving 23415.602098197676 sonhos to 492876082870091778
			[11:32:07.323] [Coroutine Executor Thread 8989/INFO]: Giving 34587.02557882243 sonhos to 494724738019491840
			[11:32:07.323] [Coroutine Executor Thread 8989/INFO]: Giving 4199.2304806730535 sonhos to 505705249592311818
			[11:32:07.324] [Coroutine Executor Thread 8989/INFO]: Giving 85210.45825564108 sonhos to 505794956560957441
			[11:32:07.324] [Coroutine Executor Thread 8989/INFO]: Giving 362058.21555099793 sonhos to 508152873461219328
			[11:32:07.325] [Coroutine Executor Thread 8989/INFO]: Giving 2730.9195868569604 sonhos to 508673053333651460
			[11:32:07.325] [Coroutine Executor Thread 8989/INFO]: Giving 12144.24410837719 sonhos to 512685192406761472
			[11:32:07.325] [Coroutine Executor Thread 8989/INFO]: Giving 50972.32297989916 sonhos to 516196559453421578
			[11:32:07.326] [Coroutine Executor Thread 8989/INFO]: Giving 65.9254557869637 sonhos to 519007274635886610
			[11:32:07.326] [Coroutine Executor Thread 8989/INFO]: Giving 49563.33444361 sonhos to 520335048680013828
			[11:32:07.327] [Coroutine Executor Thread 8989/INFO]: Giving 1029057.8921777046 sonhos to 530596271774498847
			[11:32:07.327] [Coroutine Executor Thread 8989/INFO]: Giving 45795.32027094083 sonhos to 537702663849115658
			[11:32:07.328] [Coroutine Executor Thread 8989/INFO]: Giving 8675.372003160159 sonhos to 538509858404564993
			[11:32:07.328] [Coroutine Executor Thread 8989/INFO]: Giving 3926.7218401597 sonhos to 540976244221673473
			[11:32:07.329] [Coroutine Executor Thread 8989/INFO]: Giving 259870.8872194977 sonhos to 541056348935290881
			[11:32:07.329] [Coroutine Executor Thread 8989/INFO]: Giving 316.972893650427 sonhos to 544226341340577853
			[11:32:07.330] [Coroutine Executor Thread 8989/INFO]: Giving 65405.59564424534 sonhos to 545822078662737923
			[11:32:07.330] [Coroutine Executor Thread 8989/INFO]: Giving 37978.19583343009 sonhos to 548967651603513345
			[11:32:07.331] [Coroutine Executor Thread 8989/INFO]: Giving 12936.676353179093 sonhos to 553932775074430976
			[11:32:07.331] [Coroutine Executor Thread 8989/INFO]: Giving 386101.6000746747 sonhos to 554681800291516475
			[11:32:07.332] [Coroutine Executor Thread 8989/INFO]: Giving 614.0249214151848 sonhos to 561264957921034240
			[11:32:07.332] [Coroutine Executor Thread 8989/INFO]: Giving 2170.8240813753555 sonhos to 561525046385049605
			[11:32:07.333] [Coroutine Executor Thread 8989/INFO]: Giving 23461.32872890172 sonhos to 571061893469306899
			[11:32:07.333] [Coroutine Executor Thread 8989/INFO]: Giving 26247.00649474312 sonhos to 571457337190252545
			[11:32:07.334] [Coroutine Executor Thread 8989/INFO]: Giving 104.77715095666893 sonhos to 582599554378235904
			[11:32:07.334] [Coroutine Executor Thread 8989/INFO]: Giving 4209.796133734703 sonhos to 590644186949222401
			[11:32:07.335] [Coroutine Executor Thread 8989/INFO]: Giving 7849.92175927884 sonhos to 600117321931161602
			[11:32:07.335] [Coroutine Executor Thread 8989/INFO]: Giving 247.63507316439612 sonhos to 603953652662796306
			[11:32:07.336] [Coroutine Executor Thread 8989/INFO]: Giving 41251.710847920214 sonhos to 609858068842545161
			[11:32:07.336] [Coroutine Executor Thread 8989/INFO]: Giving 33197.36457478366 sonhos to 614824852485832715
			[11:32:07.337] [Coroutine Executor Thread 8989/INFO]: Giving 15089.670698280608 sonhos to 617417345694171176
			[11:32:07.337] [Coroutine Executor Thread 8989/INFO]: Giving 130790.38994234487 sonhos to 624620883373195265
			[11:32:07.338] [Coroutine Executor Thread 8989/INFO]: Giving 5768.026184177632 sonhos to 640905391437381642
			[11:32:07.339] [Coroutine Executor Thread 8989/INFO]: Giving 17187.304858031817 sonhos to 642197930140237832
			[11:32:07.339] [Coroutine Executor Thread 8989/INFO]: Giving 13433.428997139894 sonhos to 645812175184855040
			[11:32:07.339] [Coroutine Executor Thread 8989/INFO]: Giving 479074.5923971803 sonhos to 645933386615095326
			[11:32:07.340] [Coroutine Executor Thread 8989/INFO]: Giving 4624.502446320709 sonhos to 648195220512702492
			[11:32:07.340] [Coroutine Executor Thread 8989/INFO]: Giving 71.42896110386359 sonhos to 653032908545589287
			[11:32:07.341] [Coroutine Executor Thread 8989/INFO]: Giving 555.3629240833524 sonhos to 655090949902303257
			[11:32:07.341] [Coroutine Executor Thread 8989/INFO]: Giving 35.76951056819055 sonhos to 656192425454338049
			[11:32:07.342] [Coroutine Executor Thread 8989/INFO]: Giving 311342.66273702134 sonhos to 658842971168309268
		""".trimIndent().lines()

		val pattern = Pattern.compile("Giving ([0-9.]+) sonhos to ([0-9]+)")

		for (line in lines) {
			val matcher = pattern.matcher(line)
			if (matcher.find()) {
				println("Corrigindo o dinheiro de ${matcher.group(2)}")

				transaction(Databases.loritta) {
					Profiles.update({ Profiles.id eq matcher.group(2).toLong() }) {
						with(SqlExpressionBuilder) {
							it.update(money, money - matcher.group(1).toLong())
						}
					}
				}
			}
		}
	}
}
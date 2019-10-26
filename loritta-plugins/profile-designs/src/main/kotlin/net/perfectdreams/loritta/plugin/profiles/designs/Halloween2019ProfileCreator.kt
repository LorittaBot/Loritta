package net.perfectdreams.loritta.plugin.profiles.designs

import com.google.common.cache.CacheBuilder
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.GuildProfile
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.profile.ProfileCreator
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.getOrNull
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileInputStream
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

class Halloween2019ProfileCreator : ProfileCreator {
	override fun create(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: MongoServerConfig, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): BufferedImage {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun createGif(sender: User, user: User, userProfile: Profile, guild: Guild, serverConfig: MongoServerConfig, badges: List<BufferedImage>, locale: LegacyBaseLocale, background: BufferedImage, aboutMe: String, member: Member?): List<BufferedImage> {
		val list = mutableListOf<BufferedImage>()

		val whitneySemiBold = FileInputStream(File(Loritta.ASSETS + "whitney-semibold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}
		val whitneyBold = FileInputStream(File(Loritta.ASSETS + "whitney-bold.ttf")).use {
			Font.createFont(Font.TRUETYPE_FONT, it)
		}

		val whitneyMedium22 = whitneySemiBold.deriveFont(22f)
		val whitneyBold16 = whitneyBold.deriveFont(16f)
		val whitneyMedium16 = whitneySemiBold.deriveFont(16f)
		val whitneyBold12 = whitneyBold.deriveFont(12f)
		val oswaldRegular50 = Constants.OSWALD_REGULAR
				.deriveFont(50F)
		val oswaldRegular42 = Constants.OSWALD_REGULAR
				.deriveFont(42F)

		val avatar = LorittaUtils.downloadImage(user.effectiveAvatarUrl)!!.getScaledInstance(152, 152, BufferedImage.SCALE_SMOOTH)
		val marrySection = ImageIO.read(File(Loritta.ASSETS, "profile/halloween_2019/marry.png"))

		val marriage = transaction(Databases.loritta) { userProfile.marriage }

		val marriedWithId = if (marriage?.user1 == user.idLong) {
			marriage.user2
		} else {
			marriage?.user1
		}

		val marriedWith = if (marriedWithId != null) { runBlocking { lorittaShards.retrieveUserById(marriedWithId) } } else { null }

		val reputations = transaction(Databases.loritta) {
			Reputations.select { Reputations.receivedById eq user.idLong }.count()
		}

		val globalPosition = transaction(Databases.loritta) {
			Profiles.select { Profiles.xp greaterEq userProfile.xp }.count()
		}

		val localProfile = transaction(Databases.loritta) {
			GuildProfile.find { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.userId eq user.idLong) }.firstOrNull()
		}

		val localPosition = if (localProfile != null) {
			transaction(Databases.loritta) {
				GuildProfiles.select { (GuildProfiles.guildId eq guild.idLong) and (GuildProfiles.xp greaterEq localProfile.xp) }.count()
			}
		} else { null }

		val xpLocal = localProfile?.xp

		val globalEconomyPosition = transaction(Databases.loritta) {
			Profiles.select { Profiles.money greaterEq userProfile.money }.count()
		}

		val resizedBadges = badges.map { it.getScaledInstance(35, 35, BufferedImage.SCALE_SMOOTH).toBufferedImage() }

		for (i in 0..29) {
			val result = measureTimeMillis {
				val profileWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/halloween_2019/frames/halloween_2019_${i.toString().padStart(6, '0')}.png"))

				val base = BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB) // Base
				val graphics = base.graphics.enableFontAntiAliasing()

				graphics.drawImage(background.getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH), 0, 0, null)

				graphics.color = Color.BLACK
				graphics.drawImage(profileWrapper, 0, 0, null)
				drawAvatar(avatar, graphics)

				graphics.font = oswaldRegular50
				graphics.drawText(user.name, 162, 461) // Nome do usuário
				graphics.font = oswaldRegular42

				drawReputations(user, graphics, reputations)

				drawBadges(resizedBadges, graphics)

				graphics.font = whitneyBold16
				val biggestStrWidth = drawUserInfo(user, userProfile, guild, graphics, globalPosition, localPosition, xpLocal, globalEconomyPosition)

				graphics.font = whitneyMedium22

				drawTextWrapSpaces(aboutMe, 162, 484, 773 - biggestStrWidth - 4, 600, graphics.fontMetrics, graphics)

				if (marriage != null) {
					graphics.drawImage(marrySection, 0, 0, null)

					if (marriedWith != null) {
						graphics.color = Color.WHITE
						graphics.font = whitneyBold12
						ImageUtils.drawCenteredString(graphics, locale.toNewLocale()["profile.marriedWith"], Rectangle(635, 350, 165, 14), whitneyBold12)
						graphics.font = whitneyMedium16
						ImageUtils.drawCenteredString(graphics, marriedWith.name + "#" + marriedWith.discriminator, Rectangle(635, 350 + 16, 165, 18), whitneyMedium16)
						graphics.font = whitneyBold12
						ImageUtils.drawCenteredString(graphics, DateUtils.formatDateDiff(marriage.marriedSince, System.currentTimeMillis(), locale), Rectangle(635, 350 + 16 + 18, 165, 14), whitneyBold12)
					}
				}

				list.add(base.getScaledInstance(400, 300, BufferedImage.SCALE_SMOOTH).toBufferedImage())
			}

			println("Result $i: ${result}ms")
		}

		return list
	}

	fun drawAvatar(avatar: Image, graphics: Graphics) {
		graphics.drawImage(
				avatar.toBufferedImage()
						.makeRoundedCorners(999),
				3,
				406,
				null
		)
	}

	fun drawBadges(badges: List<BufferedImage>, graphics: Graphics) {
		var x = 2
		for (badge in badges) {
			graphics.drawImage(badge, x, 564, null)
			x += 37
		}
	}

	fun drawReputations(user: User, graphics: Graphics, reputations: Int) {
		val font = graphics.font

		ImageUtils.drawCenteredString(graphics, "$reputations reps", Rectangle(634, 404, 166, 52), font)
	}

	fun drawUserInfo(user: User, userProfile: Profile, guild: Guild, graphics: Graphics, globalPosition: Int, localPosition: Int?, xpLocal: Long?, globalEconomyPosition: Int): Int {
		val userInfo = mutableListOf<String>()
		userInfo.add("Global")

		userInfo.add("#$globalPosition / ${userProfile.xp} XP")

		// Iremos remover os emojis do nome da guild, já que ele não calcula direito no stringWidth
		userInfo.add(guild.name.replace(Constants.EMOJI_PATTERN.toRegex(), ""))
		if (xpLocal != null) {
			userInfo.add("#$localPosition / $xpLocal XP")
		} else {
			userInfo.add("???")
		}

		userInfo.add("Sonhos")
		userInfo.add("#$globalEconomyPosition / ${"%.2f".format(userProfile.money)}")

		val biggestStrWidth = graphics.fontMetrics.stringWidth(userInfo.maxBy { graphics.fontMetrics.stringWidth(it) }!!)

		var y = 475
		for (line in userInfo) {
			graphics.drawText(line, 773 - biggestStrWidth - 2, y)
			y += 16
		}

		return biggestStrWidth
	}

	val emotes = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build<String, Optional<BufferedImage>>().asMap()

	fun getTwitterEmoji(text: String, index: Int): BufferedImage? {
		try {
			val imageUrl = "https://twemoji.maxcdn.com/2/72x72/" + LorittaUtils.toUnicode(text.codePointAt(index - 1)).substring(2) + ".png"
			try {
				if (emotes.containsKey(imageUrl))
					return emotes[imageUrl]?.getOrNull()

				val emoteImage = LorittaUtils.downloadImage(imageUrl)
				emotes[imageUrl] = Optional.ofNullable(emoteImage)
				return emoteImage
			} catch (e: Exception) {
				// Outro try ... catch, esse é usado para evitar baixar imagens inexistentes, mas que o codepoint existe
				emotes[imageUrl] = Optional.empty()
				return null
			}
		} catch (e: Exception) {
			return null
		}
	}

	fun drawTextWrapSpaces(text: String, startX: Int, startY: Int, endX: Int, endY: Int, fontMetrics: FontMetrics, graphics: Graphics): Int {
		val lineHeight = fontMetrics.height // Aqui é a altura da nossa fonte

		var currentX = startX // X atual
		var currentY = startY // Y atual

		val split = text.split("((?<= )|(?= ))".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // Nós precisamos deixar os espaços entre os splits!
		for (str in split) {
			var width = fontMetrics.stringWidth(str) // Width do texto que nós queremos colocar
			if (currentX + width > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
				currentX = startX // Nós iremos fazer wrapping do texto
				currentY += lineHeight
			}
			var idx = 0
			for (c in str.toCharArray()) { // E agora nós iremos printar todos os chars
				idx++
				if (c == '\n') {
					currentX = startX // Nós iremos fazer wrapping do texto
					currentY += lineHeight
					continue
				}
				width = fontMetrics.charWidth(c)
				if (!graphics.font.canDisplay(c)) {
					// Talvez seja um emoji!
					val emoteImage = getTwitterEmoji(str, idx)
					if (emoteImage != null) {
						graphics.drawImage(emoteImage.getScaledInstance(width, width, BufferedImage.SCALE_SMOOTH), currentX, currentY - width, null)
						currentX += width
					}

					continue
				}
				graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
				currentX += width // E adicione o width no nosso currentX
			}
		}
		return currentY
	}
}
package net.perfectdreams.loritta.plugin.loriguildstuff.commands

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.int
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import com.mrpowergamerbr.loritta.utils.toBufferedImage
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.NoCopyByteArrayOutputStream
import net.perfectdreams.loritta.api.utils.Rarity
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.discordCommand
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.tables.Sets
import net.perfectdreams.loritta.utils.config.FanArtArtist
import net.perfectdreams.loritta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO

object AddBackgroundCommand {
	fun create(loritta: LorittaDiscord) = discordCommand(loritta, listOf("addbackground"), CommandCategory.MAGIC) {
		this.hideInHelp = true
		this.commandCheckFilter { lorittaMessageEvent, _, _, _, _ ->
			lorittaMessageEvent.guild?.idLong == 297732013006389252L && lorittaMessageEvent.member!!.roles.any { it.idLong == 351473717194522647L }
		}

		executesDiscord {
			val split = this.args.joinToString(" ")
					.split("|")
					.map {
						it.trim()
					}

			val link = split[0]

			val downloadedImage = LorittaUtils.downloadFile(
					link,
					15_000
			)!!

			val allBytes = downloadedImage.readAllBytes()

			val asImage = ImageIO.read(allBytes.inputStream())

			val title = split[1]
			val description = split[2]
			val internalName = split[3]
			val imageFile = split[4]
			val rarity = Rarity.valueOf(split[5])
			// val createdBy = split[3]
			val crop = split.getOrNull(6)?.let { if (it != "null") JsonParser.parseString(it) else null }
			val collection = split.getOrNull(7)?.let { if (it != "null") it else null }
			val fanArtistName = split.getOrNull(8)?.let { if (it != "null") it else null }

			val fanArtist = if (fanArtistName != null) {
				val fanArtist = loritta.fanArtArtists.firstOrNull { it.id == fanArtistName }
						?: fail("Artista não existe!")

				fanArtist
			} else null

			val collectionId = if (collection != null) {
				loritta.newSuspendedTransaction {
					Sets.select { Sets.internalName eq collection }
							.firstOrNull()
				}
						?: fail("Coleção não existe!")
			} else null

			// val set = split[4]
			val availableToBuyViaDreams = true
			val availableToBuyViaMoney = false
			val addedAt = System.currentTimeMillis()

			val finalCroppedImage =
					(
							if (crop != null) {
								// Perfil possível um crop diferenciado
								val offsetX = crop["offsetX"].int
								val offsetY = crop["offsetY"].int
								val width = crop["width"].int
								val height = crop["height"].int

								// Se o background possui um width/height diferenciado, mas é idêntico ao tamanho correto do perfil... apenas faça nada
								if (!(offsetX == 0 && offsetY == 0 && width == asImage.width && height == asImage.height)) {
									// Mas... e se for diferente? sad_cat
									asImage.getSubimage(offsetX, offsetY, width, height).toBufferedImage()
								} else asImage
							} else asImage
							).getScaledInstance(800, 600, BufferedImage.SCALE_SMOOTH)
							.toBufferedImage()
			val nostalgiaWrapper = ImageIO.read(File(Loritta.ASSETS, "profile/nostalgia/profile_wrapper.png"))

			finalCroppedImage.createGraphics().drawImage(nostalgiaWrapper, 0, 0, null)

			val replies = listOf(
					LorittaReply(
							"Título: `$title`",
							mentionUser = false
					),
					LorittaReply(
							"Descrição: `$description`",
							mentionUser = false
					),
					LorittaReply(
							"Nome interno: `$internalName`",
							mentionUser = false
					),
					LorittaReply(
							"Arquivo da Imagem: `$imageFile`",
							mentionUser = false
					),
					LorittaReply(
							"Raridade: `${rarity}` (${rarity.getBackgroundPrice()} sonhos)",
							mentionUser = false
					),
					LorittaReply(
							"Crop: `$crop`",
							mentionUser = false
					),
					LorittaReply(
							"Artista: `${fanArtist?.id}`",
							mentionUser = false
					),
					LorittaReply(
							"Preview:",
							mentionUser = false
					)
			).joinToString("\n") { it.build(this) }

			sendImage(
					JVMImage(finalCroppedImage),
					"cropped.png",
					replies
			).toJDA()
					.onReactionAddByAuthor(this) {
						if (it.reactionEmote.name == "lori_pat") {
							// yay! try to add it
							File("/home/loritta/frontend/static/assets/img/profiles/backgrounds/$imageFile")
									.writeBytes(allBytes)

							loritta.newSuspendedTransaction {
								Backgrounds.insert {
									it[Backgrounds.enabled] = true
									it[Backgrounds.availableToBuyViaDreams] = true
									it[Backgrounds.availableToBuyViaMoney] = false
									it[Backgrounds.addedAt] = System.currentTimeMillis()
									if (fanArtist != null)
										it[Backgrounds.createdBy] = arrayOf(fanArtist.id)
									else
										it[Backgrounds.createdBy] = arrayOf()

									if (collectionId != null)
										it[Backgrounds.set] = collectionId[Sets.id]

									it[Backgrounds.internalName] = internalName
									it[Backgrounds.imageFile] = imageFile
									it[Backgrounds.rarity] = rarity
									it[Backgrounds.crop] = crop
								}
							}

							val original = File("/home/loritta/locales/default/items.yml")
									.readLines()
									.toMutableList()

							original.add("  $internalName:")
							original.add("    title: \"$title\"")
							original.add("    description: \"$description\"")

							File("/home/loritta/locales/default/items.yml")
									.writeText(
											original
													.joinToString("\n")
									)

							reply(
									LorittaReply(
											"Background adicionado!",
											mentionUser = false
									)
							)

							lorittaShards.queryAllLorittaClusters("/api/v1/loritta/action/locales")

							val channel = guild.getTextChannelById(736275294486528101L)!!

							val output = NoCopyByteArrayOutputStream()

							ImageIO.write(finalCroppedImage, "png", output)

							val inputStream = ByteArrayInputStream(output.toByteArray(), 0, output.size())

							val lines = mutableListOf(
									"<@&700368699701592185> <:lori_yay_ping:640141673531441153>",
									"",
									"**Novo background adicionado!**",
									"",
									"**Nome:** $title",
									"**Descrição:** $description",
									"**Raridade:** `${rarity}` (${rarity.getBackgroundPrice()} sonhos)"
							)

							if (fanArtist != null) {
								val discordId = fanArtist.socialNetworks?.asSequence()?.filterIsInstance<FanArtArtist.SocialNetwork.DiscordSocialNetwork>()
										?.firstOrNull()?.let { discordInfo ->
											discordInfo.id.toLong()
										}

								if (discordId != null) {
									lines += "**Artista:** <@$discordId>"
								} else {
									lines += "**Artista:** `${fanArtist.id}`"
								}
							}

							if (collection != null) {
								lines += "**Coleção:** `$collection`"
							}

							lines += "**Adicionado por:** ${this.user.asMention}"
							channel.sendMessage(
									lines.joinToString("\n")
							)
									.addFile(inputStream, "preview.png")
									.await()
						}
					}.addReaction("a:lori_pat:706263175892566097")
					.await()
		}
	}
}
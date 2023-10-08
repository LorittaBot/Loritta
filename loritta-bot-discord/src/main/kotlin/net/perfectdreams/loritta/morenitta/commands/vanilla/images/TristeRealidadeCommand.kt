package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.common.utils.Gender
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.CommandException
import net.perfectdreams.loritta.morenitta.api.entities.User
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordUser
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.locale.PersonalPronoun
import net.perfectdreams.loritta.morenitta.utils.locale.getPossessivePronoun
import java.awt.*
import java.awt.image.BufferedImage

class TristeRealidadeCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("sadreality", "tristerealidade"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
    }

    override fun command() = create {
        needsToUploadFiles = true

        localizedDescription("$LOCALE_PREFIX.tristerealidade.description")

        executesDiscord {
            OutdatedCommandUtils.sendOutdatedCommandMessage(this, this.locale, "sadreality")

            val context = this

            var x = 0
            var y = 0

            val base = BufferedImage(384, 256, BufferedImage.TYPE_INT_ARGB) // Iremos criar uma imagem 384x256 (tamanho do template)
            val baseGraph = base.graphics.enableFontAntiAliasing()

            val users = mutableListOf<User>()

            val user1 = context.user(0)
            val user2 = context.user(1)
            val user3 = context.user(2)
            val user4 = context.user(3)
            val user5 = context.user(4)
            val user6 = context.user(5)

            if (user1 != null) users.add(user1)
            if (user2 != null) users.add(user2)
            if (user3 != null) users.add(user3)
            if (user4 != null) users.add(user4)
            if (user5 != null) users.add(user5)
            if (user6 != null) users.add(user6)

            val members = context.guild.members.filter { !it.user.isBot}.toMutableList()

            while (6 > users.size) {
                val member = if (members.isNotEmpty()) {
                    members[LorittaBot.RANDOM.nextInt(members.size)]
                } else {
                    throw CommandException("Não existem membros suficientes para fazer uma triste realidade, sorry ;w;", Constants.ERROR)
                }

                users.add(JDAUser(member.user))
                members.remove(member)
            }

            var lovedGender = Gender.UNKNOWN

            val firstUser = users[0]
            if (firstUser is DiscordUser) {
                lovedGender = loritta.pudding.transaction {
                    val profile = loritta.getLorittaProfile(firstUser.id)
                    profile?.settings?.gender ?: Gender.UNKNOWN
                }
            }

            if (lovedGender == Gender.UNKNOWN)
                lovedGender = Gender.FEMALE

            var aux = 0
            while (6 > aux) {
                val member = users[0]

                if (member is JDAUser) {
                    val avatarImg = (
                            LorittaUtils.downloadImage(
                                loritta,
                                member.getEffectiveAvatarUrl(ImageFormat.PNG, 128)
                            ) ?: LorittaUtils.downloadImage(loritta, member.handle.defaultAvatarUrl))!!
                        .getScaledInstance(128, 128, Image.SCALE_SMOOTH)

                    baseGraph.drawImage(avatarImg, x, y, null)

                    baseGraph.font = Constants.MINECRAFTIA.deriveFont(Font.PLAIN, 8f)
                    baseGraph.color = Color.BLACK
                    baseGraph.drawString(member.name + "#" + member.handle.discriminator, x + 1, y + 12)
                    baseGraph.drawString(member.name + "#" + member.handle.discriminator, x + 1, y + 14)
                    baseGraph.drawString(member.name + "#" + member.handle.discriminator, x, y + 13)
                    baseGraph.drawString(member.name + "#" + member.handle.discriminator, x + 2, y + 13)
                    baseGraph.color = Color.WHITE
                    baseGraph.drawString(member.name + "#" + member.handle.discriminator, x + 1, y + 13)

                    baseGraph.font = ArtsyJoyLoriConstants.BEBAS_NEUE.deriveFont(22f)
                    var gender = Gender.UNKNOWN

                    gender = loritta.pudding.transaction {
                        val profile = loritta.getLorittaProfile(firstUser.id)
                        profile?.settings?.gender ?: Gender.UNKNOWN
                    }

                    if (gender == Gender.UNKNOWN)
                        gender = Gender.MALE
                    if (aux == 0)
                        gender = lovedGender

                    // If we use '0', '1', '2' in the YAML, Crowdin may think that's an array, and that's no good
                    val slot = when (aux) {
                        0 -> "theGuyYouLike"
                        1 -> "theFather"
                        2 -> "theBrother"
                        3 -> "theFirstLover"
                        4 -> "theBestFriend"
                        5 -> "you"
                        else -> throw RuntimeException("Invalid slot $aux")
                    }

                    drawCentralizedTextOutlined(
                        baseGraph,
                        locale["$LOCALE_PREFIX.tristerealidade.slot.$slot.${gender.name}", lovedGender.getPossessivePronoun(locale, PersonalPronoun.THIRD_PERSON, member.name)],
                        Rectangle(x, y + 80, 128, 42),
                        Color.WHITE,
                        Color.BLACK,
                        2
                    )

                    x += 128
                    if (x > 256) {
                        x = 0
                        y = 128
                    }
                }

                aux++
                users.removeAt(0)
            }

            context.sendImage(JVMImage(base), "sad_reality.png", context.getUserMention(true))
        }
    }
    private fun drawCentralizedTextOutlined(graphics: Graphics, text: String, rectangle: Rectangle, fontColor: Color, strokeColor: Color, strokeSize: Int) {
        val font = graphics.font
        graphics.font = font
        val fontMetrics = graphics.fontMetrics

        val lines = mutableListOf<String>()

        val split = text.split(" ")

        var x = 0
        var currentLine = StringBuilder()

        for (string in split) {
            val stringWidth = fontMetrics.stringWidth("$string ")
            val newX = x + stringWidth

            if (newX >= rectangle.width) {
                var endResult = currentLine.toString().trim()
                if (endResult.isEmpty()) { // okay wtf
                    // Se o texto é grande demais e o conteúdo atual está vazio... bem... substitua o endResult pela string atual
                    endResult = string
                    lines.add(endResult)
                    x = 0
                    continue
                }
                lines.add(endResult)
                currentLine = StringBuilder()
                currentLine.append(' ')
                currentLine.append(string)
                x = fontMetrics.stringWidth("$string ")
            } else {
                currentLine.append(' ')
                currentLine.append(string)
                x = newX
            }
        }
        lines.add(currentLine.toString().trim())

        val skipHeight = fontMetrics.ascent
        var y = (rectangle.height / 2) - ((skipHeight - 4) * (lines.size - 1))
        for (line in lines) {
            graphics.color = strokeColor
            for (strokeX in rectangle.x - strokeSize .. rectangle.x + strokeSize) {
                for (strokeY in rectangle.y + y - strokeSize .. rectangle.y + y + strokeSize) {
                    ImageUtils.drawCenteredStringEmoji(loritta, graphics, line, Rectangle(strokeX, strokeY, rectangle.width, 24), font)
                }
            }
            graphics.color = fontColor
            ImageUtils.drawCenteredStringEmoji(loritta, graphics, line, Rectangle(rectangle.x, rectangle.y + y, rectangle.width, 24), font)
            y += skipHeight
        }
    }
}
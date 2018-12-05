package com.mrpowergamerbr.loritta.dao

import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.tables.Timers
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.dv8tion.jda.client.entities.Group
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.requests.RestAction
import net.dv8tion.jda.core.requests.restaction.AuditableRestAction
import net.dv8tion.jda.core.requests.restaction.MessageAction
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import java.time.OffsetDateTime
import java.util.*

class Timer(id: EntityID<Long>) : LongEntity(id) {
	companion object : LongEntityClass<Timer>(Timers) {
		private val logger = KotlinLogging.logger {}
	}

	var guildId by Timers.guildId
	var channelId by Timers.channelId
	var startsAt by Timers.startsAt
	var repeatCount by Timers.repeatCount
	var repeatDelay by Timers.repeatDelay
	var activeOnDays by Timers.activeOnDays
	var commands by Timers.commands

	fun calculateRepetitions() {
		val repeat = repeatCount ?: 10

		var first = startsAt

		repeat(repeat) {
			print("$it às $first")
			val absoluteTime = first + getStartOfDay()
			if (System.currentTimeMillis() > absoluteTime) {
				print(" (Passado! ${System.currentTimeMillis()}/$absoluteTime)")
			} else {
				print(" (Será executado daqui ${absoluteTime - System.currentTimeMillis()}ms!)")
			}
			print("\n")
			first += repeatDelay
		}
	}

	suspend fun prepareTimer() {
		logger.info("prepareTimer() de ${id.value}...")

		var simulatedTime = startsAt

		var i = 0
		val compare = if (repeatCount == null) {
			{ true }
		} else { { repeatCount!! > i } }

		while (compare.invoke()) {
			// println("${System.currentTimeMillis()} / $simulatedTime")

			val relativeTimeNow = System.currentTimeMillis() - getStartOfDay()

			if (simulatedTime > relativeTimeNow) {
				logger.info("$i - uwu!!! (Será executado daqui ${simulatedTime - relativeTimeNow}ms!)")

				val start = System.currentTimeMillis()
				delay(simulatedTime - relativeTimeNow)

				println(System.currentTimeMillis() - start)

				execute()
				prepareTimer()
				return
			} else {
				// logger("$i - Passado...")
			}
			simulatedTime += repeatDelay
			i++
		}
	}

	suspend fun execute() {
		logger.info("Timer $id ativado!!")

		val guild = lorittaShards.getGuildById(guildId) ?: return
		val textChannel = guild.getTextChannelById(channelId) ?: return
		val config = loritta.getServerConfigForGuild(guild.id)

		val message = commands.random()

		// Vamos (tentar) executar um comando a partir de uma string arbitrária!
		loritta.commandManager.matches(
				LorittaMessageEvent(
						guild.selfMember.user,
						guild.selfMember,
						HackyMessage(
								guild.selfMember,
								message,
								textChannel
						),
						"-1",
						guild,
						textChannel,
						textChannel
				),
				loritta.getServerConfigForGuild(guild.id),
				loritta.getLocaleById(config.localeId),
				GuildLorittaUser(guild.selfMember, config, loritta.getOrCreateLorittaProfile(guild.selfMember.user.idLong))
		)
	}

	fun getStartOfDay(): Long {
		val calendar = Calendar.getInstance()
		calendar.set(Calendar.HOUR_OF_DAY, 0)
		calendar.set(Calendar.MINUTE, 0)
		calendar.set(Calendar.SECOND, 0)
		calendar.set(Calendar.MILLISECOND, 0)

		return calendar.timeInMillis
	}

	class HackyMessage(val sender: Member, val content: String, val backedTextChannel: TextChannel) : Message {
		override fun isFromType(p0: ChannelType): Boolean {
			return backedTextChannel.type == p0
		}

		override fun getGroup(): Group {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun isEdited(): Boolean {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun isPinned(): Boolean {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun mentionsEveryone(): Boolean {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun addReaction(p0: Emote?): RestAction<Void> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun addReaction(p0: String?): RestAction<Void> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun clearReactions(): RestAction<Void> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun formatTo(formatter: Formatter?, flags: Int, width: Int, precision: Int) {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getJumpUrl(): String {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getContentRaw(): String {
			return content
		}

		override fun getContentStripped(): String {
			return content
		}

		override fun getGuild(): Guild {
			return backedTextChannel.guild
		}

		override fun isTTS(): Boolean {
			return false
		}

		override fun isMentioned(p0: IMentionable?, vararg p1: Message.MentionType?): Boolean {
			return false
		}

		override fun editMessageFormat(p0: String?, vararg p1: Any?): MessageAction {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getMentionedChannels(): MutableList<TextChannel> {
			return mutableListOf()
		}

		override fun getMember(): Member {
			return sender
		}

		override fun getIdLong(): Long {
			return -1
		}

		override fun getContentDisplay(): String {
			return content
		}

		override fun getPrivateChannel(): PrivateChannel {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getChannelType(): ChannelType {
			return textChannel.type
		}

		override fun getAttachments(): MutableList<Message.Attachment> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getMentionedRoles(): MutableList<Role> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun pin(): RestAction<Void> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getMentionedMembers(p0: Guild?): MutableList<Member> {
			return mutableListOf()
		}

		override fun getMentionedMembers(): MutableList<Member> {
			return mutableListOf()
		}

		override fun unpin(): RestAction<Void> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getCategory(): Category {
			return textChannel.parent
		}

		override fun getInvites(): MutableList<String> {
			return mutableListOf()
		}

		override fun getEditedTime(): OffsetDateTime {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getMentionedUsers(): MutableList<User> {
			return mutableListOf()
		}

		override fun getEmotes(): MutableList<Emote> {
			return mutableListOf()
		}

		override fun getAuthor(): User {
			return sender.user
		}

		override fun editMessage(p0: CharSequence?): MessageAction {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun editMessage(p0: MessageEmbed?): MessageAction {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun editMessage(p0: Message?): MessageAction {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun delete(): AuditableRestAction<Void> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getMentions(vararg p0: Message.MentionType?): MutableList<IMentionable> {
			return mutableListOf()
		}

		override fun isWebhookMessage(): Boolean {
			return false
		}

		override fun getEmbeds(): MutableList<MessageEmbed> {
			TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		}

		override fun getType(): MessageType {
			return MessageType.DEFAULT
		}

		override fun getChannel(): MessageChannel {
			return backedTextChannel
		}

		override fun getJDA(): JDA {
			return jda
		}

		override fun getReactions(): MutableList<MessageReaction> {
			return mutableListOf()
		}

		override fun getTextChannel(): TextChannel {
			return backedTextChannel
		}

		override fun getNonce(): String {
			return "fake-message"
		}

	}
}
package net.perfectdreams.loritta.plugin.serversupport.responses

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.AuditableRestAction
import net.dv8tion.jda.api.requests.restaction.MessageAction
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction
import java.time.OffsetDateTime
import java.util.*

class FakeMessage : Message {
	override fun getReactionById(id: String): MessageReaction.ReactionEmote? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getReactionById(id: Long): MessageReaction.ReactionEmote? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun removeReaction(emote: Emote): RestAction<Void> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun removeReaction(emote: Emote, user: User): RestAction<Void> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun removeReaction(unicode: String): RestAction<Void> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun removeReaction(unicode: String, user: User): RestAction<Void> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun suppressEmbeds(suppressed: Boolean): AuditableRestAction<Void> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun retrieveReactionUsers(emote: Emote): ReactionPaginationAction {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun retrieveReactionUsers(unicode: String): ReactionPaginationAction {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getReactionByUnicode(unicode: String): MessageReaction.ReactionEmote? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isFromType(type: ChannelType): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isEdited(): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getActivity(): MessageActivity? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isPinned(): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun mentionsEveryone(): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getFlags(): EnumSet<Message.MessageFlag> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMentionedChannelsBag(): org.apache.commons.collections4.Bag<TextChannel> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun addReaction(emote: Emote): RestAction<Void> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun addReaction(unicode: String): RestAction<Void> {
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
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getContentStripped(): String {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getGuild(): Guild {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isTTS(): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isMentioned(mentionable: IMentionable, vararg types: Message.MentionType?): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isSuppressedEmbeds(): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun editMessageFormat(format: String, vararg args: Any?): MessageAction {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getEmotesBag(): org.apache.commons.collections4.Bag<Emote> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMentionedChannels(): MutableList<TextChannel> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMember(): Member? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getIdLong(): Long {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getContentDisplay(): String {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getPrivateChannel(): PrivateChannel {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getChannelType(): ChannelType {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

	override fun getMentionedMembers(guild: Guild): MutableList<Member> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMentionedMembers(): MutableList<Member> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun unpin(): RestAction<Void> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getCategory(): Category? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getInvites(): MutableList<String> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMentionedUsers(): MutableList<User> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMentionedRolesBag(): org.apache.commons.collections4.Bag<Role> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getEmotes(): MutableList<Emote> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getAuthor(): User {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMentionedUsersBag(): org.apache.commons.collections4.Bag<User> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun editMessage(newContent: CharSequence): MessageAction {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun editMessage(newContent: MessageEmbed): MessageAction {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun editMessage(newContent: Message): MessageAction {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun delete(): AuditableRestAction<Void> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getMentions(vararg types: Message.MentionType?): MutableList<IMentionable> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun isWebhookMessage(): Boolean {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getEmbeds(): MutableList<MessageEmbed> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getType(): MessageType {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getChannel(): MessageChannel {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getJDA(): JDA {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getReactions(): MutableList<MessageReaction> {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getTimeEdited(): OffsetDateTime? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getTextChannel(): TextChannel {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

	override fun getNonce(): String? {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}

}
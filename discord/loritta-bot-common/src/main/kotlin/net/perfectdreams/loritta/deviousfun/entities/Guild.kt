package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.optional
import dev.kord.rest.Image
import dev.kord.rest.builder.role.RoleCreateBuilder
import dev.kord.rest.json.request.CurrentUserNicknameModifyRequest
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.cache.DeviousGuildData
import net.perfectdreams.loritta.deviousfun.cache.DeviousGuildEmojiData
import net.perfectdreams.loritta.deviousfun.cache.DeviousRoleData
import net.perfectdreams.loritta.morenitta.cache.decode
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import kotlin.time.Duration.Companion.days

class Guild(
    val jda: JDA,
    val guild: DeviousGuildData,
    private val cacheWrapper: CacheWrapper
) : IdentifiableSnowflake {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val idSnowflake: Snowflake
        get() = guild.id

    val name: String
        get() = guild.name
    val ownerIdSnowflake
        get() = guild.ownerId
    val ownerId: String
        get() = ownerIdSnowflake.toString()
    val ownerIdLong: Long
        get() = ownerIdSnowflake.toLong()
    val owner: Member?
        get() = TODO()
    val memberCount: Int
        get() = guild.memberCount
    val iconId by guild::icon
    val iconUrl: String?
        get() = iconId?.let {
            "https://cdn.discordapp.com/icons/$idSnowflake/$it.${if (it.startsWith("a_")) "gif" else "png"}"
        }
    val splashId by guild::splashId
    val splashUrl: String?
        get() = splashId?.let { "https://cdn.discordapp.com/splashes/$idSnowflake/$it.png" }
    val channels: List<Channel>
        get() = cacheWrapper.channels.values.toList()
    val textChannels: List<Channel>
        get() = channels.filter { it.type == ChannelType.GuildText }
    val voiceChannels: List<Channel>
        get() = channels.filter { it.type == ChannelType.GuildVoice }
    val boosters: List<Member>
        get() = TODO()
    val emotes: List<DiscordGuildEmote>
        get() = cacheWrapper.emotes.values.toList()
    val roles: List<Role>
        get() = cacheWrapper.roles.values.toList()
    val publicRole: Role
        get() = roles.first { it.idSnowflake == idSnowflake }
    val vanityCode: String?
        get() = guild.vanityUrlCode
    val boostCount: Int
        get() = guild.premiumSubscriptionCount
    val bannerUrl: String
        get() = TODO()

    suspend fun leave() {
        jda.loritta.rest.user.leaveGuild(idSnowflake)
    }

    suspend fun isMember(user: User): Boolean = getMember(user) != null
    suspend fun getMember(user: User) = jda.getMemberByUser(this, user)

    suspend fun retrieveSelfMember() = jda.retrieveMemberById(this@Guild, jda.loritta.config.loritta.discord.applicationId)

    fun getRoleById(id: String) = roles.firstOrNull { it.id == id }
    fun getRoleById(id: Long) = roles.firstOrNull { it.idLong == id }
    fun getRolesByName(name: String, ignoreCase: Boolean) = roles.filter { it.name.equals(name, ignoreCase) }

    suspend fun selfMemberHasPermission(vararg permissions: Permission): Boolean {
        return jda.loritta.cache.getLazyCachedPermissions(idSnowflake, jda.loritta.config.loritta.discord.applicationId).hasPermission(*permissions)
    }

    suspend fun selfMemberHasPermission(channel: Channel, vararg permissions: Permission): Boolean {
        return jda.loritta.cache.getLazyCachedPermissions(idSnowflake, channel.idSnowflake, jda.loritta.config.loritta.discord.applicationId).hasPermission(*permissions)
    }

    suspend fun retrieveMembers(): List<Member> {
        // TODO - DeviousFun: Mutex
        val membersAsString = jda.loritta.redisConnection {
            it.hgetAll(jda.loritta.redisKeys.discordGuildMembers(idSnowflake).toByteArray(Charsets.UTF_8))
        }.entries // This is required to have a stable key -> value map, if else, things can be shuffled when for eaching over them later on, which won't work!

        val usersAsString = jda.loritta.redisConnection {
            it.hmget(jda.loritta.redisKeys.discordUsers().toByteArray(Charsets.UTF_8), *membersAsString.map { it.key }.toTypedArray())
        }

        val members = mutableListOf<Member>()

        for ((index, data) in membersAsString.withIndex()) {
            val memberAsString = data.value
            val userAsString = usersAsString.getOrNull(index)
            if (userAsString == null) {
                logger.warn { "Member ${data.key.toString(Charsets.UTF_8)} data is present in guild ${this.guild.id}, but I don't have that user cached! Skipping..." }
                continue
            }
            val memberData = jda.loritta.binaryCacheTransformers.members.decode(memberAsString)
            val userData = jda.loritta.binaryCacheTransformers.users.decode(userAsString)

            members.add(
                Member(
                    jda,
                    memberData,
                    this,
                    User(
                        jda,
                        Snowflake(data.key.toString(Charsets.UTF_8)),
                        userData
                    )
                )
            )
        }

        return members
    }

    suspend fun getMembersWithRoles(vararg roles: Role): List<Member> {
        val roleIds = roles.map { it.idSnowflake }

        // TODO - DeviousFun: Mutex
        // Compared to retrieveMembers, this has a smol optimization, where it checks if the role exists before querying users
        val membersAsString = jda.loritta.redisConnection {
            it.hgetAll(jda.loritta.redisKeys.discordGuildMembers(idSnowflake).toByteArray(Charsets.UTF_8))
        }
            .mapValues { jda.loritta.binaryCacheTransformers.members.decode(it.value) }
            .filter { roleIds.all { id -> id in it.value.roles } }
            .entries // This is required to have a stable key -> value map, if else, things can be shuffled when for eaching over them later on, which won't work!

        val usersAsString = jda.loritta.redisConnection {
            it.hmget(jda.loritta.redisKeys.discordUsers().toByteArray(Charsets.UTF_8), *membersAsString.map { it.key }.toTypedArray())
        }

        val members = mutableListOf<Member>()

        for ((index, data) in membersAsString.withIndex()) {
            val userAsString = usersAsString.getOrNull(index)
            if (userAsString == null) {
                logger.warn { "Member ${data.key.toString(Charsets.UTF_8)} data is present in guild ${this.guild.id}, but I don't have that user cached! Skipping..." }
                continue
            }
            val memberData = data.value
            val userData = jda.loritta.binaryCacheTransformers.users.decode(userAsString)

            members.add(
                Member(
                    jda,
                    memberData,
                    this,
                    User(
                        jda,
                        Snowflake(data.key.toString(Charsets.UTF_8)),
                        userData
                    )
                )
            )
        }

        return members
    }

    fun getTextChannelById(id: String) = textChannels.firstOrNull { it.id == id }
    fun getTextChannelById(id: Long) = textChannels.firstOrNull { it.idLong == id }
    fun getTextChannelsByName(name: String, ignoreCase: Boolean) = textChannels.filter { it.name.equals(name, ignoreCase) }

    fun getVoiceChannelById(id: String) = voiceChannels.firstOrNull { it.id == id }
    fun getVoiceChannelsByName(name: String, ignoreCase: Boolean) = voiceChannels.filter { it.name.equals(name, ignoreCase) }

    // TODO - DeviousFun
    fun getMemberByTag(name: String, discriminator: String): Member? = null
    // TODO - DeviousFun
    fun getMembersByEffectiveName(name: String, ignoreCase: Boolean): List<Member> = emptyList()
    // TODO - DeviousFun
    fun getMembersByName(name: String, ignoreCase: Boolean): List<Member> = emptyList()
    suspend fun getMemberById(id: String) = jda.getMemberById(this, Snowflake(id))
    suspend fun getMemberById(id: Long) = jda.getMemberById(this, Snowflake(id))

    fun getEmoteById(id: String) = emotes.firstOrNull { it.id == id }

    suspend fun retrieveMemberOrNull(user: User) = jda.retrieveMemberById(this, user.idSnowflake)
    suspend fun retrieveMemberById(id: String) = jda.retrieveMemberById(this, Snowflake(id))
    suspend fun retrieveMemberById(id: Long) = jda.retrieveMemberById(this, Snowflake(id))
    fun retrieveMemberOrNullById(id: String): Member? {
        TODO()
    }
    fun retrieveMemberOrNullById(id: Long): Member? {
        TODO()
    }

    suspend fun createEmote(emoteName: String, byteArray: ByteArray): Emote {
        val emoji = jda.loritta.rest.emoji.createEmoji(
            idSnowflake,
            emoteName,
            Image.raw(
                byteArray,
                when (SimpleImageInfo(byteArray).mimeType) {
                    "image/gif" -> Image.Format.GIF
                    else -> Image.Format.PNG
                }
            )
        )

        // We don't need to cache it, it will be cached after Discord sends a emoji updated event
        return DiscordGuildEmote(
            jda,
            this,
            DeviousGuildEmojiData.from(emoji)
        )
    }

    suspend fun createRole(builder: RoleCreateBuilder.() -> (Unit)): Role {
        val response = jda.loritta.rest.guild.createGuildRole(idSnowflake, builder)

        // We don't need to cache it, it will be cached after Discord sends a role created event
        return Role(
            jda,
            this,
            DeviousRoleData.from(response)
        )
    }

    suspend fun retrieveInvites(): List<Invite> {
        return jda.loritta.rest.guild.getGuildInvites(idSnowflake)
            .map {
                Invite(jda, it.inviter.value?.let { jda.cacheManager.createUser(it, true) }, it)
            }
    }

    suspend fun addRoleToMember(member: Member, role: Role, reason: String? = null) {
        jda.loritta.rest.guild.addRoleToGuildMember(guild.id, member.idSnowflake, role.idSnowflake, reason)
    }

    suspend fun removeRoleFromMember(member: Member, role: Role, reason: String? = null) {
        jda.loritta.rest.guild.deleteRoleFromGuildMember(guild.id, member.idSnowflake, role.idSnowflake, reason)
    }

    suspend fun modifyMemberRoles(member: Member, roles: List<Role>, reason: String? = null) {
        jda.loritta.rest.guild.modifyGuildMember(guild.id, member.idSnowflake) {
            this.reason = reason
            this.roles = roles.map { it.idSnowflake }.toMutableSet()
        }
    }

    suspend fun modifyNickname(member: Member, newNickname: String?) {
        jda.loritta.rest.guild.modifyGuildMember(guild.id, member.idSnowflake) {
            this.nickname = newNickname
        }
    }

    suspend fun retrieveWebhooks(): List<Webhook> {
        // TODO - DeviousFun: Instead of getting every channel individually, get it in bulk
        return jda.loritta.rest.webhook.getGuildWebhooks(idSnowflake)
            .map {
                Webhook(
                    jda,
                    jda.retrieveChannelById(this@Guild, it.channelId),
                    it.user.value?.let { jda.cacheManager.createUser(it, true) },
                    it
                )
            }
    }

    suspend fun ban(user: User, delDays: Int, reason: String? = null) {
        jda.loritta.rest.guild.addGuildBan(Snowflake(id), Snowflake(user.idLong)) {
            this.deleteMessageDuration = delDays.days
            this.reason = reason
        }
    }

    suspend fun unban(user: User, reason: String? = null) = unban(user.id, reason)

    suspend fun unban(userId: String, reason: String? = null) {
        jda.loritta.rest.guild.deleteGuildBan(Snowflake(id), Snowflake(userId), reason)
    }

    suspend fun kick(member: Member, reason: String? = null) {
        jda.loritta.rest.guild.deleteGuildMember(Snowflake(id), Snowflake(member.idLong), reason)
    }

    suspend fun kick(user: User, reason: String) {
        jda.loritta.rest.guild.deleteGuildMember(Snowflake(id), Snowflake(user.idLong), reason)
    }

    suspend fun retrieveBanById(id: Long): Ban {
        val banResponse = jda.loritta.rest.guild.getGuildBan(Snowflake(idLong), Snowflake(id))
        val user = jda.cacheManager.createUser(banResponse.user, true)

        return Ban(
            jda,
            user,
            banResponse
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Guild)
            return false

        return this.idSnowflake == other.idSnowflake
    }

    /**
     * A cache wrapper for entities that requires the [Guild] instance to be present in the constructor
     */
    class CacheWrapper {
        val roles = mutableMapOf<Snowflake, Role>()
        val channels = mutableMapOf<Snowflake, Channel>()
        val emotes = mutableMapOf<Snowflake, DiscordGuildEmote>()
    }
}
package net.perfectdreams.loritta.deviousfun.entities

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.rest.Image
import dev.kord.rest.builder.role.RoleCreateBuilder
import dev.kord.rest.request.KtorRequestException
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.utils.GuildKey
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import kotlin.time.Duration.Companion.days

class Guild(
    val deviousShard: DeviousShard,
    val guild: DeviousGuildData,
    private val cacheWrapper: CacheWrapper
) : IdentifiableSnowflake {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override val idSnowflake: Snowflake
        get() = guild.id.toKordSnowflake()

    val name: String
        get() = guild.name
    val ownerIdSnowflake
        get() = guild.ownerId.toKordSnowflake()
    val ownerId: String
        get() = ownerIdSnowflake.toString()
    val ownerIdLong: Long
        get() = ownerIdSnowflake.toLong()
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
        get() = channels.filter { it.type in Channel.TEXT_CHANNEL_LIKE_CHANNEL_TYPES }
    val voiceChannels: List<Channel>
        get() = channels.filter { it.type == ChannelType.GuildVoice }
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
    val bannerId by guild::bannerId
    val bannerUrl: String?
        get() = bannerId?.let { "https://cdn.discordapp.com/banners/$idSnowflake/$it.png" }

    suspend fun leave() {
        deviousShard.loritta.rest.user.leaveGuild(idSnowflake)
    }

    suspend fun isMember(user: User): Boolean = getMember(user) != null
    suspend fun getMember(user: User) = deviousShard.getMemberByUser(this, user)

    suspend fun retrieveSelfMember() =
        deviousShard.retrieveMemberById(this@Guild, deviousShard.loritta.config.loritta.discord.applicationId)

    fun getRoleById(id: String) = roles.firstOrNull { it.id == id }
    fun getRoleById(id: Long) = roles.firstOrNull { it.idLong == id }
    fun getRolesByName(name: String, ignoreCase: Boolean) = roles.filter { it.name.equals(name, ignoreCase) }

    suspend fun selfMemberHasPermission(vararg permissions: Permission) = retrieveSelfMember().hasPermission(*permissions)

    suspend fun selfMemberHasPermission(channel: Channel, vararg permissions: Permission) = retrieveSelfMember().hasPermission(channel, *permissions)

    suspend fun retrieveOwner() = deviousShard.retrieveMemberById(this, ownerIdSnowflake)

    suspend fun retrieveMembers(): List<Member> {
        val lightweightSnowflake = idSnowflake.toLightweightSnowflake()

        deviousShard.getCacheManager().withLock(GuildKey(lightweightSnowflake)) {
            logger.info { "Retrieving members of guild ${guild.id}..." }

            val cachedMembers = deviousShard.getCacheManager().members[lightweightSnowflake] ?: return emptyList()
            val members = mutableListOf<Member>()

            for ((id, member) in cachedMembers) {
                val user = deviousShard.getCacheManager().users[id] ?: continue

                members.add(
                    Member(
                        deviousShard,
                        member,
                        this,
                        User(
                            deviousShard,
                            id.toKordSnowflake(),
                            user
                        )
                    )
                )
            }

            return members
        }
    }

    suspend fun retrieveMembersWithRoles(vararg roles: Role): List<Member> {
        val lightweightSnowflake = idSnowflake.toLightweightSnowflake()

        deviousShard.getCacheManager().withLock(GuildKey(lightweightSnowflake)) {
            logger.info { "Retrieving members of guild ${guild.id} that have the role ${roles}..." }

            val cachedMembers = deviousShard.getCacheManager().members[lightweightSnowflake] ?: return emptyList()
            val members = mutableListOf<Member>()

            for ((id, member) in cachedMembers) {
                if (!member.roles.containsAll(roles.map { it.idSnowflake.toLightweightSnowflake() }))
                    continue

                val user = deviousShard.getCacheManager().users[id] ?: continue

                members.add(
                    Member(
                        deviousShard,
                        member,
                        this,
                        User(
                            deviousShard,
                            id.toKordSnowflake(),
                            user
                        )
                    )
                )
            }

            return members
        }
    }

    suspend fun retrieveBoosters(): List<Member> {
        val lightweightSnowflake = idSnowflake.toLightweightSnowflake()

        deviousShard.getCacheManager().withLock(GuildKey(lightweightSnowflake)) {
            logger.info { "Retrieving boosters of guild ${guild.id}..." }

            val cachedMembers = deviousShard.getCacheManager().members[lightweightSnowflake] ?: return emptyList()
            val members = mutableListOf<Member>()

            for ((id, member) in cachedMembers) {
                if (member.premiumSince == null)
                    continue

                val user = deviousShard.getCacheManager().users[id] ?: continue

                members.add(
                    Member(
                        deviousShard,
                        member,
                        this,
                        User(
                            deviousShard,
                            id.toKordSnowflake(),
                            user
                        )
                    )
                )
            }

            return members
        }
    }

    fun getChannelById(id: String) = channels.firstOrNull { it.id == id }
    fun getChannelById(id: Long) = channels.firstOrNull { it.idLong == id }
    fun getTextChannelById(id: String) = textChannels.firstOrNull { it.id == id }
    fun getTextChannelById(id: Long) = textChannels.firstOrNull { it.idLong == id }
    fun getTextChannelsByName(name: String, ignoreCase: Boolean) =
        textChannels.filter { it.name.equals(name, ignoreCase) }

    fun getVoiceChannelById(id: String) = voiceChannels.firstOrNull { it.id == id }
    fun getVoiceChannelsByName(name: String, ignoreCase: Boolean) =
        voiceChannels.filter { it.name.equals(name, ignoreCase) }

    // TODO - DeviousFun
    fun getMemberByTag(name: String, discriminator: String): Member? = null

    // TODO - DeviousFun
    fun getMembersByEffectiveName(name: String, ignoreCase: Boolean): List<Member> = emptyList()

    // TODO - DeviousFun
    fun getMembersByName(name: String, ignoreCase: Boolean): List<Member> = emptyList()
    suspend fun getMemberById(id: String) = deviousShard.getMemberById(this, Snowflake(id))
    suspend fun getMemberById(id: Long) = deviousShard.getMemberById(this, Snowflake(id))

    fun getEmoteById(id: String) = emotes.firstOrNull { it.id == id }

    suspend fun retrieveMemberOrNull(user: User) = try { deviousShard.retrieveMemberById(this, user.idSnowflake) } catch (e: KtorRequestException) { null }
    suspend fun retrieveMemberById(id: String) = deviousShard.retrieveMemberById(this, Snowflake(id))
    suspend fun retrieveMemberById(id: Long) = deviousShard.retrieveMemberById(this, Snowflake(id))

    suspend fun retrieveMemberOrNullById(id: String) = try {
        deviousShard.retrieveMemberById(this, Snowflake(id))
    } catch (e: KtorRequestException) {
        null
    }

    suspend fun retrieveMemberOrNullById(id: Long) = try {
        deviousShard.retrieveMemberById(this, Snowflake(id))
    } catch (e: KtorRequestException) {
        null
    }

    suspend fun createEmote(emoteName: String, byteArray: ByteArray): Emote {
        val emoji = deviousShard.loritta.rest.emoji.createEmoji(
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
            deviousShard,
            this,
            DeviousGuildEmojiData.from(emoji)
        )
    }

    suspend fun createRole(builder: RoleCreateBuilder.() -> (Unit)): Role {
        val response = deviousShard.loritta.rest.guild.createGuildRole(idSnowflake, builder)

        // We don't need to cache it, it will be cached after Discord sends a role created event
        return Role(
            deviousShard,
            this,
            DeviousRoleData.from(response)
        )
    }

    suspend fun retrieveInvites(): List<Invite> {
        return deviousShard.loritta.rest.guild.getGuildInvites(idSnowflake)
            .map {
                Invite(deviousShard, it.inviter.value?.let { deviousShard.getCacheManager().createUser(it, true) }, it)
            }
    }

    suspend fun addRoleToMember(member: Member, role: Role, reason: String? = null) {
        deviousShard.loritta.rest.guild.addRoleToGuildMember(guild.id.toKordSnowflake(), member.idSnowflake, role.idSnowflake, reason)
    }

    suspend fun removeRoleFromMember(member: Member, role: Role, reason: String? = null) {
        deviousShard.loritta.rest.guild.deleteRoleFromGuildMember(guild.id.toKordSnowflake(), member.idSnowflake, role.idSnowflake, reason)
    }

    suspend fun modifyMemberRoles(member: Member, roles: List<Role>, reason: String? = null) {
        deviousShard.loritta.rest.guild.modifyGuildMember(guild.id.toKordSnowflake(), member.idSnowflake) {
            this.reason = reason
            this.roles = roles.map { it.idSnowflake }.toMutableSet()
        }
    }

    suspend fun modifyNickname(member: Member, newNickname: String?) {
        deviousShard.loritta.rest.guild.modifyGuildMember(guild.id.toKordSnowflake(), member.idSnowflake) {
            this.nickname = newNickname
        }
    }

    suspend fun retrieveWebhooks(): List<Webhook> {
        // TODO - DeviousFun: Instead of getting every channel individually, get it in bulk
        return deviousShard.loritta.rest.webhook.getGuildWebhooks(idSnowflake)
            .map {
                Webhook(
                    deviousShard,
                    it.channelId,
                    getChannelById(it.channelId.toLong()),
                    it.user.value?.let { deviousShard.getCacheManager().createUser(it, true) },
                    it
                )
            }
    }

    suspend fun ban(user: User, delDays: Int, reason: String? = null) {
        deviousShard.loritta.rest.guild.addGuildBan(Snowflake(id), Snowflake(user.idLong)) {
            this.deleteMessageDuration = delDays.days
            this.reason = reason
        }
    }

    suspend fun unban(user: User, reason: String? = null) = unban(user.id, reason)

    suspend fun unban(userId: String, reason: String? = null) {
        deviousShard.loritta.rest.guild.deleteGuildBan(Snowflake(id), Snowflake(userId), reason)
    }

    suspend fun kick(member: Member, reason: String? = null) {
        deviousShard.loritta.rest.guild.deleteGuildMember(Snowflake(id), Snowflake(member.idLong), reason)
    }

    suspend fun kick(user: User, reason: String) {
        deviousShard.loritta.rest.guild.deleteGuildMember(Snowflake(id), Snowflake(user.idLong), reason)
    }

    suspend fun retrieveBanById(id: Long): Ban {
        val banResponse = deviousShard.loritta.rest.guild.getGuildBan(Snowflake(idLong), Snowflake(id))
        val user = deviousShard.getCacheManager().createUser(banResponse.user, true)

        return Ban(
            deviousShard,
            user,
            banResponse
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Guild)
            return false

        return this.idSnowflake == other.idSnowflake
    }

    override fun hashCode() = this.idSnowflake.hashCode()

    /**
     * A cache wrapper for entities that requires the [Guild] instance to be present in the constructor
     */
    class CacheWrapper {
        val roles = mutableMapOf<Snowflake, Role>()
        val channels = mutableMapOf<Snowflake, Channel>()
        val emotes = mutableMapOf<Snowflake, DiscordGuildEmote>()
    }
}
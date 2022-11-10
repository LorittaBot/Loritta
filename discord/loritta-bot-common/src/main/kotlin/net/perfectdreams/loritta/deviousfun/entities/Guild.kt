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
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.utils.GuildKey
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import kotlin.time.Duration.Companion.days

class Guild(
    val deviousFun: DeviousFun,
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
        deviousFun.loritta.rest.user.leaveGuild(idSnowflake)
    }

    suspend fun isMember(user: User): Boolean = getMember(user) != null
    suspend fun getMember(user: User) = deviousFun.getMemberByUser(this, user)

    suspend fun retrieveSelfMember() =
        deviousFun.retrieveMemberById(this@Guild, deviousFun.loritta.config.loritta.discord.applicationId)

    fun getRoleById(id: String) = roles.firstOrNull { it.id == id }
    fun getRoleById(id: Long) = roles.firstOrNull { it.idLong == id }
    fun getRolesByName(name: String, ignoreCase: Boolean) = roles.filter { it.name.equals(name, ignoreCase) }

    suspend fun selfMemberHasPermission(vararg permissions: Permission): Boolean {
        return deviousFun.loritta.cache.getLazyCachedPermissions(
            idSnowflake,
            deviousFun.loritta.config.loritta.discord.applicationId
        ).hasPermission(*permissions)
    }

    suspend fun selfMemberHasPermission(channel: Channel, vararg permissions: Permission): Boolean {
        return deviousFun.loritta.cache.getLazyCachedPermissions(
            idSnowflake,
            channel.idSnowflake,
            deviousFun.loritta.config.loritta.discord.applicationId
        ).hasPermission(*permissions)
    }

    suspend fun retrieveOwner() = deviousFun.retrieveMemberById(this, ownerIdSnowflake)

    suspend fun retrieveMembers(): List<Member> {
        val lightweightSnowflake = idSnowflake.toLightweightSnowflake()

        deviousFun.cacheManager.withLock(GuildKey(lightweightSnowflake)) {
            logger.info { "Retrieving members of guild ${guild.id}..." }

            val cachedMembers = deviousFun.cacheManager.members[lightweightSnowflake] ?: return emptyList()
            val members = mutableListOf<Member>()

            for ((id, member) in cachedMembers) {
                val user = deviousFun.cacheManager.users[id] ?: continue

                members.add(
                    Member(
                        deviousFun,
                        member,
                        this,
                        User(
                            deviousFun,
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

        deviousFun.cacheManager.withLock(GuildKey(lightweightSnowflake)) {
            logger.info { "Retrieving members of guild ${guild.id} that have the role ${roles}..." }

            val cachedMembers = deviousFun.cacheManager.members[lightweightSnowflake] ?: return emptyList()
            val members = mutableListOf<Member>()

            for ((id, member) in cachedMembers) {
                if (!member.roles.containsAll(roles.map { it.idSnowflake.toLightweightSnowflake() }))
                    continue

                val user = deviousFun.cacheManager.users[id] ?: continue

                members.add(
                    Member(
                        deviousFun,
                        member,
                        this,
                        User(
                            deviousFun,
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

        deviousFun.cacheManager.withLock(GuildKey(lightweightSnowflake)) {
            logger.info { "Retrieving boosters of guild ${guild.id}..." }

            val cachedMembers = deviousFun.cacheManager.members[lightweightSnowflake] ?: return emptyList()
            val members = mutableListOf<Member>()

            for ((id, member) in cachedMembers) {
                if (member.premiumSince == null)
                    continue

                val user = deviousFun.cacheManager.users[id] ?: continue

                members.add(
                    Member(
                        deviousFun,
                        member,
                        this,
                        User(
                            deviousFun,
                            id.toKordSnowflake(),
                            user
                        )
                    )
                )
            }

            return members
        }
    }

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
    suspend fun getMemberById(id: String) = deviousFun.getMemberById(this, Snowflake(id))
    suspend fun getMemberById(id: Long) = deviousFun.getMemberById(this, Snowflake(id))

    fun getEmoteById(id: String) = emotes.firstOrNull { it.id == id }

    suspend fun retrieveMemberOrNull(user: User) = try { deviousFun.retrieveMemberById(this, user.idSnowflake) } catch (e: KtorRequestException) { null }
    suspend fun retrieveMemberById(id: String) = deviousFun.retrieveMemberById(this, Snowflake(id))
    suspend fun retrieveMemberById(id: Long) = deviousFun.retrieveMemberById(this, Snowflake(id))

    suspend fun retrieveMemberOrNullById(id: String) = try {
        deviousFun.retrieveMemberById(this, Snowflake(id))
    } catch (e: KtorRequestException) {
        null
    }

    suspend fun retrieveMemberOrNullById(id: Long) = try {
        deviousFun.retrieveMemberById(this, Snowflake(id))
    } catch (e: KtorRequestException) {
        null
    }

    suspend fun createEmote(emoteName: String, byteArray: ByteArray): Emote {
        val emoji = deviousFun.loritta.rest.emoji.createEmoji(
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
            deviousFun,
            this,
            DeviousGuildEmojiData.from(emoji)
        )
    }

    suspend fun createRole(builder: RoleCreateBuilder.() -> (Unit)): Role {
        val response = deviousFun.loritta.rest.guild.createGuildRole(idSnowflake, builder)

        // We don't need to cache it, it will be cached after Discord sends a role created event
        return Role(
            deviousFun,
            this,
            DeviousRoleData.from(response)
        )
    }

    suspend fun retrieveInvites(): List<Invite> {
        return deviousFun.loritta.rest.guild.getGuildInvites(idSnowflake)
            .map {
                Invite(deviousFun, it.inviter.value?.let { deviousFun.cacheManager.createUser(it, true) }, it)
            }
    }

    suspend fun addRoleToMember(member: Member, role: Role, reason: String? = null) {
        deviousFun.loritta.rest.guild.addRoleToGuildMember(guild.id.toKordSnowflake(), member.idSnowflake, role.idSnowflake, reason)
    }

    suspend fun removeRoleFromMember(member: Member, role: Role, reason: String? = null) {
        deviousFun.loritta.rest.guild.deleteRoleFromGuildMember(guild.id.toKordSnowflake(), member.idSnowflake, role.idSnowflake, reason)
    }

    suspend fun modifyMemberRoles(member: Member, roles: List<Role>, reason: String? = null) {
        deviousFun.loritta.rest.guild.modifyGuildMember(guild.id.toKordSnowflake(), member.idSnowflake) {
            this.reason = reason
            this.roles = roles.map { it.idSnowflake }.toMutableSet()
        }
    }

    suspend fun modifyNickname(member: Member, newNickname: String?) {
        deviousFun.loritta.rest.guild.modifyGuildMember(guild.id.toKordSnowflake(), member.idSnowflake) {
            this.nickname = newNickname
        }
    }

    suspend fun retrieveWebhooks(): List<Webhook> {
        // TODO - DeviousFun: Instead of getting every channel individually, get it in bulk
        return deviousFun.loritta.rest.webhook.getGuildWebhooks(idSnowflake)
            .map {
                Webhook(
                    deviousFun,
                    deviousFun.retrieveChannelById(this@Guild, it.channelId),
                    it.user.value?.let { deviousFun.cacheManager.createUser(it, true) },
                    it
                )
            }
    }

    suspend fun ban(user: User, delDays: Int, reason: String? = null) {
        deviousFun.loritta.rest.guild.addGuildBan(Snowflake(id), Snowflake(user.idLong)) {
            this.deleteMessageDuration = delDays.days
            this.reason = reason
        }
    }

    suspend fun unban(user: User, reason: String? = null) = unban(user.id, reason)

    suspend fun unban(userId: String, reason: String? = null) {
        deviousFun.loritta.rest.guild.deleteGuildBan(Snowflake(id), Snowflake(userId), reason)
    }

    suspend fun kick(member: Member, reason: String? = null) {
        deviousFun.loritta.rest.guild.deleteGuildMember(Snowflake(id), Snowflake(member.idLong), reason)
    }

    suspend fun kick(user: User, reason: String) {
        deviousFun.loritta.rest.guild.deleteGuildMember(Snowflake(id), Snowflake(user.idLong), reason)
    }

    suspend fun retrieveBanById(id: Long): Ban {
        val banResponse = deviousFun.loritta.rest.guild.getGuildBan(Snowflake(idLong), Snowflake(id))
        val user = deviousFun.cacheManager.createUser(banResponse.user, true)

        return Ban(
            deviousFun,
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
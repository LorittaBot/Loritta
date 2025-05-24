package net.perfectdreams.loritta.morenitta.interactions.commands.options

import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

open class ApplicationCommandOptions {
    companion object {
        val NO_OPTIONS = object : ApplicationCommandOptions() {}
    }

    val registeredOptions = mutableListOf<OptionReference<*>>()

    fun string(name: String, description: StringI18nData, range: IntRange? = null, builder: StringDiscordOptionReference<String>.() -> (Unit) = {}) = StringDiscordOptionReference<String>(name, description, true, range)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun optionalString(name: String, description: StringI18nData, range: IntRange? = null, builder: StringDiscordOptionReference<String?>.() -> (Unit) = {}) = StringDiscordOptionReference<String?>(name, description, false, range)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun boolean(name: String, description: StringI18nData, builder: BooleanDiscordOptionReference<Boolean>.() -> (Unit) = {}) = BooleanDiscordOptionReference<Boolean>(name, description, true)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun optionalBoolean(name: String, description: StringI18nData, builder: BooleanDiscordOptionReference<Boolean?>.() -> (Unit) = {}) = BooleanDiscordOptionReference<Boolean?>(name, description, false)
        .apply(builder)
        .also { registeredOptions.add(it) }

    fun long(name: String, description: StringI18nData, requiredRange: LongRange? = null) = LongDiscordOptionReference<Long>(name, description, true, requiredRange)
        .also { registeredOptions.add(it) }

    fun optionalLong(name: String, description: StringI18nData, requiredRange: LongRange? = null) = LongDiscordOptionReference<Long?>(name, description, false, requiredRange)
        .also { registeredOptions.add(it) }

    fun double(name: String, description: StringI18nData, requiredRange: ClosedFloatingPointRange<Double>? = null) = NumberDiscordOptionReference<Double>(name, description, true, requiredRange)
        .also { registeredOptions.add(it) }

    fun optionalDouble(name: String, description: StringI18nData, requiredRange: ClosedFloatingPointRange<Double>? = null) = NumberDiscordOptionReference<Double?>(name, description, false, requiredRange)
        .also { registeredOptions.add(it) }

    fun channel(name: String, description: StringI18nData) = ChannelDiscordOptionReference<GuildChannel>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalChannel(name: String, description: StringI18nData) = ChannelDiscordOptionReference<GuildChannel?>(name,description, false)
        .also { registeredOptions.add(it) }

    fun user(name: String, description: StringI18nData) = UserDiscordOptionReference<UserAndMember>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalUser(name: String, description: StringI18nData) = UserDiscordOptionReference<UserAndMember?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun role(name: String, description: StringI18nData) = RoleDiscordOptionReference<Role>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalRole(name: String, description: StringI18nData) = RoleDiscordOptionReference<Role?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun attachment(name: String, description: StringI18nData) = AttachmentDiscordOptionReference<Attachment>(name, description, true)
        .also { registeredOptions.add(it) }

    fun optionalAttachment(name: String, description: StringI18nData) = AttachmentDiscordOptionReference<Attachment?>(name, description, false)
        .also { registeredOptions.add(it) }

    fun imageReference(name: String) = ImageReferenceDiscordOptionReference<ImageReference>(name)
        .also { registeredOptions.add(it) }

    fun imageReferenceOrAttachment(name: String, description: StringI18nData) = ImageReferenceOrAttachmentDiscordOptionReference<ImageReferenceOrAttachment>(name)
        .also { registeredOptions.add(it) }
}
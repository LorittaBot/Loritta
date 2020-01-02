package net.perfectdreams.loritta

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullLong
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.tables.ServerConfigs
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.perfectdreams.loritta.commands.BirthdayCommand
import net.perfectdreams.loritta.commands.DocesCommand
import net.perfectdreams.loritta.commands.LoriToolsQuirkyStuffCommand
import net.perfectdreams.loritta.commands.SouTopDoadorCommand
import net.perfectdreams.loritta.dao.Payment
import net.perfectdreams.loritta.listeners.*
import net.perfectdreams.loritta.modules.*
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import net.perfectdreams.loritta.profile.badges.CanecaBadge
import net.perfectdreams.loritta.profile.badges.HalloweenBadge
import net.perfectdreams.loritta.tables.BoostedCandyChannels
import net.perfectdreams.loritta.tables.CollectedCandies
import net.perfectdreams.loritta.tables.Halloween2019Players
import net.perfectdreams.loritta.tables.Payments
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.payments.PaymentGateway
import net.perfectdreams.loritta.utils.payments.PaymentReason
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.math.BigDecimal

class QuirkyStuff : DiscordPlugin() {
    private val dreamsBoostTypes = mapOf(
            59.99.toBigDecimal() to 1.0,
            39.99.toBigDecimal() to 1.0,
            19.99.toBigDecimal() to 1.0
    )


    val task = GlobalScope.launch(LorittaLauncher.loritta.coroutineDispatcher) {
        while (true) {
            delay(60_000)
            val moneySumId = Payments.money.sum()
            val mostPayingUsers = transaction(Databases.loritta) {
                Payments.slice(Payments.userId, moneySumId)
                        .select {
                            Payments.paidAt.isNotNull() and
                                    (Payments.reason eq PaymentReason.DONATION) or (Payments.reason eq PaymentReason.SPONSORED) and
                                    (Payments.expiresAt greaterEq System.currentTimeMillis())
                        }
                        .groupBy(Payments.userId)
                        .orderBy(moneySumId, SortOrder.DESC)
                        .toMutableList()
            }

            for ((bigDecimal, quantity) in dreamsBoostTypes) {
                val deserveTheRewardUsers = mostPayingUsers.filter { it[moneySumId]!! >= bigDecimal }.map { it[Payments.userId] }

                transaction(Databases.loritta) {
                    Profiles.update({ Profiles.id inList deserveTheRewardUsers }) {
                        with(SqlExpressionBuilder) {
                            it.update(money, money + quantity)
                        }
                    }
                }
            }

            val guild = lorittaShards.getGuildById(Constants.PORTUGUESE_SUPPORT_GUILD_ID)

            if (guild != null) {
                // Remover key de boosts inválidos
                transaction(Databases.loritta) {
                    val nitroBoostPayments = Payment.find {
                        (Payments.gateway eq PaymentGateway.NITRO_BOOST)
                    }.toMutableList()

                    val invalidNitroPayments = mutableListOf<Long>()

                    for (nitroBoostPayment in nitroBoostPayments) {
                        val metadata = nitroBoostPayment.metadata
                        val isFromThisGuild = metadata != null && metadata.obj["guildId"].nullLong == guild.idLong

                        if (isFromThisGuild) {
                            val member = guild.getMemberById(nitroBoostPayment.userId)

                            if (member == null || member.timeBoosted == null) {
                                logger.warn { "Deleting Nitro Boost payment by ${nitroBoostPayment.userId} because user is not boosting the guild anymore! (is member null? ${member != null})" }
                                invalidNitroPayments.add(nitroBoostPayment.userId)
                                nitroBoostPayment.delete()
                            }
                        }
                    }

                    DonationKey.find {
                        (DonationKeys.expiresAt eq Long.MAX_VALUE) and (DonationKeys.value eq 19.99)
                    }.forEach {
                        val metadata = it.metadata
                        val isFromThisGuild = metadata != null && metadata.obj["guildId"].nullLong == guild.idLong

                        if (isFromThisGuild) {
                            val member = guild.getMemberById(it.userId)

                            if (member == null || member.timeBoosted == null) {
                                logger.warn { "Deleting donation key via Nitro Boost by ${it.userId} because user is not boosting the guild anymore! (is member null? ${member != null})" }

                                ServerConfigs.update({ ServerConfigs.donationKey eq it.id }) {
                                    it[donationKey] = null
                                }

                                it.delete()
                            }
                        }
                    }
                }
            }
        }
    }

    var changeBanner: ChangeBanner? = null
    var topDonatorsRank: TopDonatorsRank? = null
    var topVotersRank: TopVotersRank? = null
    var birthdaysRank: BirthdaysRank? = null
    var sponsorsAdvertisement: SponsorsAdvertisement? = null

    override fun onEnable() {
        val config = Constants.HOCON_MAPPER.readValue<QuirkyConfig>(File(dataFolder, "config.conf"))

        if (config.changeBanner.enabled) {
            logger.info { "Change Banner is enabled! Enabling banner stuff... :3"}
            changeBanner = ChangeBanner(this, config).apply {
                this.start()
            }
        }

        if (config.topDonatorsRank.enabled) {
            logger.info { "Top Donators Rank is enabled! Enabling top donators rank stuff... :3"}
            topDonatorsRank = TopDonatorsRank(this, config).apply {
                this.start()
            }
        }

        if (config.topVotersRank.enabled) {
            logger.info { "Top Voters Rank is enabled! Enabling top voters rank stuff... :3"}
            topVotersRank = TopVotersRank(this, config).apply {
                this.start()
            }
        }

        if (config.sponsorsAdvertisement.enabled) {
            logger.info { "Sponsors Advertisement is enabled! Enabling sponsors advertisement stuff... :3"}
            sponsorsAdvertisement = SponsorsAdvertisement(this, config).apply {
                this.start()
            }
        }

        birthdaysRank = BirthdaysRank(
                this,
                config
        ).apply {
            this.start()
        }

        registerEventListeners(
                AddReactionListener(config),
                BoostGuildListener(config),
                GetCandyListener(config),
                AddReactionForLoriBanListener(config),
                AddReactionFurryAminoPtListener(config)
        )

        registerMessageReceivedModules(
                QuirkyModule(config),
                ThankYouLoriModule(config),
                DropCandyModule(config),
                AddReactionForStaffLoriBanModule(config),
                AddReactionForHeathecliffModule()
        )

        registerCommand(LoriToolsQuirkyStuffCommand(this))
        registerCommand(SouTopDoadorCommand(config))
        registerCommand(BirthdayCommand(this))

        // ===[ HALLOWEEN 2019 ]===
        registerCommand(DocesCommand())
        registerBadge(HalloweenBadge())
        registerBadge(CanecaBadge(config))

        transaction(Databases.loritta) {
            SchemaUtils.createMissingTablesAndColumns(
                    Halloween2019Players,
                    CollectedCandies,
                    BoostedCandyChannels
            )
        }

        onGuildReady { guild, mongoServerConfig ->
            birthdaysRank?.updateBirthdayRank(guild, mongoServerConfig)
        }

        onGuildMemberJoinListeners { member, guild, mongoServerConfig ->
            val shouldBeUpdated = transaction(Databases.loritta) {
                Profile.findById(member.idLong)?.settings?.birthday != null
            }

            if (shouldBeUpdated)
                birthdaysRank?.updateBirthdayRank(guild, mongoServerConfig)
        }

        onGuildMemberLeaveListeners { member, guild, mongoServerConfig ->
            val shouldBeUpdated = transaction(Databases.loritta) {
                Profile.findById(member.idLong)?.settings?.birthday != null
            }

            if (shouldBeUpdated)
                birthdaysRank?.updateBirthdayRank(guild, mongoServerConfig)
        }
    }

    override fun onDisable() {
        super.onDisable()
        task.cancel()
        changeBanner?.task?.cancel()
        topDonatorsRank?.task?.cancel()
        topVotersRank?.task?.cancel()
        birthdaysRank?.task?.cancel()
        sponsorsAdvertisement?.task?.cancel()
    }

    companion object {
        private val logger = KotlinLogging.logger {}

        suspend fun onBoostActivate(member: Member) {
            val guild = member.guild

            logger.info { "Enabling donation features via boost for $member in $guild!" }

            val now = System.currentTimeMillis()

            transaction(Databases.loritta) {
                // Gerar pagamento
                Payment.new {
                    this.userId = member.idLong
                    this.gateway = PaymentGateway.NITRO_BOOST
                    this.reason = PaymentReason.DONATION
                    this.createdAt = now
                    this.paidAt = now
                    this.money = BigDecimal(19.99)
                    this.expiresAt = Long.MAX_VALUE // Nunca!
                    this.metadata = jsonObject(
                            "guildId" to member.guild.idLong
                    )
                }

                // Gerar key de doação
                DonationKey.new {
                    this.userId = member.idLong
                    this.value = 19.99
                    this.expiresAt = Long.MAX_VALUE // Nunca!
                    this.metadata = jsonObject(
                            "guildId" to member.guild.idLong
                    )
                }
            }

            // Fim!
            try {
                member.user.openPrivateChannel().await().sendMessage(
                        EmbedBuilder()
                                .setTitle("Obrigada por ativar o seu boost! ${Emotes.LORI_HAPPY}")
                                .setDescription(
                                        "Obrigada por ativar o seu Nitro Boost no meu servidor! ${Emotes.LORI_NITRO_BOOST}\n\nA cada dia eu estou mais próxima de virar uma digital influencer de sucesso, graças a sua ajuda! ${Emotes.LORI_HAPPY}\n\nAh, e como agradecimento por você ter ativado o seu boost no meu servidor, você irá receber todas as minhas vantagens de quem doa 19,99 reai! (Até você desativar o seu boost... espero que você não desative... ${Emotes.LORI_CRYING})\n\nContinue sendo incrível!"
                                )
                                .setImage("https://loritta.website/assets/img/fanarts/Loritta_-_Raspoza.png")
                                .setColor(Constants.LORITTA_AQUA)
                                .build()
                ).await()
            } catch (e: Exception) {}
        }

        suspend fun onBoostDeactivate(member: Member) {
            val guild = member.guild

            logger.info { "Disabling donation features via boost for $member in $guild!"}

            transaction(Databases.loritta) {
                Payment.find {
                    (Payments.userId eq member.idLong) and (Payments.gateway eq PaymentGateway.NITRO_BOOST)
                }.firstOrNull {
                    val metadata = it.metadata
                    metadata != null && metadata.obj["guildId"].nullLong == guild.idLong
                }?.delete()

                DonationKey.find {
                    (DonationKeys.userId eq member.idLong) and (DonationKeys.expiresAt eq Long.MAX_VALUE) and (DonationKeys.value eq 19.99)
                }.firstOrNull {
                    val metadata = it.metadata
                    metadata != null && metadata.obj["guildId"].nullLong == guild.idLong
                }?.apply {
                    this.expiresAt = System.currentTimeMillis() // Ou seja, a key estará expirada
                }
            }
        }
    }
}
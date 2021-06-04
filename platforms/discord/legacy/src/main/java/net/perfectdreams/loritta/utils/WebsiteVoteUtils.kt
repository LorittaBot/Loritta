package net.perfectdreams.loritta.utils

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.tables.Profiles
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.dao.BotVote
import net.perfectdreams.loritta.tables.BotVotes
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import java.util.concurrent.TimeUnit

object WebsiteVoteUtils {
	const val SONHOS_AMOUNT = 1200L

	// This is used to avoid issues with top.gg's retry vote feature
	//
	// Because if Loritta is GC'd at the same time the votes is received, the response may take more than 5s and
	// top.gg retries the vote... but Loritta did process the previous request! It just did take more time than it should...
	//
	// Before the expiration was after 1 minutes, but we had issues with top.gg retrying after 1 minute, so now it is 15 minutes
	private val TOP_GG_USER_VOTED_AT = Caffeine.newBuilder()
		.expireAfterWrite(15, TimeUnit.MINUTES)
		.build<Long, Long>()

	// Also used to avoid the issue described above
	private val voteMutex = Mutex()

	/**
	 * Adds a new vote (made by the [userId] on the [websiteSource]) to the database
	 *
	 * Also checks if the user is eligible for a key
	 *
	 * @param userId        the user that created the vote
	 * @param websiteSource where the vote originated from
	 */
	suspend fun addVote(userId: Long, websiteSource: WebsiteVoteSource) {
		voteMutex.withLock {
			// Check if the user voted in the last 60s
			// This is to avoid top.gg issues while retrying votes
			if (TOP_GG_USER_VOTED_AT.getIfPresent(userId) != null)
				return@withLock
			TOP_GG_USER_VOTED_AT.put(userId, System.currentTimeMillis())

			loritta.newSuspendedTransaction {
				BotVote.new {
					this.userId = userId
					this.websiteSource = websiteSource
					this.votedAt = System.currentTimeMillis()
				}
			}

			loritta.newSuspendedTransaction {
				Profiles.update({ Profiles.id eq userId }) {
					with(SqlExpressionBuilder) {
						it.update(money, money + SONHOS_AMOUNT)
					}
				}
			}

			loritta.newSuspendedTransaction {
				PaymentUtils.addToTransactionLogNested(
					SONHOS_AMOUNT,
					SonhosPaymentReason.DISCORD_BOTS,
					receivedBy = userId
				)
			}

			val voteCount = loritta.newSuspendedTransaction {
				BotVotes.select { BotVotes.userId eq userId }.count()
			}

			val user = lorittaShards.retrieveUserById(userId)

			if (voteCount % 60 == 0L) {
				// Can give reward!
				loritta.newSuspendedTransaction {
					DonationKey.new {
						this.userId = userId
						this.expiresAt = System.currentTimeMillis() + Constants.ONE_MONTH_IN_MILLISECONDS
						this.value = 199.99
					}
				}

				try {
					user?.openPrivateChannel()?.await()?.sendMessage(
						EmbedBuilder()
							.setColor(Constants.LORITTA_AQUA)
							.setThumbnail("https://loritta.website/assets/img/fanarts/Loritta_Presents_-_Gabizinha.png")
							.setTitle("Obrigada por votar, e aqui está um presentinho para você... \uD83D\uDC9D")
							.setDescription("Obrigada por votar em mim, cada voto me ajuda a crescer! ${Emotes.LORI_SMILE}\n\nVocê agora tem $voteCount votos e, como recompensa, você ganhou **$SONHOS_AMOUNT sonhos e uma key premium que você pode ativar nas configurações do seu servidor no meu painel**! ${Emotes.LORI_OWO}\n\nOstente as novidades, você merece por ter me ajudado tanto! ${Emotes.LORI_TEMMIE}\n\nContinue votando e sendo uma pessoa incrível! ${Emotes.LORI_HAPPY}")
							.build()
					)?.await()
				} catch (e: Exception) {
				}
			} else {
				try {
					user?.openPrivateChannel()?.await()?.sendMessage(
						EmbedBuilder()
							.setColor(Constants.LORITTA_AQUA)
							.setThumbnail("https://loritta.website/assets/img/fanarts/l7.png")
							.setTitle("Obrigada por votar! ⭐")
							.setDescription("Obrigada por votar em mim, cada voto me ajuda a crescer! ${Emotes.LORI_SMILE}\n\nVocê agora tem $voteCount votos e, como recompensa, você ganhou **$SONHOS_AMOUNT sonhos**! ${Emotes.LORI_OWO}\n\nAh, e sabia que a cada 60 votos você ganha um prêmio especial? ${Emotes.LORI_WOW}\n\nContinue votando e sendo uma pessoa incrível! ${Emotes.LORI_HAPPY}")
							.build()
					)?.await()
				} catch (e: Exception) {
				}
			}
		}
	}
}

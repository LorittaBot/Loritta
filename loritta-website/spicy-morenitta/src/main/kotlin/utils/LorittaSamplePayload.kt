package utils

class LorittaSamplePayload(
		val sponsored: Array<LorittaServerSample>,
		val partners: Array<LorittaServerSample>,
		val top: Array<LorittaServerSample>,
		val random: Array<LorittaServerSample>,
		val recentlyBumped: Array<LorittaServerSample>,
		val sponsoredCount: Long,
		val partnersCount: Long,
		val totalCount: Long
)
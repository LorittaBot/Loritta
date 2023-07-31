package net.perfectdreams.loritta.morenitta.websiteinternal.rpc

import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse

/**
 * Fails the RPC request with a [response]
 */
class RPCResponseException(val response: LorittaInternalRPCResponse) : RuntimeException()
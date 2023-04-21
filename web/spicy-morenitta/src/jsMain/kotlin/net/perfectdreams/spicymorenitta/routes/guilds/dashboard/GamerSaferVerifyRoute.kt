package net.perfectdreams.spicymorenitta.routes.guilds.dashboard

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.perfectdreams.loritta.serializable.GamerSaferVerificationUserAndRole
import net.perfectdreams.loritta.serializable.requests.PostGamerSaferVerifyConfigRequest
import net.perfectdreams.loritta.serializable.responses.DiscordAccountError
import net.perfectdreams.loritta.serializable.responses.GetGamerSaferVerifyConfigResponse
import net.perfectdreams.loritta.serializable.responses.PostGamerSaferVerifyConfigResponse
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.components.SelectMenu
import net.perfectdreams.spicymorenitta.components.SelectMenuEntry
import net.perfectdreams.spicymorenitta.routes.UpdateNavbarSizePostRender
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.launchWithLoadingScreenAndFixContent
import net.perfectdreams.spicymorenitta.utils.DashboardUtils.switchContentAndFixLeftSidebarScroll
import net.perfectdreams.spicymorenitta.utils.State
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.renderComposable
import kotlin.time.Duration.Companion.days

class GamerSaferVerifyRoute(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/guild/{guildid}/configure/gamersafer-verify") {
	override val keepLoadingScreen: Boolean
		get() = true

	// Because we are "retro-fitting" Jetpack Compose to SpicyMorenitta, we need to act like our "renderComposable" is our own tiiiny application
	var screen by mutableStateOf<GamerSaferScreen?>(null)

	override fun onRender(call: ApplicationCall) {
		val guildId = call.parameters["guildid"]!!

		m.launch {
			// Start the default screen
			val verifyScreen = GamerSaferScreen.GamerSaferVerifyScreen(this@GamerSaferVerifyRoute, guildId.toLong())
			verifyScreen.onLoad()
			m.hideLoadingScreen()
			this@GamerSaferVerifyRoute.screen = verifyScreen
		}

		launchWithLoadingScreenAndFixContent(call) {
			switchContentAndFixLeftSidebarScroll(call)

			renderComposable("gamersafer-verify-wrapper") {
				when (val screen = screen) {
					is GamerSaferScreen.GamerSaferVerifyScreen -> {
						Div {
							when (val state = screen.responseState) {
								is State.Failure -> {
									Text("Failure!")
								}
								is State.Loading -> {
									Text("Loading...")
								}
								is State.Success -> {
									when (val response = state.value) {
										is GetGamerSaferVerifyConfigResponse.Success -> {
											Div {
												Div {
													var userId by remember { mutableStateOf<String>("") }
													var roleId by remember { mutableStateOf<Long?>(null) }
													var verifyEveryX by remember { mutableStateOf<String>(1.days.toIsoString()) }

													Div {
														Text("ID do Usuário: ")
														Input(type = InputType.Text) {
															value(userId) // calling value(...) is necessary to make input "Controlled"
															onInput { event ->
																userId = event.value
															}
														}
													}

													Div {
														Text("Cargo: ")
														val entries = response.roles
															.filter {
																it.id !in screen.gamerSaferVerificationRoles.map { it.roleId }
															}
															.map {
																SelectMenuEntry(
																	{
																		Text(it.name)
																	},
																	roleId == it.id,
																	{
																		roleId = it.id
																	},
																	{}
																)
															}

														SelectMenu(entries)
													}

													Div {
														Text("Verificação a cada... ")
														val entries2 = listOf(
															SelectMenuEntry(
																{
																	Text("1 dia")
																},
																verifyEveryX == 1.days.toIsoString(),
																{
																	verifyEveryX = 1.days.toIsoString()
																},
																{}
															),
															SelectMenuEntry(
																{
																	Text("3 dias")
																},
																verifyEveryX == 3.days.toIsoString(),
																{
																	verifyEveryX = 3.days.toIsoString()
																},
																{}
															),
															SelectMenuEntry(
																{
																	Text("7 dias")
																},
																verifyEveryX == 7.days.toIsoString(),
																{
																	verifyEveryX = 7.days.toIsoString()
																},
																{}
															)
														)

														SelectMenu(entries2)

													}

													Div {
														Button(
															attrs = {
																classes("button-discord", "button-discord-info", "pure-button")

																onClick {
																	println("omg u clicked on the button!")
																	println(roleId)
																	println(verifyEveryX)

																	screen.gamerSaferVerificationRoles.add(
																		GamerSaferVerificationUserAndRole(
																			userId.toLong(),
																			roleId!!,
																			verifyEveryX
																		)
																	)
																}
															}
														) {
															Text("Adicionar")
														}
													}
												}
												Hr {}
												if (screen.gamerSaferVerificationRoles.isNotEmpty()) {
													for (role in screen.gamerSaferVerificationRoles) {
														Div {
															Text("Usuário: ")
															Text(role.userId.toString())
															Text(" - Cargo: ")
															Text(role.roleId.toString())

															Text(" ")
															Button(
																attrs = {
																	classes("button-discord", "button-discord-info", "pure-button")
																	onClick {
																		screen.gamerSaferVerificationRoles.remove(role)
																	}
																}
															) {
																Text("Remover")
															}
														}
													}
												} else {
													Div {
														Text("Nenhum cargo configurado...")
													}
												}
												Hr {}
												Button(
													attrs = {
														classes("button-discord", "button-discord-success", "pure-button")

														onClick {
															screen.launch {
																m.sendRPCRequest<PostGamerSaferVerifyConfigResponse>(
																	PostGamerSaferVerifyConfigRequest(
																		screen.guildId,
																		screen.gamerSaferVerificationRoles
																	)
																)
															}
														}
													}
												) {
													Text("Salvar")
												}
											}
										}
										is GetGamerSaferVerifyConfigResponse.Unauthorized -> {}
										is GetGamerSaferVerifyConfigResponse.UnknownGuild -> {}
										is DiscordAccountError.UserIsLorittaBanned -> {}
										is DiscordAccountError.InvalidDiscordAuthorization -> {}
									}
								}
							}
						}
					}
					null -> {}
				}
			}
		}
	}
}
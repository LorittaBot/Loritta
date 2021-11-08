package net.perfectdreams.spicymorenitta.routes

import LoriDashboard
import io.ktor.client.request.*
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.dom.clear
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h1
import kotlinx.html.hr
import kotlinx.html.i
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.js.onClickFunction
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.serialization.decodeFromString
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.BackgroundListResponse
import net.perfectdreams.spicymorenitta.SpicyMorenitta
import net.perfectdreams.spicymorenitta.application.ApplicationCall
import net.perfectdreams.spicymorenitta.http
import net.perfectdreams.spicymorenitta.utils.GoogleAdSense
import net.perfectdreams.spicymorenitta.utils.LockerUtils
import net.perfectdreams.spicymorenitta.utils.Logging
import net.perfectdreams.spicymorenitta.utils.awaitLoad
import net.perfectdreams.spicymorenitta.utils.generateAd
import net.perfectdreams.spicymorenitta.utils.select
import net.perfectdreams.spicymorenitta.utils.selectAll
import net.perfectdreams.spicymorenitta.utils.width
import org.w3c.dom.Audio
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLImageElement
import org.w3c.dom.Image
import org.w3c.dom.url.URLSearchParams
import kotlin.collections.set
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.js.Json

class Birthday2020Route(val m: SpicyMorenitta) : UpdateNavbarSizePostRender("/birthday-2020"), Logging {
	companion object {
		val pantufaRewards = listOf(
				BackgroundReward(100, "birthday2020TeamPantufa"),

				SonhosReward(200, 7_000),

				BackgroundReward(300, "birthday2020Brabas"),

				SonhosReward(400, 7_000),

				BackgroundReward(500, "birthday2020PantufaAllouette"),

				SonhosReward(600, 7_000),

				BackgroundReward(700, "birthday2020PantufaSonikaSan"),

				SonhosReward(800, 7_000),

				BackgroundReward(900, "birthday2020PantufaLaurenha"),

				SonhosReward(1_000, 7_000),

				BackgroundReward(1_100, "birthday2020PantufaDelly"),

				SonhosReward(1_200, 7_000),

				BackgroundReward(1_300, "birthday2020PantufaHugoo"),

				SonhosReward(1_400, 7_000),

				BackgroundReward(1_500, "birthday2020PantufaOusado"),

				SonhosReward(1_600, 7_000),

				BackgroundReward(1_700, "birthday2020PantufaDezato"),

				PremiumKeyReward(2_000)
		)
		val gabrielaRewards = listOf(
				BackgroundReward(100, "birthday2020TeamGabriela"),

				SonhosReward(200, 7_000),

				BackgroundReward(300, "birthday2020Brabas"),

				SonhosReward(400, 7_000),

				BackgroundReward(500, "birthday2020PantufaAllouette"),

				SonhosReward(600, 7_000),

				BackgroundReward(700, "birthday2020GabrielaCoffee"),

				SonhosReward(800, 7_000),

				BackgroundReward(900, "birthday2020GabrielaInnerDesu"),

				SonhosReward(1_000, 7_000),

				BackgroundReward(1_100, "birthday2020GabrielaStar"),

				SonhosReward(1_200, 7_000),

				BackgroundReward(1_300, "birthday2020GabrielaItsGabi"),

				SonhosReward(1_200, 7_000),

				BackgroundReward(1_500, "birthday2020GabrielaCoffee2"),

				SonhosReward(1_600, 7_000),

				BackgroundReward(1_700, "birthday2020GabrielaPinotti"),

				PremiumKeyReward(2_000)
		)
	}

	val subText: HTMLDivElement
		get() = document.select("#sub-text")
	val whoDat: HTMLImageElement
		get() = document.select("#who-dat")
	val animationOverlay: HTMLDivElement
		get() = document.select("#animation-overlay")
	val preAnimationOverlay: HTMLDivElement
		get() = document.select("#pre-animation-overlay")

	override fun onRender(call: ApplicationCall) {
		super.onRender(call)

		val searchParams = URLSearchParams(window.location.search)
		val skipIntro = searchParams.get("skipIntro")
		val forceIntro = searchParams.get("forceIntro")

		m.launch {
			val backgroundsJob = m.async {
				val result = http.get<String> {
					url("${window.location.origin}/api/v1/loritta/backgrounds")
				}
				kotlinx.serialization.json.JSON.nonstrict.decodeFromString<BackgroundListResponse>(result)
			}

			val profileWrapperJob = m.async {
				val profileWrapper = Image()
				debug("Awaiting load...")
				profileWrapper.awaitLoad("${window.location.origin}/api/v1/users/@me/profile")
				debug("Load complete!")
				profileWrapper
			}

			val teamStatsJob = m.async {
				val result = http.get<String> {
					url("${window.location.origin}/api/v1/birthday-2020/team")
				}
				JSON.parse<Json>(result)
			}

			val teamStats = teamStatsJob.await()
			val isActive = teamStats["isActive"] as Boolean
			val currentTeam = teamStats["team"] as String?

			if ((skipIntro == null && forceIntro != null) || currentTeam == null) {
				// Para que toque um som, vamos fazer que o usuário tenha que clicar na página
				// Se não, pode falhar a reprodução
				document.body!!.append {
					div {
						id = "pre-animation-overlay"
						style = "position: fixed;background-color: black;width: 100vw;height: 100vh;display:flex;align-items:center;justify-content:center;"

						div {
							div("add-me button pink shadow big") {
								id = "play-intro-button"
								+"Clique para tocar a introdução"

								onClickFunction = {
									m.launch {
										preAnimationOverlay.clear()
										playIntro().await() // Esperar a intro carregar para depois continuar

										animationOverlay.remove()

										val backgroundsResponse = backgroundsJob.await()
										val profileWrapper = profileWrapperJob.await()

										createBody(backgroundsResponse.dreamStorageServiceUrl, backgroundsResponse.namespace, backgroundsResponse.backgrounds, profileWrapper, currentTeam != null)
										fillBodyStuff(backgroundsResponse.dreamStorageServiceUrl, backgroundsResponse.namespace, backgroundsResponse.backgrounds, profileWrapper)

										val flashOverlay = document.select<HTMLImageElement>("#flash-overlay")

										repeat(100) {
											flashOverlay.style.opacity = "${1 - ((it + 1).toDouble() / 100)}"
											delay(10)
										}

										flashOverlay.remove()
									}
								}
							}
							div("add-me button pink shadow big") {
								id = "skip-intro-button"
								+"Pular introdução"

								onClickFunction = {
									m.launch {
										document.select<HTMLDivElement>("#pre-animation-overlay").remove()

										val backgroundsResponse = backgroundsJob.await()
										val profileWrapper = profileWrapperJob.await()

										createBody(backgroundsResponse.dreamStorageServiceUrl, backgroundsResponse.namespace, backgroundsResponse.backgrounds, profileWrapper, currentTeam != null)
										fillBodyStuff(backgroundsResponse.dreamStorageServiceUrl, backgroundsResponse.namespace, backgroundsResponse.backgrounds, profileWrapper)
									}
								}
							}
						}
					}
				}
			} else {
				val backgroundsResponse = backgroundsJob.await()
				val profileWrapper = profileWrapperJob.await()

				createBody(backgroundsResponse.dreamStorageServiceUrl, backgroundsResponse.namespace, backgroundsResponse.backgrounds, profileWrapper, currentTeam != null)
				fillBodyStuff(backgroundsResponse.dreamStorageServiceUrl, backgroundsResponse.namespace, backgroundsResponse.backgrounds, profileWrapper)
			}
		}
	}

	suspend fun fillBodyStuff(backgroundsUrl: String, namespace: String, backgrounds: List<Background>, profileWrapper: Image) {
		val backgroundsToBeFilled = document.selectAll<HTMLDivElement>(".loritta-background-fill")

		backgroundsToBeFilled.forEach {
			val backgroundInternalName = it.getAttribute("data-background-name")

			it.append {
				div(classes = "canvas-preview-wrapper") {
					val needsToRotateTheOppositeWay = it.hasAttribute("data-background-revert")
					val rotateStr = if (needsToRotateTheOppositeWay)
						"transform: rotateY(10deg);"
					else
						""
					canvas("canvas-preview-only-bg") {
						style = """width: 350px; $rotateStr margin-top: 20px;"""
						width = "800"
						height = "600"
					}

					canvas("canvas-preview") {
						style = """width: 350px; $rotateStr margin-top: 20px;"""
						width = "800"
						height = "600"
					}
				}
			}

			m.launch {
				val canvasPreview = it.select<HTMLCanvasElement>(".canvas-preview")
				val canvasPreviewOnlyBg = it.select<HTMLCanvasElement>(".canvas-preview-only-bg")

				val background = backgrounds.firstOrNull { it.internalName == backgroundInternalName }
				if (background == null) {
					warn("There isn't a background called $backgroundInternalName")
					return@launch
				}

				val backgroundImg = Image()
				backgroundImg.awaitLoad(LockerUtils.getBackgroundUrl(backgroundsUrl, namespace, background))

				val canvasPreviewOnlyBgContext = (canvasPreviewOnlyBg.getContext("2d")!! as CanvasRenderingContext2D)
				canvasPreviewOnlyBgContext
						.drawImage(
								backgroundImg,
								(background.crop?.offsetX ?: 0).toDouble(),
								(background.crop?.offsetY ?: 0).toDouble(),
								(background.crop?.width ?: backgroundImg.width).toDouble(),
								(background.crop?.height ?: backgroundImg.height).toDouble(),
								0.0,
								0.0,
								800.0,
								600.0
						)

				val canvasPreviewContext = (canvasPreview.getContext("2d")!! as CanvasRenderingContext2D)
				canvasPreviewContext
						.drawImage(
								backgroundImg,
								(background.crop?.offsetX ?: 0).toDouble(),
								(background.crop?.offsetY ?: 0).toDouble(),
								(background.crop?.width ?: backgroundImg.width).toDouble(),
								(background.crop?.height ?: backgroundImg.height).toDouble(),
								0.0,
								0.0,
								800.0,
								600.0
						)

				canvasPreviewContext.drawImage(profileWrapper, 0.0, 0.0)
			}
		}

		GoogleAdSense.renderAds()
	}

	suspend fun Audio.awaitLoad() {
		return kotlin.coroutines.suspendCoroutine { cont ->
			this.addEventListener("canplaythrough", {
				cont.resume(Unit)
			})
			this.onerror = { b: dynamic, s: String, i: Int, i1: Int, any: Any? ->
				cont.resumeWithException(Exception())
			}
		}
	}

	suspend fun playIntro(): Deferred<Unit> {
		preAnimationOverlay.append {
			div {
				style = "color: white; font-size: 2em;"
				+ "Carregando..."
			}
		}

		val imagePreloadJobs = arrayOf(
				"/assets/img/birthday2020/pantufa_braba.png",
				"/assets/img/birthday2020/gabriela_braba.png",
				"/assets/img/birthday2020/pantufa.png",
				"/assets/img/birthday2020/gabriela.png",
				"/assets/img/birthday2020/pantufa_hero.png",
				"/assets/img/birthday2020/gabriela_hero.png"
		).map {
			m.async {
				Image().awaitLoad(it)
			}
		}

		val pantufaVoiceJob = m.async {
			Audio("/assets/snd/birthday_pantufa.ogg")
					.also { it.awaitLoad() }
		}
		val gabrielaVoiceJob = m.async {
			Audio("/assets/snd/birthday_gabriela.ogg")
					.also { it.awaitLoad() }
		}
		val watchOutJob = m.async {
			Audio("/assets/snd/snd_b.wav")
					.also { it.awaitLoad() }
		}
		val damageJob = m.async {
			Audio("/assets/snd/snd_damage.wav")
					.also { it.awaitLoad() }
		}
		val hyperGonerChargeJob = m.async {
			Audio("/assets/snd/mus_sfx_hypergoner_charge.ogg")
					.also { it.awaitLoad() }
		}

		val imagePreload = imagePreloadJobs.awaitAll()
		val pantufaVoice = pantufaVoiceJob.await()
		val gabrielaVoice = gabrielaVoiceJob.await()
		val watchOut = watchOutJob.await()
		val damage = damageJob.await()
		val hyperGonerCharge = hyperGonerChargeJob.await()

		document.body!!.append {
			div {
				id = "animation-overlay"
				style = "position: fixed;background-color: black;width: 100vw;height: 100vh;"

				img(src = "/assets/img/birthday2020/pantufa_braba.png") {
					id = "pantufa-braba"
					style = "position: absolute; left: 0px; height: 100vh;opacity:0;z-index:2;"
				}
				img(src = "/assets/img/birthday2020/gabriela_braba.png") {
					id = "gabriela-braba"
					style = "position: absolute; right: 0px; height: 100vh;opacity:0;z-index:1;"
				}

				div {
					style = "color: yellow;position: fixed;bottom: 0.25em;font-weight: 1000;font-size: 4em;text-align: center;"

					img(src = "/assets/img/birthday2020/pantufa.png") {
						id = "who-dat"
						style = "max-height: 60vh;"
					}

					div {
						id = "sub-text"
						+"Tanto faz, eu vou fazer o melhor aniversário para a Loritta"
					}
				}
			}
		}

		preAnimationOverlay.remove()

		return m.async {
			pantufaVoice.play()
			delay(3_600)

			subText.innerText = "Não, EU que vou fazer o melhor aniversário para a Loritta"
			whoDat.src = "/assets/img/birthday2020/gabriela.png"
			gabrielaVoice.play()
			delay(3_800)
			watchOut.play()
			delay(100)
			val width = document.body!!.scrollWidth
			val middle = width / 2

			val pantufaBraba = document.select<HTMLImageElement>("#pantufa-braba")
			val gabrielaBraba = document.select<HTMLImageElement>("#gabriela-braba")

			pantufaBraba.style.opacity = "1"
			gabrielaBraba.style.opacity = "1"

			val pantufaJob = m.async {
				val middlePosition = (middle - pantufaBraba.width()).toInt()

				for (i in (pantufaBraba.width() * -1).toInt() until middlePosition step 8) {
					pantufaBraba.style.left = "${i}px"
					delay(1)
				}
				pantufaBraba.style.left = "${middlePosition}px"
			}

			val gabrielaJob = m.async {
				val middlePosition = (middle - gabrielaBraba.width()).toInt()
				for (i in (gabrielaBraba.width() * -1).toInt() until middlePosition step 8) {
					gabrielaBraba.style.right = "${i}px"
					delay(1)
				}
				gabrielaBraba.style.right = "${middlePosition}px"
			}

			pantufaJob.await()
			gabrielaJob.await()

			pantufaBraba.style.animation = "0.5s linear infinite forwards shake-animation"
			gabrielaBraba.style.animation = "0.5s linear infinite forwards shake-animation"

			damage.play()
			hyperGonerCharge.play()
			hyperGonerCharge.volume = 0.6

			delay(9_000)

			document.body!!.append {
				div {
					id = "flash-overlay"
					style = "position: fixed;background-color: white;width: 100vw;height: 100vh;z-index: 1;opacity:0;"
				}
			}

			val flashOverlay = document.select<HTMLImageElement>("#flash-overlay")

			repeat(100) {
				flashOverlay.style.opacity = "${(it + 1).toDouble() / 100}"
				delay(10)
			}

			delay(1_000)

			return@async
		}
	}

	suspend fun createBody(backgroundUrl: String, namespace: String, backgrounds: List<Background>, profileWrapper: Image, isAlreadyOnATeam: Boolean) {
		document.select<HTMLDivElement>("#birthday-2020").append {
			div {
				style = "height: calc(100vh - 46px); display: flex; position: relative;"

				div {
					style = "position: absolute;top: 0.25em;z-index:1;font-weight: 1000;color: white;font-size: 3em;transform: translate(-50%, 0);margin-left: 50%;text-shadow: 0px 0px 10px black;animation: swing 10s 10s;"

					+"Qual lado você irá escolher?"
				}

				div {
					style = "position: relative; overflow: hidden; text-align: center; width: 50vw; height: 100%; background: url(\"/v2/assets/img/website_bg.png\"), #40e0d0; animation: background-scroll 60s linear infinite; position: relative;"

					div {
						style = "background: linear-gradient(to bottom, #0000, #39c9ba), linear-gradient(to right, #0000001a, #0000, #0000001a); height: 100%;"

						img(src = "/assets/img/birthday2020/pantufa_hero.png") {
							style = "height: 100%;"
						}

						div {
							style = "font-family: Pacifico; bottom: 0; position: absolute; color: white; text-align: center; left: 50%; transform: translate(-50%, 0); text-shadow: 0px 0px 10px black; font-size: 4em;"
							+"Pantufa"
						}
					}
					/* h1 {
						style = "color: white;"
						+ "Pantufa"
					} */
				}

				div {
					style = "overflow: hidden; text-align: center; width: 50vw; height: 100%; background: url(\"/v2/assets/img/website_bg.png\"), #b448c8; animation: background-scroll 60s linear infinite reverse; position: relative;"

					div {
						style = "background: linear-gradient(to bottom, #0000, #a240b4), linear-gradient(to right, #0000001a, #0000, #0000001a); height: 100%;"
						img(src = "/assets/img/birthday2020/gabriela_hero.png") {
							style = "height: 100%;"
						}

						div {
							style = "font-family: Pacifico; bottom: 0; position: absolute; color: white; text-align: center; left: 50%; transform: translate(-50%, 0); text-shadow: 0px 0px 10px black; font-size: 4em;"
							+"Gabriela"
						}
					}
					/* h1 {
						style = "color: white;"
						+ "Gabriela"
					} */
				}
			}

			div {
				style = "display: flex;"

				div {
					id = "team-pantufa-rewards"
					style = "width: 50vw; height: 100%; border-right: 20px solid #39c9ba; border-bottom: 20px solid #39c9ba; border-left: 20px solid #39c9ba; box-sizing: border-box; background-color: #33b6a9; padding: 10px;"

					h1 {
						style = "text-align: center; font-weight: 1000; color: white;"
						+"Prêmios da "
						span("has-rainbow-text") {
							style = "text-shadow: 0px 0px 1px #ffffff7d;"
							+"#TeamPantufa"
						}
					}

					for (reward in pantufaRewards) {
						div {
							style = "display: flex; flex-direction: column;"

							div {
								style = "display: flex;"
								if (reward is BackgroundReward) {
									div {
										i(classes = "fas fa-images")

										style = "font-size: 2.5em; color: #0003;"
									}
									div {
										style = "display: flex; color: white; flex-direction: column; margin-left: 20px; margin-right: 20px;"

										div {
											style = "font-size: 1.5em;"
											+"Ao coletar ${reward.requiredPoints} presentes..."
										}
										div {
											style = "font-size: 2em;"
											div(classes = "loritta-background-fill") {
												attributes["data-background-name"] = reward.internalName
												attributes["data-background-revert"] = "true"
											}
										}
									}
								} else if (reward is SonhosReward) {
									div {
										i(classes = "fas fa-money-bill-wave")

										style = "font-size: 2.5em; color: #0003;"
									}
									div {
										style = "display: flex; color: white; flex-direction: column; margin-left: 20px; margin-right: 20px;"

										div {
											style = "font-size: 1.5em;"
											+"Ao coletar ${reward.requiredPoints} presentes..."
										}
										div {
											style = "font-size: 2em;"
											+"Você irá ganhar ${reward.sonhosReward} sonhos!"
										}
									}
								} else if (reward is PremiumKeyReward) {
									div {
										i(classes = "fas fa-star")

										style = "font-size: 2.5em; color: #0003;"
									}
									div {
										style = "display: flex; color: white; flex-direction: column; margin-left: 20px; margin-right: 20px;"

										div {
											style = "font-size: 1.5em;"
											+"Ao coletar ${reward.requiredPoints} presentes..."
										}
										div {
											style = "font-size: 2em;"
											+"Você irá ganhar uma key premium da Loritta que dura 30 dias com todas as vantagens para servidores disponíveis!"
										}
									}
								}
							}
						}
						hr {}
					}
				}

				div {
					id = "team-gabriela-rewards"
					style = "width: 50vw; height: 100%; border-left: 20px solid #a240b4; border-right: 20px solid #a240b4; border-bottom: 20px solid #a240b4; box-sizing: border-box; background-color: #913ca1; padding: 10px;"

					h1 {
						style = "text-align: center; font-weight: 1000; color: white;"
						+"Prêmios da "
						span("has-rainbow-text") {
							style = "text-shadow: 0px 0px 1px #ffffff7d;"
							+"#TeamGabriela"
						}
					}

					for (reward in gabrielaRewards) {
						div {
							style = "display: flex; flex-direction: column; text-align: right; flex-direction: row-reverse;"

							div {
								style = "display: flex; flex-direction: row-reverse;"
								if (reward is BackgroundReward) {
									div {
										i(classes = "fas fa-images")

										style = "font-size: 2.5em; color: #0003;"
									}
									div {
										style = "display: flex; color: white; flex-direction: column; margin-left: 20px; margin-right: 20px;"

										div {
											style = "font-size: 1.5em;"
											+"Ao coletar ${reward.requiredPoints} presentes..."
										}
										div {
											style = "font-size: 2em;"
											div(classes = "loritta-background-fill") {
												attributes["data-background-name"] = reward.internalName
											}
										}
									}
								} else if (reward is SonhosReward) {
									div {
										i(classes = "fas fa-money-bill-wave")

										style = "font-size: 2.5em; color: #0003;"
									}
									div {
										style = "display: flex; color: white; flex-direction: column; margin-left: 20px; margin-right: 20px;"

										div {
											style = "font-size: 1.5em;"
											+"Ao coletar ${reward.requiredPoints} presentes..."
										}
										div {
											style = "font-size: 2em;"
											+"Você irá ganhar ${reward.sonhosReward} sonhos!"
										}
									}
								} else if (reward is PremiumKeyReward) {
									div {
										i(classes = "fas fa-star")

										style = "font-size: 2.5em; color: #0003;"
									}
									div {
										style = "display: flex; color: white; flex-direction: column; margin-left: 20px; margin-right: 20px;"

										div {
											style = "font-size: 1.5em;"
											+"Ao coletar ${reward.requiredPoints} presentes..."
										}
										div {
											style = "font-size: 2em;"
											+"Você irá ganhar uma key premium da Loritta que dura 30 dias com todas as vantagens para servidores disponíveis!"
										}
									}
								}
							}
						}
						hr {}
					}
				}
			}

			div {
				div {
					style = "background: linear-gradient(90deg, #39C9BA 50%, #A240B4 50%);color: white;font-weight: 1000;font-size: 3em;text-align: center;"

					div {
						style = "text-align: center;margin: auto;"
						+ "10.000.000 sonhos serão distribuidos os membros da a equipe vencedora! (apenas para quem REALMENTE participou do evento ;))"
					}
				}
			}

			if (isAlreadyOnATeam) {
				div {
					style = "background: linear-gradient(90deg, #39C9BA 50%, #A240B4 50%);color: white;font-weight: 1000;font-size: 3em;text-align: center;"

					div("add-me button pink shadow big") {
						style = "display: inline-block; animation: 7s linear infinite forwards swing-button; cursor: pointer;"

						+"Clique aqui para ver quantos presentes você já coletou!"

						onClickFunction = {
							window.location.replace("/birthday-2020/stats")
						}
					}
				}
			} else {
				div {
					style = "background: linear-gradient(90deg, #39C9BA 50%, #A240B4 50%);color: white;font-weight: 1000;font-size: 3em;text-align: center;"

					div {
						generateAd("8895970055", "Loritta Birthday 2020 Team Select", insertAdsByGoogleScript = false)
					}

					div {
						style = "text-align: center;margin: auto;"
						+"E aí, qual time você irá escolher?"
					}
				}

				div {
					style = "display: flex;"

					div {
						style = "width: 50vw; height: 100%; border-right: 20px solid #39c9ba; border-bottom: 20px solid #39c9ba; border-left: 20px solid #39c9ba; box-sizing: border-box; background-color: #39c9ba; padding: 10px; text-align: center;"

						div("add-me button pink shadow big") {
							style = "display: inline-block; animation: 7s linear infinite forwards swing-button; cursor: pointer;"

							+"Entrar no Time da Pantufa #TeamPantufa"

							onClickFunction = {
								selectTeam("PANTUFA")
							}
						}
					}

					div {
						style = "width: 50vw; height: 100%; border-left: 20px solid #a240b4; border-right: 20px solid #a240b4; border-bottom: 20px solid #a240b4; box-sizing: border-box; background-color: #a240b4; padding: 10px; text-align: center;"

						div("add-me button pink shadow big") {
							style = "display: inline-block; animation: 7s linear infinite forwards swing-button; cursor: pointer;"

							+"Entrar no Time da Gabriela #TeamGabriela"

							onClickFunction = {
								selectTeam("GABRIELA")
							}
						}
					}
				}

				div {
					style = "background: linear-gradient(90deg, #39C9BA 50%, #A240B4 50%);color: white;font-weight: 500;font-size: 3em;text-align: center;"
					div {
						style = "text-align: center;margin: auto;"
						+ "Mas lembre-se, após escolher você não poderá trocar de time!"
					}
					generateAd("8895970055", "Loritta Birthday 2020 Team Select", insertAdsByGoogleScript = false)
				}
			}

			div {
				style = "display: flex;"
				div {
					style = "width: 100vw; text-align: center;border-right: 20px solid #29a6fe;border-bottom: 20px solid #29a6fe;border-left: 20px solid #29a6fe;box-sizing: border-box;background-color: #1e8fdf;padding: 10px;border-top: 20px solid #29a6fe;font-weight: 1000;"

					img(src = "/assets/img/birthday2020/lori_ameno.png") {
						width = "150"
					}

					div {
						style = "font-size: 2em;color: white;"
						+"Não seria mais fácil vocês apenas trabalharem... juntas?"
					}
				}
			}
		}
	}

	fun selectTeam(team: String) {
		val teamStatsJob = m.launch {
			m.showLoadingScreen()
			val result = http.post<String> {
				url("${window.location.origin}/api/v1/birthday-2020/team")

				body = JSON.stringify(
						object {
							val team = team
						}
				)
			}
			JSON.parse<Json>(result)
			LoriDashboard.configSavedSfx.play()
			delay(1_000)
			window.location.replace("/birthday-2020/stats")
		}
	}

	open class Reward(val requiredPoints: Int)
	class BackgroundReward(requiredPoints: Int, val internalName: String) : Reward(requiredPoints)
	class SonhosReward(requiredPoints: Int, val sonhosReward: Int) : Reward(requiredPoints)
	class PremiumKeyReward(requiredPoints: Int) : Reward(requiredPoints)
}
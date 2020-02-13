package com.mrpowergamerbr.loritta.audio

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import mu.KotlinLogging
import net.dv8tion.jda.api.audio.AudioReceiveHandler
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.audio.CombinedAudio
import net.dv8tion.jda.api.audio.UserAudio
import net.dv8tion.jda.api.entities.User
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread


class AudioRecorderHandler(val options: AudioRecorder.RecordingOptions) : AudioSendHandler, AudioReceiveHandler {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	fun showProcessOutputOnConsole(process: Process) {
		thread {
			val bufR = process.inputStream.bufferedReader()

			while (true) {
				val line = bufR.readLine() ?: break

				logger.info(line)
			}
		}
	}

	/*
		All methods in this class are called by JDA threads when resources are available/ready for processing.
		The receiver will be provided with the latest 20ms of PCM stereo audio
		Note you can receive even while setting yourself to deafened!
		The sender will provide 20ms of PCM stereo audio (pass-through) once requested by JDA
		When audio is provided JDA will automatically set the bot to speaking!
	 */
	val process by lazy {
		ProcessBuilder(
				"/usr/bin/ffmpeg",
				"-f",
				"s16be",
				"-ar",
				"48.0k",
				"-ac",
				"2",
				"-i",
				"pipe:0",
				"/home/audio_test/stream-${System.currentTimeMillis()}.ogg"
		).redirectErrorStream(true)
				.start()
				.apply {
					showProcessOutputOnConsole(this)
				}
	}

	val processes = mutableMapOf<User, Process>()

	fun getOrCreateProcessForUser(user: User): Process {
		return processes.getOrPut(user) {
			val process = ProcessBuilder(
					"/usr/bin/ffmpeg",
					"-f",
					"s16be",
					"-ar",
					"48.0k",
					"-ac",
					"2",
					"-i",
					"pipe:0",
					"/home/audio_test/stream-user-${user.idLong}-${System.currentTimeMillis()}.ogg"
			).redirectErrorStream(true)
					.start()
					.apply {
						showProcessOutputOnConsole(this)
					}
			process
		}
	}

	var currentUsersTalking = listOf<User>()
	var startedAt = -1L // Começa a gravar quando começar a receber dados do client
	val dataTracker = jsonArray()

	/* Receive Handling */
	// combine multiple user audio-streams into a single one
	override fun canReceiveCombined() = options.recordSingleTrack
	override fun canReceiveUser() = options.recordMultiTrack

	val userAudioDataQueue = mutableMapOf<User, Queue<ByteArray>>()

	var isRunning = false
	var multiTrackTimer: Timer? = null

	fun startListeningTaskIfNeeded() {
		if (isRunning)
			return

		isRunning = true
		val timer = Timer()
		timer.scheduleAtFixedRate(
				object : TimerTask() {
					override fun run() {
						// poll the queue
						for ((user, queue) in userAudioDataQueue) {
							val process = getOrCreateProcessForUser(user)

							if (queue.isEmpty()) {
								// write empty data
								process.outputStream.write(ByteArray(3840))
							} else {
								// write queue
								while (queue.isNotEmpty()) {
									process.outputStream.write(queue.poll())
								}
							}

							process.outputStream.flush()
						}
					}
				},
				0L,
				20L
		)
		multiTrackTimer = timer
	}

	override fun handleUserAudio(userAudio: UserAudio) {
		startListeningTaskIfNeeded()

		if (startedAt == -1L)
			startedAt = System.currentTimeMillis()

		val queue = userAudioDataQueue.getOrPut(userAudio.user) { ConcurrentLinkedQueue() }

		val data = userAudio.getAudioData(1.0) // volume at 100% = 1.0 (50% = 0.5 / 55% = 0.55)
		queue.offer(data)
	}

	override fun handleCombinedAudio(combinedAudio: CombinedAudio) { // we only want to send data when a user actually sent something, otherwise we would just send silence
		if (startedAt == -1L)
			startedAt = System.currentTimeMillis()

		val diffSinceStart = System.currentTimeMillis() - startedAt

		val newlyUsersTalking = combinedAudio.users.filter { !currentUsersTalking.contains(it) }

		newlyUsersTalking.forEach {
			dataTracker.add(
					jsonObject(
							"id" to it.idLong,
							"state" to "up",
							"at" to diffSinceStart
					)
			)
		}

		val wasTalkingButNotAnymore = currentUsersTalking.filter { !combinedAudio.users.contains(it) }

		wasTalkingButNotAnymore.forEach {
			dataTracker.add(
					jsonObject(
							"id" to it.idLong,
							"state" to "down",
							"at" to diffSinceStart
					)
			)
		}

		currentUsersTalking = combinedAudio.users

		val data = combinedAudio.getAudioData(1.0) // volume at 100% = 1.0 (50% = 0.5 / 55% = 0.55)
		process.outputStream.write(data)
		process.outputStream.flush()
	}

	// Partes para enviar áudio
	override fun canProvide(): Boolean { // If we have something in our buffer we can provide it to the send system
		return true
	}

	override fun provide20MsAudio(): ByteBuffer? {
		// Iremos criar um buffer vazio para o JDA enviar, isto é necessário porque se você não enviar algo, você não
		// irá receber dados do Discord. (bug do dDiscord) porque eles não sportam voice chat de voz))
		return ByteBuffer.wrap(ByteArray(3840)) // Wrap this in a java.nio.ByteBuffer
	}

	override fun isOpus(): Boolean { // since we send audio that is received from discord we don't have opus but PCM
		return false
	}

	fun stop() {
		processes.values.onEach { it.outputStream.close() }
		if (options.recordSingleTrack) // Vamos verificar antes de usar a variável process, já que ela é lazy
			process.outputStream.close()
		/* outputStream.close()
		File("/home/audio_test/data.json")
				.writeText(dataTracker.toString()) */
	}
}
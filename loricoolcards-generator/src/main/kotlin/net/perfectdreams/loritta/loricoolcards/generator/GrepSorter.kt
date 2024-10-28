package net.perfectdreams.loritta.loricoolcards.generator

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class LogEntry(val timestamp: LocalDateTime, val content: String)

fun parseTimestamp(logEntry: String): LocalDateTime {
    // Extract the timestamp part from the log entry using regex or string manipulation
    val timestampRegex = Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2},\d{3}""")
    val timestampString = timestampRegex.find(logEntry)?.value
        ?: throw IllegalArgumentException("No valid timestamp found in the log entry.")

    // Define the formatter to match the log's timestamp format
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")

    // Parse the timestamp string to LocalDateTime
    return LocalDateTime.parse(timestampString, formatter)
}

fun processLogFile(file: File): List<LogEntry> {
    // Read the entire file content
    val content = file.readText()

    // Split log entries by "--"
    val logEntries = content.split("--").filter { it.isNotBlank() }

    // Map log entries to LogEntry objects, parsing the timestamp
    return logEntries.map { logEntry ->
        LogEntry(timestamp = parseTimestamp(logEntry), content = logEntry.trim())
    }.sortedBy { it.timestamp }
}

fun main() {
    // List of files to process
    val logFiles = listOf(
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-1.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-2.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-3.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-4.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-5.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-6.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-7.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-8.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-9.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-10.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-11.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-12.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-13.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-14.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-15.log"),
        File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\webhooks-16.log"),
    )

    // Process each file and sort the entries by timestamp
    val outputLogFile = File("C:\\Users\\leona\\Documents\\LorittaInteractionIssues\\output.log")
    val sortedEntries = mutableListOf<LogEntry>()
    for (logFile in logFiles) {
        println("Processing file: ${logFile.name}")

        // Get sorted log entries
        try {
            sortedEntries.addAll(processLogFile(logFile))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    sortedEntries.groupBy { it.timestamp.hour.toString().padStart(2, '0') + ":" + it.timestamp.minute.toString().padStart(2, '0') }
        .toList()
        .sortedBy { it.first }
        .forEach { (t, u) ->
            println("$t: ${u.size}")
        }


    /* sortedEntries.filter { it.content.contains("COMBO") }.groupBy { it.timestamp.hour.toString().padStart(2, '0') + ":" + it.timestamp.minute.toString().padStart(2, '0') }
        .toList()
        .sortedBy { it.first }
        .forEach { (t, u) ->
            println("$t: ${u.size}")
        } */

    // Print sorted log entries
    for (entry in sortedEntries.sortedBy { it.timestamp }) {
        // outputLogFile.appendText("--\n${entry.content}\n--")
    }
}
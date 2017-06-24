package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.reminders.Reminder
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.regex.Pattern


class LembrarCommand : CommandBase() {
	override fun getLabel(): String {
		return "lembrar"
	}

	override fun getAliases(): List<String> {
		return listOf("lembre", "remind", "remindme");
	}

	override fun getDescription(): String {
		return "Precisa lembrar de dar comida para o dog? Talvez você queira marcar um lembrete para que no futuro você possa ver se você conseguir fazer todos os seus \"Life Goals\" deste ano? Então crie um lembrete!"
	}

	override fun getExample(): List<String> {
		return listOf("1 minuto dar comida para o dog");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.MISC;
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			var message = context.args.joinToString(separator = " ");
			var years = 0L;
			var months = 0L;
			var weeks = 0L;
			var days = 0L;
			var hours = 0L;
			var minutes = 0L;
			var seconds = 0L;
			var instant = LocalDateTime.now();
			var zoneId = ZoneId.systemDefault()
			// Vamos usar RegEx para detectar!
			var yearsPattern = Pattern.compile("(?i)([0-9]+) ano(s)?");
			var yearsMatcher = yearsPattern.matcher(context.message.content);
			var monthsPattern = Pattern.compile("(?i)([0-9]+) m(e|ê)(s|es)?");
			var monthsMatcher = monthsPattern.matcher(context.message.content);
			var weeksPattern = Pattern.compile("(?i)([0-9]+) semana(s)?");
			var weeksMatcher = weeksPattern.matcher(context.message.content);
			var daysPattern = Pattern.compile("(?i)([0-9]+) dia(s)?");
			var daysMatcher = daysPattern.matcher(context.message.content);
			var hoursPattern = Pattern.compile("(?i)([0-9]+) hora(s)?");
			var hoursMatcher = hoursPattern.matcher(context.message.content);
			var minutesPattern = Pattern.compile("(?i)([0-9]+) minuto(s)?");
			var minutesMatcher = minutesPattern.matcher(context.message.content);
			var secondsPattern = Pattern.compile("(?i)([0-9]+) segundo(s)?");
			var secondsMatcher = secondsPattern.matcher(context.message.content);
			if (yearsMatcher.find()) {
				var group = yearsMatcher.group(1);
				years = group.toLong();
				message = message.replace(yearsMatcher.group(), "");
			}
			if (monthsMatcher.find()) {
				var group = monthsMatcher.group(1);
				months = group.toLong();
				message = message.replace(monthsMatcher.group(), "");
			}
			if (weeksMatcher.find()) {
				var group = weeksMatcher.group(1);
				weeks = group.toLong();
				message = message.replace(weeksMatcher.group(), "");
			}
			if (daysMatcher.find()) {
				var group = daysMatcher.group(1);
				days = group.toLong();
				message = message.replace(daysMatcher.group(), "");
			}
			if (hoursMatcher.find()) {
				var group = hoursMatcher.group(1);
				hours = group.toLong();
				message = message.replace(hoursMatcher.group(), "");
			}
			if (minutesMatcher.find()) {
				var group = minutesMatcher.group(1);
				minutes = group.toLong();
				message = message.replace(minutesMatcher.group(), "");
			}
			if (secondsMatcher.find()) {
				var group = secondsMatcher.group(1);
				seconds = group.toLong();
				message = message.replace(secondsMatcher.group(), "");
			}

			// Agora vamos somar!
			instant = instant.plusYears(years);
			instant = instant.plusMonths(months);
			instant = instant.plusWeeks(weeks);
			instant = instant.plusDays(days);
			instant = instant.plusHours(hours);
			instant = instant.plusMinutes(minutes);
			instant = instant.plusSeconds(seconds);

			// E agora em millis
			var inMillis = instant.atZone(zoneId).toInstant().toEpochMilli();

			// Criar o Lembrete
			var reminder = Reminder(context.guild.id, context.message.textChannel.id, inMillis, message.trim());
			var profile = context.lorittaUser.profile

			profile.reminders.add(reminder);

			LorittaLauncher.getInstance().ds.save(profile)

			context.sendMessage(context.getAsMention(true) + "Eu irei te lembrar deste lembrete em ${instant.dayOfMonth}/${instant.month}/${instant.year} as ${instant.hour}:${instant.minute}!")
		} else {
			this.explain(context);
		}
	}
}
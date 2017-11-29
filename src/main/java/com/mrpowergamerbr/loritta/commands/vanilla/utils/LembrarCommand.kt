package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.msgFormat
import com.mrpowergamerbr.loritta.utils.reminders.Reminder
import com.mrpowergamerbr.loritta.utils.save
import java.text.DateFormatSymbols
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.regex.Pattern


class LembrarCommand : CommandBase("lembrar") {
	override fun getAliases(): List<String> {
		return listOf("lembre", "remind", "remindme");
	}

	override fun getUsage(): String {
		return "tempo mensagem"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.LEMBRAR_DESCRIPTION
	}

	override fun getExample(): List<String> {
		return listOf("1 minuto dar comida para o dog");
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.UTILS;
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			var message = context.strippedArgs.joinToString(separator = " ");
			var years: Long? = 0L;
			var months: Long? = 0L;
			var weeks: Long? = 0L;
			var days: Long? = 0L;
			var hours: Long? = 0L;
			var minutes: Long? = 0L;
			var seconds: Long? = 0L;
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
				years = group.toLongOrNull();
				message = message.replace(yearsMatcher.group(), "");
			}
			if (monthsMatcher.find()) {
				var group = monthsMatcher.group(1);
				months = group.toLongOrNull();
				message = message.replace(monthsMatcher.group(), "");
			}
			if (weeksMatcher.find()) {
				var group = weeksMatcher.group(1);
				weeks = group.toLongOrNull();
				message = message.replace(weeksMatcher.group(), "");
			}
			if (daysMatcher.find()) {
				var group = daysMatcher.group(1);
				days = group.toLongOrNull();
				message = message.replace(daysMatcher.group(), "");
			}
			if (hoursMatcher.find()) {
				var group = hoursMatcher.group(1);
				hours = group.toLongOrNull();
				message = message.replace(hoursMatcher.group(), "");
			}
			if (minutesMatcher.find()) {
				var group = minutesMatcher.group(1);
				minutes = group.toLongOrNull();
				message = message.replace(minutesMatcher.group(), "");
			}
			if (secondsMatcher.find()) {
				var group = secondsMatcher.group(1);
				seconds = group.toLongOrNull();
				message = message.replace(secondsMatcher.group(), "");
			}

			if (years == null || months == null || weeks == null || days == null || hours == null || minutes == null || seconds == null) {
				context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + "Tempo de espera inválido! Talvez você tenha colocado um tempo muito grande...")
				return;
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

			// Transformar o nome do mês em PT-BR
			var strMonth = DateFormatSymbols().months[instant.month.value - 1]

			// Criar o Lembrete
			var reminder = Reminder(context.guild.id, context.message.textChannel.id, inMillis, message.trim());
			var profile = context.lorittaUser.profile

			profile.reminders.add(reminder);

			loritta save profile

			context.sendMessage(context.getAsMention(true) + context.locale.LEMBRAR_SUCCESS.msgFormat(instant.dayOfMonth, strMonth, instant.year, instant.hour, instant.minute))
		} else {
			this.explain(context);
		}
	}
}
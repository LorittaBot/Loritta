package net.perfectdreams.loritta.utils

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class CalendarTest {
	@Test
	fun `reset calendar to start of the day`() {
		val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
		calendar.set(Calendar.DAY_OF_MONTH, 11)
		calendar.set(Calendar.MONTH, 10)
		calendar.set(Calendar.YEAR, 2019)

		CalendarUtils.resetToBeginningOfTheDay(calendar)
		assertThat(calendar.toString()).isEqualTo("java.util.GregorianCalendar[time=?,areFieldsSet=false,areAllFieldsSet=true,lenient=true,zone=sun.util.calendar.ZoneInfo[id=\"UTC\",offset=0,dstSavings=0,useDaylight=false,transitions=0,lastRule=null],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=1,YEAR=2019,MONTH=10,WEEK_OF_YEAR=30,WEEK_OF_MONTH=4,DAY_OF_MONTH=11,DAY_OF_YEAR=204,DAY_OF_WEEK=3,DAY_OF_WEEK_IN_MONTH=4,AM_PM=0,HOUR=11,HOUR_OF_DAY=0,MINUTE=0,SECOND=0,MILLISECOND=0,ZONE_OFFSET=0,DST_OFFSET=0]")
	}

	@Test
	fun `reset calendar to end of the day`() {
		val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
		calendar.set(Calendar.DAY_OF_MONTH, 11)
		calendar.set(Calendar.MONTH, 10)
		calendar.set(Calendar.YEAR, 2019)

		CalendarUtils.resetToEndOfTheDay(calendar)
		assertThat(calendar.toString()).isEqualTo("java.util.GregorianCalendar[time=?,areFieldsSet=false,areAllFieldsSet=true,lenient=true,zone=sun.util.calendar.ZoneInfo[id=\"Etc/UTC\",offset=0,dstSavings=0,useDaylight=false,transitions=0,lastRule=null],firstDayOfWeek=1,minimalDaysInFirstWeek=1,ERA=1,YEAR=2019,MONTH=10,WEEK_OF_YEAR=30,WEEK_OF_MONTH=4,DAY_OF_MONTH=11,DAY_OF_YEAR=204,DAY_OF_WEEK=3,DAY_OF_WEEK_IN_MONTH=4,AM_PM=0,HOUR=11,HOUR_OF_DAY=23,MINUTE=59,SECOND=59,MILLISECOND=999,ZONE_OFFSET=0,DST_OFFSET=0]")
	}
}
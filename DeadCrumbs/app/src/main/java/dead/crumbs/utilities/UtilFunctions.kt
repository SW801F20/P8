package dead.crumbs.utilities

import java.util.*

class UtilFunctions() {
    companion object Functions {
        fun getDatetime(): String{
            val currYear = Calendar.getInstance().get(Calendar.YEAR).toString().padStart(4,'0')
            val currMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1).toString().padStart(2,'0')
            val currDate = Calendar.getInstance().get(Calendar.DATE).toString().padStart(2,'0')
            val currHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toString().padStart(2,'0')
            val currMinute = Calendar.getInstance().get(Calendar.MINUTE).toString().padStart(2,'0')
            val currSecond = Calendar.getInstance().get(Calendar.SECOND).toString().padStart(2,'0')
            val dateTimeString = currYear + "-" + currMonth + "-" + currDate+ "T" + currHour + ":" + currMinute + ":" + currSecond
            return dateTimeString
        }
    }
}
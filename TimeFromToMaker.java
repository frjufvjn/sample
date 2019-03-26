import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TimeFromToMaker {

	public static void main(String[] args) {

		String start = "20190201";
		String end = "20190203";
		//		String start_detail = start + "000000";
		//		String end_detail = end + "235959";


		runWrapper("DAY", start, end);
	}

	private static void runWrapper(String type, String start, String end) {
		String[] tm = {
				"00","01","02","03","04","05","06","07","08","09",
				"10","11","12","13","14","15","16","17","18","19",
				"20","21","22","23"
		};
		String[] min = {
				"00","01","02","03","04","05","06","07","08","09",
				"10","11","12","13","14","15","16","17","18","19",
				"20","21","22","23","24","25","26","27","28","29",
				"30","31","32","33","34","35","36","37","38","39",
				"40","41","42","43","44","45","46","47","48","49",
				"50","51","52","53","54","55","56","57","58","59"
		};

		int count = 0;
		ArrayList<String> dates;

		try {
			dates = getDateList(start, end);
			switch (type) {
			case "DAY":
				for ( String date : dates ) {
					callee(type, date);
				}
				break;
			case "HOUR":
				for ( String date : dates ) {
					for ( int i=0; i<tm.length; i++ ) {
						String tmstr = date + tm[i];
						callee(type, tmstr);
					}
				}
				break;
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private static void callee(String type, String str) {
		String start, end = "";
		if ( "DAY".equals(type) ) {
			start = str + "000000";
			end = str + "235959";
		} else {
			start = str + "0000";
			end = str + "5959";
		}
		System.out.println(start + " : " + end);
	}

	private static ArrayList<String> getDateList(String start, String end) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date startDate = sdf.parse(start);
		Date endDate = sdf.parse(end);
		ArrayList<String> dates = new ArrayList<String>();
		Date currentDate = startDate;
		while( currentDate.compareTo(endDate) <= 0 ) {
			dates.add(sdf.format(currentDate));
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DAY_OF_MONTH, 1);
			currentDate = c.getTime();
		}
		return dates;
	}


}









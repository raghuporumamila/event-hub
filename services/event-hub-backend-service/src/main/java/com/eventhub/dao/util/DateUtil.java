package com.eventhub.dao.util;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static java.lang.System.currentTimeMillis;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.google.cloud.Timestamp;

public class DateUtil {

	public static Timestamp getCurrentTimestamp() {
		/*com.google.protobuf.Timestamp timestampProto = fromMillis(currentTimeMillis());
		Timestamp timestamp = new Timestamp();*/
		return Timestamp.now();
	}
	
	public static Timestamp get7DaysBackTimestamp() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		
		com.google.protobuf.Timestamp timestampProto = fromMillis(cal.getTime().getTime());
		Timestamp timestamp = Timestamp.fromProto(timestampProto);
		return timestamp;
	}
	
	public static String getTimestampStr(Timestamp timestamp)  {
		return timestamp.toString();
	}
	
	
	public static String getDayName(Date date) {
		SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE"); // the day of the week spelled out completely
        //System.out.println(simpleDateformat.format(date));
        
        return simpleDateformat.format(date);
	}
}

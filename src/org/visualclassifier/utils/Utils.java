package org.visualclassifier.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	
	public static String getCurrentTimeStamp() {
	    SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
	    Date now = new Date();
	    String strDate = sdfDate.format(now);
	    return strDate;
	}
	
}

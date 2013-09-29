package it.tomlolriff.hoveringinformation.activity;

import android.net.wifi.WifiManager;

public class Utility {
	// 0.00001 circa 1.11 metri
	public static final Long METER_CONVERSION_FACTOR = (long)100000;
	public static final Double X_ANCHOR = 43.83007;
	public static final Double Y_ANCHOR = 13.00915;
	public static final Double SAFE_RADIUS = 0.01;     // circa 1000 m
	public static final Double RISK_RADIUS = 0.03;     // circa 3000 m
	public static final Double RELEVANT_RADIUS = 0.1;  // circa 10000 m
	
	public static final double LATITUDE_START = 43.83007;
	public static final double LONGITUDE_START = 13.00915;
	public static final float BEARING_START = (float) 0.0;
	
	public static String generateMACizeName(String prefix, WifiManager manager, String postfix) {
		String res = prefix;
		String mac = manager.getConnectionInfo().getMacAddress();
		if(mac != null) {
			res +=  mac + postfix;
		}
		else { // nel caso avessi un mac null, ad esempio quando il wifi è spento
			res += Math.random() + postfix;
		}
		return res;
	}
}

package com.devinabbott.handlebars;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Utils {

	private Utils() {}
	
	public static Boolean isTruthy(Object v) {
		if (v instanceof Boolean) {
			return (Boolean) v;
		} else if (v instanceof Double) {
			return ((Double) v).intValue() != 0;
		} else if (v instanceof Number) {
			// TODO what should this be?
			Number a = (Number) v;
			return a.intValue() != 0;
		} else if (v instanceof JSONObject || v instanceof JSONArray) {
			return true;
		} else if (v instanceof String) {
			// Empty string is not truthy
			return ! ((String)v).equals("");
		}
		return false;
	}

}

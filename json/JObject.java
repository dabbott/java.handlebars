package json;

import java.util.HashMap;

public class JObject extends JSON {

	HashMap<String, JSON> h;
	
	public JObject() {
		h = new HashMap<String, JSON>();
	}
	
	public void set(String k, JSON v) {
		h.put(k, v);
		v.parent = this;
	}
	
	public void set(String k, String v) {
		JString s = new JString(v);
		h.put(k, s);
		s.parent = this;
	}
	
	public JSON unset(String k) {
		JSON j = h.remove(k);
		if (j != null)
			j.parent = null;
		return j;
	}
	
	public JSON get(String k) {
		return h.get(k);
	}
	
	public String str(String k) {
		return this.get(k).toString();
	}
	
	public String toString() {
		String output = "{ ";
		Boolean hasOne = false;
		for (String key : h.keySet()) {
			hasOne = true;
			output += key + ":\"" + h.get(key) + "\", ";
		}
		if (hasOne)
			return output.substring(0, output.length()-2) + " }";
		else
			return "{}";
	}

}

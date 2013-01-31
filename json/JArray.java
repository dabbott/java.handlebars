package json;

import java.util.ArrayList;

public class JArray extends JSON {

	public ArrayList<JSON> a; 
	
	public JArray() {
		a = new ArrayList<JSON>();
	}
	
	public void push(JSON v) {
		a.add(v);
		v.parent = this;
	}
	
	public void push(String v) {
		JString s = new JString(v);
		a.add(s);
		s.parent = this;
	}
	
	public JSON get(int n) {
		return a.get(n);
	}
	
	public int size() {
		return a.size();
	}
	
	public String toString() {
		String output = "[ ";
		Boolean hasOne = false;
		for (JSON v : a) {
			hasOne = true;
			output += v.toString() + ", ";
		}
		if (hasOne)
			return output.substring(0, output.length()-2) + " ]";
		else
			return "[]";
	}

}

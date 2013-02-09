package com.devinabbott.handlebars;

import org.json.simple.*;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JSONArray a = new JSONArray();
		a.add("Cool");
		a.add(false);
		a.add(42);
		JSONObject j = new JSONObject();
		j.put("Test", "Ok");
		j.put("ok", 1.24);
		j.put("array", a);
		
		JSONObject sub = new JSONObject();
		sub.put("test", 123);
		j.put("sub", sub);
		System.out.println(j.toString());
		
		Parser p = new Parser();
		p.data = "<h1>{{#eachIndex array}}\n<p>{{$index}}</p>{{/eachIndex}}\n</h1>";
		String output = p.parse(j);
		System.out.println(output);
	}

}

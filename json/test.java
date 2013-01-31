package json;

import template.Parser;

public class test {
	
	public static void main(String args[]) {
		
		System.out.println("Ok");
		
		JObject wrapper = new JObject();
		JArray a = new JArray();
		for (int i = 0; i < 3; i++) {
			JObject o = new JObject();
			o.set("test", new JString("ok-" + i));
			a.push(o);
			if (i == 2) {
				o.set("switch", new JString("ON!"));
			}
		}
		wrapper.set("data", a);
		
//		JNumber n = new JNumber(45);
//		o.set("test", n);
		
//		JArray o2 = new JArray();
//		JNumber n2 = new JNumber(42);
//		o2.set("Meaning of life", n2);
//		o.set("Test 2", o2);
//		
//		System.out.println(((JNumber) o.get("Test")).d);
		
		Parser p = new Parser();
		p.read("item.xml");
		System.out.println(p.data);
		String result = p.parse(wrapper);
		System.out.println("---");
		System.out.println(result);
	}

}

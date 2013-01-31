package json;

public class JString extends JSON {

	public String s;
	
	public JString(String s) {
		this.s = s;
	}
	
	public JString(JString js) {
		this.s = js.s;
	}
	
	public String toString() {
		return s;
	}

}

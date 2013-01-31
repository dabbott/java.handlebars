package json;

public class JNumber extends JSON {

	double d;
	
	public JNumber(double d) {
		this.d = d;
	}
	
	public String toString() {
		return new Double(d).toString();
	}

}

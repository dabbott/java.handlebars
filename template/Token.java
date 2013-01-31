package template;

public class Token {
	 public int start;
	 public int end;
	 public String tag;
	 public Type type;
	 
	 public Token(int start, int end, String tag, Type type) {
		 this.start = start;
		 this.end = end;
		 this.tag = tag;
		 this.type = type;
	 }
	 
	 public Token(Token t) {
		 this.start = t.start;
		 this.end = t.end;
		 this.tag = t.tag;
		 this.type = t.type;
	 }
}
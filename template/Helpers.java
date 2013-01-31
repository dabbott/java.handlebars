package template;

import java.util.HashMap;
import java.util.Stack;
import json.JArray;
import json.JObject;
import json.JSON;
import json.JString;

/**
 * Static collection of Helper objects.
 * Ideally, I could insert the default helpers statically...
 * @author devinabbott
 */
public class Helpers {

	public static HashMap<String, IHelper> helpers = 
			new HashMap<String, IHelper>();

	private Helpers() {}
	
	static {
		Helpers.registerHelper("#each", new Each());
		Helpers.registerHelper("#eachIndex", new EachIndex());
		Helpers.registerHelper("#if", new If());
		Helpers.registerHelper("#with", new With());
		Helpers.registerHelper(">", new Partial());
		Helpers.registerHelper("#eq_const", new EqConst());
		
		Helpers.registerHelper("escape", new EscapeHTML());
		Helpers.registerHelper("escapeAttr", new EscapeAttr());
	}
		
	public static void registerHelper(String name, IHelper f) {
		helpers.put(name, f);
	}
	
	public static Boolean hasHelper(String name) {
		return helpers.containsKey(name);
	}
	
	public static String apply(String name, JSON j,
			ASTNode n, Stack<JObject> stack) {
		IHelper f = helpers.get(name);
		String result = null; 
		if (f != null)
			result = f.apply(stack, n, j);
		else
			System.out.println("Helper " + name + "doesn't exist!");
		return result;
	}
	
	/**
	 * Simply set the current scope
	 */
	private static class With implements IHelper {
		public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
			JSON newScope = Parser.lookup(stack, j, n.args.get(0).t.tag);
			return Parser.parseChildren(stack, newScope, n);
		}
	}

	/**
	 * Iterate over each object in an array
	 */
	private static class Each implements IHelper {
		public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
			String output = "";
			JArray arr = (JArray) Parser.lookup(stack, j, n.args.get(0).t.tag);
			for (JSON v : arr.a) {
				System.out.println(v);
				output += Parser.parseChildren(stack, v, n);				
			}
			return output;
		}
	}
	
	/**
	 * Iterate over each object in an array, with helper variables
	 * $index, $first, $last, and $only
	 */
	private static class EachIndex implements IHelper {
		public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
			String output = "";
			JObject top = stack.peek();
			JArray arr = (JArray) Parser.lookup(stack, j, n.args.get(0).t.tag);
			for (int i = 0; i < arr.a.size(); i++) {
				top.set("$index", new JString("" + i));
				if (i == 0)
					top.set("$first", "true");
				else
					top.unset("$first");
				if (i == arr.a.size() - 1)
					top.set("$last",  "true");
				else
					top.unset("$last");
				if (arr.a.size() == 1)
					top.set("$only", "true");
				output += Parser.parseChildren(stack, arr.a.get(i), n);				
			}
			top.unset("$only");
			return output;
		}
	}
	
	/**
	 * If with optional else clause
	 */
	private static class If implements IHelper {
		public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
			String output = "";
			JSON v = Parser.lookup(stack, j, n.args.get(0).t.tag);
			// If there's no key, lookup simply returns null
			// However, a stored <key, null> requires the .toString()
			// because it was created as a new JString(null)
			if (v != null && v.toString() != null && ! v.toString().equals("0")) {
				for (ASTNode child : n.children) {
					if (child.t.type == Type.Simple && child.t.tag.equals("else"))
						break;
					output += Parser.parse(stack, j, child);
				}
			} else {
				// Search for an {{else}}, output any children after it's found
				Boolean found = false;
				for (ASTNode child : n.children) {
					if (found)
						output += Parser.parse(stack, j, child);
					if (! found && child.t.type == Type.Simple && child.t.tag.equals("else"))
						found = true;
				}
			}
			return output;
		}
	}
	
	/**
	 * Compare variable with a string constant
	 */
	private static class EqConst implements IHelper {
		public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
			String output = "";
			JSON v = Parser.lookup(stack, j, n.args.get(0).t.tag);
			String userConstant = n.args.get(1).t.tag;
//			output += "Constant: " + userConstant + " v: " + v.toString();
			// If there's no key, lookup simply returns null
			// However, a stored <key, null> requires the .toString()
			// because it was created as a new JString(null)
			if (v != null && v.toString() != null && v.toString().equals(userConstant)) {
				for (ASTNode child : n.children) {
					output += Parser.parse(stack, j, child);
				}
			}
			return output;
		}
	}
	
	/**
	 * Recursively create a new parser under the current JSON scope
	 */
	private static class Partial implements IHelper {
		public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
			String filename = n.args.get(0).t.tag;
			Parser p = new Parser(filename);
			String output = p.parse(stack, j);
			return output;
		}
	}

	
	private static class EscapeHTML implements IHelper {
		public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
			JSON v = Parser.lookup(stack, j, n.args.get(0).t.tag);
			return escapeHTML(v.toString());
		}
	}
	
	private static class EscapeAttr implements IHelper {
		public String apply(Stack<JObject> stack, ASTNode n, JSON j) {
			JSON v = Parser.lookup(stack, j, n.args.get(0).t.tag);
			return escapeAttr(v.toString());
		}
	}
	
    private static final String apos = "&apos;";
    private static final String amp = "&amp;";
    private static final String lt = "&lt;";
    private static final String gt = "&gt;";
    private static final String quot = "&quot;";
	
	public static String escapeHTML(String in) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			switch(c) {
				case '&': s.append(amp); continue;
				case '\'': s.append(apos); continue;
				case '<': s.append(lt); continue;
				case '>': s.append(gt); continue;
				case '\\': s.append("\\"); continue;
				case '"': s.append("\""); continue;
			}
			s.append(c);
		}
		return s.toString();
	}
	
	public static String escapeAttr(String in) {
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			switch(c) {
				case '&': s.append(amp); continue;
				case '\'': s.append(apos); continue;
				case '<': s.append(lt); continue;
				case '>': s.append(gt); continue;
				case '"': s.append(quot); continue;
			}
			s.append(c);
		}
		return s.toString();
	}
}
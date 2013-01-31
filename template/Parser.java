package template;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import json.JObject;
import json.JSON;
import json.JString;

/** 
 * @author devinabbott
 * 
 * Parses a simple subset of the Handlebars templating language.
 * The language consists of identifiers wrapped in Handlebars "{{", "}}"
 * The parser then replaces such identifiers with JSON objects,
 * allowing array iteration and dynamic scoping.
 * 
 * This implementation differs by automatically traversing the JSON data
 * when an identifier is not found in the current scope. Identifiers are
 * looked up across sub-template boundaries.
 */
public class Parser {
	
	public String data;
	public TokenStream tokenStream;
	public AST ast;

	public Parser() {
		tokenStream = new TokenStream();
	}
	
	public Parser(String filename) {
		if (! read(filename))
			System.exit(4);
		tokenStream = new TokenStream();
	}
	
	public Boolean read(String filename) {
		// Get system resource from package directory.
		// There must be a better way to do this...
		filename = "template/" + filename + ".html";
		InputStream input = 
				Parser.class.getClassLoader().getResourceAsStream(filename);
		if (input == null) {
			System.out.println("Unable to open file " + filename);
			return false;
		}
		InputStreamReader stream = new InputStreamReader(input);
		
		StringBuilder data = new StringBuilder();
		String line = null;
		try {
			BufferedReader reader = new BufferedReader(stream);
			while ((line = reader.readLine()) != null) {
				data.append(line + "\n");
			}
			// Delete extra newline appended
			data.deleteCharAt(data.length() - 1);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		this.data = data.toString();
		return true;
	}
	
	public void lex() {
		ast = new AST(Tokenizer.tokenize(data));
	}
	
	public String parse() {
		return parse(new JObject());
	}
	
	public String parse(JSON j) {
		return parse(new Stack<JObject>(), j);
	}
	
	public String parse(Stack<JObject> stack, JSON j) {
		// Save the AST so we only tokenize/lex once.
		if (ast == null) 
			lex();
		return parse(stack, j, ast.root);
	}
	
	public static JSON lookup(Stack<JObject> stack, JSON j, String k) {
		
		if (k.equals("this")) {
			System.out.println(k + " found");
			return j;
		}

		JSON v = null;
		if (JObject.class.isAssignableFrom(j.getClass())) {
			JObject obj = (JObject) j;
			v = obj.get(k);
		}
		
		if (v == null) {
			// Check parent scope for variable
			if (j.parent != null)
				return lookup(stack, j.parent, k);
			else {
				// If no more parents, try the stack 
				v = searchStack(stack, k);
				if (v == null && Helpers.hasHelper(k))
					return new JString(Helpers.apply(k, j, null, stack));
			}
		}
		
		return v;
	}
	
	/**
	 * Search the internal variable stack.
	 * @param k name
	 * @return JSON object
	 */
	public static JSON searchStack(Stack<JObject> stack, String k) {
		JObject scope = null;
		JSON v = null;
		for (int i = stack.size() - 1; i >= 0; i--) {
			scope = stack.get(i);
			if ((v = scope.get(k)) != null) {
				return v;
			}
		}
		return v;
	}
	
	public static String parse(Stack<JObject> stack, JSON j, ASTNode n) {
		String output = "";
		Token t = n.t;
		switch (t.type) {
			case Text:
				output += parseText(t);
				break;
			case Root:
				output += parseRoot(stack, j, n);
				break;
			case Block:
				stack.push(new JObject());
				output += parseBlock(stack, j, n);
				stack.pop();
				break;
			case Simple:
				output += parseSimple(stack, j, t);
				break;
			case Function:
				output += parseFunction(stack, j, n);
				break;
			default:
				break;
		}
		return output;
	}
	
	public static String parseRoot(Stack<JObject> stack, JSON j, ASTNode n) {
		String output = "";
		for (ASTNode child : n.children) {
			output += parse(stack, j, child);
		}
		return output;
	}
	
	public static String parseChildren(Stack<JObject> stack, JSON j, ASTNode n) {
		String output = "";
		for (ASTNode child : n.children) {
			output += parse(stack, j, child);
		}
		return output;
	}
	
	public static String parseFunction(Stack<JObject> stack, JSON j, ASTNode n) {
		return Helpers.apply(n.t.tag, j, n, stack);
	}
	
	public static String parseBlock(Stack<JObject> stack, JSON j, ASTNode n) {
		// At the moment, blocks are the same as functions
//		System.out.println("Entering block " + n.t.tag);
		return parseFunction(stack, j, n);
	}
	
	public static String parseSimple(Stack<JObject> stack, JSON j, Token t) {
		JSON v = lookup(stack, j, t.tag);
		if (v == null)
			return "undefined";
		return v.toString();
	}
	
	public static String parseText(Token t) {
		return t.tag;
	}
	
}
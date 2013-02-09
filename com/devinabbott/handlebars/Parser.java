package com.devinabbott.handlebars;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import org.json.simple.JSONObject;

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
		return parse(new JSONObject());
	}
	
	public String parse(Object j) {
		return parse(new Stack<JSONObject>(), j);
	}
	
	public String parse(Stack<JSONObject> stack, Object j) {
		// Save the AST so we only tokenize/lex once.
		if (ast == null) 
			lex();
		return parse(stack, j, ast.root);
	}
	
	public static Object lookup(Stack<JSONObject> stack, Object j, String k) {
		
		if (k.equals("this")) {
			System.out.println("this found");
			return j;
		}

		Object v = null;
		if (JSONObject.class.isAssignableFrom(j.getClass())) {
			JSONObject obj = (JSONObject) j;
//			v = obj.get(k);
			v = obj.get(k);
		}
		
		// Check the object stack
		if (v == null) {
			v = searchStack(stack, k);
			
			// If stack lookup fails, check for a helper and apply it
			if (v == null && Helpers.hasHelper(k))
				return Helpers.apply(k, j, null, stack);
		}
		
//		if (v == null) {
//			// Check parent scope for variable
//			if (j.parent != null)
//				return lookup(stack, j.parent, k);
//			else {
//				// If no more parents, try the stack 
//				v = searchStack(stack, k);
//				if (v == null && Helpers.hasHelper(k))
//					return new JString(Helpers.apply(k, j, null, stack));
//			}
//		}
		
		return v;
	}
	
	/**
	 * Search the internal variable stack.
	 * @param k name
	 * @return JSON object
	 */
	public static Object searchStack(Stack<JSONObject> stack, String k) {
		JSONObject scope = null;
		Object v = null;
		for (int i = stack.size() - 1; i >= 0; i--) {
			scope = stack.get(i);
			if ((v = scope.get(k)) != null) {
				return v;
			}
		}
		return v;
	}
	
	public static String parse(Stack<JSONObject> stack, Object j, ASTNode n) {
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
				stack.push(new JSONObject());
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
	
	public static String parseRoot(Stack<JSONObject> stack, Object j, ASTNode n) {
		String output = "";
		for (ASTNode child : n.children) {
			output += parse(stack, j, child);
		}
		return output;
	}
	
	// TODO, isn't this exactly the same as parseRoot?
	public static String parseChildren(Stack<JSONObject> stack, Object j, ASTNode n) {
		String output = "";
		for (ASTNode child : n.children) {
			output += parse(stack, j, child);
		}
		return output;
	}
	
	public static String parseFunction(Stack<JSONObject> stack, Object j, ASTNode n) {
		return Helpers.apply(n.t.tag, j, n, stack);
	}
	
	public static String parseBlock(Stack<JSONObject> stack, Object j, ASTNode n) {
		// At the moment, blocks are the same as functions
//		System.out.println("Entering block " + n.t.tag);
		return parseFunction(stack, j, n);
	}
	
	public static String parseSimple(Stack<JSONObject> stack, Object j, Token t) {
		Object v = lookup(stack, j, t.tag);
		if (v == null)
			return "undefined";
		return v.toString();
	}
	
	public static String parseText(Token t) {
		return t.tag;
	}
	
}
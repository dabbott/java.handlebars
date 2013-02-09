package com.devinabbott.handlebars;

import java.util.Stack;

import org.json.simple.*;

/**
 * Extensible template function interface
 */
public interface IHelper {
	
	public String apply(Stack<JSONObject> stack, ASTNode n, Object j);
	
}

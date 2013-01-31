package template;

import java.util.Stack;

import json.JObject;
import json.JSON;

/**
 * Extensible template function interface
 */
public interface IHelper {
	
	public String apply(Stack<JObject> stack, ASTNode n, JSON j);
	
}

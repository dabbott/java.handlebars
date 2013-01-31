package template;

import java.util.Vector;

public class ASTNode {
	public Token t;
	public Vector<ASTNode> children;
	public ASTNode parent;
	public Vector<ASTNode> args;
	
	public ASTNode(Token t) {
		this.t = t;
		this.parent = null;
		this.children = new Vector<ASTNode>();
	}
	
	public ASTNode(Token t, ASTNode parent) {
		this.t = t;
		this.parent = parent;
		this.children = new Vector<ASTNode>();
	}
	
	public void push(ASTNode n) {
		children.add(n);
	}
}


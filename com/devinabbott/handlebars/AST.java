package com.devinabbott.handlebars;

import java.util.Vector;

/**
 * @author devinabbott
 *
 * Organizes tokens into an abstract syntax tree, e.g.
 * 
 * Block #each
 *    args name1 name2 name3...
 *    children:
 *       Text "<div>"
 *       Simple name
 *       Text "</div>"
 */
public class AST {

	public ASTNode root;
	
	public AST(TokenStream tokenStream) {
		root = new ASTNode(new Token(-1, -1, "", Type.Root));
		lex(tokenStream);
//		print(tokenStream);
	}
	
//	private void print(TokenStream tokenStream) {
//		System.out.println("TOKENS -");
//		for (Token t : tokenStream)
//			System.out.println("Token: " + t.type + " " + t.tag);
//		System.out.println("--------");
//		print(root, "");
//		System.out.println("AST ----");
//		print(root, "");
//		System.out.println("--------");
//	}
//	
//	private void print(ASTNode n, String indent) {
//		String info = "";
//		if (n.t.type != null) info += n.t.type + " ";
//		if (n.t.tag != null) info += n.t.tag + " ";
//		System.out.println(indent + info);
//		for (ASTNode child : n.children) {
//			print(child, indent + "  ");
//		}
//	}
	
	private void lex(TokenStream tokenStream) {
		ASTNode n = root;
		Token t = null;
		int index = 0;
		while (index < tokenStream.size()) {
			t = tokenStream.get(index);
			switch (t.type) {
				case Function:
				case Block:
					Type endType;
					if (t.type == Type.Block) 
						endType = Type.BlockClose; 
					else
						endType = Type.FunctionClose;
					
					ASTNode fn = new ASTNode(t, n);
					fn.args = new Vector<ASTNode>();
					n.push(fn);
					
					for (;;) {
						Token arg = tokenStream.get(++index);
						if (arg.type == endType)
							break;
						fn.args.add(new ASTNode(arg, n));
					}
					
					// Set up block as new parent
					if (t.type == Type.Block)
						n = fn;
					break;
				case EndBlock:
					// Compare block names (e.g. #each and /each)
					if (! n.t.tag.substring(1).equals(t.tag.substring(1))) {
						System.out.println(n.t.tag.length() + " " + t.tag.length());
						System.out.println("Tag mismatch:" + n.t.tag + " " + t.tag);
					}
					n = n.parent;
					break;
				default:
					n.push(new ASTNode(t));
			}
			index++;
		}
	}
}

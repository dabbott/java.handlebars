package com.devinabbott.handlebars;

public class Tokenizer {

	private Tokenizer() {}
	
	public static final TokenStream tokenize(String data) {
		TokenStream tokenStream = new TokenStream();
		try {
			int pos = 0;
			int startBrace = -1;
			int endBrace = -1;
			
			while((startBrace = data.indexOf("{{", pos)) != -1) {
				
				// Get text between braces
				if (pos < startBrace) {
					String text = data.substring(pos, startBrace);
					//text = text.trim().replaceAll(" +", " ");
					tokenStream.add(new Token(pos, startBrace, text, Type.Text));
				}
				
				pos = startBrace + 2;
				endBrace = data.indexOf("}}", pos);
				if (endBrace == -1) {
					throw new Exception("No matching end brace after " + pos + ".");
				}
				
				String tag = data.substring(pos, endBrace);
				
				if (tagHas(tag, "#")) {
					splitTokens(tokenStream, tag, pos, Type.Block, Type.BlockClose);
				} else if (tagHas(tag, "/")) {
					tokenStream.add(new Token(pos, pos + tag.length(), tag, Type.EndBlock));
				} else if (tagHas(tag, " ")) {
					splitTokens(tokenStream, tag, pos, Type.Function, Type.FunctionClose);
				} else {
					tokenStream.add(new Token(pos, pos + tag.length(), tag, Type.Simple));
				}
				
				// Add token with type "Unknown" - determine Block/Simple later
				tokenStream.add(new Token(pos, endBrace, tag, Type.Unknown));
				
				// Advance cursor over braces
				pos = endBrace + 2;
				
//				System.out.println("Pos: " + pos + "  startBrace: " + startBrace + "  endBrace: " + endBrace);
			}
			
			// Add remaining text. If empty string, this will parse to nothing.
			String remainder = data.substring(pos);
			tokenStream.add(new Token(pos, data.length(), remainder, Type.Text));
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(3);
		}
		return tokenStream;
	}
	
	private static Boolean tagHas(String tag, String c) {
		return (tag.indexOf(c) != -1);
	}
	
	private static void splitTokens(TokenStream tokenStream, String tag, int start,
			Type type, Type closeType) {
		
		String[] values = tag.split(" ");
		
		// Add open token (e.g. #each)
		int endFirst = start + values[0].length();
		tokenStream.add(new Token(start, endFirst, values[0], type));
		
		// Add argument tokens (e.g. name1 name2)
		int st = endFirst;
		for (int i = 1; i < values.length; i++) {
			int len = values[i].length();
			tokenStream.add(new Token(st, st + len, values[i], Type.Identifier));
			st += len;
		}
		
		// Add optional close token (has no textual significance)
		if (closeType != null)
			tokenStream.add(new Token(-1, -1, "", closeType));
	}
}


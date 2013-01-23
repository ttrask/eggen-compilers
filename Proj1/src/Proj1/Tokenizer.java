package Proj1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokenizer {

	public SourceLine TokenizeLine(String line, int commentDepth, int blockDepth ) {

		List<Token> tokens = new ArrayList<Token>();

		for (String s : line.split(" ")) {
			// remove all empty tokens.
			
			Token tk = new Token();
			
			if (s.trim().length() > 0) {
				if (Keywords.contains(s)) {
					// add logic for keywords
				} else if (MathOperators.contains(s)) {
					// add logic for keywords
				} else if (LogicOperators.contains(s)) {
					// add logic for keywords
				} else if (BlockOperators.contains(s)) {
					// add logic for keywords
				} else if (CommentOperators.contains(s)) {
					// add logic for keywords
				} else {
					// basic token.  Determine if number or variable.
					
					
					tk.ID = s;
					

					//check to see if it's fully numeric.
						//if it is, process it as a number. (floats are valid numbers)
				
					//split token on all invalid characters. Include invalid characters.
						//if so, make a new token for each new one.
						//recursively call Tokenizeline to process new tokens.
					
					//check to see if its a word.
						//if it is, process it as an identifier.
					
			
				}
			}
			
			tokens.add(tk);
		}
		
		SourceLine sc = new SourceLine();
		
		sc.Tokens = tokens;
		sc.SourceCode = line;
		
		return sc;
	}

	public List<String> Keywords = Arrays.asList("else", "if", "int", "return",
			"void", "while");
	public List<String> MathOperators = Arrays.asList("+", "-", "*", "/", "<",
			"=", ";", ",");
	public List<String> LogicOperators = Arrays.asList("<=", ">", ">=", "==",
			"!=");
	public List<String> BlockOperators = Arrays.asList("(", ")", "[", "]", "{",
			"}");
	public List<String> CommentOperators = Arrays.asList("/*", "*/", "//");
}

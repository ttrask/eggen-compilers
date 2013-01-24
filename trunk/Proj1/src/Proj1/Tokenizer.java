package Proj1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Tokenizer {

	static private Boolean _isComment = false;
	static private Boolean _isFullLineComment = false;
	static private int _commentDepth = 0;
	static private int _blockDepth = 0;

	public List<String> Keywords = Arrays.asList("else", "if", "int", "float",
			"return", "void", "while");
	public List<String> AssignmentOperators = Arrays.asList("+", "-", "*", "/",
			"<", "=", ";", ",");
	public List<String> LogicOperators = Arrays.asList("<=", ">", ">=", "==",
			"!=");
	public List<String> BlockOperators = Arrays.asList("{", "}", "[", "]");
	public List<String> ArgumentOperators = Arrays.asList("(", ")", ",", ";");
	public List<String> CommentOperators = Arrays.asList("/*", "*/", "//");

	public SourceLine TokenizeLine(String line, int commentDepth, int blockDepth) {

		_commentDepth = commentDepth;
		_blockDepth = blockDepth;

		List<Token> tokens = new ArrayList<Token>();

		for (String s : line.split(" ")) {
			// ignore all empty tokens.
			s = s.trim();
			if (s.length() > 0) {
				// concatenate strings if in comment
				if (commentDepth > 0 || _isFullLineComment)
					_isComment = true;
				else{
					_isComment = false;
				}
				
				if (_isComment != true
						&& !OperatorInString(s, CommentOperators)) {
					tokens.addAll(TokenizeString(s));
				} else {

					tokens.addAll(ParseComment(s));
				}
			}
		}

		SourceLine sc = new SourceLine();
		sc.CommentDepth = _commentDepth;
		sc.BlockDepth = _blockDepth;
		sc.Tokens = tokens;
		sc.SourceCode = line;

		return sc;
	}

	public List<Token> TokenizeString(String s) {

		HashMap<String, List<String>> operators = new HashMap<String, List<String>>();
		operators.put("Keywords", Keywords);
		// operators.put("CommentOperators", CommentOperators);
		operators.put("AssignmentOperators", AssignmentOperators);
		operators.put("LogicOperators", LogicOperators);
		operators.put("BlockOperators", BlockOperators);
		operators.put("ArgumentOperators", ArgumentOperators);

		List<Token> genTokens = new ArrayList<Token>();

		if (s.contains(" ")) {
			// if the string contains spaces, parse it like you would a line.
			for (String subS : s.split(" ")) {
				subS = subS.trim();
				if (subS.length() > 0)
					genTokens.addAll(TokenizeString(subS));
			}
			return genTokens;
		} else {
			boolean cont = true;
			Token tk = new Token();
			tk.ID = s;

			if (Keywords.contains(s)) {
				tk.Type = TokenType.Keyword;
			} else if (AssignmentOperators.contains(s)) {
				tk.Type = TokenType.Assignment;
			} else if (LogicOperators.contains(s)) {
				tk.Type = TokenType.Logic;
			} else if (BlockOperators.contains(s)) {
				// add logic for Block Operator
				if (s.compareTo("{") == 0)
					_blockDepth++;
				else if (s.compareTo("}") == 0)
					if (_blockDepth > 0)
						_blockDepth--;
				tk.Type = TokenType.Block;
			} else if (ArgumentOperators.contains(s)) {
				tk.Type = TokenType.Argument;
			} else if (CommentOperators.contains(s)) {
				{
					return ParseComment(s);
				}
			} else if (OperatorInString(s, CommentOperators)) {

				// stub for something I can't quite think of right now.

			} else {

				for (List<String> ops : operators.values()) {
					if (OperatorInString(s, ops)) {
						return TokenizeString(SpaceifyStringContainingOperators(
								s, ops));
					}
				}

				// if (cont && s.contains("[")) {
				// if (!(s.contains("]"))) {
				// tk.Type = TokenType.Error;
				// tk.Note = "Open Subscript missing closing subscript";
				// } else if (CountOccurrences(s, ']') > 1
				// || CountOccurrences(s, '[') > 1
				// || s.indexOf(']') < s.indexOf('[')) {
				// tk.Type = TokenType.Error;
				// tk.Note = "Syntax Error";
				// } else {
				//
				// String sub = s
				// .substring(s.indexOf('['), s.indexOf(']'));
				// genTokens.addAll(TokenizeString("["));
				// genTokens.addAll(TokenizeString(sub));
				// genTokens.addAll(TokenizeString("]"));
				// s = s.substring(0, s.indexOf('[') - 1);
				// tk.ID = s;
				// }
				// } else {

				// if the block has non-standard unicode characters, do
				// something.
				// llike throw an error or something.
				if (cont
						&& s.replaceAll("[^\\p{L}\\p{N}]", "").compareTo(s) != 0) {
					tk.Type = TokenType.Error;
					tk.Note = "Invalid character in token";
				} else {
					// check to see if it's fully numeric.
					if (cont && IsNumeric(s)) {
						// if it is, process it as a number. (floats are
						// valid
						// numbers)
						tk.Type = TokenType.Num;
					} else
					// check to see if its a word.
					// if it is, process it as an identifier.
					if (cont && IsValidVarName(s)) {
						tk.Type = TokenType.ID;
					}
				}
				// }
			}

			tk.Depth = _blockDepth;

			List<Token> tokens = new ArrayList<Token>();
			tokens.add(tk);

			return tokens;
		}
	}

	public List<Token> ParseComment(String s) {
		// add logic to parse comments.

		if (s.startsWith("//") && _commentDepth == 0) {
			_commentDepth = 1;
			_isFullLineComment = true;
			return new ArrayList<Token>();

		} else if (s.startsWith("/*")) {
			_commentDepth++;
		}

		if (s.endsWith("*/")) {
			if (_commentDepth > 0) {
				_commentDepth--;
			} else
				return TokenizeString(SpaceifyStringContainingOperators(s,
						AssignmentOperators));
		}

		return new ArrayList<Token>();
	}

	// separate any operator from a valuable string with a space.
	public String SpaceifyStringContainingOperators(String s, List<String> ops) {
		for (String o : ops) {
			s = s.replaceAll("\\" + o, " " + o + " ");
		}
		return s;
	}

	// determines if a member of an operator list is in a string.
	public boolean OperatorInString(String s, List<String> ops) {

		for (String o : ops) {
			if (s.contains(o)) {
				return true;
			}
		}

		return false;
	}

	public boolean IsNumeric(String s) {
		return s.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
	}

	public boolean IsValidVarName(String s) {
		// return s.matches("[^a-zA-Z0-9]");
		return true;
	}

	private static int CountOccurrences(String haystack, char needle) {
		int count = 0;
		for (int i = 0; i < haystack.length(); i++) {
			if (haystack.charAt(i) == needle) {
				count++;
			}
		}
		return count;
	}

}

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

	// Split Sets of Operators. Used to parse code.
	// Seemed like a good idea at the time.
	public List<String> Keywords = Arrays.asList("else", "if", "int", "float",
			"void", "return", "void", "while");
	public List<String> ArithmeticOperators = Arrays.asList("+", "-");
	public List<String> AssignmentOperators = Arrays.asList("*", "/", "=",
			";", ",");
	public List<String> LogicOperators = Arrays.asList("<=", ">", ">=", "==", "<",
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
				if (_commentDepth > 0 || _isFullLineComment)
					_isComment = true;
				else {
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

		if (_isFullLineComment == true) {
			_isFullLineComment = false;
			_commentDepth = 0;
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
		operators.put("CommentOperators", CommentOperators);
		operators.put("AssignmentOperators", AssignmentOperators);
		operators.put("LogicOperators", LogicOperators);
		operators.put("BlockOperators", BlockOperators);
		operators.put("ArgumentOperators", ArgumentOperators);
		operators.put("ArithmeticOperators", ArithmeticOperators);

		List<Token> genTokens = new ArrayList<Token>();

		if (s.contains(" ")) {
			// if the string contains spaces, parse it like you would a line.
			for (String subS : s.split(" ")) {
				subS = subS.trim();
				if (subS.length() > 0)
					if (_commentDepth == 0
							&& !OperatorInString(subS, CommentOperators)) {
						genTokens.addAll(TokenizeString(subS));
					} else {

						genTokens.addAll(ParseComment(subS));
					}
			}
			return genTokens;
		} else {
			boolean cont = true;
			Token tk = new Token();
			tk.ID = s;

			// short circuits the code when a
			if (Keywords.contains(s)) {
				tk.Type = TokenType.Keyword;
			} else if (AssignmentOperators.contains(s)) {
				tk.Type = TokenType.Assignment;
			} else if (LogicOperators.contains(s)) {
				tk.Type = TokenType.Logic;
			} else if (ArithmeticOperators.contains(s)) {
				tk.Type = TokenType.Arithmetic;
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

				// if the block has non-standard unicode characters, do
				// something.
				// llike throw an error or something.
				if (!IsNumeric(s)) {
					for (List<String> ops : operators.values()) {
						if (OperatorInString(s, ops)) {
							return TokenizeString(SpaceifyStringContainingOperators(
									s, ops));
						}
					}
				} else if (cont
						&& s.replaceAll("[^\\p{L}\\p{N}]", "").compareTo(s) != 0
						&& !IsNumeric(s)) {
					tk.Type = TokenType.Error;
					tk.Note = "Invalid character in token";
				}

				// check to see if it's fully numeric.
				if (cont && IsNumeric(s)) {
					// if it is, process it as a number. (floats are
					// valid
					// numbers)
					if(IsInt(s))
						tk.Type = TokenType.Int;
					else
						tk.Type = TokenType.Float;
				} else
				// check to see if its a word.
				// if it is, process it as an identifier.
				if (cont && IsValidVarName(s)) {
					tk.Type = TokenType.ID;
				} else {
					tk.Type = TokenType.Error;
					tk.Note = "Invalid Token. Tokens must be either fully alpha, fully numeric, operators or keywords.";
				}
			}
			// }

			tk.Depth = _blockDepth;

			List<Token> tokens = new ArrayList<Token>();
			tokens.add(tk);

			return tokens;
		}
	}

	public List<Token> ParseComment(String s) {
		// add logic to parse comments.

		String subS = "";

		if ((s.contains("/*") || s.contains("*/"))
				&& !(s.compareTo("/*") == 0 || (s.compareTo("*/") == 0))) {

			for (int i = 0; i < s.length() - 1; i++) {
				String s2 = s.substring(i, i + 2);
				if (s2.compareTo("/*") == 0 || s2.compareTo("*/") == 0) {
					i++;
					subS += " " + s2 + " ";
				}

				else
					subS += s.toCharArray()[i];
			}

			if (!(s.endsWith("/*") || s.endsWith("*/")))
				subS += s.substring(s.length() - 1);

			s = subS;
			return TokenizeString(s);
		}

		if (s.startsWith("//") && _commentDepth == 0) {
			_commentDepth = 1;
			_isFullLineComment = true;
			return new ArrayList<Token>();

		}

		if (s.compareTo("/*") == 0) {
			_commentDepth += 1;
		} else if (s.compareTo("*/") == 0) {
			if (_commentDepth > 0)
				_commentDepth -= 1;
			else
				return TokenizeString(SpaceifyStringContainingOperators(s,
						AssignmentOperators));
		}

		return new ArrayList<Token>();
	}

	// separate any operator from a valuable string with a space.
	public String SpaceifyStringContainingOperators(String s, List<String> ops) {
		for (String o : ops) {
			try {
				s = s.replaceAll(o, " " + o + " ");
			} catch (Exception ex) {
				s = s.replaceAll("\\" + o, " " + o + " ");
			}
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

	public boolean IsNumeric(String s){
		return s.matches("[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?");
	}
	
	// checks to see if the input string is a number.
	// Valid numbers are:
	// integers
	// floats: (+/-)numbers(optional .Numbers)(optional E/e[Numbers])
	// valid floats: 5E10 1.5e5 1.4,
	// invalid floats 1.E, 1., 1E
	public boolean IsInt(String s) {

		return s.matches("^\\d+$");
	}

	// checks to see if the input string a variable
	// is valid. Valid vars are any string that starts with
	// a letter and contains only letters and numbers.
	public boolean IsValidVarName(String s) {
		// alphanum valid varname
		return s.matches("^[a-zA-Z]+[a-zA-Z0-9]?+$");
		// alpha valid varname
		// return s.matches("^[a-zA-Z]+$");
	}

}

package proj3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Proj1.*;

public class LexicalAnalyzer {

	public static List<Token> SymbolTable = new ArrayList<Token>();

	private static boolean _isValid = true;

	private static Token t;
	private static List<Token> _tokens = new ArrayList<Token>();
	private static int _tokenIndex = 0;

	public static boolean LexicallyAnalyzeSource(List<SourceLine> source) {

		for (SourceLine s : source) {
			try {
				for (Token t : s.Tokens) {
					t.SourceLineNumber = s.LineNumber;
					t.SourceLine = s.SourceCode;
				}
				_tokens.addAll(s.Tokens);
			} catch (Exception e) {
				_isValid = false;
			}
		}

		// add an end-of-file token.
		Token eof = new Token();
		eof.Type = TokenType.EOF;
		_tokens.add(eof);

		// push the first token onto the stack and start processing
		try {
			if (_tokens.size() > 0) {
				_tokenIndex = -1;
				Pop();
				// loop until all of the tokens are processed.
				while (t.Type != TokenType.EOF) {
					dec_list();
				}
			}
		} catch (TokenizationDoneException e) {
			_isValid = true;
		} catch (UnexpectedTokenException e) {
			System.out.println("Error parsing input file: Unexpected Token");
			System.out.println("Error on line " + t.SourceLineNumber + ": "
					+ t.SourceLine);
			System.out.println("Error on token: " + t.ID);
			_isValid = false;
		} catch (InvalidEndOfFileException e) {
			System.out.println("Error parsing input file: Invalid end of file");
		} catch (Exception e) {
			System.out.println("Error parsing input file:" + e.getMessage());
			System.out.println("Error on line: " + t.SourceLine);
			System.out.println("Error on token: " + t.ID);
			System.out
					.println("token " + _tokenIndex + " of " + _tokens.size());
			_isValid = false;

		}

		return _isValid;
	}

	public static TokenType dec_list() throws Exception {

		if (t.Type == TokenType.Keyword) {
			if (AreStringsSimilar(t.ID, "int")
					|| AreStringsSimilar(t.ID, "void")
					|| AreStringsSimilar(t.ID, "float")) {
				declaration();
				dec_list_p();
			} else {
				throw new UnexpectedTokenException();
			}
		}

		return TokenType.Unknown;
	}

	public static TokenType dec_list_p() throws Exception {
		// 2: B'=> C B'
		if (t.Type == TokenType.EOF) {
			throw new TokenizationDoneException();
		} else if (AreStringsSimilar(t.ID, "int")
				|| AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			declaration();
			dec_list_p();
		} else if (t.Type == TokenType.EOF) {
			throw new TokenizationDoneException();
		}

		return TokenType.Unknown;
	}

	public static TokenType declaration() throws Exception {

		if (!IsTokenInSet(Arrays.asList("int", "float", "void"))) {
			throw new UnexpectedTokenException();
		}
		// 4: C->E id C'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			type_spec();
			if (!IsTokenInSymbolTable(t)) {
				throw new UnexpectedTokenException();
			} else {
				
				TokenType meta = t.Metatype;
				TokenType rt = t.ReturnType;

				Pop();

				TokenType returnType = dec_p();

				if (t.Metatype == TokenType.Function && returnType != rt) {
					throw new InvalidTypeException();
				}
			}
		}
		// 3: C => empty

		return TokenType.Unknown;
	}

	public static TokenType dec_p() throws Exception {

		if (!IsTokenInSet(Arrays.asList("(", "[", ";"))) {
			throw new UnexpectedTokenException();
		}

		// 6: C' => (G)J
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			params();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				compound_stmt();
			} else {
				throw new UnexpectedTokenException();
			}

			// J();
		}
		// 5: C'=>D'
		else if (AreStringsSimilar(t.ID, "[") || AreStringsSimilar(t.ID, ";")) {
			var_dec_p();
		}
		return TokenType.Unknown;
	}

	public static TokenType var_dec() throws Exception {
		// 8: D=> E id D'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			type_spec();
			if (!IsTokenInSymbolTable(t)) {
				throw new UnexpectedTokenException();
			} else {
				Pop();
				var_dec_p();
			}

		}
		return TokenType.Unknown;
	}

	public static TokenType var_dec_p() throws Exception {
		// 9: D'-> ;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
			return TokenType.Unknown;
		}
		// 10: D'=> [ number ] ;
		else {
			if (AreStringsSimilar(t.ID, "[")) {
				Pop();
				if (t.Type == TokenType.Int) {
					Pop();
					if (AreStringsSimilar(t.ID, "]")) {
						Pop();
						if (AreStringsSimilar(t.ID, ";")) {
							Pop();
							return TokenType.Unknown;
						} else
							throw new UnexpectedTokenException();
					}

					else
						throw new UnexpectedTokenException();
				} else {
					throw new UnexpectedTokenException();
				}
			} else {
				throw new UnexpectedTokenException();
			}

		}

	}

	public static TokenType type_spec() throws Exception {
		// 11. E=>int
		// 12. E->float
		// 13. E->void

		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			Pop();
			return ConvertStringToType(t.ID);
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static TokenType params() throws Exception {
		// 14 G=> int G'
		// 15 G=> float G'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "float")) {
			Pop();
			return params_p();
		} else
		// 16 G=> void
		if (AreStringsSimilar(t.ID, "void")) {
			Pop();
			// special case of passing in a void parameter
			if (IsTokenInSymbolTable(t)) {
				return params_p();
			}
			return TokenType.Unknown;
		} else {
			throw new Exception();
		}

	}

	public static TokenType params_p() throws Exception {
		// 17: G'-> id I' H'
		if (IsTokenInSymbolTable(t)) {
			Pop();
			param_p();
			param_list();
		} else {
			throw new SymbolNotFoundException();
		}
		return TokenType.Unknown;
	}

	public static TokenType param_list() throws Exception {

		if (!IsTokenInSet(Arrays.asList(")", ","))) {
			throw new UnexpectedTokenException();
		}

		// 18: H-'>, I H'
		if (AreStringsSimilar(t.ID, ",")) {
			Pop();
			param();
			param_list();
		}

		// 19: H'-> empty
		return TokenType.Unknown;
	}

	public static TokenType param() throws Exception {
		// 20 I=> E id I'
		type_spec();
		if (IsTokenInSymbolTable(t)) {
			Pop();
			param_p();
		} else {
			throw new SymbolNotFoundException();
		}
		return TokenType.Unknown;
	}

	public static TokenType param_p() throws Exception {
		if (!IsTokenInSet(Arrays.asList("[", ")", ","))) {
			throw new UnexpectedTokenException();
		}

		// 21: I'-> []
		if (AreStringsSimilar(t.ID, "[")) {
			Pop();
			if (AreStringsSimilar(t.ID, "]")) {
				Pop();
				return TokenType.Unknown;
			}
		}
		// 22: I'-> empty
		return TokenType.Unknown;
	}

	public static TokenType compound_stmt() throws Exception {
		// 23 J-> { K' L' }

		if (AreStringsSimilar(t.ID, "{")) {
			Pop();
			local_declaration();
			TokenType rStmt = stmt_list();
			if (AreStringsSimilar(t.ID, "}")) {
				Pop();
				return rStmt;
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static TokenType local_declaration() throws Exception {
		return local_declaration_p();

	}

	public static TokenType local_declaration_p() throws Exception {
		if (!(IsTokenInSet(Arrays.asList("int", "float", "void", "(", "{", "}",
				"if", "while", "return"))
				|| t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}
		// 24: K'-> D K'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			var_dec();
			return local_declaration();
		}

		// 25 K'->empty
		return TokenType.Unknown;
	}

	public static TokenType stmt_list() throws Exception {
		return stmt_list_p();

	}

	public static TokenType stmt_list_p() throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(", "{", "if", "while", "return",
				"}", ";"))
				|| t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}

		// 27 L'-> empty
		if (AreStringsSimilar(t.ID, "}")) {
			return TokenType.Unknown;
		} else {
			// 26 L'-> M L'
			statement();
			return stmt_list();
		}

	}

	public static TokenType statement() throws Exception {

		// 28: M-> N (applies to Float, Int, id, (, ; )
		if (AreStringsSimilar(t.ID, "(") || IsTokenInSymbolTable(t)
				|| AreStringsSimilar(t.ID, ";") || t.Type == TokenType.Float
				|| t.Type == TokenType.Int) {
			return expression_stmt();
		} else
		// 29: M->J
		if (AreStringsSimilar(t.ID, "{")) {
			return compound_stmt();
		} else
		// 30: M-> O
		if (AreStringsSimilar(t.ID, "if")) {
			return selection_stmt();
		} else
		// 31 M->P
		if (AreStringsSimilar(t.ID, "while")) {
			return iteration_stmt();
		} else
		// 32 M->Q
		if (AreStringsSimilar(t.ID, "return")) {
			return return_stmt();
		} else {
			throw new UnexpectedTokenException();
		}

	}

	public static TokenType expression_stmt() throws Exception {
		// 33: N-> R ;
		if (AreStringsSimilar(t.ID, "(") || IsTokenInSymbolTable(t)
				|| t.Type == TokenType.Float || t.Type == TokenType.Int) {
			return expression();
		}

		// 34: N-> ;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return TokenType.Unknown;
	}

	public static TokenType selection_stmt() throws Exception {
		// 35: O-> if( R ) M O'
		if (AreStringsSimilar(t.ID, "if")) {
			Pop();
			if (AreStringsSimilar(t.ID, "(")) {
				Pop();
				expression();
				if (AreStringsSimilar(t.ID, ")")) {
					Pop();
					statement();
					selection_stmt_p();
					return TokenType.Unknown;
				} else {
					throw new UnexpectedTokenException();
				}
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static TokenType selection_stmt_p() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("(", "{", "if", "while", "return",
				"}", "else"))
				|| IsTokenInSymbolTable(t)
				|| t.Type == TokenType.Int || t.Type == TokenType.Float)) {
			throw new UnexpectedTokenException();
		}
		// 36: O'->else M
		if (AreStringsSimilar(t.ID, "else")) {
			Pop();
			statement();
		}

		// 37: O'-> empty
		return TokenType.Unknown;
	}

	public static TokenType iteration_stmt() throws Exception {
		// 38: P-> while ( R ) M
		if (AreStringsSimilar(t.ID, "while")) {
			Pop();
			if (AreStringsSimilar(t.ID, "(")) {
				Pop();
				expression();
				if (AreStringsSimilar(t.ID, ")")) {
					Pop();
					statement();
					return TokenType.Unknown;
				} else {
					throw new UnexpectedTokenException();
				}
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			throw new UnexpectedTokenException();
		}

	}

	public static TokenType return_stmt() throws Exception {
		// 39: Q->return Q'
		if (AreStringsSimilar(t.ID, "return")) {
			Pop();
			return return_stmt_p();
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static TokenType return_stmt_p() throws Exception {
		// 41: Q'=>R;
		if (IsTokenInSymbolTable(t) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || AreStringsSimilar(t.ID, "(")) {
			return expression();
		}
		// 40: Q'=>;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
			return TokenType.Void;
		} else {
			throw new UnexpectedTokenException();
		}

	}

	public static TokenType expression() throws Exception {

		// 43: R-> id R'
		if (IsTokenInSymbolTable(t)) {
			Pop();
			expression_p();
		} else
		// 44: R=> (R) X' V' T'
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			expression();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				term_p();
				add_exp();
				simple_expression();
			} else {
				throw new UnexpectedTokenException();
			}
		} else // 45: R-> num X' V' T'
				// 46: R-> floatnum X' V' T'
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			Pop();
			term_p();
			add_exp();
			simple_expression();
		} else {

			throw new UnexpectedTokenException();
		}

		return TokenType.Unknown;
	}

	public static TokenType expression_p() throws Exception {

		if (!AreStringsSimilar(t.ID, "(")) {
			// 47: R'-> S' R''
			var();
			expression_pp();
		} else {
			// 48: R'-> (AB)X'V'T'
			Pop();
			args();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				term_p();
				add_exp();
				simple_expression();
			} else {
				throw new UnexpectedTokenException();
			}
		}
		return TokenType.Unknown;
	}

	public static TokenType expression_pp() throws Exception {
		// 49: R''=> = R
		if (AreStringsSimilar(t.ID, "=")) {
			Pop();
			return expression();
		}
		// 50: R''-> X' V' T'
		else {
			TokenType type = term_p();
			if (type != add_exp()) {
				throw new InvalidTypeException();
			}
			add_exp();
			simple_expression();
		}
		return TokenType.Unknown;
	}

	public static TokenType var() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("=", "*", "/", "+", "-", "[", ";",
				"(", ")", ",", "]")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}

		// 51: S'-> [R]
		if (AreStringsSimilar(t.ID, "[")) {
			Pop();
			expression();
			if (AreStringsSimilar(t.ID, "]")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		} else if (AreStringsSimilar(t.ID, "(")) {
			call();
		}

		// 52 S' -> empty
		return TokenType.Unknown;
	}

	public static TokenType simple_expression() throws Exception {

		if (!(IsTokenInSet(Arrays
				.asList("]", ")", ";", ",", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 53: T'-> U V T'
		if (t.Type == TokenType.Logic) {
			logic_op();
			V();
			simple_expression();
		}

		// 54: T'-> empty
		return TokenType.Unknown;
	}

	public static TokenType logic_op() throws Exception {
		// 55-60: U=> Logic Operator
		if (t.Type == TokenType.Logic) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return TokenType.Unknown;
	}

	public static TokenType V() throws Exception {
		// 61: V-> X V'
		term();
		add_exp();
		return TokenType.Unknown;
	}

	public static TokenType add_exp() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("+", "-", ";", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 62: V'-> W X V'
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {
			add_op();
			term();
			add_exp();
		}

		// 63: V'-> EMPTY
		return TokenType.Unknown;
	}

	public static TokenType add_op() throws Exception {
		// 64: W=>+; 65: W=>-
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return TokenType.Unknown;
	}

	public static TokenType term() throws Exception {
		// 66: X-> Z X' |

		factor();
		term_p();
		return TokenType.Unknown;
	}

	public static TokenType term_p() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("+", "-", "*", "/", ";", ")", ",",
				"]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}

		// 67: X'-> Y Z X'
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			TokenType t1 = mulop();
			TokenType t2 = factor();
			if (t1 != t2)
				throw new InvalidTypeException();
			term_p();
		}

		// 68: X'-> EMPTY
		return TokenType.Unknown;
	}

	public static TokenType mulop() throws Exception {
		// 67, 70: Y-> *|/
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}
		return TokenType.Unknown;
	}

	public static TokenType factor() throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(")) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}
		// 71: Z->(R)
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			expression();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		} else
		// 72: Z->id S'
		if (IsTokenInSymbolTable(t)) {
			Pop();
			var();

		} else
		// 73, 74: Z-> num, floatnum
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			Pop();
		} else {
			call();
		}
		return TokenType.Unknown;
	}

	public static TokenType call() throws Exception {

		// 76:Z'->(delta)
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			args();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			// 75: Z'-> S'
			var();
		}
		return TokenType.Unknown;
	}

	public static TokenType args() throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(", ")")) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}

		// 78: DELTA->empty
		if (AreStringsSimilar(t.ID, ")")) {
			return TokenType.Unknown;
		} else
		// 77: DELTA -> Beta
		{
			args_list();
		}
		return TokenType.Unknown;
	}

	public static TokenType args_list() throws Exception {
		// 79: beta-> R gamma
		expression();
		args_list_p();
		return TokenType.Unknown;
	}

	public static TokenType args_list_p() throws Exception {

		if (!(IsTokenInSet(Arrays.asList(";", ",", ")")))) {
			throw new UnexpectedTokenException();
		}

		// 80:; gamma->, R gamma
		if (AreStringsSimilar(t.ID, ",")) {
			Pop();
			expression();
			args_list_p();
		}
		// 81: GAMMA -> empty
		return TokenType.Unknown;

	}

	private static void Pop() throws Exception {
		_tokenIndex++;
		if (_tokenIndex >= _tokens.size()) {
			throw new InvalidEndOfFileException();
		}

		t = _tokens.get(_tokenIndex);
		// t.ID = t.ID.toUpperCase();
	}

	private static boolean AreStringsSimilar(String s1, String s2) {
		return s1.compareToIgnoreCase(s2) == 0;
	}

	private static boolean IsTokenInSymbolTable(Token t) {
		for (Token s : SymbolTable) {
			if (s.ID.compareTo(t.ID) == 0)
				return true;
		}

		return false;
	}

	private static boolean IsTokenInSet(List<String> set) {

		for (String s : set) {
			if (AreStringsSimilar(t.ID, s))
				return true;
		}

		// if the string wasn't in the set, return false
		return false;
	}

	private static TokenType ConvertStringToType(String type) {
		switch (type.toLowerCase()) {
		case "id":
			return TokenType.ID;
		case "int":
			return TokenType.Int;
		case "void":
			return TokenType.Void;
		case "float":
			return TokenType.Float;
		default:
			return TokenType.Unknown;
		}
	}

}

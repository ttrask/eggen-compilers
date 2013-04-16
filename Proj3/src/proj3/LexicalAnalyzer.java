package proj3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

import exception.*;

import Proj1.*;

public class LexicalAnalyzer {

	public static List<Token> SymbolTable = new ArrayList<Token>();

	private static boolean _isValid = true;
	private static boolean _isFunctionCall = false;
	private static Map<String, List<TokenType>> _functionParams = new HashMap<String, List<TokenType>>();
	private static int _currentFunctionCallId = -1;
	private static Token t, lastT, nextT;

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
			return _isValid;

		} catch (LocalException e) {
			System.out.println("Error parsing input file: "
					+ e.ExceptionMessage);
			System.out.println("Error on line " + t.SourceLineNumber + ": "
					+ t.SourceLine);
			System.out.println("Error on token: " + t.ID);
			_isValid = false;
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

				TokenType meta = TokenType.valueOf(t.Metatype.toString());
				TokenType rt = TokenType.valueOf(t.ReturnType.toString());
				Pop();
				TokenType returnType = dec_p();

				// added for void return types with no return statement.
				if (!(returnType == TokenType.Unknown && rt == TokenType.Void))
					if (meta == TokenType.Function && returnType != rt) {
						throw new InvalidTypeException(
								"Invalid return type.  Expecting: " + rt
										+ "; Returned " + returnType);
					}
			}
		}
		// 3: C => empty

		return TokenType.Unknown;
	}

	public static TokenType dec_p() throws Exception {
		TokenType t1 = TokenType.Unknown;
		if (!IsTokenInSet(Arrays.asList("(", "[", ";"))) {
			throw new UnexpectedTokenException();
		}

		// 6: C' => (G)J
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			params();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				// get return type of compound statement;
				t1 = compound_stmt();
			} else {
				throw new UnexpectedTokenException();
			}

			// J();
		}
		// 5: C'=>D'
		else if (AreStringsSimilar(t.ID, "[") || AreStringsSimilar(t.ID, ";")) {
			t1 = var_dec_p();
		}
		return t1;
	}

	public static TokenType var_dec() throws Exception {
		// 8: D=> E id D'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			type_spec();
			if (!IsTokenInSymbolTable(t)) {
				throw new UnexpectedTokenException();
			} else {

				boolean isArray = t.IsArray;

				Pop();
				if ((isArray && t.ID.compareTo("[") != 0)
						|| (!isArray && t.ID.compareTo("[") == 0))
					throw new LocalException("Array index improperly used");
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
				int depth = t.Depth;

				if (t.TokenId > 0) {
					Token func = GetParentFunction(t.TokenId);

					if (depth == 0) {

						Token t2 = lastT;
						Pop();
						return GetLastMetatype(t2);
					}
					// TokenType.Void;
				}

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
			TokenType t1 = statement();
			TokenType t2 = stmt_list();
			if (t2 == TokenType.Float || t2 == TokenType.Int
					|| t2 == TokenType.Void) {
				return t2;
			} else
				return t1;
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
			TokenType t1 = return_stmt();
			Token parentFunc = GetParentFunction(t.TokenId);
			if (parentFunc != null) {
				CheckTypeConcurrency(t1, parentFunc.ReturnType);
			}
			return t1;
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
					return selection_stmt_p();

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

		if (!(IsTokenInSet(Arrays.asList("(", "{", "if", "while", "return", ";",
				"}", "else"))
				|| IsTokenInSymbolTable(t)
				|| t.Type == TokenType.Int || t.Type == TokenType.Float)) {
			throw new UnexpectedTokenException();
		}
		// 36: O'->else M
		if (AreStringsSimilar(t.ID, "else")) {
			Pop();
			return statement();
		}

		// 37: O'-> empty
		return GetLastMetatype();
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
		TokenType t1, t2;

		// 43: R-> id R'
		if (IsTokenInSymbolTable(t)) {

			if (t.Metatype == TokenType.Function)
				t1 = GetFunctionToken(t.ID).ReturnType;
			else
				t1 = TokenType.valueOf(t.Metatype.toString());

			boolean isArray = t.IsArray;

			Pop();
			if ((isArray && t.ID.compareTo("[") != 0)
					|| (!isArray && t.ID.compareTo("[") == 0))
				throw new LocalException("Array index improperly used");
			t2 = expression_p();
			CheckTypeConcurrency(t1, t2);

			return t2;
		} else
		// 44: R=> (R) X' V' T'
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			t1 = expression();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				t2 = term_p();
				CheckTypeConcurrency(t1, t2);
				t2 = add_exp();
				CheckTypeConcurrency(t1, t2);
				t2 = simple_expression();
				CheckTypeConcurrency(t1, t2);
				return t1;
			} else {
				throw new UnexpectedTokenException();
			}
		} else // 45: R-> num X' V' T'
				// 46: R-> floatnum X' V' T'
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			t1 = t.Type;
			Pop();
			t2 = term_p();
			CheckTypeConcurrency(t1, t2);
			t2 = add_exp();
			CheckTypeConcurrency(t1, t2);
			t2 = simple_expression();
			CheckTypeConcurrency(t1, t2);
			return t1;
		} else {

			throw new UnexpectedTokenException();
		}

	}

	public static TokenType expression_p() throws Exception {

		TokenType t1, t2;
		if (!AreStringsSimilar(t.ID, "(")) {
			// 47: R'-> S' R'
			t1 = var();

			t2 = expression_pp();
			CheckTypeConcurrency(t1, t2);
			return t2;
		} else {
			// 48: R'-> (AB)X'V'T'

			Token func = GetFunctionToken(lastT.ID);
			int previousFuncId = _currentFunctionCallId;
			_currentFunctionCallId = lastT.TokenId;
			Pop();

			_isFunctionCall = true;

			_functionParams.put(Integer.toString(_currentFunctionCallId),
					new ArrayList<TokenType>());

			args();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				List<TokenType> params = _functionParams.get(Integer
						.toString(_currentFunctionCallId));
				// add logic to deal with function call parameters.
				if (func.FuncParams.size() != params.size())
					throw new LocalException(
							"Invalid number of function parameters.");
				for (int i = 0; i < func.FuncParams.size(); i++) {

					if (func.FuncParams.get(i) != params.get(i))
						throw new LocalException(
								"Invalid function parameter at:" + i);
				}
				_isFunctionCall = false;
				_functionParams
						.remove(Integer.toString(_currentFunctionCallId));
				Token t3 =GetTokenById(_currentFunctionCallId-1); 
				if (t3.ID.compareTo(";") == 0 || t3.ID.compareTo("{") == 0) {
					_currentFunctionCallId = previousFuncId;
					return func.ReturnType;
				} else {

					_currentFunctionCallId = previousFuncId;

					t1 = term_p();
					CheckTypeConcurrency(t1, func.ReturnType);
					t2 = add_exp();
					CheckTypeConcurrency(t2, func.ReturnType);
					simple_expression();
					return t2;
				}
			} else {
				throw new UnexpectedTokenException();
			}
		}
	}

	public static TokenType expression_pp() throws Exception {
		// 49: R''=> = R
		if (AreStringsSimilar(t.ID, "=")) {
			Pop();
			return expression();
		}
		// 50: R''-> X' V' T'
		else {
			TokenType t1, t2;
			t1 = term_p();
			t2 = add_exp();
			CheckTypeConcurrency(t1, t2);
			simple_expression();
			return t1;
		}
	}

	public static TokenType var() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("=", "*", "/", "+", "-", "[", ";",
				"(", ")", ",", "]")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		TokenType t1 = GetLastMetatype();
		// 51: S'-> [R]
		if (AreStringsSimilar(t.ID, "[")) {
			Pop();
			if (expression() != TokenType.Int) {
				throw new InvalidTypeException();
			}

			if (AreStringsSimilar(t.ID, "]")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		} else if (AreStringsSimilar(t.ID, "(")) {
			// function call
			call();
		}

		// 52 S' -> empty
		return t1;
	}

	public static TokenType simple_expression() throws Exception {

		if (!(IsTokenInSet(Arrays
				.asList("]", ")", ";", ",", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 53: T'-> U V T'
		if (t.Type == TokenType.Logic) {
			TokenType t1, t2;
			t1 = GetLastMetatype();
			logic_op();
			t2 = V();
			CheckTypeConcurrency(t1, t2);
			simple_expression();
			return t1;
		}

		// 54: T'-> empty
		return GetLastMetatype();
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
		TokenType t1, t2;
		t1 = term();
		t2 = add_exp();
		CheckTypeConcurrency(t1, t2);
		return t1;
	}

	public static TokenType add_exp() throws Exception {
		TokenType t1, t2;
		if (!(IsTokenInSet(Arrays.asList("+", "-", ";", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 62: V'-> W X V'
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {
			t2 = GetLastMetatype();
			add_op();
			t1 = term();
			CheckTypeConcurrency(t1, t2);

			add_exp();

			return t1;
		}

		// 63: V'-> EMPTY
		if (t.Type != TokenType.Int || t.Type != TokenType.Int
				|| t.Type == TokenType.Void)
			return GetLastMetatype();
		else
			return t.Type;
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
		TokenType t1, t2;
		t1 = factor();
		t2 = term_p();

		if (t2 != TokenType.Int && t2 != TokenType.Float) {
			return t1;
		} else
			CheckTypeConcurrency(t1, t2);
		return t2;
	}

	public static TokenType term_p() throws Exception {
		TokenType t1, t2;
		if (!(IsTokenInSet(Arrays.asList("+", "-", "*", "/", ";", ")", ",",
				"]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}

		// 67: X'-> Y Z X'
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			t1 = GetLastMetatype();
			mulop();
			t2 = factor();

			CheckTypeConcurrency(t1, t2);

			return term_p();
		}

		// 68: X'-> EMPTY
		return GetLastMetatype();
	}

	public static TokenType mulop() throws Exception {
		// 67, 70: Y-> *|/
		TokenType t1, t2;
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}
		return TokenType.Unknown;
	}

	public static TokenType factor() throws Exception {
		TokenType t1, t2;
		if (!(IsTokenInSet(Arrays.asList("(")) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}
		// 71: Z->(R)
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			t1 = expression();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				return t1;
			} else {
				throw new UnexpectedTokenException();
			}
		} else
		// 72: Z->id S' { id[int] }
		if (IsTokenInSymbolTable(t)) {
			t1 = t.Type;
			boolean isArray = t.IsArray;

			Pop();
			if ((isArray && t.ID.compareTo("[") != 0)
					|| (!isArray && t.ID.compareTo("[") == 0))
				throw new LocalException("Array index improperly used");
			t2 = var();
			return t2;

		} else
		// 73, 74: Z-> num, floatnum
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			t1 = t.Type;
			Pop();
			return t1;
		} else {
			// function call
			Token func = GetFunctionFromSymbolTable(t);
			t1 = t.ReturnType;
			call();
			return t1;

		}
	}

	public static TokenType call() throws Exception {

		// 76:Z'->(delta)

		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			_isFunctionCall = true;

			args();
			if (AreStringsSimilar(t.ID, ")")) {
				_isFunctionCall = false;
				Pop();
				return TokenType.Unknown;
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			// 75: Z'-> S'
			TokenType t1 = var();
			return t1;
		}

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
		_functionParams.get(Integer.toString(_currentFunctionCallId)).add(
				expression());
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
			_functionParams.get(Integer.toString(_currentFunctionCallId)).add(
					expression());
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

		lastT = t;
		t = _tokens.get(_tokenIndex);
		if (_tokenIndex < _tokens.size() - 1)
			nextT = _tokens.get(_tokenIndex + 1);
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

	private static Token GetFunctionFromSymbolTable(Token s)
			throws LocalException {
		for (Token r : SymbolTable) {
			if (r.ID == s.ID && r.Metatype == TokenType.Function)
				return r;
		}

		throw new SymbolNotFoundException();
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

	private static Boolean CheckTypeConcurrency(TokenType t1, TokenType t2)
			throws LocalException {
		if (t1 != t2)
			throw new InvalidTypeException();
		return true;
	}

	private static TokenType GetLastMetatype() {
		return GetLastMetatype(t);
	}

	private static TokenType GetLastMetatype(Token s) {
		int arrayIndex = 0;
		int funcCallIndex = 0;
		int offset = 0;
		String currentid = s.ID;
		int _index = s.TokenId - 1;
		if (_index > 0
				&& (currentid.compareTo(";") == 0
						|| currentid.compareTo("]") == 0 || (currentid
						.compareTo(")") == 0)))
			return GetLastMetatype(_tokens.get(s.TokenId - 2));
		for (int i = _index - offset; i > 0; i--) {

			Token temp = _tokens.get(i);
			if (temp.ID.compareTo(";") == 0 || temp.ID.compareTo("}") == 0
					|| temp.ID.compareTo("{") == 0) {
				return TokenType.Unknown;
			} else if (temp.ID.compareTo("]") == 0)
				arrayIndex++;
			else if (temp.ID.compareTo("[") == 0)
				arrayIndex--;
			else if (temp.ID.compareTo(")") == 0)
				funcCallIndex++;
			else if (temp.ID.compareTo("(") == 0)
				funcCallIndex--;

			if (arrayIndex <= 0 && funcCallIndex <= 0) {
				if (temp.Type == TokenType.ID)
					if (temp.Metatype == TokenType.Function) {
						return GetFunctionToken(temp.ID).ReturnType;
					} else
						return temp.Metatype;
				else if (temp.Type == TokenType.Int
						|| temp.Type == TokenType.Void
						|| temp.Type == TokenType.Float)
					return temp.Type;
			}
		}
		return TokenType.Unknown;

	}

	private static Token GetFunctionToken(String id) {
		for (Token s : SymbolTable) {
			if (s.ID.compareTo(id) == 0 && s.Metatype == TokenType.Function) {
				return s;
			}
		}
		return null;
	}

	private static Token GetParentFunction(int id) {

		Token t2 = GetTokenById(id);
		if (t2.Metatype == TokenType.Function)
			return t2;
		else if (t2.ParentId == -1) {
			return t2;
		} else {
			return GetParentFunction(t2.ParentId);
		}
	}

	private static Token GetTokenById(int id) {
		for (Token t : _tokens) {
			if (t.TokenId == id) {
				return t;
			}
		}
		return null;
	}

}

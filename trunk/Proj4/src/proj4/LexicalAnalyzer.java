package proj4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import __proj4.Quadruple;

import exception.InvalidEndOfFileException;
import exception.LocalException;
import exception.SymbolNotFoundException;
import exception.TokenizationDoneException;
import exception.UnexpectedTokenException;

import Proj1.*;

public class LexicalAnalyzer {

	public static List<Token> SymbolTable = new ArrayList<Token>();
	public static List<String> _tempVars = new ArrayList<String>();
	private static int _currentFunctionCallId = -1;
	private static Map<String, List<TokenType>> _functionParams = new HashMap<String, List<TokenType>>();
	public static List<Quadruple> _printStack = new ArrayList<Quadruple>();

	private static boolean _isFunctionCall = false;
	private static List<Quadruple> _quadruples = new ArrayList<Quadruple>();

	private static boolean _isValid = true;

	private static Token t, lastT;
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

	public static String dec_list() throws Exception {

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

		return "";
	}

	public static String dec_list_p() throws Exception {
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

		return "";
	}

	public static String declaration() throws Exception {

		if (!IsTokenInSet(Arrays.asList("int", "float", "void"))) {
			throw new UnexpectedTokenException();
		}

		Quadruple q = new Quadruple();
		q.CallSymbol = "func";
		q.Param2 = t.ID;

		// 4: C->E id C'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			type_spec();

			q.Param1 = t.ID;
			q.Output = Integer.toString(t.FuncParams.size());

			Quadruple funcQ = new Quadruple();

			PrintQuadruple(q);
			if (t.FuncParams.size() > 0) {
				Quadruple q2 = new Quadruple();
				q2.CallSymbol = "params";
				PrintQuadruple(q2);
			}

			funcQ.CallSymbol = "end";
			funcQ.Param2 = q.Param1;
			funcQ.Param1 = "func";
			funcQ.Output = null;

			if (!IsTokenInSymbolTable(t)) {
				throw new UnexpectedTokenException();
			} else {
				Pop();
				dec_p();
				PrintQuadruple(funcQ);
			}

		}
		// 3: C => empty

		return "";
	}

	public static String dec_p() throws Exception {

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
		return "";
	}

	public static String var_dec() throws Exception {
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
		return "";
	}

	public static String var_dec_p() throws Exception {
		// 9: D'-> ;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
			return "";
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
							return "";
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

	public static String type_spec() throws Exception {
		// 11. E=>int
		// 12. E->float
		// 13. E->void

		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			Pop();
			return "";
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static String params() throws Exception {
		// 14 G=> int G'
		// 15 G=> float G'
		Quadruple q = new Quadruple();
		q.CallSymbol = "alloc";
		q.Param1 = "4";

		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "float")) {
			Pop();
			q.Output = t.ID;
			PrintQuadruple(q);
			return params_p();
		} else
		// 16 G=> void
		if (AreStringsSimilar(t.ID, "void")) {
			Pop();
			// special case of passing in a void parameter
			if (IsTokenInSymbolTable(t)) {
				return params_p();
			}
			return "";
		} else {
			throw new Exception();
		}

	}

	public static String params_p() throws Exception {
		// 17: G'-> id I' H'
		if (IsTokenInSymbolTable(t)) {
			Pop();
			param_p();
			param_list();
		} else {
			throw new SymbolNotFoundException();
		}
		return "";
	}

	public static String param_list() throws Exception {

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
		return "";
	}

	public static String param() throws Exception {
		// 20 I=> E id I'
		type_spec();
		if (IsTokenInSymbolTable(t)) {
			Pop();
			param_p();
		} else {
			throw new SymbolNotFoundException();
		}
		return "";
	}

	public static String param_p() throws Exception {
		if (!IsTokenInSet(Arrays.asList("[", ")", ","))) {
			throw new UnexpectedTokenException();
		}

		// 21: I'-> []
		if (AreStringsSimilar(t.ID, "[")) {
			Pop();
			if (AreStringsSimilar(t.ID, "]")) {
				Pop();
				return "";
			}
		}
		// 22: I'-> empty
		return "";
	}

	public static String compound_stmt() throws Exception {
		// 23 J-> { K' L' }

		if (AreStringsSimilar(t.ID, "{")) {
			Pop();
			local_declaration();
			stmt_list();
			if (AreStringsSimilar(t.ID, "}")) {
				Pop();
				return "";
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static String local_declaration() throws Exception {
		local_declaration_p();
		return "";
	}

	public static String local_declaration_p() throws Exception {
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
			local_declaration();
		}

		// 25 K'->empty
		return "";
	}

	public static String stmt_list() throws Exception {
		stmt_list_p();
		return "";
	}

	public static String stmt_list_p() throws Exception {
		if (!(IsTokenInSet(Arrays
				.asList("(", "{", "if", "while", "return", "}"))
				|| t.Type == TokenType.Int || t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}

		// 27 L'-> empty
		if (AreStringsSimilar(t.ID, "}")) {
			return "";
		} else {
			// 26 L'-> M L'
			statement();
			stmt_list();
		}
		return "";
	}

	public static String statement() throws Exception {

		// 28: M-> N (applies to Float, Int, id, (, ; )
		if (AreStringsSimilar(t.ID, "(") || IsTokenInSymbolTable(t)
				|| AreStringsSimilar(t.ID, ";") || t.Type == TokenType.Float
				|| t.Type == TokenType.Int) {
			expression_stmt();
		} else
		// 29: M->J
		if (AreStringsSimilar(t.ID, "{")) {
			compound_stmt();
		} else
		// 30: M-> O
		if (AreStringsSimilar(t.ID, "if")) {
			selection_stmt();
		} else
		// 31 M->P
		if (AreStringsSimilar(t.ID, "while")) {
			iteration_stmt();
		} else
		// 32 M->Q
		if (AreStringsSimilar(t.ID, "return")) {
			return_stmt();
		} else {
			throw new UnexpectedTokenException();
		}

		return "";
	}

	public static String expression_stmt() throws Exception {
		// 33: N-> R ;
		if (AreStringsSimilar(t.ID, "(") || IsTokenInSymbolTable(t)
				|| t.Type == TokenType.Float || t.Type == TokenType.Int) {
			expression();
		}

		// 34: N-> ;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return "";
	}

	public static String selection_stmt() throws Exception {
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
					return "";
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

	public static String selection_stmt_p() throws Exception {

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
		return "";
	}

	public static String iteration_stmt() throws Exception {
		// 38: P-> while ( R ) M
		if (AreStringsSimilar(t.ID, "while")) {
			Pop();
			if (AreStringsSimilar(t.ID, "(")) {
				Pop();
				expression();
				if (AreStringsSimilar(t.ID, ")")) {
					Pop();
					statement();
					return "";
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

	public static String return_stmt() throws Exception {
		// 39: Q->return Q'
		if (AreStringsSimilar(t.ID, "return")) {
			Quadruple r = new Quadruple();
			r.CallSymbol = "return";
			
			Pop();
			r.Output = return_stmt_p();
		} else {
			throw new UnexpectedTokenException();
		}

		return "";
	}

	public static String return_stmt_p() throws Exception {
		// 41: Q'=>R;
		if (IsTokenInSymbolTable(t) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || AreStringsSimilar(t.ID, "(")) {
			return expression();
		}
		// 40: Q'=>;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
			return "";
		} else {
			throw new UnexpectedTokenException();
		}

	}

	public static String expression() throws Exception {
		Quadruple q = new Quadruple();
		// 43: R-> id R'
		if (IsTokenInSymbolTable(t)) {
			if (t.Metatype == TokenType.Function) {
				q.CallSymbol = "call";
				q.Param1 = t.ID;
				Token func = GetFunctionToken(t.ID);
				q.Param2 = Integer.toString(func.FuncParams.size());
				q.Output = "_t" + _tempVars.size();
				_tempVars.add(q.Output);
				_printStack.add(q);
			} else {
				if (_isFunctionCall) {
					q.CallSymbol = "arg";
					q.Output = t.ID;
					_tempVars.add(q.Output);
					_printStack.add(q);
				}
			}
			Pop();
			expression_p();
		} else
		// 44: R=> (R) X' V' T'
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			String s = expression();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				String s2 = term_p();
				if(s2.compareTo(s)==0)
					s = s2;
				s2 = add_exp();
				if(s2.compareTo(s)==0)
					s = s2;
				s2 = simple_expression();
				if(s2.compareTo(s)==0)
					s = s2;
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

		return "";
	}

	public static String expression_p() throws Exception {

		if (!AreStringsSimilar(t.ID, "(")) {
			// 47: R'-> S' R''
			var();
			expression_pp();
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
				PrintStack();
				term_p();
				add_exp();
				simple_expression();

			} else {
				throw new UnexpectedTokenException();
			}

		}

		return "";
	}

	public static String expression_pp() throws Exception {
		// 49: R''=> = R
		if (AreStringsSimilar(t.ID, "=")) {
			Pop();
			expression();
		}
		// 50: R''-> X' V' T'
		else {
			term_p();
			add_exp();
			simple_expression();
		}
		return "";
	}

	public static String var() throws Exception {

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
		return "";
	}

	public static String simple_expression() throws Exception {

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
		return "";
	}

	public static String logic_op() throws Exception {
		// 55-60: U=> Logic Operator
		if (t.Type == TokenType.Logic) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return "";
	}

	public static String V() throws Exception {
		// 61: V-> X V'
		term();
		return add_exp();
	}

	public static String add_exp() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("+", "-", ";", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 62: V'-> W X V'
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {

			Quadruple left = new Quadruple();

			left.CallSymbol = (t.ID.compareTo("+") == 0 ? "add" : "sub");

			left.Param1 = lastT.ID;

			add_op();

			// left.Param2 = t.ID;

			String tempVar = NewTempVar();
			left.Output = tempVar;

			left.Param2 = term();

			_printStack.add(left);
			add_exp();
			return tempVar;
		}

		// 63: V'-> EMPTY
		return "";
	}

	public static String add_op() throws Exception {
		// 64: W=>+; 65: W=>-
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return "";
	}

	public static String term() throws Exception {
		// 66: X-> Z X' |

		String s = factor();

		term_p();

		return s;

	}

	public static String term_p() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("+", "-", "*", "/", ";", ")", ",",
				"]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}

		// 67: X'-> Y Z X'
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			mulop();
			factor();
			term_p();
		}

		// 68: X'-> EMPTY
		return t.ID;
	}

	public static String mulop() throws Exception {
		// 67, 70: Y-> *|/
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}
		return "";
	}

	public static String factor() throws Exception {
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
			String s1 = t.ID;
			Pop();
			String s2 = var();
			if(s2.compareTo("")==0)
				return s1;
			else return s2;

		} else
		// 73, 74: Z-> num, floatnum
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			String s = t.ID;
			Pop();
			return s;
		} else {
			call();
		}
		return "";
	}

	public static String call() throws Exception {

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
		return "";
	}

	public static String args() throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(", ")")) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}

		// 78: DELTA->empty
		if (AreStringsSimilar(t.ID, ")")) {
			return "";
		} else
		// 77: DELTA -> Beta
		{
			args_list();
		}
		return "";
	}

	public static String args_list() throws Exception {
		// 79: beta-> R gamma
		expression();
		args_list_p();
		return "";
	}

	public static String args_list_p() throws Exception {

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
		return "";

	}

	private static void Pop() throws Exception {
		_tokenIndex++;
		if (_tokenIndex >= _tokens.size()) {
			throw new InvalidEndOfFileException();
		}

		lastT = t;
		t = _tokens.get(_tokenIndex);
		
		if(lastT!= null && t.SourceLineNumber != lastT.SourceLineNumber)
			PrintStack();
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

	private static void PrintQuadruple(Quadruple q) {
		q.LineNumber = _quadruples.size() + 1;
		_quadruples.add(q);

		System.out.format("%d\t%s\t%s\t%s\t%s", q.LineNumber, q.CallSymbol,
				(q.Param1 == null ? "" : q.Param1), (q.Param2 == null ? ""
						: q.Param2), (q.Output == null ? "" : q.Output));
		System.out.println();
	}

	private static void PrintStack() {
		for (int i = _printStack.size() - 1; i >= 0; i--) {
			PrintQuadruple(_printStack.get(i));
		}

		_printStack.clear();
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

	private static String NewTempVar() {
		String s = "_t" + _tempVars.size() + 1;
		_tempVars.add(s);
		return s;
	}

}

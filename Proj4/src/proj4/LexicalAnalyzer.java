package proj4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import __proj4.Quadruple;

import exception.*;

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

		String startVar = "";

		if (t.Type == TokenType.Keyword) {
			if (AreStringsSimilar(t.ID, "int")
					|| AreStringsSimilar(t.ID, "void")
					|| AreStringsSimilar(t.ID, "float")) {
				declaration("");
				dec_list_p(startVar);
			} else {
				throw new UnexpectedTokenException();
			}
		}

		return startVar;
	}

	public static String dec_list_p(String startVar) throws Exception {
		// 2: B'=> C B'
		if (t.Type == TokenType.EOF) {
			throw new TokenizationDoneException();
		} else if (AreStringsSimilar(t.ID, "int")
				|| AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			declaration(startVar);
			dec_list_p(startVar);
		} else if (t.Type == TokenType.EOF) {
			throw new TokenizationDoneException();
		}

		return startVar;
	}

	public static String declaration(String startVar) throws Exception {

		if (!IsTokenInSet(Arrays.asList("int", "float", "void"))) {
			throw new UnexpectedTokenException();
		}

		Quadruple q = new Quadruple();
		q.CallSymbol = "func";
		q.Param2 = t.ID;

		// 4: C->E id C'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			type_spec(startVar);

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
				dec_p(startVar);
				PrintQuadruple(funcQ);
			}

		}
		// 3: C => empty

		return startVar;
	}

	public static String dec_p(String startVar) throws Exception {

		if (!IsTokenInSet(Arrays.asList("(", "[", ";"))) {
			throw new UnexpectedTokenException();
		}

		// 6: C' => (G)J
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			params(startVar);
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				compound_stmt(startVar);
			} else {
				throw new UnexpectedTokenException();
			}

			// J();
		}
		// 5: C'=>D'
		else if (AreStringsSimilar(t.ID, "[") || AreStringsSimilar(t.ID, ";")) {
			var_dec_p(startVar);
		}
		return startVar;
	}

	public static String var_dec(String startVar) throws Exception {
		// 8: D=> E id D'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			type_spec(startVar);
			if (!IsTokenInSymbolTable(t)) {
				throw new UnexpectedTokenException();
			} else {
				Pop();
				var_dec_p(startVar);
			}

		}
		return startVar;
	}

	public static String var_dec_p(String startVar) throws Exception {
		// 9: D'-> ;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
			return startVar;
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
							return startVar;
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

	public static String type_spec(String startVar) throws Exception {
		// 11. E=>int
		// 12. E->float
		// 13. E->void

		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			Pop();
			return startVar;
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static String params(String startVar) throws Exception {
		// 14 G=> int G'
		// 15 G=> float G'
		Quadruple q = new Quadruple();
		q.CallSymbol = "alloc";
		q.Param1 = "4";

		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "float")) {
			Pop();
			q.Output = t.ID;
			PrintQuadruple(q);
			return params_p(startVar);
		} else
		// 16 G=> void
		if (AreStringsSimilar(t.ID, "void")) {
			Pop();
			// special case of passing in a void parameter
			if (IsTokenInSymbolTable(t)) {
				return params_p(startVar);
			}
			return startVar;
		} else {
			throw new Exception();
		}

	}

	public static String params_p(String startVar) throws Exception {
		// 17: G'-> id I' H'
		if (IsTokenInSymbolTable(t)) {
			Pop();
			param_p(startVar);
			param_list(startVar);
		} else {
			throw new SymbolNotFoundException();
		}
		return startVar;
	}

	public static String param_list(String startVar) throws Exception {

		if (!IsTokenInSet(Arrays.asList(")", ","))) {
			throw new UnexpectedTokenException();
		}

		// 18: H-'>, I H'
		if (AreStringsSimilar(t.ID, ",")) {
			Pop();
			param(startVar);
			param_list(startVar);
		}

		// 19: H'-> empty
		return startVar;
	}

	public static String param(String startVar) throws Exception {
		// 20 I=> E id I'
		type_spec(startVar);
		if (IsTokenInSymbolTable(t)) {
			Pop();
			param_p(startVar);
		} else {
			throw new SymbolNotFoundException();
		}
		return startVar;
	}

	public static String param_p(String startVar) throws Exception {
		if (!IsTokenInSet(Arrays.asList("[", ")", ","))) {
			throw new UnexpectedTokenException();
		}

		// 21: I'-> []
		if (AreStringsSimilar(t.ID, "[")) {
			Pop();
			if (AreStringsSimilar(t.ID, "]")) {
				Pop();
				return startVar;
			}
		}
		// 22: I'-> empty
		return startVar;
	}

	public static String compound_stmt(String startVar) throws Exception {
		// 23 J-> { K' L' }

		if (AreStringsSimilar(t.ID, "{")) {
			Pop();
			local_declaration(startVar);
			stmt_list(startVar);
			if (AreStringsSimilar(t.ID, "}")) {
				Pop();
				return startVar;
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static String local_declaration(String startVar) throws Exception {
		local_declaration_p(startVar);
		return startVar;
	}

	public static String local_declaration_p(String startVar) throws Exception {
		if (!(IsTokenInSet(Arrays.asList("int", "float", "void", "(", "{", "}",
				"if", "while", "return"))
				|| t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}
		// 24: K'-> D K'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			var_dec(startVar);
			local_declaration(startVar);
		}

		// 25 K'->empty
		return startVar;
	}

	public static String stmt_list(String startVar) throws Exception {
		stmt_list_p(startVar);
		return startVar;
	}

	public static String stmt_list_p(String startVar) throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(", "{", "if", "while", "return",
				"}", ";"))
				|| t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}

		// 27 L'-> empty
		if (AreStringsSimilar(t.ID, "}")) {
			return startVar;
		} else {
			// 26 L'-> M L'
			statement(startVar);
			stmt_list(startVar);
		}
		return startVar;
	}

	public static String statement(String startVar) throws Exception {

		// 28: M-> N (applies to Float, Int, id, (, ; )
		if (AreStringsSimilar(t.ID, "(") || IsTokenInSymbolTable(t)
				|| AreStringsSimilar(t.ID, ";") || t.Type == TokenType.Float
				|| t.Type == TokenType.Int) {
			expression_stmt(startVar);
		} else
		// 29: M->J
		if (AreStringsSimilar(t.ID, "{")) {
			compound_stmt(startVar);
		} else
		// 30: M-> O
		if (AreStringsSimilar(t.ID, "if")) {
			selection_stmt(startVar);
		} else
		// 31 M->P
		if (AreStringsSimilar(t.ID, "while")) {
			iteration_stmt(startVar);
		} else
		// 32 M->Q
		if (AreStringsSimilar(t.ID, "return")) {
			return_stmt(startVar);
		} else {
			throw new UnexpectedTokenException();
		}

		return startVar;
	}

	public static String expression_stmt(String startVar) throws Exception {
		// 33: N-> R ;
		if (AreStringsSimilar(t.ID, "(") || IsTokenInSymbolTable(t)
				|| t.Type == TokenType.Float || t.Type == TokenType.Int) {
			expression(startVar);
		}

		// 34: N-> ;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
			PrintStack();
		} else {
			throw new UnexpectedTokenException();
		}

		return startVar;
	}

	public static String selection_stmt(String startVar) throws Exception {
		// 35: O-> if( R ) M O'
		if (AreStringsSimilar(t.ID, "if")) {
			Pop();
			if (AreStringsSimilar(t.ID, "(")) {
				Pop();
				expression(startVar);
				if (AreStringsSimilar(t.ID, ")")) {
					Pop();
					statement(startVar);
					selection_stmt_p(startVar);
					return startVar;
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

	public static String selection_stmt_p(String startVar) throws Exception {

		if (!(IsTokenInSet(Arrays.asList("(", "{", "if", "while", "return",
				"}", "else"))
				|| IsTokenInSymbolTable(t)
				|| t.Type == TokenType.Int || t.Type == TokenType.Float)) {
			throw new UnexpectedTokenException();
		}
		// 36: O'->else M
		if (AreStringsSimilar(t.ID, "else")) {
			Pop();
			statement(startVar);
		}

		// 37: O'-> empty
		return startVar;
	}

	public static String iteration_stmt(String startVar) throws Exception {
		// 38: P-> while ( R ) M
		if (AreStringsSimilar(t.ID, "while")) {
			Pop();
			if (AreStringsSimilar(t.ID, "(")) {
				Pop();
				expression(startVar);
				if (AreStringsSimilar(t.ID, ")")) {
					Pop();
					statement(startVar);
					return startVar;
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

	public static String return_stmt(String startVar) throws Exception {
		// 39: Q->return Q'
		if (AreStringsSimilar(t.ID, "return")) {
			Quadruple r = new Quadruple();
			r.CallSymbol = "return";

			Pop();
			r.Output = return_stmt_p(startVar);
			PrintStack();
			_printStack.add(r);
		} else {
			throw new UnexpectedTokenException();
		}

		return startVar;
	}

	public static String return_stmt_p(String startVar) throws Exception {
		// 41: Q'=>R;
		if (IsTokenInSymbolTable(t) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || AreStringsSimilar(t.ID, "(")) {
			return expression(startVar);
		}
		// 40: Q'=>;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
			return startVar;
		} else {
			throw new UnexpectedTokenException();
		}

	}

	public static String expression(String startVar) throws Exception {
		Quadruple q = new Quadruple();
		// 43: R-> id R'
		if (IsTokenInSymbolTable(t)) {
			startVar = t.ID;
			if (t.Metatype == TokenType.Function) {
				q.CallSymbol = "call";
				q.Param1 = t.ID;
				Token func = GetFunctionToken(t.ID);
				q.Param2 = Integer.toString(func.FuncParams.size());
				q.Output = NewTempVar();
				startVar = q.Output;
				_printStack.add(q);
			} else {
				
			}
			Pop();
			startVar =  expression_p(startVar);
			
			if (_isFunctionCall) {
				q.CallSymbol = "arg";
				q.Output = startVar;
				_printStack.add(q);
			}
			
			return startVar;
		} else
		// 44: R=> (R) X' V' T'
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			startVar = expression(startVar);
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				startVar = term_p(startVar);
				startVar = add_exp(startVar);
				startVar = simple_expression(startVar);

			} else {
				throw new UnexpectedTokenException();
			}
		} else // 45: R-> num X' V' T'
				// 46: R-> floatnum X' V' T'
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			startVar = t.ID;
			Pop();
			startVar = term_p(startVar);
			startVar = add_exp(startVar);
			startVar = simple_expression(startVar);
			
		} else {

			throw new UnexpectedTokenException();
		}

		return startVar;
	}

	public static String expression_p(String startVar) throws Exception {

		if (!AreStringsSimilar(t.ID, "(")) {
			// 47: R'-> S' R''
			startVar  = var(startVar);
			startVar = expression_pp(startVar);
		} else {
			// 48: R'-> (AB)X'V'T'

			Token func = GetFunctionToken(lastT.ID);
			int previousFuncId = _currentFunctionCallId;
			_currentFunctionCallId = lastT.TokenId;
			Pop();

			_isFunctionCall = true;

			_functionParams.put(Integer.toString(_currentFunctionCallId),
					new ArrayList<TokenType>());

			args(startVar);
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				PrintStack();
				_isFunctionCall = false;
				startVar = term_p(startVar);
				startVar = add_exp(startVar);
				startVar = simple_expression(startVar);

			} else {
				throw new UnexpectedTokenException();
			}

		}

		return startVar;
	}

	public static String expression_pp(String startVar) throws Exception {
		// 49: R''=> = R
		if (AreStringsSimilar(t.ID, "=")) {
			
			//store left information into a quadruple
			Quadruple q = new Quadruple();
			q.CallSymbol = "assign";
			q.Param1 = startVar;
			
			
			//generate right expression
			Pop();
			startVar = expression(startVar);
			q.Output = startVar;
			//print right expression and assignment.
			PrintStack();
			PrintQuadruple(q);
			
		}
		// 50: R''-> X' V' T'
		else {
			startVar = term_p(startVar);
			startVar = add_exp(startVar);
			startVar = simple_expression(startVar);
		}
		return startVar;
	}

	public static String var(String startVar) throws Exception {

		if (!(IsTokenInSet(Arrays.asList("=", "*", "/", "+", "-", "[", ";",
				"(", ")", ",", "]")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}

		// 51: S'-> [R]
		if (AreStringsSimilar(t.ID, "[")) {
			Pop();
			expression(startVar);
			if (AreStringsSimilar(t.ID, "]")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		} else if (AreStringsSimilar(t.ID, "(")) {
			call(startVar);
		}

		// 52 S' -> empty
		return startVar;
	}

	public static String simple_expression(String startVar) throws Exception {

		if (!(IsTokenInSet(Arrays
				.asList("]", ")", ";", ",", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 53: T'-> U V T'
		if (t.Type == TokenType.Logic) {
			logic_op(startVar);
			V(startVar);
			simple_expression(startVar);
		}

		// 54: T'-> empty
		return startVar;
	}

	public static String logic_op(String startVar) throws Exception {
		// 55-60: U=> Logic Operator
		if (t.Type == TokenType.Logic) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return startVar;
	}

	public static String V(String startVar) throws Exception {
		// 61: V-> X V'
		term(startVar);
		return add_exp(startVar);
	}

	public static String add_exp(String startVar) throws Exception {

		if (!(IsTokenInSet(Arrays.asList("+", "-", ";", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 62: V'-> W X V'
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {

			Quadruple left = new Quadruple();

			left.CallSymbol = (t.ID.compareTo("+") == 0 ? "add" : "sub");

			left.Param1 = lastT.ID;

			add_op(startVar);

			// left.Param2 = t.ID;

			String tempVar = NewTempVar();
			left.Output = tempVar;

			startVar = term(startVar);
			left.Param2 = startVar;

			startVar = add_exp(startVar);
			
			_printStack.add(left);
			
			return tempVar;
		}

		// 63: V'-> EMPTY
		return startVar;
	}

	public static String add_op(String startVar) throws Exception {
		// 64: W=>+; 65: W=>-
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return startVar;
	}

	public static String term(String startVar) throws Exception {
		// 66: X-> Z X' |

		startVar = factor(startVar);

		startVar = term_p(startVar);

		return startVar;

	}

	public static String term_p(String startVar) throws Exception {

		if (!(IsTokenInSet(Arrays.asList("+", "-", "*", "/", ";", ")", ",",
				"]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}

		// 67: X'-> Y Z X'
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			startVar = mulop(startVar);
			startVar = factor(startVar);
			startVar = term_p(startVar);
		}

		// 68: X'-> EMPTY
		return startVar;
	}

	public static String mulop(String startVar) throws Exception {
		// 67, 70: Y-> *|/
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}
		return startVar;
	}

	public static String factor(String startVar) throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(")) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}
		// 71: Z->(R)
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			expression(startVar);
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
			String s2 = var(startVar);
			if (s2.compareTo("") == 0)
				return s1;
			else
				return s2;

		} else
		// 73, 74: Z-> num, floatnum
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			String s = t.ID;
			Pop();
			return s;
		} else {
			call(startVar);
		}
		return startVar;
	}

	public static String call(String startVar) throws Exception {

		// 76:Z'->(delta)
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			args(startVar);
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			// 75: Z'-> S'
			var(startVar);
		}
		return startVar;
	}

	public static String args(String startVar) throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(", ")")) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}

		// 78: DELTA->empty
		if (AreStringsSimilar(t.ID, ")")) {
			return startVar;
		} else
		// 77: DELTA -> Beta
		{
			startVar = args_list(startVar);
		}
		return startVar;
	}

	public static String args_list(String startVar) throws Exception {
		// 79: beta-> R gamma
		
		Quadruple q = new Quadruple();
		q.CallSymbol = "arg";
		q.Output = expression(startVar);
		
		args_list_p(startVar);
		PrintStack();
		_printStack.add(q);
		return startVar;
	}

	public static String args_list_p(String startVar) throws Exception {

		if (!(IsTokenInSet(Arrays.asList(";", ",", ")")))) {
			throw new UnexpectedTokenException();
		}

		// 80:; gamma->, R gamma
		if (AreStringsSimilar(t.ID, ",")) {
			Pop();
			expression(startVar);
			args_list_p(startVar);
		}
		// 81: GAMMA -> empty
		return startVar;

	}

	private static void Pop() throws Exception {
		_tokenIndex++;
		if (_tokenIndex >= _tokens.size()) {
			throw new InvalidEndOfFileException();
		}

		lastT = t;
		t = _tokens.get(_tokenIndex);

		if (lastT != null && t.SourceLineNumber != lastT.SourceLineNumber)
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
		String s = "_t" + (_tempVars.size() + 1);
		_tempVars.add(s);
		return s;
	}

}

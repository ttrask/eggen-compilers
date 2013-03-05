package proj2;

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
					B();
				}
			}
		} catch (TokenizationDoneException e) {
			_isValid = true;
		} catch (UnexpectedTokenException e) {
			System.out.println("Error parsing input file: Unexpected Token");
			System.out.println("Error on line: " + t.SourceLine);
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

	public static boolean B() throws Exception {

		if (t.Type == TokenType.Keyword) {
			if (AreStringsSimilar(t.ID, "int")
					|| AreStringsSimilar(t.ID, "void")
					|| AreStringsSimilar(t.ID, "float")) {
				C();
				B_P();
			} else {
				throw new UnexpectedTokenException();
			}
		}

		return true;
	}

	public static boolean B_P() throws Exception {
		// 2: B'=> C B'
		if (t.Type == TokenType.EOF) {
			throw new TokenizationDoneException();
		} else if (AreStringsSimilar(t.ID, "int")
				|| AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			C();
			B_P();
		} else if (t.Type == TokenType.EOF) {
			throw new TokenizationDoneException();
		}

		return true;
	}

	public static boolean C() throws Exception {

		if (!IsTokenInSet(Arrays.asList("int", "float", "void"))) {
			throw new UnexpectedTokenException();
		}
		// 4: C->E id C'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			E();
			if (!IsTokenInSymbolTable(t)) {
				throw new UnexpectedTokenException();
			} else {
				Pop();
				C_P();
			}
		}
		// 3: C => empty

		return true;
	}

	public static boolean C_P() throws Exception {

		if (!IsTokenInSet(Arrays.asList("(", "[", ";"))) {
			throw new UnexpectedTokenException();
		}

		// 6: C' => (G)J
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			G();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				J();
			} else {
				throw new UnexpectedTokenException();
			}

			// J();
		}
		// 5: C'=>D'
		else if (AreStringsSimilar(t.ID, "[") || AreStringsSimilar(t.ID, ";")) {
			D_P();
		}
		return true;
	}

	public static boolean D() throws Exception {
		// 8: D=> E id D'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			E();
			if (!IsTokenInSymbolTable(t)) {
				throw new UnexpectedTokenException();
			} else {
				Pop();
				D_P();
			}

		}
		return true;
	}

	public static boolean D_P() throws Exception {
		// 9: D'-> ;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
			return true;
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
							return true;
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

	public static boolean E() throws Exception {
		// 11. E=>int
		// 12. E->float
		// 13. E->void

		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			Pop();
			return true;
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static boolean G() throws Exception {
		// 14 G=> int G'
		// 15 G=> float G'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "float")) {
			Pop();
			return G_P();
		} else
		// 16 G=> void
		if (AreStringsSimilar(t.ID, "void")) {
			Pop();
			return true;
		} else {
			throw new Exception();
		}

	}

	public static boolean G_P() throws Exception {
		// 17: G'-> id I' H'
		if (IsTokenInSymbolTable(t)) {
			Pop();
			I_P();
			H_P();
		} else {
			throw new SymbolNotFoundException();
		}
		return true;
	}

	public static boolean H_P() throws Exception {

		if (!IsTokenInSet(Arrays.asList(")", ","))) {
			throw new UnexpectedTokenException();
		}

		// 18: H-'>, I H'
		if (AreStringsSimilar(t.ID, ",")) {
			Pop();
			I();
			H_P();
		}

		// 19: H'-> empty
		return true;
	}

	public static boolean I() throws Exception {
		// 20 I=> E id I'
		E();
		if (IsTokenInSymbolTable(t)) {
			Pop();
			I_P();
		} else {
			throw new SymbolNotFoundException();
		}
		return true;
	}

	public static boolean I_P() throws Exception {
		if (!IsTokenInSet(Arrays.asList("[", ")", ","))) {
			throw new UnexpectedTokenException();
		}

		// 21: I'-> []
		if (AreStringsSimilar(t.ID, "[")) {
			Pop();
			if (AreStringsSimilar(t.ID, "]")) {
				Pop();
				return true;
			}
		}
		// 22: I'-> empty
		return true;
	}

	public static boolean J() throws Exception {
		// 23 J-> { K' L' }

		if (AreStringsSimilar(t.ID, "{")) {
			Pop();
			K_P();
			L_P();
			if (AreStringsSimilar(t.ID, "}")) {
				Pop();
				return true;
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			throw new UnexpectedTokenException();
		}
	}

	public static boolean K_P() throws Exception {
		if (!(IsTokenInSet(Arrays.asList("int", "float", "void", "(", "{", "}",
				"if", "while", "return"))
				|| t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}
		// 24: K'-> D K'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			D();
			K_P();
		}

		// 25 K'->empty
		return true;
	}

	public static boolean L_P() throws Exception {
		if (!(IsTokenInSet(Arrays
				.asList("(", "{", "if", "while", "return", "}"))
				|| t.Type == TokenType.Int || t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}

		// 27 L'-> empty
		if (AreStringsSimilar(t.ID, "}")) {
			return true;
		} else {
			// 26 L'-> M L'
			M();
			L_P();
		}
		return true;
	}

	public static boolean M() throws Exception {

		// 28: M-> N (applies to Float, Int, id, (, ; )
		if (AreStringsSimilar(t.ID, "(") || IsTokenInSymbolTable(t)
				|| AreStringsSimilar(t.ID, ";") || t.Type == TokenType.Float
				|| t.Type == TokenType.Int) {
			N();
		} else
		// 29: M->J
		if (AreStringsSimilar(t.ID, "{")) {
			J();
		} else
		// 30: M-> O
		if (AreStringsSimilar(t.ID, "if")) {
			O();
		} else
		// 31 M->P
		if (AreStringsSimilar(t.ID, "while")) {
			P();
		} else
		// 32 M->Q
		if (AreStringsSimilar(t.ID, "return")) {
			Q();
		} else {
			throw new UnexpectedTokenException();
		}

		return true;
	}

	public static boolean N() throws Exception {
		// 33: N-> R ;
		if (AreStringsSimilar(t.ID, "(") || IsTokenInSymbolTable(t)
				|| t.Type == TokenType.Float || t.Type == TokenType.Int) {
			R();
		}

		// 34: N-> ;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return true;
	}

	public static boolean O() throws Exception {
		// 35: O-> if( R ) M O'
		if (AreStringsSimilar(t.ID, "if")) {
			Pop();
			if (AreStringsSimilar(t.ID, "(")) {
				Pop();
				R();
				if (AreStringsSimilar(t.ID, ")")) {
					Pop();
					M();
					O_P();
					return true;
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

	public static boolean O_P() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("(", "{", "if", "while", "return",
				"else")) || IsTokenInSymbolTable(t) || t.Type == TokenType.Int || t.Type == TokenType.Float)) {
			throw new UnexpectedTokenException();
		}
		// 36: O'->else M
		if (AreStringsSimilar(t.ID, "else")) {
			Pop();
			M();
		}

		// 37: O'-> empty
		return true;
	}

	public static boolean P() throws Exception {
		// 38: P-> while ( R ) M
		if (AreStringsSimilar(t.ID, "while")) {
			Pop();
			if (AreStringsSimilar(t.ID, "(")) {
				Pop();
				R();
				if (AreStringsSimilar(t.ID, ")")) {
					Pop();
					M();
					return true;
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

	public static boolean Q() throws Exception {
		// 39: Q->return Q'
		if (AreStringsSimilar(t.ID, "return")) {
			Pop();
			Q_P();
		} else {
			throw new UnexpectedTokenException();
		}

		return true;
	}

	public static boolean Q_P() throws Exception {
		// 41: Q'=>R;
		if (IsTokenInSymbolTable(t) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || AreStringsSimilar(t.ID, "(")) {
			R();
		}
		// 40: Q'=>;
		if (AreStringsSimilar(t.ID, ";")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return true;
	}

	public static boolean R() throws Exception {

		// 43: R-> id R'
		if (IsTokenInSymbolTable(t)) {
			Pop();
			R_P();
		} else
		// 44: R=> (R) X' V' T'
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			R();
			if (AreStringsSimilar(t.ID, ")")) {
				X_P();
				V_P();
				T_P();
			} else {
				throw new UnexpectedTokenException();
			}
		} else // 45: R-> num X' V' T'
				// 46: R-> floatnum X' V' T'
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			Pop();
			X_P();
			V_P();
			T_P();
		} else {

			throw new UnexpectedTokenException();
		}

		return true;
	}

	public static boolean R_P() throws Exception {
		// 48: R'-> (AB)X'V'T'
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			DELTA();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				X_P();
				V_P();
				T_P();
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			// 47: R'-> S' R''
			S_P();
			R_PP();
		}
		return true;
	}

	public static boolean R_PP() throws Exception {
		// 49: R''=> = R
		if (AreStringsSimilar(t.ID, "=")) {
			Pop();
			R();
		}
		// 50: R''-> X' V' T'
		else {
			X_P();
			V_P();
			T_P();
		}
		return true;
	}

	public static boolean S_P() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("=", "*", "/", "+", "-", "[", ";","(",
				")", ",", "]")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}

		// 51: S'-> [R]
		if (AreStringsSimilar(t.ID, "[")) {
			Pop();
			R();
			if (AreStringsSimilar(t.ID, "]")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		}
		else if(AreStringsSimilar(t.ID, "("))
		{
			Z_P();
		}

		// 52 S' -> empty
		return true;
	}

	public static boolean T_P() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("]", ")", ";", ",", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 53: T'-> U V T'
		if (t.Type == TokenType.Logic) {
			U();
			V();
			T_P();
		}

		// 54: T'-> empty
		return true;
	}

	public static boolean U() throws Exception {
		// 55-60: U=> Logic Operator
		if (t.Type == TokenType.Logic) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return true;
	}

	public static boolean V() throws Exception {
		// 61: V-> X V'
		X();
		V_P();
		return true;
	}

	public static boolean V_P() throws Exception {

		if (!(IsTokenInSet(Arrays.asList("+", "-", ";", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}
		// 62: V'-> W X V'
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {
			W();
			X();
			V_P();
		}

		// 63: V'-> EMPTY
		return true;
	}

	public static boolean W() throws Exception {
		// 64: W=>+; 65: W=>-
		if (AreStringsSimilar(t.ID, "+") || AreStringsSimilar(t.ID, "-")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}

		return true;
	}

	public static boolean X() throws Exception {
		// 66: X-> Z X'
		Z();
		X_P();
		return true;
	}

	public static boolean X_P() throws Exception {

		if (!(IsTokenInSet(Arrays
				.asList("+", "-", "*", "/", ";", ")", ",", "]", "(")) || t.Type == TokenType.Logic)) {
			throw new UnexpectedTokenException();
		}

		// 67: X'-> Y Z X'
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			Y();
			Z();
			X_P();
		}

		// 68: X'-> EMPTY
		return true;
	}

	public static boolean Y() throws Exception {
		// 67, 70: Y-> *|/
		if (AreStringsSimilar(t.ID, "*") || AreStringsSimilar(t.ID, "/")) {
			Pop();
		} else {
			throw new UnexpectedTokenException();
		}
		return true;
	}

	public static boolean Z() throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(")) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}
		// 71: Z->(R)
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			R();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		} else
		// 72: Z->id S'
		if (IsTokenInSymbolTable(t)) {
			Pop();
			S_P();

		} else
		// 73, 74: Z-> num, floatnum
		if (t.Type == TokenType.Int || t.Type == TokenType.Float) {
			Pop();
		} else {
			Z_P();
		}
		return true;
	}

	public static boolean Z_P() throws Exception {

		// 76:Z'->(delta)
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			DELTA();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
			} else {
				throw new UnexpectedTokenException();
			}
		} else {
			// 75: Z'-> S'
			S_P();
		}
		return true;
	}

	public static boolean DELTA() throws Exception {
		if (!(IsTokenInSet(Arrays.asList("(", ")")) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || IsTokenInSymbolTable(t))) {
			throw new UnexpectedTokenException();
		}

		// 78: DELTA->empty
		if (AreStringsSimilar(t.ID, ")")) {
			return true;
		} else
		// 77: DELTA -> Beta
		{
			BETA();
		}
		return true;
	}

	public static boolean BETA() throws Exception {
		// 79: beta-> R gamma
		R();
		GAMMA();
		return true;
	}

	public static boolean GAMMA() throws Exception {

		if (!(IsTokenInSet(Arrays.asList(";", ",", ")")))) {
			throw new UnexpectedTokenException();
		}

		// 80:; gamma->, R gamma
		if (AreStringsSimilar(t.ID, ",")) {
			Pop();
			R();
			GAMMA();
		}
		// 81: GAMMA -> empty
		return true;

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

}

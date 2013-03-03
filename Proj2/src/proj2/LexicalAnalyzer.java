package proj2;

import java.util.ArrayList;
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

		// push the first token onto the stack and start processing
		try {
			if (_tokens.size() > 0) {
				_tokenIndex = -1;

				// loop until all of the tokens are processed.
				while (_tokenIndex < _tokens.size()) {
					Pop();
					B();
				}
			}
		} catch (TokenizationDoneException e) {
			_isValid = true;
		} catch (Exception e) {
			System.out.println("Error parsing input file");
			System.out.println("Error on line: " + t.SourceLine);
			System.out.println("Error on token: " + t.ID);
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
				throw new Exception();
			}
		}

		return true;
	}

	public static boolean B_P() throws Exception {
		// 2: B'=> C B'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			C();
			B_P();
		}

		return true;
	}

	public static boolean C() throws Exception {
		// 4: C->E id C'
		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			if (E()) {
				Pop();
				if (!IsTokenInSymbolTable(t)) {
					throw new Exception();
				} else {
					Pop();
					C_P();
				}
			}
		}
		// 3: C => empty
		return true;
	}

	public static boolean C_P() throws Exception {

		// 6: C' => (G)J
		if (AreStringsSimilar(t.ID, "(")) {
			Pop();
			G();
			if (AreStringsSimilar(t.ID, ")")) {
				Pop();
				J();
			} else {
				throw new Exception();
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
			if (E()) {
				Pop();
				if (!IsTokenInSymbolTable(t)) {
					throw new Exception();
				} else {
					Pop();
					D_P();
				}
			}
		}
		return true;
	}

	public static boolean D_P() throws Exception {
		// 9: D'-> ;
		if (AreStringsSimilar(t.ID, ";")) {
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
							throw new Exception();
					}

					else
						throw new Exception();
				} else {
					throw new Exception();
				}
			} else {
				throw new Exception();
			}

		}

	}

	public static boolean E() throws Exception {
		// 11. E=>int
		// 12. E->float
		// 13. E->void

		if (AreStringsSimilar(t.ID, "int") || AreStringsSimilar(t.ID, "void")
				|| AreStringsSimilar(t.ID, "float")) {
			return true;
		} else
			return false;
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
		// 18: H-'>, I H'
		if (AreStringsSimilar(t.ID, ",")) {
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

		// 24: K'-> D K'
		D();
		K_P();
		// 25 K'->empty
		return true;
	}

	public static boolean L_P() throws Exception {
		// 26 L'-> M L'

		M();
		L_P();

		// 27 L'-> empty
		return true;
	}

	public static boolean M() throws Exception {

		// 28: M-> N (applies to Float, Int, id, (, ; )
		if (AreStringsSimilar(t.ID, "(") || AreStringsSimilar(t.ID, "id")
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
		}

		return true;
	}

	public static boolean N() throws Exception {
		// 33: N-> R ;
		if (AreStringsSimilar(t.ID, "(") || AreStringsSimilar(t.ID, "id")
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
		// 36: O'->else M
		if (AreStringsSimilar(t.ID, "{") || AreStringsSimilar(t.ID, "(")
				|| IsTokenInSymbolTable(t) || t.Type == TokenType.Int
				|| t.Type == TokenType.Float || AreStringsSimilar(t.ID, "num")
				|| AreStringsSimilar(t.ID, "if")
				|| AreStringsSimilar(t.ID, "while")
				|| AreStringsSimilar(t.ID, "return")
				|| AreStringsSimilar(t.ID, "else")) {
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
		if(AreStringsSimilar(t.ID, ";"))
		{
			Pop();
		}
		else{
			throw new UnexpectedTokenException();
		}
		
		return true;
	}

	public static boolean R() throws Exception {
		return true;
	}

	public static boolean R_P() throws Exception {
		return true;
	}

	public static boolean S_P() throws Exception {
		return true;
	}

	public static boolean T_P() throws Exception {
		return true;
	}

	public static boolean U() throws Exception {
		return true;
	}

	public static boolean V() throws Exception {
		return true;
	}

	public static boolean V_P() throws Exception {
		return true;
	}

	public static boolean W() throws Exception {
		return true;
	}

	public static boolean X() throws Exception {
		return true;
	}

	public static boolean X_P() throws Exception {
		return true;
	}

	public static boolean Y() throws Exception {
		return true;
	}

	public static boolean Z() throws Exception {
		return true;
	}

	public static boolean Z_P() throws Exception {
		return true;
	}

	public static boolean DELTA() throws Exception {
		return true;
	}

	public static boolean BETA() throws Exception {
		return true;
	}

	public static boolean GAMMA() throws Exception {
		return true;
	}

	private static void Pop() throws Exception {
		_tokenIndex++;
		if (_tokenIndex >= _tokens.size())
			throw new TokenizationDoneException();
		t = _tokens.get(_tokenIndex);
		// t.ID = t.ID.toUpperCase();
	}

	private static boolean AreStringsSimilar(String s1, String s2) {
		return s1.compareToIgnoreCase(s2) == 0;
	}

	private static boolean IsTokenInSymbolTable(Token t) {
		return SymbolTable.contains(t);
	}

}

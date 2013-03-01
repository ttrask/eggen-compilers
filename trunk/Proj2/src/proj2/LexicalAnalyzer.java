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
		} catch (Exception e) {
			_isValid = false;
		}

		return _isValid;
	}

	public static boolean B() throws Exception {

		if (t.Type == TokenType.Keyword) {
			if (AreStringsSimilar(t.ID, "INT")
					|| AreStringsSimilar(t.ID, "VOID")
					|| AreStringsSimilar(t.ID, "FLOAT")) {
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
		if (AreStringsSimilar(t.ID, "INT") || AreStringsSimilar(t.ID, "VOID")
				|| AreStringsSimilar(t.ID, "FLOAT")) {
			C();
			B_P();
		}

		return true;
	}

	public static boolean C() throws Exception {
		// 4: C->E id C'
		if (AreStringsSimilar(t.ID, "INT") || AreStringsSimilar(t.ID, "VOID")
				|| AreStringsSimilar(t.ID, "FLOAT")) {
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
			Pop();
			if (!AreStringsSimilar(t.ID, ")")) {
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
		if (AreStringsSimilar(t.ID, "INT") || AreStringsSimilar(t.ID, "VOID")
				|| AreStringsSimilar(t.ID, "FLOAT")) {
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
				if (t.Type == TokenType.Num) {
					Pop();
					if (AreStringsSimilar(t.ID, "]")) {
						Pop();
						if (AreStringsSimilar(t.ID, ";")) {
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
		return true;
	}

	public static boolean E() throws Exception {
		if (AreStringsSimilar(t.ID, "INT") || AreStringsSimilar(t.ID, "VOID")
				|| AreStringsSimilar(t.ID, "FLOAT")) {
			return true;
		} else
			return false;
	}

	public static boolean G() {

		return true;
	}

	private static void Pop() {
		_tokenIndex++;
		t = _tokens.get(_tokenIndex);
		t.ID = t.ID.toUpperCase();
	}

	private static boolean AreStringsSimilar(String s1, String s2) {
		return s1.compareToIgnoreCase(s2) == 0;
	}

	private static boolean IsTokenInSymbolTable(Token t) {
		return SymbolTable.contains(t);
	}

}

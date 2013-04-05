package Proj1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import exception.*;

public class FileTokenizer {

	public static List<SourceLine> _Source = new ArrayList<SourceLine>();
	public static List<Token> _Symbols = new ArrayList<Token>();

	public static List<Token> _Tokens = new ArrayList<Token>();

	private static boolean _isFileGood = true;

	public static boolean TokenizeFile(String inputFile) {

		List<SourceLine> Source = new ArrayList<SourceLine>();

		println("Attempting to tokenize Input File: " + inputFile);
		try {

			FileReader fr = new FileReader(inputFile);
			BufferedReader br = new BufferedReader(fr);

			String line;

			int lineNumber = 0;
			int blockDepth = 0;
			int commentDepth = 0;

			Tokenizer tk = new Tokenizer();

			// TODO: Add logic to concat multiple source code lines
			// until a semcol is found.
			while ((line = br.readLine()) != null) {

				lineNumber++;
				line = line.trim();
				if (line.length() > 0) {

					// generates a basic source code line for each line
					// given.
					SourceLine sc = tk.TokenizeLine(line, commentDepth,
							blockDepth);
					sc.LineNumber = lineNumber;
					Source.add(sc);

					// TODO: add logic to deal with changing block-depth.
					// not needed until p2.
					blockDepth = sc.BlockDepth;

					// comment depth logic is in tokenizer.
					commentDepth = sc.CommentDepth;
				}
			}

			blockDepth = 0;
			int parentId = -1;
			int tokenId = 0;

			for (SourceLine sc : Source) {
				Token parentFunction = null;
				println("INPUT: " + sc.SourceCode);
				Boolean isFuncDec = false;
				
				if (sc.Tokens.size() > 0) {
					int srcTokenNum = 0;
					for (Token t : sc.Tokens) {

						tokenId++;
						t.TokenId = tokenId;
						t.ParentId = parentId;
						// links token to parent depth

						if (t.Depth > blockDepth) {
							parentId = t.TokenId;
						} else if (t.Depth < blockDepth) {
							t.ParentId = parentId;
							if (t.Depth == 0)
								parentId = -1;
						}

						blockDepth = t.Depth;
						// builds symbol table.

						if (t.Type == TokenType.ID) {
							Boolean isVar = true;
							Boolean isFunc = false;
							Boolean isVarDeclaration = false;
							if (sc.Tokens.size() > srcTokenNum + 1) {
								switch (sc.Tokens.get(srcTokenNum + 1).ID
										.toLowerCase()) {
								case "(":
									isVar = false;
									isFunc = true;
									isVarDeclaration = false;
									if(blockDepth==0)
									isFuncDec = true;
									parentFunction = t;
									parentFunction.ParentId = -1;
									parentFunction.FuncParams = new ArrayList<TokenType>();
									break;
								case "[":
									t.IsArray = true;
								}
							}
							
							if (srcTokenNum > 0) {
								switch (sc.Tokens.get(srcTokenNum - 1).ID
										.toLowerCase()) {
								case "int":
									t.Metatype = TokenType.Int;
									isVarDeclaration = true;
									break;
								case "float":
									isVarDeclaration = true;
									t.Metatype = TokenType.Float;
									break;
								case "void":
									isVarDeclaration = true;
									t.Metatype = TokenType.Void;
									break;
								}
							}
							
							if (isFuncDec && isVarDeclaration && parentFunction.TokenId != t.TokenId) {
								parentFunction.ParentId = -1;
								parentFunction.FuncParams.add(t.Metatype);
								
								t.Depth = 1;
								t.ParentId = parentFunction.TokenId;
							}
							t.IsVarDeclaration = isVarDeclaration;
							// if the variable has been flagged as a
							// function,

							if (!isVar) {
								t.ReturnType = t.Metatype;
								t.Metatype = TokenType.Function;
							} else {
								if (!isVarDeclaration
										&& !IsTokenInSymbolTable(t)) {
									throw new SymbolNotFoundException();
								} else if (isVar && !isVarDeclaration) {
									Token j = GetMostLocalSymbol(t.ID,
											t.ParentId);
									t.Metatype = j.Metatype;
									t.IsArray = j.IsArray;
								}
							}

							if (isVarDeclaration
									&& !(t.Metatype == TokenType.Function && t.Depth > 0)) {
								if (IsTokenLocallyDeclared(t.ID, t.ParentId,
										false)) {
									if (!IsTokenLocallyDeclared(t.ID,
											t.ParentId, true))
										AddTokenToSmybolTable(t);
									else {
										throw new LocalException(
												"Local variable declared twice.");
									}
								} else {
									AddTokenToSmybolTable(t);
								}

							}
						} else {
							switch (t.ID) {
							case ")":

								if (isFuncDec) {
									isFuncDec = false;
									//parentFunction.FuncParams.remove(0);
									AddTokenToSmybolTable(parentFunction);
									parentId = parentFunction.TokenId;
									t.ParentId = parentId;
									blockDepth = 1;
								}
								break;

							}
						}

						String s = "";
						if (t.Type == TokenType.ID || t.Type == TokenType.Float
								|| t.Type == TokenType.Int
								|| t.Type == TokenType.Void
								|| t.Type == TokenType.Keyword
								|| t.Type == TokenType.Error)
							s += t.Type.toString() + ": ";
						s += t.ID;
						println(s);
						if (t.Type == TokenType.Error) {
							_isFileGood = false;
						}

						_Tokens.add(t);
						srcTokenNum++;

					}
					println("");
				}
			}

			_Source = Source;

			println("\n\nSymbol Table\n*************************");
			println("Id\tName\tType\t    Depth\tParent Id");
			for (Token t : _Symbols) {
				
				
				if(t.Metatype == TokenType.Function){
					String s =t.TokenId + "\t" + t.ID + "\t" + t.Metatype + "\t    "
							+ t.Depth + "\t" + t.ParentId;
					for(TokenType t2: t.FuncParams){
						s += "\t" + t2; 
					}
					println(s);	
				}
				else{
					println(t.TokenId + "\t" + t.ID + "\t" + t.Metatype + "\t    "
							+ t.Depth + "\t" + t.ParentId);
				}
			}

		} catch (FileNotFoundException ex) {
			System.out.println("File " + inputFile + " not found!");
			_isFileGood = false;
		} catch (IOException ex) {
			println("There was an error reading the input file:"
					+ ex.getMessage());
			_isFileGood = false;
		} catch (LocalException ex) {
			println("There was an error processing the file tokens: "
					+ ex.ExceptionMessage);
		}

		return _isFileGood;
	}

	// adds the token to the symbol table if there is no symbol
	// with the same Name&Depth.
	// TODO: Add logic to give each code block a unique id.

	public static void AddTokenToSmybolTable(Token t) throws LocalException {
		for (Token s : _Symbols) {
			if (s.ID.compareTo(t.ID) == 0)
				if (s.Metatype != TokenType.Function) {
					// if the variable is declared twice locally, throw an
					// error.
					if (IsTokenLocallyDeclared(t.ID, t.ParentId, true)) {
						throw new LocalException(
								"locally defined token already exists.");
					}
					break;
				} else {
					return;
				}
		}

		_Symbols.add(t);
		return;
	}

	private static boolean IsTokenInSymbolTable(Token t) {

		return IsTokenLocallyDeclared(t.ID, t.ParentId, false);
	}

	private static boolean IsTokenLocallyDeclared(String id, int parentId,
			boolean onlySearchLocal) {
		Boolean isDeclared = false;

		Token parentToken = null;
		for (Token s : _Tokens) {
			if (s.Type == TokenType.ID && s.ID.compareTo(id) == 0
					&& s.ParentId == parentId && s.IsVarDeclaration) {
				return true;
			}
			if (s.TokenId == parentId) {
				parentToken = s;
			}

		}

		if (onlySearchLocal)
			return false;

		if (parentToken != null)
			return IsTokenLocallyDeclared(id, parentToken.ParentId, false);
		else
			return false;
	}

	private static Token GetMostLocalSymbol(String id, int parentId)
			throws LocalException {
		Token parentToken = null;
		for (Token s : _Tokens) {
			if (s.Type == TokenType.ID && s.ID.compareTo(id) == 0
					&& s.ParentId == parentId && s.IsVarDeclaration) {
				return s;
			}
			if (s.TokenId == parentId) {
				parentToken = s;
			}
		}

		if (parentToken != null)
			return GetMostLocalSymbol(id, parentToken.ParentId);
		else
			throw new LocalException("Token not found.");
	}

	private static Token GetTokenById(int id) {
		for (Token t : _Tokens) {
			if (t.TokenId == id) {
				return t;
			}
		}
		return null;
	}

	// shortcut print for the lazy.
	public static void println(String msg) {
		System.out.println(msg);
	}
}

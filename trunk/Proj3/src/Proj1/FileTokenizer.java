package Proj1;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileTokenizer {

	public static List<SourceLine> _Source = new ArrayList<SourceLine>();
	public static List<Token> _Symbols = new ArrayList<Token>();

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
				println("INPUT: " + sc.SourceCode);

				if (sc.Tokens.size() > 0) {
					int srcTokenNum = 0;
					for (Token t : sc.Tokens) {
						t.TokenId = tokenId;
						t.ParentId = parentId;
						tokenId++;
						//links token to parent depth 
						if(t.Depth > blockDepth){
							t.ParentId = parentId;
						}
						else if(t.Depth<blockDepth){
							parentId = t.TokenId;
						}
						blockDepth = t.Depth;
						// builds symbol table.
						if (srcTokenNum > 0) {
							if (t.Type == TokenType.ID
									&& !IsTokenInSymbolTable(t)) {

								Boolean isVar = true;
								if (sc.Tokens.size() > srcTokenNum + 1) {
									switch (sc.Tokens.get(srcTokenNum + 1).ID
											.toLowerCase()) {
									case "(":
										isVar = false;
										break;
									case "[":
										t.IsArray = true;
									}
								}

								switch (sc.Tokens.get(srcTokenNum - 1).ID
										.toLowerCase()) {
								case "int":
									t.Metatype = TokenType.Int;
									break;
								case "float":
									t.Metatype = TokenType.Float;
									break;
								case "void":
									t.Metatype = TokenType.Void;
									break;

								}

								// if the variable has been flagged as a
								// function,
								if (!isVar) {
									t.ReturnType = t.Metatype;
									t.Metatype = TokenType.Function;
								}

								if(!(t.Metatype==TokenType.Function && t.Depth>0))
								AddTokenToSmybolTable(t);
							}

							String s = "";
							if (t.Type == TokenType.ID
									|| t.Type == TokenType.Float
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
						}
						srcTokenNum++;

					}
					println("");
				}
			}

			_Source = Source;

			println("\n\nSymbol Table\n*************************");
			println("Id\tName\tType\t    Depth\tParent Id");
			for (Token t : _Symbols) {
				println(t.TokenId + "\t" + t.ID + "\t" + t.Metatype + "\t    " + t.Depth + "\t" + t.ParentId);
			}

		} catch (FileNotFoundException ex) {
			System.out.println("File " + inputFile + " not found!");
			_isFileGood = false;
		} catch (IOException ex) {
			println("There was an error reading the input file:"
					+ ex.getMessage());
			_isFileGood = false;
		}

		return _isFileGood;
	}

	// adds the token to the symbol table if there is no symbol
	// with the same Name&Depth.
	// TODO: Add logic to give each code block a unique id.

	public static void AddTokenToSmybolTable(Token t) {
		for (Token s : _Symbols) {
			if (s.Depth == t.Depth && s.ID.compareTo(t.ID) == 0)
				return;
		}

		_Symbols.add(t);
		return;
	}

	private static boolean IsTokenInSymbolTable(Token t) {
		for (Token s : _Symbols) {
			if (s.ID.compareTo(t.ID) == 0 && s.Depth == t.Depth)
				return true;
		}

		return false;
	}

	// shortcut print for the lazy.
	public static void println(String msg) {
		System.out.println(msg);
	}
}

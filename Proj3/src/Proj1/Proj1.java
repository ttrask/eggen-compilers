package Proj1;

import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Proj1 {

	private static List<Token> _symbols = new ArrayList<Token>();

	public static void main(String[] args) {

		String _inputFileName;

		println("Project Started");

		List<SourceLine> Source = new ArrayList<SourceLine>();

		if (args.length > 0) {
			_inputFileName = args[0];

			println("Attempting to tokenize Input File: " + _inputFileName);
			try {

				FileReader fr = new FileReader(_inputFileName);
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

				for (SourceLine sc : Source) {
					println("INPUT: " + sc.SourceCode);

					if (sc.Tokens.size() > 0) {
						for (Token t : sc.Tokens) {

							// builds symbol table.
							if (t.Type == TokenType.ID) {
								AddTokenToSmybolTable(t);
							}

							String s = "";
							if (t.Type == TokenType.ID
									|| t.Type == TokenType.Float
									|| t.Type == TokenType.Int
									|| t.Type == TokenType.Keyword
									|| t.Type == TokenType.Error)
								s += t.Type.toString() + ": ";
							s += t.ID;
							println(s);
							// if (t.Type == TokenType.Error) println(t.Note);
						}
						println("");
					}
				}

				println("\n\nSymbol Table\n*************************");
				println("Name\tDepth");
				for (Token t : _symbols) {
					println(t.ID + "\t" + t.Depth);
				}

			} catch (FileNotFoundException ex) {
				System.out.println("File " + _inputFileName + " not found!");
			} catch (IOException ex) {
				println("There was an error reading the input file:"
						+ ex.getMessage());
			}
		} else {
			println("No input file specified");
		}

	}

	// adds the token to the symbol table if there is no symbol
	// with the same Name&Depth.
	// TODO: Add logic to give each code block a unique id.
	public static void AddTokenToSmybolTable(Token t) {
		for (Token s : _symbols) {
			if (s.Depth == t.Depth && s.ID.compareTo(t.ID) == 0)
				return;
		}

		_symbols.add(t);
		return;
	}

	// shortcut print for the lazy.
	public static void println(String msg) {
		System.out.println(msg);
	}

}

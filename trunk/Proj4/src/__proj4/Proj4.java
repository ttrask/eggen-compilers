package __proj4;

import java.util.ArrayList;
import java.util.List;

import Proj1.*;

public class Proj4 {

	/**
	 * @param args
	 */

	private static List<SourceLine> _source = new ArrayList<SourceLine>();
	private static List<Token> _symbols = new ArrayList<Token>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String _inputFileName;

		if (args.length > 0) {
			_inputFileName = args[0];

			println("Project Started");

			FileTokenizer tokenizer = new FileTokenizer();

			if (tokenizer.TokenizeFile(_inputFileName)) {

				_source = tokenizer._Source;
				_symbols = tokenizer._Symbols;

				LexicalAnalyzer la = new LexicalAnalyzer();
				
				la.SymbolTable = _symbols;
				
				boolean isLexicallyValidFile = LexicalAnalyzer.LexicallyAnalyzeSource(_source);
				
				if(isLexicallyValidFile)
					println("VALID FILE");
				else
					println("INVALID FILE");
			}
			else{
				println("There was an error tokenizing your input file.");
			}
			
			
			println("Project Ended");
		} else {
			println("No input file specified");
		}

	}

	// shortcut print for the lazy.
	public static void println(String msg) {
		System.out.println(msg);
	}

	

}

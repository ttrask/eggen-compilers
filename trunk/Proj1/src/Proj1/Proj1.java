package Proj1;

import java.util.*;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Proj1 {

	public static void main(String[] args) {

		String inputFileName;

		println("Project Started");

		List<SourceLine> Source = new ArrayList<SourceLine>();

		if (args.length > 0) {
			inputFileName = args[0];

			println("Attempting to tokenize Input File: " + inputFileName);
			try {
				FileReader fr = new FileReader(inputFileName);
				BufferedReader br = new BufferedReader(fr);

				String line;

				int lineNumber = 0;
				int blockDepth = 0;
				int commentDepth = 0;

				Tokenizer tk = new Tokenizer();

				while ((line = br.readLine()) != null) {
					lineNumber++;
					SourceLine sc = tk.TokenizeLine(line, blockDepth,
							commentDepth);
					sc.LineNumber = lineNumber;
					Source.add(sc);

					// add logic to deal with changing block-depth.
					// not needed until p2.
					blockDepth = sc.BlockDepth;

					// add logic to deal with changing comment-depth.
					commentDepth = sc.CommentDepth;

				}

				for (SourceLine sc : Source) {
					println("Source Line: " + sc.SourceCode);
					if (sc.Tokens.size() > 0) {
						for (Token t : sc.Tokens) {
							println("token: " + t.ID);
						}
						println("");
					}
				}

			} catch (FileNotFoundException ex) {
				System.out.println("File " + inputFileName + " not found!");
			} catch (IOException ex) {
				println("There was an error reading the input file:"
						+ ex.getMessage());
			}
		} else {
			println("No input file specified");
		}

	}

	public static void println(String msg) {
		System.out.println(msg);
	}

}

package proj4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.System;
import exception.InvalidEndOfFileException;
import exception.SymbolNotFoundException;
import exception.TokenizationDoneException;
import exception.UnexpectedTokenException;

import Proj1.*;

public class QuadrupleGenerator {
	
	private static List<String> _iCode = new ArrayList<String>();
	
	public static List<String> GenerateQuadruple(List<SourceLine> srcCode, List<Token> symbolTable){
		
		generate(srcCode);
		
		return _iCode;
	}
	
	private static void generate(List<SourceLine> srcCode){
		
		for(SourceLine src: srcCode){
			Token starter = src.Tokens.get(0);
			
			switch(starter.ID){
			case "func":
				GenerateFunctionCall(src);
				break;
			case "if":
				break;
			case "when":
				break;
			}
			
			for(Token t: src.Tokens){
				
			}
		}
	}
	
	private static boolean GenerateFunctionCall(SourceLine src){
		
		
		
		return true;
	}
	
	private static void PrintQuadruple(Quadruple q){
		System.out.format("%d\t%s\t%s\t%s", q.LineNumber, q.CallSymbol, q.Param1, q.Param2, q.Output);
	}
}

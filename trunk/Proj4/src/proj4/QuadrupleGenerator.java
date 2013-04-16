package proj4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.System;

import __proj4.Quadruple;

import exception.InvalidEndOfFileException;
import exception.SymbolNotFoundException;
import exception.TokenizationDoneException;
import exception.UnexpectedTokenException;

import Proj1.*;

public class QuadrupleGenerator {

	private static List<Quadruple> _quadruples = new ArrayList<Quadruple>();
	private static List<String> _iCode = new ArrayList<String>();
	private static List<String> _tempVars = new ArrayList<String>();

	public static List<String> GenerateQuadruple(List<SourceLine> srcCode,
			List<Token> symbolTable) {

		generate(srcCode);

		return _iCode;
	}

	private static void generate(List<SourceLine> srcCode) {

		int srclineNumber = 0;
		for (SourceLine src : srcCode) {
			
			int tokenIndex = 0;
			for (Token t : src.Tokens) {
				List<SourceLine> children;
				switch (t.Metatype) {
				case Function:
					if (tokenIndex != 0) {
						Token tPrev = src.Tokens.get(tokenIndex - 1);
						switch (tPrev.Metatype) {
						case Declaration:
							Quadruple endCall = GenerateFunctionDeclaration(src);
							children = GetChildCode(
									src,
									srcCode.subList(srclineNumber,
											srcCode.size()));
							generate(children);
							PrintQuadruple(endCall);
							break;
						default:
							GenerateFunctionCall(src.Tokens.subList(tokenIndex+1, src.Tokens.size()));
							break;
						}
					}
					else{
						GenerateFunctionCall(src.Tokens.subList(tokenIndex+1, src.Tokens.size()));
						
					}
					break;
				case IfStatement:
					GenerateIfStatement(src);
					children = GetChildCode(src,
							srcCode.subList(srclineNumber, srcCode.size()));
					generate(children);
					break;
				case WhenStatement:
					break;
				}
				
				tokenIndex++;
			}
		}
	}

	private static void GenerateFunctionCall(List<Token> tokens){
		
	}
	
	private static List<SourceLine> GetChildCode(SourceLine parent,
			List<SourceLine> code) {
		List<SourceLine> children = new ArrayList<SourceLine>();

		int parentId = parent.Tokens.get(0).TokenId;

		for (SourceLine src : code) {
			for (Token t : src.Tokens) {

			}
		}

		return children;
	}

	private static Quadruple GenerateFunctionDeclaration(SourceLine src) {

		Quadruple funcQ = new Quadruple();

		List<Quadruple> params = new ArrayList<Quadruple>();

		funcQ.CallSymbol = "func";
		funcQ.Param1 = src.Tokens.get(1).ID;
		funcQ.Param2 = src.Tokens.get(0).ID;

		int i = 0;
		for (Token t : src.Tokens) {
			if (t.Type == TokenType.ID && t.Metatype != TokenType.Function) {
				Quadruple param = new Quadruple();
				param.CallSymbol = "alloc";
				param.Param1 = "4";
				param.Output = t.ID;
				params.add(param);
			}
			i++;
		}

		funcQ.Output = Integer.toString(params.size());

		PrintQuadruple(funcQ);
		for (Quadruple param : params) {
			PrintQuadruple(param);
		}

		funcQ.CallSymbol = "end";
		funcQ.Param2 = funcQ.Param1;
		funcQ.Param1 = "func";
		funcQ.Output = null;

		return funcQ;
	}

	private static boolean GenerateIfStatement(SourceLine src) {
		
		
		
		return true;
	}

	private static void PrintQuadruple(Quadruple q) {
		q.LineNumber = _quadruples.size() + 1;
		_quadruples.add(q);

		System.out.format("%d\t%s\t%s\t%s\t%s", q.LineNumber, q.CallSymbol,
				q.Param1, (q.Param2 == null ? "" : q.Param2),
				(q.Output == null ? "" : q.Output));
		System.out.println();
	}
}

package Proj1;

import java.util.List;
public class Token {
	
	public int TokenId;
	public int ParentId;
	public String ID;
	public String SourceLine;
	public int SourceLineNumber;
	public TokenType Type = TokenType.Unknown;
	public int Depth;
	public CodeBlock ParentBlock;
	public String Note;
	public TokenType Metatype = TokenType.Unknown;
	public TokenType ReturnType = TokenType.Unknown;
	public List<TokenType> FuncParams;
	public Boolean IsArray;
	public Boolean isInitialized;
}


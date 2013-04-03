

public class Token {
	
	public String ID;
	public String SourceLine;
	public String SourceLineNumber;
	public TokenType Type = TokenType.Unknown;
	public int Depth;
	public CodeBlock ParentBlock;
	public String Note;
}


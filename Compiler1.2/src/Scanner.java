import java.io.*;

public class Scanner {
	/* int String mehrZeilig
	 * 
	 * 
	 * 
	 */
	public final int
	_NUMBER = 1,		// 0,...,9
	_IDENTIFIER = 2,	// a,...,z,A,...,Z
	_NOT = 3,			// !
	_QUOTE = 4,			// "
	_PERCENT = 5,		// %
	_AND = 6,			// &&
	_SLASH = 7,			// /
	_LPAREN = 8,		// (
	_RPAREN = 9,		// )
	_LBRACK = 10,		// [
	_RBRACK = 11,		// ]
	_LBRACE = 12,		// {
	_RBRACE = 13,		// }
	_ASSIGN = 14,		// =
	_CHAR = 15,			// char
	_SQUOTE = 16,		// '
	_PLUS = 17,			// +
	_MINUS = 18,		// -
	_TIMES = 19,		// *
	_SEMIKOLON = 20,	// ;
	_COMMA = 21,		// ,
	//_DOT = 22,			// .
	_GREATER = 23,		// >
	_LESS = 24,			// <
	_OR = 25,			// ||
	_METHOD = 26,		// method
	_IF = 27,			// if
	_ELSE = 28,			// else
	_WHILE = 29,		// while
	_INT = 30,			// int
	_ARRAY = 31,		// []
	_STRING = 32,		// String
	_VOID = 33,			// void
	_BOOLEAN = 34,		// boolean
	_GEQ = 35,			// >=
	_LEQ = 36,			// <=
	_NOTEQ = 37,		// !=
	_EQUAL = 38,		// ==
	_INCREMENT = 39,	// ++
	_DECREMENT = 40,	// --
	_BREAK = 41,		// break
	_CLASS = 42,		// class
	_CONTINUE = 43,		// continue
	_DEFAULT = 44,		// default
	_IMPORT = 45,		// import
	//_RECORD = 46,		// record
	_DO = 47,			// do
	_FINAL = 48,		// final
	_NULL = 49,			// null
	_NEW = 50,			// new
	_RETURN = 51,		// return
	_ELSEIF = 52,		// else if /*TODO*/
	//_COMMOPEN = 53,		// /*
	//_COMMCLOSE = 54,	// */
	_PRIVATE = 55,		// private
	_PUBLIC = 56,		// public
	_STATIC = 57;		// static
	
	
	// TODO: 65535 ist beim mac eof,
	// ka wie das bei anderen systemen aussieht
	// sollte eigentlich -1 sein
	public final int _EOF = 65535;
	
	
	private String value;
	private InputStreamReader reader;
	private char nextChar;
	
	public void openFile(String fName) throws FileNotFoundException {
		reader = new InputStreamReader(new FileInputStream(fName));
	}
	
	private boolean checkLetter(char ch) {
		if (ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z')
			return true;
		
		return false;
	}
	
	private boolean checkDigit(char ch) {
		if (ch >= '0' && ch <= '9')
			return true;
		
		return false;
	}
	
	private char getNextChar() throws IOException {
		char ch = (char)reader.read();
		return ch;
	}
	
	private char readToNextChar() throws IOException {
		if (nextChar != 0 && nextChar != ' ' && nextChar != '\n' && nextChar != '\t')
			return nextChar;
		
		char ch = getNextChar();
		while (ch == ' ' || ch == '\n' || ch == '\t')
			ch = getNextChar();
		
		return ch;
	}
	
	private int checkKeyword(String id) {
		if (id.compareTo("if") == 0)
			return _IF;
		else if (id.compareTo("else") == 0)
			return _ELSE;
		else if (id.compareTo("while") == 0)
			return _WHILE;
		else if (id.compareTo("do") == 0)
			return _DO;
		else if (id.compareTo("int") == 0)
			return _INT;
		else if (id.compareTo("char") == 0)
			return _CHAR;
		else if (id.compareTo("String") == 0)
			return _STRING;
		else if (id.compareTo("boolean") == 0)
			return _BOOLEAN;
		else if (id.compareTo("break") == 0)
			return _BREAK;
		else if (id.compareTo("class") == 0)
			return _CLASS;
		else if (id.compareTo("continue") == 0)
			return _CONTINUE;
		else if (id.compareTo("default") == 0)
			return _DEFAULT;
		else if (id.compareTo("public") == 0)
			return _PUBLIC;
		else if (id.compareTo("private") == 0)
			return _PRIVATE;
		else if (id.compareTo("static") == 0)
			return _STATIC;
		else if (id.compareTo("final") == 0)
			return _FINAL;
		else if (id.compareTo("null") == 0)
			return _NULL;
		else if (id.compareTo("void") == 0)
			return _VOID;
		else if (id.compareTo("import") == 0)
			return _IMPORT;
		else if (id.compareTo("new") == 0)
			return _NEW;
		else if (id.compareTo("return") == 0)
			return _RETURN;
		else if (id.compareTo("method") == 0)
			return _METHOD;

		value = id;
		return _IDENTIFIER;
	}
	
	public int readToken() throws IOException {
		value = "";
		String token = "";
		char ch = readToNextChar();
		if (ch == _EOF)
			return -1;
		
		nextChar = 0;
		
		// starts with letter
		if (checkLetter(ch) || ch == '_') {
			token += ch;
			ch = getNextChar();
			while (checkLetter(ch) || checkDigit(ch) || ch == '_') {
				token += ch;
				ch = getNextChar();
			}
			
			nextChar = ch;
			return checkKeyword(token);
		}
		
		// starts with digit -> only digits allowed
		else if (checkDigit(ch)) {
			token += ch;
			ch = getNextChar();
			while (checkDigit(ch)) {
				token += ch;
				ch = getNextChar();
			}
			
			nextChar = ch;
			value = token;
			return _NUMBER;
		}
		
		// single signs
		else if (ch == '(')
			return _LPAREN;
		else if (ch == ')')
			return _RPAREN;
		else if (ch == '{')
			return _LBRACE;
		else if (ch == '}')
			return _RBRACE;
		else if (ch == ';')
			return _SEMIKOLON;
		else if (ch == ',')
			return _COMMA;
		//else if (ch == '.')
		//	return _DOT;
		else if (ch == '%')
			return _PERCENT;
		else if (ch == ']')
			return _RBRACK;
		else if (ch == '*')
			return _TIMES;
		
		// combined signs
		else if (ch == '=') {
			ch = getNextChar();
			if (ch == '=')
				return _EQUAL;
			
			nextChar = ch;
			return _ASSIGN;
		}
		
		else if (ch == '!') {
			ch = getNextChar();
			if (ch == '=')
				return _NOTEQ;
			
			nextChar = ch;
			return _NOT;
		}
		
		else if (ch == '+') {
			ch = getNextChar();
			if (ch == '+')
				return _INCREMENT;
			
			nextChar = ch;
			return _PLUS;
		}		
		
		else if (ch == '-') {
			ch = getNextChar();
			if (ch == '-')
				return _DECREMENT;
			
			nextChar = ch;
			return _MINUS;
		}
		
		else if (ch == '>') {
			ch = getNextChar();
			if (ch == '=')
				return _GEQ;
			
			nextChar = ch;
			return _GREATER;
		}
		
		else if (ch == '<') {
			ch = getNextChar();
			if (ch == '=')
				return _LEQ;
			
			nextChar = ch;
			return _LEQ;
		}
		
		else if (ch == '&') {
			ch = getNextChar();
			if (ch == '&')
				return _AND;
		}
		
		else if (ch == '|') {
			ch = getNextChar();
			if (ch == '|')
				return _OR;
		}
		
		else if (ch == '[') {
			ch = getNextChar();
			if (ch == ']')
				return _ARRAY;
		}
		
		// comments
		else if (ch == '/') {
			ch = getNextChar();
			
			// one line
			if (ch == '/') {
				while (ch != '\n') {
					ch = getNextChar();
				}
				
				return readToken();
			}
			
			// multiple lines
			else if (ch == '*') {
				while (ch != _EOF) {
					ch = getNextChar();
					if (ch == '*') {
						ch = getNextChar();
						if (ch == '/')
							return readToken();
					}
				}
				
				// TODO: vmtl richtiges EOF einfuegen
				return -1;
			}
			
			else return _SLASH;
		}
		
		// strings
		else if (ch == '"') {
			ch = getNextChar();
			while (ch != '"')  {
				value = value + ch;
				
				if (ch == '\\')
					value = value + getNextChar();
				
				ch = getNextChar();
			}
			
			
			return _QUOTE;
		}
		
		// chars
		// TODO: suboptimal bzgl debugging
		else if (ch == '\'') {
			ch = getNextChar();
			if (ch != '\'') {
				value = value + ch;
				if (ch == '\\')
					value = value + getNextChar();
			}
			
			getNextChar();
			return _SQUOTE;
		}
		
		
		return -1;
	}
	
	public String getStringValue(){
		return value;
	}
	
	public int getIntValue(){
		return Integer.parseInt(value);
	}
	
	public void testScanner() throws Exception{
		if (reader == null)
			throw new RuntimeException("open file first");
		
		int token = readToken();
		
		while (token != -1){
			if (token == _NUMBER) System.out.print("number '" + value + "'");
			else if (token == _IDENTIFIER) System.out.print("identifier '" + value + "'");
			else if (token == _NOT) System.out.print("not");
			else if (token == _QUOTE) System.out.print("stringvalue '" + value + "'");
			else if (token == _PERCENT) System.out.print("%");
			else if (token == _AND) System.out.print("&&");
			else if (token == _SLASH) System.out.print("/");
			else if (token == _LPAREN) System.out.print("(");
			else if (token == _RPAREN) System.out.print(")");
			else if (token == _LBRACK) System.out.print("[");
			else if (token == _RBRACK) System.out.print("]");
			else if (token == _LBRACE) System.out.print("{");
			else if (token == _RBRACE) System.out.print("}");
			else if (token == _ASSIGN) System.out.print("=");
			else if (token == _CHAR) System.out.print("char");
			else if (token == _SQUOTE) System.out.print("singlechar '" + value + "'");
			else if (token == _PLUS) System.out.print("+");
			else if (token == _MINUS) System.out.print("-");
			else if (token == _TIMES) System.out.print("*");
			else if (token == _SEMIKOLON) System.out.print(";");
			else if (token == _COMMA) System.out.print(",");
			//else if (token == _DOT) System.out.print(".");
			else if (token == _GREATER) System.out.print(">");
			else if (token == _LESS) System.out.print("<");
			else if (token == _OR) System.out.print("||");
			else if (token == _IF) System.out.print("if");
			else if (token == _ELSE) System.out.print("else");
			else if (token == _WHILE) System.out.print("while");
			else if (token == _INT) System.out.print("int");
			else if (token == _ARRAY) System.out.print("[]");
			else if (token == _STRING) System.out.print("String");
			else if (token == _VOID) System.out.print("void");
			else if (token == _BOOLEAN) System.out.print("boolean");
			else if (token == _GEQ) System.out.print(">=");
			else if (token == _LEQ) System.out.print("<=");
			else if (token == _NOTEQ) System.out.print("!=");
			else if (token == _EQUAL) System.out.print("==");
			else if (token == _INCREMENT) System.out.print("++");
			else if (token == _DECREMENT) System.out.print("--");
			else if (token == _BREAK) System.out.print("break");
			else if (token == _CLASS) System.out.print("class");
			else if (token == _CONTINUE) System.out.print("continue");
			else if (token == _DEFAULT) System.out.print("default");
			else if (token == _IMPORT) System.out.print("import");
			else if (token == _DO) System.out.print("do");
			else if (token == _FINAL) System.out.print("final");
			else if (token == _NULL) System.out.print("null");
			else if (token == _NEW) System.out.print("new");
			else if (token == _RETURN) System.out.print("return");
			//else if (token == _COMMENT) System.out.print("comment");
			else if (token == _PRIVATE) System.out.print("private");
			else if (token == _PUBLIC) System.out.print("public");
			else if (token == _STATIC) System.out.print("static");
			
			System.out.println(" - " + token);
			token = readToken();
		}
		
	}
	
	public void testScanner2() throws Exception{
		FileWriter fstream = new FileWriter("testout.txt");
		BufferedWriter out = new BufferedWriter(fstream);		
		
		if (reader == null)
			throw new RuntimeException("open file first");
		
		int token = readToken();
		
		while (token != -1){
			if (token == _NUMBER) out.write("number '" + value + "'");
			else if (token == _IDENTIFIER) out.write("identifier '" + value + "'");
			else if (token == _NOT) out.write("not");
			else if (token == _QUOTE) out.write("stringvalue '" + value + "'");
			else if (token == _PERCENT) out.write("%");
			else if (token == _AND) out.write("&&");
			else if (token == _SLASH) out.write("/");
			else if (token == _LPAREN) out.write("(");
			else if (token == _RPAREN) out.write(")");
			else if (token == _LBRACK) out.write("[");
			else if (token == _RBRACK) out.write("]");
			else if (token == _LBRACE) out.write("{");
			else if (token == _RBRACE) out.write("}");
			else if (token == _ASSIGN) out.write("=");
			else if (token == _CHAR) out.write("char");
			else if (token == _SQUOTE) out.write("singlechar '" + value + "'");
			else if (token == _PLUS) out.write("+");
			else if (token == _MINUS) out.write("-");
			else if (token == _TIMES) out.write("*");
			else if (token == _SEMIKOLON) out.write(";");
			else if (token == _COMMA) out.write(",");
			//else if (token == _DOT) out.write(".");
			else if (token == _GREATER) out.write(">");
			else if (token == _LESS) out.write("<");
			else if (token == _OR) out.write("||");
			else if (token == _IF) out.write("if");
			else if (token == _ELSE) out.write("else");
			else if (token == _WHILE) out.write("while");
			else if (token == _INT) out.write("int");
			else if (token == _ARRAY) out.write("[]");
			else if (token == _STRING) out.write("String");
			else if (token == _VOID) out.write("void");
			else if (token == _BOOLEAN) out.write("boolean");
			else if (token == _GEQ) out.write(">=");
			else if (token == _LEQ) out.write("<=");
			else if (token == _NOTEQ) out.write("!=");
			else if (token == _EQUAL) out.write("==");
			else if (token == _INCREMENT) out.write("++");
			else if (token == _DECREMENT) out.write("--");
			else if (token == _BREAK) out.write("break");
			else if (token == _CLASS) out.write("class");
			else if (token == _CONTINUE) out.write("continue");
			else if (token == _DEFAULT) out.write("default");
			else if (token == _IMPORT) out.write("import");
			else if (token == _DO) out.write("do");
			else if (token == _FINAL) out.write("final");
			else if (token == _NULL) out.write("null");
			else if (token == _NEW) out.write("new");
			else if (token == _RETURN) out.write("return");
			//else if (token == _COMMENT) out.write("comment");
			else if (token == _PRIVATE) out.write("private");
			else if (token == _PUBLIC) out.write("public");
			else if (token == _STATIC) out.write("static");
			
			out.write(" - " + token);
			out.newLine();
			token = readToken();
		}
		//Close the output stream
		out.close();
		
	}
	

	public static void main(String[] args) {
		Scanner s = new Scanner();
		try {
			s.openFile("E:\\workspace\\Compiler1.2\\src\\Scanner.java");
			//s.openFile("E:\\workspace\\Compiler1.2\\src\\test.txt");
			//s.openFile("E:\\workspace\\Compiler1.2\\src\\test.java");
			//s.openFile("/Users/patrick/Documents/workspace/compiler/src/Scanner.java");
			//s.openFile("/Users/patrick/Documents/workspace/compiler/src/test.java");
			s.testScanner();
		}
		catch (Exception e) {
			e.getStackTrace();
		}
	}
}
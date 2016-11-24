import java.io.*;

class scanner
{
	private final int 
		_DIGIT = 1,			// 0,...,9
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
		_DOT = 22,			// .
		_GREATER = 23,		// >
		_LESS = 24,			// <
		_OR = 25,			// ||
		//_DD = 26,			// :
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
		_COMMENT = 52,		// //
		//_COMMOPEN = 53,		// /*
		//_COMMCLOSE = 54,	// */
		_PRIVATE = 55,		// private
		_PUBLIC = 56,		// public
		_STATIC = 57;		// static
	
	public static String[] keywords = {"!", "!=", "%", "&&", "(", ")", "*",
        "+", "++", ",", "-", "--", ".", "/", ";",
        "<", "<=", "=", "==", ">", ">=", "[", "[]", "]",
        "boolean", "break", "char",
        "class", "continue", "default", "do", "else",
        "final", "if",
        "import", "int", "new", "null",
        "private", "protected", "public", "return",
        "static",
        "void", "while", "{", "||", "}", "//", "/*", "*/", "\"", "'"};
	
	private FileInputStream fis;
	private InputStreamReader in;
	private char nextSymbol;
	
	public void openf(String file) throws Exception
	{
		fis = new FileInputStream(file);
		in = new InputStreamReader(fis, "UTF-8");
		
		//symbol = (char)in.read();
		nextSymbol = (char)in.read();
	}
	
	public void getNextSymbol() throws Exception {
		nextSymbol = (char)in.read();
		//nextSymbol = (char)in.read();
	}
	
	public int nextToken() throws Exception
	{
		char current = nextSymbol;
		getNextSymbol();
		/*int i;
		char c;
		while((i = in.read()) != -1)
		{
			c = (char)i;
			System.out.print(c);
		}*/

		//int token;
		String identifier = "";
		
		if(current == '(')
			return _LPAREN;
		else if(current == ')')
			return _RPAREN;
		else if(current == '{')
			return _LBRACE;
		else if(current == '}')
			return _RBRACE;
		else if(current == ';')
			return _SEMIKOLON;
		else if(current == ',')
			return _COMMA;
		else if(current == '.')
			return _DOT;
		else if(current == '%')
			return _PERCENT;
		else if(current == ']')
			return _RBRACK;
		else if(current == '*')
			return _TIMES;
		
		else if(current >= '0' && current <= '9')
		{
			identifier = identifier + current;
			while(nextSymbol >= '0' && nextSymbol <= '9')
			{
				identifier = identifier + nextSymbol;
				getNextSymbol();
			}
			
			return _DIGIT;
		}
		
		else if((current >= 'A' && current <= 'Z') || (current >= 'a' && current <= 'z') || current == '_')
		{
			identifier = identifier + current;
			while ((nextSymbol >= 'A' && nextSymbol <= 'Z') || (nextSymbol >= 'a' && nextSymbol <= 'z') || (nextSymbol >= '0' && nextSymbol <= '9') || nextSymbol == '_')
			{
				identifier = identifier + nextSymbol;
				getNextSymbol();
			}
			
			return checkKeyword(identifier);
		}
		
		else if (current == '=')
		{
			if (nextSymbol == '=') 
			{
				getNextSymbol();
				return _EQUAL;
			}
			
			return _ASSIGN;
		}
		
		else if (current == '+')
		{
			if (nextSymbol == '+')
			{
				getNextSymbol();
				return _INCREMENT;
			}
			
			return _PLUS;
		}
		
		else if (current == '-')
		{
			if (nextSymbol == '-')
			{
				getNextSymbol();
				return _DECREMENT;
			}
			
			return _MINUS;
		}
		
		else if (current == '>')
		{
			if (nextSymbol == '=')
			{
				getNextSymbol();
				return _GEQ;
			}
			
			return _GREATER;
		}		
		
		else if (current == '<')
		{
			if (nextSymbol == '=')
			{
				getNextSymbol();
				return _LEQ;
			}
			
			return _LESS;
		}
		
		else if (current == '&' && nextSymbol == '&')
		{
			getNextSymbol();
			return _AND;
		}
		
		else if (current == '|' && nextSymbol == '|')
		{
			getNextSymbol();
			return _OR;
		}
		
		else if (current == '!')
		{
			if (nextSymbol == '=')
			{
				getNextSymbol();
				return _NOTEQ;
			}
			
			return _NOT;
		}
		
		else if (current == '[')
		{
			if (nextSymbol == ']')
			{
				getNextSymbol();
				return _ARRAY;
			}
			
			return _LBRACK;
		}
		
		else if (current == '/')
		{
			if (nextSymbol == '/')
			{
				while (nextSymbol != '\n')
					getNextSymbol();
				
				return _COMMENT;
			}
			
			else if (nextSymbol == '*')
			{
				returnEnd();
				return _COMMENT;
			}
			
			return _SLASH;
		}
		
		else if (current == '"')
		{
			if (nextSymbol == '"')
				return _QUOTE;
			
			else 
			{
				while (nextSymbol != '"' && current != '\\')
				{
					current = nextSymbol;
					getNextSymbol();
					identifier = identifier + current;
				
				}
			}
			
			return _QUOTE;
		}

		
		
		return -1;
	}
	
	private void returnEnd() throws Exception
	{
		if(nextSymbol == '*')
		{
			getNextSymbol();
			if (nextSymbol != '/')
				returnEnd();
		}
		
		else {
			getNextSymbol();
			returnEnd();
		}
	}
	
	
	private int checkKeyword(String id) {
		if (id == "if")
			return _IF;
		else if (id == "else")
			return _ELSE;
		else if (id == "while")
			return _WHILE;
		else if (id == "do")
			return _DO;
		else if (id == "int")
			return _INT;
		else if (id == "char")
			return _CHAR;
		else if (id == "String")
			return _STRING;
		else if (id == "boolean")
			return _BOOLEAN;
		else if (id == "break")
			return _BREAK;
		else if (id == "class")
			return _CLASS;
		else if (id == "continue")
			return _CONTINUE;
		else if (id == "default")
			return _DEFAULT;
		else if (id == "pulbic")
			return _PUBLIC;
		else if (id == "private")
			return _PRIVATE;
		else if (id == "static")
			return _STATIC;
		else if (id == "final")
			return _FINAL;
		else if (id == "null")
			return _NULL;
		else if (id == "void")
			return _VOID;
		else if (id == "import")
			return _IMPORT;
		else if (id == "new")
			return _NEW;
		else if (id == "return")
			return _RETURN;

		else
			return _IDENTIFIER;
	}
	
	public char getNext() throws Exception
	{
		char next = (char)in.read();
		return next;
	}
	
}
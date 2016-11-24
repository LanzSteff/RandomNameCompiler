public class Parser{
	private Scanner scanner;
	private int token;
	private int nextToken;
	SymbolTable st;
	
	private boolean isParameter;
	private boolean funcNotFound;
	
	public Parser(String filename) throws Exception{
		scanner = new Scanner();
		scanner.openFile(filename);
		st = new SymbolTable();
		isParameter = false;
		funcNotFound = false;
	}
	
	public void parse() throws Exception{
		token = scanner.readToken();
		nextToken = scanner.readToken();
		//if(token != _EOF)
			syntax();
		//else System.err.println("EOF reached");
	}
	
	private void getNextToken() throws Exception {
		token = nextToken;
		nextToken = scanner.readToken();
	}
	
	private void syntax() throws Exception{
		//syntax = {method} {statement}.
		while(token != scanner._EOF && nextToken != scanner._EOF) {
			
			if(token == scanner._RBRACE || token == scanner._SEMIKOLON || token == scanner._LBRACE) {
				System.out.println("ERROR - } or ; or ( not expected (Line ~" + scanner.getLine() + ")");
				getNextToken();
			}
			
			if(token != scanner._METHOD) {
				statement();
			}
			
			else {
				method();
			}
		}
	}
	
	private void method() throws Exception{
		//method = "method" methodhead "{" methodbody "}".
		getNextToken();
		st.add(null, 10, null, 1);
		methodhead();
		
		if (token != scanner._LBRACE) {
			error('{');
			//System.out.println("ERROR - '{' expected");
		}
		
		else {
			//getNextToken();
			methodbody();
		}
	}
	
	private void methodhead() throws Exception {
		//methodhead = type identifier "(" [parameter] ")".
		type();
		getNextToken();
		identifier();
		
		getNextToken();
		if (token != scanner._LPAREN) {
			error('(');
			//System.out.println("ERROR -  '(' expected");
		}
		
		else if (nextToken == scanner._RPAREN) {
			/* 
			 * TODO: symboltable ohne parameter
			 */
			//System.out.println("methode deklariert - keine parameter");
			getNextToken(); getNextToken();
		}
		
		else {
			getNextToken();
			parameter();
			
			if (nextToken == scanner._RPAREN) {
				//System.out.println("methode deklariert - mit parameter");
				getNextToken(); getNextToken();
			}
		}
		
	}
	
	private void type() throws Exception {
		//type = ["public" | "private"] "int" | "String" | "char" | "void" | "boolean" | "class".
		int type = 0;
		int vis = st.VISIBILITY_PUBLIC;
		if (token == scanner._PRIVATE || token == scanner._PUBLIC) {
			 if (token == scanner._PRIVATE) {
				 vis = st.VISIBILITY_PRIVATE;
			 }
			 getNextToken();
		 }
		
		 if (token == scanner._INT) {
			 type = st.TYPE_INT;
		 }
	 
		 else if (token == scanner._STRING) {
			 type = st.TYPE_STRING;
		 }
		 
		 else if (token == scanner._CHAR) {
			 type = st.TYPE_CHAR;
		 }
		 
		 /*else if (token == scanner._VOID) {
			 type = st.TYPE_VOID;
		 }*/
		 
		 else if (token == scanner._BOOLEAN) {
			 type = st.TYPE_BOOLEAN;
		 }
		 
		 else if (token == scanner._CLASS || token == scanner._VOID) {
			 type = st.TYPE_CLASS;
		 }
		 
		 if (type == 0) {
			 System.out.println("ERROR - Type definition expected");
		 }
		 
		 if (isParameter) {
			 //TODO: implementieren
			 if (st.last.parameter == null)
				 st.last.createTable(type);
			 else
				 st.last.addElement(type);
		 }
		 
		 else if (st.last != null && st.last.type == 10) {
			 st.last.updateType(type+10);
			 st.last.updateVis(vis);
		 }
		 
		 else {
			 st.add(null, type, null, vis);
		 }
	}
	
	private void parameter() throws Exception {
		//parameter = type identifier {"," parameter}.
		
		isParameter = true;
		type();
		getNextToken();
		identifier();
		
		while (nextToken == scanner._COMMA) {
			getNextToken(); getNextToken();
			parameter();
		}
		
		isParameter = false;
	}
	
	private void identifier() throws Exception {
		if (isParameter) {
			st.last.updateLastParameter(scanner.getValue());
		}
		
		else if (st.last == null || st.last.getName() == null) {
			st.updateName(scanner.getValue());
		}
		
		else if (nextToken == scanner._LPAREN) {
			SymbolTable.Node tmp = null;
			tmp = st.find(scanner.getValue());
			if (tmp != null) {
				System.out.println("Method found");
			}
			else {
				System.out.println("Method '" + scanner.getValue() + "' not found");
				funcNotFound = true;
			}
		}
	
		/*
		else if (nextToken == scanner._COMMA || nextToken == scanner._RPAREN) {
			System.out.println("Parameter found");
		}*/
	}
	
	private void methodbody() throws Exception {
		//methodbody = {statement}.
		//TODO: statement implementieren bzw. while
		getNextToken();
		while (token != scanner._RBRACE) {
			statement();
		}
		getNextToken();
	}
	
	private void statement() throws Exception {
		//statement = (assignment | declaration | methodcall | return) ";" | ifstatement | whilestatement.
		//TODO: int y=3; implementieren
		if (token == scanner._IDENTIFIER && nextToken == scanner._ASSIGN) {
			assignment();
			if (nextToken != scanner._SEMIKOLON) {
				error(';');
				//System.out.println("ERROR - ';' expected");
			}
			else {
				getNextToken(); getNextToken();
			}
		}
		
		else if (token == scanner._IDENTIFIER && nextToken == scanner._LPAREN) {
			methodcall();
			if (nextToken != scanner._SEMIKOLON) {
				error(';');
				//System.out.println("ERROR - ';' expected");
			}
			else {
				getNextToken(); getNextToken();
			}
		}
		
		else if (token == scanner._RETURN) {
			ret();
			if (nextToken != scanner._SEMIKOLON) {
				error(';');
				//System.out.println("ERROR - ';' expected");
			}
			else {
				getNextToken(); getNextToken();
			}
		}
		
		else if (token == scanner._IF) {
			System.out.println("if");
			ifstatement();
		}
		
		else if (token == scanner._WHILE) {
			whilestatement();
		}
		else {
			declaration();
			if (token != scanner._SEMIKOLON) {
				error(';');
				//System.out.println("ERROR - ';' expected");
			}
			else {
				getNextToken();
			}
		}
	}
	
	private void declaration() throws Exception {
		//declaration = type identifier ["[" expression "]"].
		type();
		getNextToken();
		identifier();
		getNextToken();
		if(token == scanner._LBRACK) {
			getNextToken();
			expression();
			getNextToken();
			if(token != scanner._RBRACK) {
				error(']');
			}
		}
	}
	
	private void whilestatement() throws Exception {
		//whilestatement = "while" "(" expression ")" body.
		System.out.println("while");
		if(nextToken == scanner._LPAREN) {
			getNextToken(); getNextToken();
			expression();
			if(nextToken == scanner._RPAREN) {
				getNextToken(); getNextToken();
			}
			else {
				error(')');
				//System.out.println("ERROR - ')' expected");
			}
		}
		else {
			error('(');
			//System.out.println("ERROR - '(' expected");
			getNextToken();
		}
		
		body();
		
	}
	
	private void ifstatement() throws Exception {
		//ifstatement = "if" "(" expression ")" body {"else if" "(" expression ")" body} | ["else" body].
		if(nextToken == scanner._LPAREN) {
			getNextToken(); getNextToken();
			if(token != scanner._RPAREN) {
				expression();
				getNextToken();
			}
			else {
				System.out.println("IF condition requested!");
			}
			
			if(token == scanner._RPAREN) {
				getNextToken();
			}
			else {
				error(')');
				//System.out.println("ERROR - ')' expected");
			}
		}
		else {
			error('(');
			//System.out.println("ERROR - '(' expected");
			getNextToken();
		}
		
		body();
//		getNextToken();
		if(token == scanner._ELSEIF) {
			System.out.println("else if");
			ifstatement();
		}
		
		if(token == scanner._ELSE) {
			System.out.println("else");
			getNextToken();
			body();
		}
	}
	
	private void body() throws Exception {
		//body = statement | "{" {statement} "}".
		if(token == scanner._LBRACE) {
			getNextToken();
			while(token != scanner._RBRACE && token != scanner._EOF && token != scanner._METHOD) {
				statement();
				
				if(token ==  scanner._EOF || token != scanner._METHOD) {
					error('}');
				}
			}
			getNextToken();
		}
		else
			statement();
	}
	
	private void ret() throws Exception {
		//return = "return" expression.
		System.out.println("return");
		getNextToken();
		expression();
	}
	
	private void assignment() throws Exception {
		//assignment = identifier {"." identifier} "=" expression ';'.
		identifier();
		String name = scanner.getValue();
		getNextToken(); getNextToken();
		
		// wert aus funktion
		if (token == scanner._IDENTIFIER && nextToken == scanner._LPAREN) {
			//TODO: ??
			methodcall();
		}
	
		// normaler wert
		else {
			SymbolTable.Node tmp = st.find(name);
			if (tmp != null) {
				tmp.updateValue(scanner.getValue());
			}
			
			else {
				System.out.println("Variable not found");
			}
		}
	}
	
	private void methodcall() throws Exception {
		//methodcall = identifier "(" [expression {"," expression}] ")".
		// TODO part 2
		identifier();
		
		// token = function name
		getNextToken(); getNextToken();
		if (funcNotFound) {
			while (nextToken != scanner._SEMIKOLON)
				getNextToken();
			
			funcNotFound = false;
		}
		
		// ohne par
		else if (token == scanner._RPAREN) {
			System.out.println("Methode ausfuehren");
		}
		
		// mit par
		else {
			expression();
			while (nextToken == scanner._COMMA){
				getNextToken(); getNextToken();
				expression();
			}
		}
	}
	
	private void expression() throws Exception {
		//expression = subexpression [("==" | "!=" | "<" | ">" | "<=" | ">=") subexpression].
		subexpression();
		getNextToken();
		if (token == scanner._EQUAL) {
			System.out.println("equal");
			getNextToken();
			subexpression();
		}
		
		else if (token == scanner._NOTEQ) {
			System.out.println("noteq");
			getNextToken();
			subexpression();
		}
		
		else if (token == scanner._GREATER) {
			System.out.println("greater");
			getNextToken();
			subexpression();
		}
		else if (token == scanner._LESS) {
			System.out.println("less");
			getNextToken();
			subexpression();
		}
		else if (token == scanner._LEQ) {
			System.out.println("less equal");
			getNextToken();
			subexpression();
		}
		else if (token == scanner._GEQ) {
			System.out.println("greater equal");
			getNextToken();
			subexpression();
		}
		
		else {
			System.out.println("ERROR - sign expected");
			getNextToken();
			subexpression();
		}
		
		
	}
	
	private void subexpression() throws Exception {
		//subexpression = ["-"] term {("+" | "-" | "||") term}.
		if (token == scanner._MINUS) {
			System.out.println("minus");
			getNextToken();
		}
		
		term();
		while (nextToken == scanner._PLUS || nextToken == scanner._MINUS || nextToken == scanner._OR) {
			getNextToken();
			if (token == scanner._PLUS) {
				System.out.println("plus");
			}
			
			else if (token == scanner._MINUS) {
				System.out.println("minus");
			}
			
			else if (token == scanner._OR) {
				System.out.println("or");
			}
			
			else {
				System.out.println("ERROR - sign expected 2");
			}
			
			getNextToken();
			term();
		}
		
	}
	
	private void term() throws Exception {
		//term = factor {("*" | "/" | "&&" | "%") factor}.
		factor();
		
		while (nextToken == scanner._TIMES || nextToken == scanner._SLASH || nextToken == scanner._AND || nextToken == scanner._PERCENT) {
			getNextToken();
			if (token == scanner._TIMES) {
				System.out.println("times");
			}
			
			else if (token == scanner._SLASH) {
				System.out.println("slash");
			}
			
			else if (token == scanner._AND) {
				System.out.println("and");
			}
			
			else if (token == scanner._PERCENT) {
				System.out.println("percent");
			}
			
			else {
				System.out.println("ERROR - * or / or && or % expected");
			}
			
			getNextToken();
			factor();
		}
	}
	
	private void factor() throws Exception {
		//factor = identifier {"." identifier} | number | string | char | "(" expression ")" | methodcall | ("!" factor).
		if (token == scanner._IDENTIFIER && nextToken != scanner._LPAREN) {
			identifier();
		}
		
		else if (token == scanner._NUMBER) {
			number();
		}
		
		else if (token == scanner._QUOTE) {
			quote();
		}
		
		else if (token == scanner._SQUOTE) {
			squote();
		}
		
		else if (token == scanner._IDENTIFIER && nextToken == scanner._LPAREN) {
			methodcall();
		}
		
		else if (token == scanner._NOT) {
			//TODO: not function
			//factor();
		}
		
		else if (token == scanner._LPAREN) {
			getNextToken();
			expression();
			getNextToken();
			
			if (token != scanner._RPAREN) {
				error('(');
				//System.out.println("ERROR - '(' expected");
			}
		}
	}
	
	private void number() {
		//number = [-] digit {digit}.
		System.out.println("number");
	}
	
	private void quote() {
		System.out.println("quote");
	}	
	
	private void squote() {
		System.out.println("squote");
	}
	
	private void error(char ch) throws Exception {
		System.out.println("ERROR - '" + ch + "' expected (Line ~" + scanner.getLine() + ")");
		
		while(token != scanner._SEMIKOLON && token != scanner._IF && token != scanner._ELSEIF && token != scanner._ELSE && 
				token != scanner._WHILE && token != scanner._RBRACE && token != scanner._METHOD && token != scanner._EOF) {
		/*while(nextToken != scanner._SEMIKOLON && nextToken != scanner._IF && nextToken != scanner._ELSEIF && nextToken != scanner._ELSE && 
				nextToken != scanner._WHILE && nextToken != scanner._RBRACE && nextToken != scanner._METHOD && nextToken != scanner._EOF) {*/
			getNextToken();
			
			if(token == scanner._SEMIKOLON || token == scanner._RBRACE) {
				getNextToken();
			}
			
		}
	}
}
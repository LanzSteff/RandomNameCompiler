public class Parser{
	private Scanner scanner;
	private int token;
	private int nextToken;
	
	public Parser(String filename) throws Exception{
		scanner.openFile(filename);
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
		while(token != scanner._METHOD && token != scanner._EOF){
			statement();
		}
		
		while(token != scanner._EOF){
			method();
		}
	}
	
	private void method() throws Exception{
		//method = "method" methodhead "{" methodbody "}".
		getNextToken();
		methodhead();
		
		if (token != scanner._LBRACE) {
			System.out.println("ERROR - '{' expected");
		}
		
		else {
			getNextToken();
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
			System.out.println("ERROR -  '(' expected");
		}
		
		else if (nextToken == scanner._RPAREN) {
			System.out.println("funktion ausführen");
			getNextToken(); getNextToken();
		}
		
		else {
			getNextToken();
			parameter();
			
			if (nextToken == scanner._RPAREN) {
				System.out.println("funktion ausführen");
				getNextToken(); getNextToken();
			}
		}
		
	}
	
	private void type() throws Exception {
		//type = ["public" | "private"] "int" | "String" | "char" | "void" | "boolean" | "class".
		 if (token == scanner._PRIVATE || token == scanner._PUBLIC) {
			System.out.println("PRIVATE/PUBLIC");
			getNextToken();
		 }
		 
		 if (token == scanner._INT) {
			 System.out.println("int");
		 }
		 
		 else if (token == scanner._STRING) {
			 System.out.println("string");
		 }
		 
		 else if (token == scanner._CHAR) {
			 System.out.println("char");
		 }
		 
		 else if (token == scanner._VOID) {
			 System.out.println("void");
		 }
		 
		 else if (token == scanner._BOOLEAN) {
			 System.out.println("boolean");
		 }
		 
		 else if (token == scanner._CLASS) {
			 System.out.println("class");
		 }
	}
	
	private void parameter() throws Exception {
		//parameter = type identifier {"," parameter}.
		type();
		getNextToken();
		identifier();
		
		while (nextToken == scanner._COMMA) {
			getNextToken(); getNextToken();
			parameter();
		}
	}
	
	private void identifier() {
		System.out.println("identifier");
	}
	
	private void methodbody() throws Exception {
		//methodbody = {statement}.
		//TODO: statement implementieren bzw. while
		while (token != scanner._RBRACE) {
			statement();
		}
	}
	
	private void statement() throws Exception {
		//statement = (assignment | declaration | methodcall | return) ";" | ifstatement | whilestatement.
		//TODO: int y=3; implementieren
		if (token == scanner._IDENTIFIER && nextToken == scanner._ASSIGN) {
			assignment();
			if (nextToken != scanner._SEMIKOLON) {
				System.out.println("ERROR - ';' expected");
			}
			getNextToken(); getNextToken();
		}
		
		else if (token == scanner._IDENTIFIER && nextToken == scanner._LPAREN) {
			methodcall();
			if (nextToken != scanner._SEMIKOLON) {
				System.out.println("ERROR - ';' expected");
			}
			getNextToken(); getNextToken();
		}
		
		else if (token == scanner._RETURN) {
			ret();
			if (nextToken != scanner._SEMIKOLON) {
				System.out.println("ERROR - ';' expected");
			}
			getNextToken(); getNextToken();
		}
		
		else if (token == scanner._IF) {
			ifstatement();
		}
		
		else if (token == scanner._WHILE) {
			whilestatement();
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
				System.out.println("ERROR - ')' expected");
			}
		}
		else {
			System.out.println("ERROR - '(' expected");
			getNextToken();
		}
		
		body();
		
	}
	
	private void ifstatement() throws Exception {
		//ifstatement = "if" "(" expression ")" body {"else if" "(" expression ")" body} | ["else" body].
		System.out.println("if");
		if(nextToken == scanner._LPAREN) {
			getNextToken(); getNextToken();
			expression();
			if(nextToken == scanner._RPAREN) {
				getNextToken(); getNextToken();
			}
			else {
				System.out.println("ERROR - ')' expected");
			}
		}
		else {
			System.out.println("ERROR - '(' expected");
			getNextToken();
		}
		
		body();
		getNextToken();
		while(token == scanner._ELSEIF) {
			System.out.println("else if");
			if(nextToken == scanner._LPAREN) {
				getNextToken(); getNextToken();
				expression();
				if(nextToken == scanner._RPAREN) {
					getNextToken(); getNextToken();
				}
				else {
					System.out.println("ERROR - ')' expected");
				}
			}
			else {
				System.out.println("ERROR - '(' expected");
				getNextToken();
			}
			
			body();
			getNextToken();
		}
		
		if(token == scanner._ELSE) {
			System.out.println("else");			
			body();
		}
	}
	
	private void body() throws Exception {
		//body = statement | "{" {statement} "}".
		if(token == scanner._LBRACE) {
			getNextToken();
			while(token != scanner._RBRACE) {
				statement();
				getNextToken();
			}
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
		//assignment = identifier {"." identifier} "=" expression.
		identifier();
		getNextToken(); getNextToken();
		System.out.println("assign - symboltable");
	}
	
	private void methodcall() throws Exception {
		//methodcall = identifier {"." identifier} "(" [expression {"," expression}] ")".
		identifier();
		getNextToken(); getNextToken();
		
		if (token == scanner._RPAREN) {
			System.out.println("methode ausführen");
		}
		
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
			subexpression();
		}
		
		else if (token == scanner._NOTEQ) {
			System.out.println("noteq");
			subexpression();
		}
		
		else if (token == scanner._GREATER) {
			System.out.println("greater");
			subexpression();
		}
		else if (token == scanner._LESS) {
			System.out.println("less");
			subexpression();
		}
		else if (token == scanner._LEQ) {
			System.out.println("less equal");
			subexpression();
		}
		else if (token == scanner._GEQ) {
			System.out.println("greater equal");
			subexpression();
		}
		
		else {
			System.out.println("ERROR - sign expected");
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
				System.out.println("ERROR - sign expected");
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
				System.out.println("ERROR - sign expected");
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
				System.out.println("ERROR - '(' expected");
			}
		}
	}
	
	private void number() {
		System.out.println("number");
	}
	
	private void quote() {
		System.out.println("quote");
	}	
	
	private void squote() {
		System.out.println("squote");
	}
}
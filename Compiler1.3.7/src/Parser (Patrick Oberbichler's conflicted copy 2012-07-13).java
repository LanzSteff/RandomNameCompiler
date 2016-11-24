import java.util.Stack;
import java.util.Vector;

public class Parser{
	private Scanner scanner;
	public CodeGenerator cg;
	private int token;
	private int nextToken;
	private String prevTokenValue;
	SymbolTable st;
	Stack<FixupNode> fixupList = new Stack<FixupNode>();
	Stack<FixupNode> instructionFixList = new Stack<FixupNode>();
	Stack<FixupNode> branchFixList = new Stack<FixupNode>();
	Vector<SymbolTable> symTable = new Vector<SymbolTable>();
	Stack<Integer> currTable = new Stack<Integer>();
	ItemDesc returnItem = null;
	
	private boolean isParameter;
	private boolean funcNotFound;
	
	public Parser(String filename) throws Exception{
		cg = new CodeGenerator();
		scanner = new Scanner();
		scanner.openFile(filename);
		st = new SymbolTable("global");
		symTable.add(st);
		currTable.push(0);
		isParameter = false;
		funcNotFound = false;
	}
	
	public void parse() throws Exception{
		token = scanner.readToken();
		nextToken = scanner.readToken();
		syntax();
	}
	
	private void getNextToken() throws Exception {
		prevTokenValue = scanner.getValue();
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

		cg.tm.startOfMain = symTable.get(0).find("main").offset;
		int firstMeth = 0;
		for (int i=0; i < symTable.get(0).size; i++) {
			if (symTable.get(0).get(i).type > 10 && symTable.get(0).get(i).type < 20) {
				firstMeth = i;
				break;
			}
		}
		cg.tm.endOfGlobal = symTable.get(0).get(firstMeth).offset;
		
//		for (int i=0; i < symTable.size(); i++) {
//			if (symTable.get(i).name.compareTo("main") == 0) {
//				currSTid = i;
//			}
//		}
//		System.out.println(cg.tm.startOfMain);
//		int size = branchFixList.size();
//		for (int i=0; i < size-1; i++)
//			fixBranch();
		
		while(!branchFixList.empty()) {
			FixupNode node = branchFixList.pop();
			if (node.c == 1)
				fixReturn(node);
			else
				fixBranch(node);
		}
		
		while(!instructionFixList.empty())
			fixInstruction();
		
		cg.tm.decode();
		cg.tm.printInstructions();
		cg.tm.execute();
	}
	
	private void method() throws Exception{
		//method = "method" methodhead "{" methodbody "}".
		getNextToken();
		String methName = methodhead();
		
		if (token != scanner._LBRACE && token != scanner._SEMIKOLON) {
			System.out.println("ERROR - '{' or ';' expected (Line ~" + scanner.getLine() + ")");
		}
		
		else if (token == scanner._SEMIKOLON) {
			getNextToken();
		}
		
		else {
			methodbody(methName);
		}
	}
	
	private String methodhead() throws Exception {
		//methodhead = type identifier "(" [parameter] ")".
		st = symTable.get(0);
		st.add(null,10,1);
		type();
		getNextToken();
		identifier();
		
		String methName = scanner.getValue();
		
		if (methName.equals("main")) {
			cg.encode("SUBI", 30, 30, 0);
			instructionFixList.push(new FixupNode("SUBI", 30,30,cg.tm.instructions.size()-1, "main"));
			cg.encode("ADD", 28, 0, 30);
		}
		
		boolean exists = false;
		for (int i=0; i < st.size-1; i++) {
			if (st.get(i).name.compareTo(methName)==0) {
				st.deleteLastNode();
				exists = true;
			}
		}
		
		if (!exists) {
			st = new SymbolTable(methName);
			symTable.add(st);
		}
		
		else {
			for (int i=0; i < symTable.size(); i++) {
				if (symTable.get(i).name.compareTo(methName) == 0) {
					st = symTable.get(i);
				}
			}
		}
		
		getNextToken();
		if (token != scanner._LPAREN) {
			error('(');
		}
		
		else if (nextToken == scanner._RPAREN) {
			getNextToken(); getNextToken();
		}
		
		else {
			getNextToken();
			if (!exists)
				parameter();
			else 
				while (nextToken != scanner._RPAREN)
					getNextToken();
			
			if (nextToken == scanner._RPAREN) {
				getNextToken(); getNextToken();
			}
		}
		
		return methName;
	}
	
	private void methodbody(String name) throws Exception {
		//methodbody = {statement}.
		getNextToken();
		st = symTable.get(0);
		
		Node n = st.find(name);
		if (cg.tm.instructions.size() == 0)
			n.offset = 0;
		else
			n.offset = cg.tm.instructions.size();
		
		for (int i=0; i < symTable.size(); i++) {
			if (symTable.get(i).name.compareTo(name) == 0) {
				st = symTable.get(i);
			}
		}
		while (token != scanner._RBRACE && token != scanner._EOF && nextToken != scanner._RBRACE) {
			statement();
		}
		
		n.size = cg.tm.instructions.size() - n.offset;
		if (name.compareTo("main")==0)
			cg.encode("EXT",0,0,0);
		else
			cg.encode("BRR", 31);
		
		getNextToken();
	}
	
	private void methodcall() throws Exception {
		//methodcall = identifier "(" [expression {"," expression}] ")".
		
		Node tmp = null;
		if (nextToken == scanner._LPAREN) {
			tmp = symTable.get(0).find(scanner.getValue());
			if (tmp != null) {
				String currName = st.name;
//				currTable.push(currSTid);
				for (int i=0; i < symTable.size(); i++) {
					if (symTable.get(i).name.compareTo(tmp.name) == 0) {
						st = symTable.get(i);
					}
					if (symTable.get(i).name.compareTo(currName) == 0) {
						currTable.push(i);
					}
				}
			}
			else {
				System.out.println("Method '" + scanner.getValue() + "' not found");
				funcNotFound = true;
			}
		}
		/*
		cg.encode("ADDI", 31, 0, cg.tm.progCounter);
		fixupList.push(new FixupNode("ADDI", 31, 0, 0, cg.tm.instructions.size()-1));
		cg.encode("PSH", 31, 30, 1);
		cg.encode("PSH", 28, 30, 1);
		cg.encode("SUBI", 30, 30, 0);
		instructionFixList.push(new FixupNode("SUBI", 30,30,cg.tm.instructions.size()-1, st.name));
		cg.encode("ADD", 28, 0, 30);
		*/
		// token = function name
		getNextToken(); getNextToken();
		if (funcNotFound) {
			while (nextToken != scanner._SEMIKOLON)
				getNextToken();
			
			funcNotFound = false;
		}
		
		if (tmp.parameter == null) {
			// ohne par
			if (token == scanner._RPAREN) {
				cg.encode("ADDI", 31, 0, cg.tm.progCounter);
				fixupList.push(new FixupNode("ADDI", 31, 0, 0, cg.tm.instructions.size()-1));
				cg.encode("PSH", 31, 30, 1);
				cg.encode("PSH", 28, 30, 1);
				cg.encode("SUBI", 30, 30, 0);
				instructionFixList.push(new FixupNode("SUBI", 30,30,cg.tm.instructions.size()-1, st.name));
				cg.encode("ADD", 28, 0, 30);
				
				cg.encode("BR", tmp.offset);
				fixupMeth();
				branchFixList.push(new FixupNode("BR",0,cg.tm.instructions.size()-1, tmp.getName()));
			}
			else
				error("no parameter expected");
		}
		
		// mit par
		else {
			ItemDesc item;
			boolean typeCheck = true;
			for (int i=0; i < tmp.parameter.size; i++) {
				
				SymbolTable current = st;
				int h = currTable.pop();
				st = symTable.get(h);
				item = expression();
				st = current;
				currTable.push(h);
				
				cg.encode("ADDI", 31, 0, cg.tm.progCounter);
				fixupList.push(new FixupNode("ADDI", 31, 0, 0, cg.tm.instructions.size()-1));
				cg.encode("PSH", 31, 30, 1);
				cg.encode("PSH", 28, 30, 1);
				cg.encode("SUBI", 30, 30, 0);
				instructionFixList.push(new FixupNode("SUBI", 30,30,cg.tm.instructions.size()-1, st.name));
				cg.encode("ADD", 28, 0, 30);
				
				if (item.type.type != tmp.parameter.get(i).type) {
					error("wrong type");
					typeCheck = false;
				}
				
				if (item.mode == ItemDesc.CONST) {
					cg.encode("ADDI", cg.tm.getFreeReg(), 0, item.value);
					cg.encode("STW", cg.tm.getFreeReg(), 28, item.offset);
				}
				else {
					cg.encode("STW", item.reg, 28, item.offset);
					cg.tm.decrementReg();
				}
				
				getNextToken();
				if (token == scanner._COMMA)
					getNextToken();
			}
			if (typeCheck) {
				cg.encode("BR", tmp.offset);
				fixupMeth();
				branchFixList.push(new FixupNode("BR",0,cg.tm.instructions.size()-1, tmp.getName()));
			}
			
//			while (nextToken == scanner._COMMA){
//				getNextToken(); getNextToken();
//				expression();
//			}
		}
		cleanUp(0);
	}
	
	public void cleanUp(int localSize) throws Exception {
		cg.encode("ADDI", 30, 30, localSize);
		instructionFixList.push(new FixupNode("ADDI",30,30,cg.tm.instructions.size()-1, st.name));
		cg.encode("POP", 28, 30, 1);
		cg.encode("POP", 31, 30, 1);
		st = symTable.get(currTable.pop());
	}
	
	
	private void type() throws Exception {
		//type = ["public" | "private"] "int" | "String" | "char" | "void" | "boolean" | "class".
		int type = 0;
		int vis = st.VISIBILITY_PUBLIC;
		Node tmp = null;
		if (token == scanner._PRIVATE || token == scanner._PUBLIC) {
			 if (token == scanner._PRIVATE) {
				 vis = st.VISIBILITY_PRIVATE;
			 }
			 getNextToken();
		 }
		 
		 if (token == scanner._INT) {
			 type = SymbolTable.TYPE_INT;
		 }
	 
		 else if (token == scanner._STRING) {
			 type = SymbolTable.TYPE_STRING;
		 }
		 
		 else if (token == scanner._CHAR) {
			 type = SymbolTable.TYPE_CHAR;
		 }
		 
		 else if (token == scanner._BOOLEAN) {
			 type = SymbolTable.TYPE_BOOLEAN;
		 }
		 
		 else if (token == scanner._CLASS || token == scanner._VOID) {
			 type = SymbolTable.TYPE_CLASS;
		 }
		 
		 else if (token == scanner._IDENTIFIER) {
			 tmp = symTable.get(0).find(prevTokenValue);
			 if (tmp != null && tmp.type == SymbolTable.TYPE_CLASS) {
				 type = SymbolTable.TYPE_CLASS;
			 }
			 else
				 error("class not found");
		 }
		 
		 if (type == 0) {
			 error("ERROR - Type definition expected");
		 }
		 
		 if (isParameter) {
			 if (symTable.get(0).last.parameter == null) {
				 symTable.get(0).last.createTable(type);
			 }
			 else {
				 symTable.get(0).last.addElement(type);
			 }
			 
			 st.add(type);
		 }
		 
		 else if (st.last != null && st.last.type == 10) {
			 st.last.updateType(type+10);
			 st.last.updateVis(vis);
		 }
		 
		 else if (nextToken == scanner._ARRAY) {
			 st.add(null, type+20, vis);
			 getNextToken();
		 }
		 
		 else {
			 st.add(null, type, vis);
			 if (tmp != null) {
				 st.last.parameter = tmp.parameter;
				 st.last.size = tmp.size;
			 }
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
			symTable.get(0).last.updateLastParameter(scanner.getValue());
			st.updateName(scanner.getValue());
		}
		
		else if (st.last != null && st.last.getName() == null) {
			st.updateName(scanner.getValue());
		}
	}
	
	private void statement() throws Exception {
		//statement = (assignment | declaration | methodcall | return) ";" | ifstatement | whilestatement.
		if (token == scanner._IDENTIFIER && (nextToken == scanner._ASSIGN || nextToken == scanner._LBRACK || nextToken == scanner._DOT)) {
			assignment();
			if (nextToken != scanner._SEMIKOLON) {
				error('a');
			}
			else {
				getNextToken(); getNextToken();
			}
		}
		
		else if (token == scanner._IDENTIFIER && nextToken == scanner._LPAREN) {
			methodcall();
			if (nextToken != scanner._SEMIKOLON) {
				error('.');
			}
			else {
				getNextToken(); getNextToken();
			}
		}
		
		else if (token == scanner._RETURN) {
			ret();
			
			cg.encode("BR", 0);
			branchFixList.push(new FixupNode("BR", 1, cg.tm.instructions.size()-1, st.name));
			
			if (nextToken != scanner._SEMIKOLON) {
				error(';');
			}
			else {
				getNextToken(); getNextToken();
			}
		}
		
		else if (token == scanner._IF) {
			ifstatement();
		}
		
		else if (token == scanner._WHILE) {
			whilestatement();
		}
		
		else if (token == scanner._PRINTF) {
			getNextToken();
			if (token != scanner._LPAREN)
				error('"');
			
			getNextToken();
			if (token == scanner._QUOTE || token == scanner._SQUOTE || token == scanner._IDENTIFIER) {
				if (token == scanner._IDENTIFIER) {
					Node stringNode = null;
					String name = scanner.getValue();
					
					// std on heap
					int offset = cg.tm.reg[29];
					stringNode = st.find(name);
					if (stringNode == null) {
						stringNode = symTable.get(0).find(name);
						offset = 0;
					}
					
					if (stringNode == null)
						error("variable not found");
					
					offset += stringNode.offset;
					cg.encode("PRT", offset);
				}
						
				else
					error("global strings not yet implemented");
			}
			
			else
				error("string expected");
			
			if (nextToken != scanner._RPAREN)
				error(')');
			
			getNextToken();getNextToken();getNextToken();
		
		}
		
		else {
			declaration();
			if (token != scanner._SEMIKOLON) {
				error(':');
			}
			else {
				getNextToken();
			}
		}
	}
	
	private void declaration() throws Exception {
		//declaration = type["[]"] identifier["{" declaration "}"].
		type();
		getNextToken();		
		identifier();
		getNextToken();
		
		if (st.last.type == SymbolTable.TYPE_STRING) {
			st.last.offset = cg.tm.malloc(Node.stringSize);
		}
		
		// classes
		if(token == scanner._LBRACE) {
			getNextToken();
			int type = 0;
			while (token != scanner._RBRACE && token != scanner._EOF && nextToken != scanner._RBRACE) {
				// get type
				if (token == scanner._INT)
					type = SymbolTable.TYPE_INT;
				
				else if (token == scanner._CHAR)
					type = SymbolTable.TYPE_CHAR;
				
				else if (token == scanner._BOOLEAN)
					type = SymbolTable.TYPE_BOOLEAN;
				
				else if (token == scanner._STRING)
					type = SymbolTable.TYPE_STRING;
				
				else if (token == scanner._CLASS)
					type = SymbolTable.TYPE_CLASS;
				
				// write into symboltable
				if (st.last.parameter == null)
					st.last.createTable(type);
				else
					st.last.addElement(type);
				
				// update name
				getNextToken();
				st.last.parameter.last.name = scanner.getStringValue(); 
				getNextToken(); getNextToken();
			}
			
			getNextToken();
			st.last.size = st.last.parameter.last.offset + st.last.parameter.last.size;
		}
	}
	
	private void whilestatement() throws Exception {
		//whilestatement = "while" "(" expression {("&&" | "||") expression} ")" body.
		ItemDesc item = null;
		int tmp = 0;
		if(nextToken == scanner._LPAREN) {
			getNextToken(); getNextToken();
			
			tmp = cg.tm.instructions.size()-1;
			item = condition();
			if(nextToken == scanner._RPAREN) {
				getNextToken(); getNextToken();
			}
			else {
				error(')');
			}
		}
		else {
			error('(');
			getNextToken();
		}
		
		cg.encode("BEQ", 0, 0);
		fixupList.push(new FixupNode("BEQ", item.reg, 0, cg.tm.instructions.size()-1));
		cg.tm.decrementReg();
		
		body();
		cg.encode("BEQ", 0, -(cg.tm.instructions.size()-1-tmp));
		fixup();
	}
	
	private ItemDesc condition() throws Exception {
		//TODO: ebnf
		ItemDesc item1 = null;
		if(token != scanner._RPAREN) {
			item1 = expression();
//			cg.encode("POP", item1.reg, item1.type.size);
//			getNextToken();
			while (nextToken == scanner._AND || nextToken == scanner._OR) {
				getNextToken();
				if (token == scanner._AND) {
					getNextToken();
					ItemDesc item2 = expression();
					cg.encode("AND", item1.reg, item1.reg, item2.reg);
					cg.tm.decrementReg();
				}
				else if (token == scanner._OR) {
					getNextToken();
					ItemDesc item2 = expression();
					cg.encode("OR", item1.reg, item1.reg, item2.reg);
					cg.tm.decrementReg();
				}
				else
					error("'&&' or '||' missing");
			}
		}
		else
			error("no valid condition");
		
		return item1;
	}
	
	private void ifstatement() throws Exception {
		//ifstatement = "if" "(" expression {("&&" | "||") expression} ")" body {"else if" "(" expression {("&&" | "||") expression} ")" body} | ["else" body].
		ItemDesc item = null;
		if(nextToken == scanner._LPAREN) {
			getNextToken(); getNextToken();
			
			//item = condition();
			item = expression();
			getNextToken();
			
			if(token == scanner._RPAREN) {
				getNextToken();
			}
			else {
				error("')' is missing");
			}
		}
		else {
			error("'(' is missing");
			getNextToken();
		}
		
		cg.encode("BEQ", item.reg, 0);
		fixupList.push(new FixupNode("BEQ", item.reg, 0, cg.tm.instructions.size()-1));
		cg.tm.decrementReg();
		
		body();
		if (token == scanner._ELSE || token == scanner._ELSEIF) {
			cg.encode("BEQ", 0, 0);
		}
		
		fixup();
		
		if(token == scanner._ELSEIF) {
			fixupList.push(new FixupNode("BEQ", 0, 0, cg.tm.instructions.size()-1));
			ifstatement();
			fixup();
		}
		
		if(token == scanner._ELSE) {
			fixupList.push(new FixupNode("BEQ", 0, 0, cg.tm.instructions.size()-1));
			getNextToken();
			
			body();
			fixup();
		}
	}
	
	public void fixInstruction() throws Exception {
		FixupNode node = instructionFixList.pop();
		for (int i=0; i < symTable.size(); i++) {
			if (symTable.get(i).name.compareTo(node.name) == 0) {
				node.c = symTable.get(i).size;
				break;
			}
		}
		
		cg.tm.instructions.set(node.pc, cg.encode(node));
		cg.tm.instructions.remove(cg.tm.instructions.size()-1);
	}
	
	// if/while
	public void fixup() throws Exception {
		FixupNode node = fixupList.pop();
		node.c = cg.tm.instructions.size()-node.pc;
		cg.tm.instructions.set(node.pc, cg.encode(node));
		cg.tm.instructions.remove(cg.tm.instructions.size()-1);
	}
	
	// methodcalls
	public void fixupMeth() throws Exception {
		FixupNode node = fixupList.pop();
		node.c = cg.tm.instructions.size();
		cg.tm.instructions.set(node.pc, cg.encode(node));
		cg.tm.instructions.remove(cg.tm.instructions.size()-1);
	}
	
	// return address
	public void fixBranch(FixupNode node) throws Exception {
		node.c = symTable.get(0).find(node.name).offset;
		cg.tm.instructions.set(node.pc, cg.encode(node));
		cg.tm.instructions.remove(cg.tm.instructions.size()-1);
	}
	
	public void fixReturn(FixupNode node) throws Exception {
		node.c = symTable.get(0).find(node.name).offset + symTable.get(0).find(node.name).size;
		cg.tm.instructions.set(node.pc, cg.encode(node));
		cg.tm.instructions.remove(cg.tm.instructions.size()-1);
	}
	
	private void body() throws Exception {
		//body = statement | "{" {statement} "}".
		if(token == scanner._LBRACE) {
			getNextToken();
			while(token != scanner._RBRACE && token != scanner._EOF && token != scanner._METHOD) {
				statement();
				
				if(token ==  scanner._EOF || token == scanner._METHOD) {
					error("'}' is missing");
				}
			}
			getNextToken();
		}
		else
			statement();
	}
	
	private void ret() throws Exception {
		//return = "return" expression.
		getNextToken();
		returnItem = expression();
	}
	
	private void assignment() throws Exception {
		//assignment = identifier "=" ["new"] expression[("[" expression "]") | "()"].
		identifier();
		String name = scanner.getValue();
		Node node = st.find(name);
		boolean global = false;
		if (node == null) {
			node = symTable.get(0).find(name);
			
			if (node == null)
				error("variable not found");

			else
				global = true;
		}
		
		int lhsOffset = node.offset;
		getNextToken();
		boolean lhsHeap = false;
		
		// lhs ist array
		if (token == scanner._LBRACK){
			global = true;
			getNextToken();
			ItemDesc item = expression();
			
			//TODO: offset berechnung bei array of records stimmt nicht
			if (item.value < st.find(name).size) {
				lhsOffset += item.value;
				//lhsOffset += item.value*st.find(name).size;
				lhsHeap = true;
			}
			else
				error("Arraysize out of bounds");
			
			getNextToken();
			if (token != scanner._RBRACK)
				error("']' missing");

			getNextToken();
		}
		
		
		while (token == scanner._DOT) {
			global = true;
			lhsHeap = true;
			getNextToken();
			lhsOffset += st.find(name).parameter.find(scanner.getValue()).offset;
			getNextToken();
		}
		
		if (lhsHeap)
			lhsOffset += cg.tm.reg[29];
		
		if (token != scanner._ASSIGN)
			error('=');
		
		getNextToken();
		
		// rechter wert
		// wert aus funktion
		if (token == scanner._IDENTIFIER && nextToken == scanner._LPAREN) {
			methodcall();
			
			if (global)
				cg.encode("STW", returnItem.reg, 0, lhsOffset);
			else
				cg.encode("STW", returnItem.reg, 28, lhsOffset);
			
			cg.tm.decrementReg();
		}
		
		// speicher holen
		else if (token == scanner._NEW) {
			getNextToken();
			// speicher fuer array
			if (nextToken == scanner._LBRACK) {
				if (token == scanner._INT && node.type == 21) {
					getNextToken(); getNextToken();
					if (token != scanner._RBRACK) {
						ItemDesc item = expression();
						node.size = item.value;
					}
					else {
						error("Arraysize missing");
					}
				}
				else if (token == scanner._CHAR && node.type == 22) {
					getNextToken(); getNextToken();
					if (token != scanner._RBRACK) {
						ItemDesc item = expression();
						node.size = item.value;
					}
					else {
						error("Arraysize missing");
					}
				}
				else if (token == scanner._STRING && node.type == 23) {
					getNextToken(); getNextToken();
					if (token != scanner._RBRACK) {
						ItemDesc item = expression();
						node.size = item.value * Node.stringSize;
					}
					else {
						error("Arraysize missing");
					}
				}
				else if (token == scanner._BOOLEAN && node.type == 24) {
					getNextToken(); getNextToken();
					if (token != scanner._RBRACK) {
						ItemDesc item = expression();
						node.size = item.value;
					}
					else {
						error("Arraysize missing");
					}
				}
				else if (token == scanner._IDENTIFIER && node.type == 25) {
					String rhsName = scanner.getValue();
					getNextToken(); getNextToken();
					if (token != scanner._RBRACK) {
						Node rec = symTable.get(0).find(rhsName);
						ItemDesc item = expression();
						node.size = item.value * rec.size;

						node.createTable(rec.parameter.get(0).type);
						node.parameter.get(0).name = rec.parameter.get(0).name;
						for (int i=1; i < rec.parameter.size; i++) {
							node.addElement(rec.parameter.get(i).type);
							node.parameter.last.name = rec.parameter.get(i).name;
						}	
					}
					else {
						error("Arraysize missing");
					}
				}
				else {
					error("Array type not valid");
					getNextToken(); getNextToken();
				}
				
				getNextToken();
				if (token != scanner._RBRACK) {
					error(']');
				}
			}		
			
			// speicher fuer record
			else if (token == scanner._IDENTIFIER) {
				Node rec = st.find(scanner.getValue());
				if (rec != null)
					node.size = rec.size;
				
				node.createTable(rec.parameter.get(0).type);
				for (int i=1; i < rec.parameter.size; i++)
					node.addElement(rec.parameter.get(i).type);
			}

			node.offset = cg.tm.malloc(node.size);
			node.onHeap = true;
			System.out.println(node.size + " speicher reserviert");
		}
		
		// String
		else if (token == scanner._QUOTE) {
			parseString(scanner.getStringValue(), lhsOffset);

			if (token != scanner._QUOTE)
				error("'\"' is missing");
		}
		
		// normaler wert
		else if ((token == scanner._IDENTIFIER) && (nextToken == scanner._SEMIKOLON || nextToken == scanner._LBRACK)) {
			String value = scanner.getValue();
			
			// rhs suchen
			boolean rhsGlobal = false;
			Node rhs = st.find(value);
			if (rhs == null) {
				rhs = symTable.get(0).find(value);
				rhsGlobal = true;
			}	
				
			if (rhs == null)
				error("variable not found");
				
			// array
			int arrayOffset = 0;
			int rhsOffset = rhs.offset;
			if (nextToken == scanner._LBRACK) {
				rhsOffset += cg.tm.reg[29];
				rhsGlobal = true;
				getNextToken(); getNextToken();
				ItemDesc item = expression();
				if (item.mode == ItemDesc.CONST) {
					arrayOffset = item.value;
					if (rhs.type == SymbolTable.TYPE_STRING_ARRAY)
						arrayOffset *= Node.stringSize;
					
					else if (rhs.type == SymbolTable.TYPE_CLASS_ARRAY)
						arrayOffset *= symTable.get(0).find(scanner.getValue()).size;
				}
					
				else {
					System.out.println("arraysize via variables not implemented yet");
				}
				
				getNextToken();
				if (token != scanner._RBRACK)
					error(']');
			}
				
				
			int freeReg = cg.tm.getFreeReg();
			cg.tm.incrementReg();
			
			if (rhsGlobal)
				cg.encode("LDW", freeReg, 0, rhsOffset+arrayOffset);
			else
				cg.encode("LDW", freeReg, 28, rhsOffset+arrayOffset);
			
			if (global)
				cg.encode("STW", freeReg, 0, lhsOffset);
			else
				cg.encode("STW", freeReg, 28, lhsOffset);
			
			cg.tm.decrementReg();
		}
		
		// Arithmetic Expressions:
		else {
			ItemDesc item = expression();
			if (item.mode == ItemDesc.CONST) {
				int freeReg = cg.tm.getFreeReg();
				cg.tm.incrementReg();
				cg.encode("ADDI", freeReg, 0, item.value);
				if (global)
					cg.encode("STW", freeReg, 0, lhsOffset);
				else
					cg.encode("STW", freeReg, 28, lhsOffset);
			}

			else {
				if (global)
					cg.encode("STW", item.reg, 0, lhsOffset);
				else
					cg.encode("STW", item.reg, 28, lhsOffset);
			}
			
			cg.tm.decrementReg();
		}
	}
	
	private ItemDesc expression() throws Exception {
		//expression = subexpression [("==" | "!=" | "<" | ">" | "<=" | ">=") subexpression].
		ItemDesc item1 = subexpression();
		if (nextToken != scanner._EQUAL && nextToken != scanner._NOTEQ && nextToken != scanner._GREATER && nextToken != scanner._LESS 
				&& nextToken != scanner._LEQ && nextToken != scanner._GEQ)
			return item1;
		
		if (item1.mode == ItemDesc.CONST) {
			item1.reg = cg.tm.getFreeReg();
			cg.tm.incrementReg();
			cg.encode("ADDI", item1.reg, 0, item1.value);
			item1.mode = ItemDesc.REG;
		}
		else if (item1.mode == ItemDesc.VAR)
			cg.loadItem(item1);
		
		getNextToken();
		int operator = token;
		getNextToken();
		ItemDesc item2 = subexpression();
		
		if (item2.mode == ItemDesc.CONST) {
			item2.reg = cg.tm.getFreeReg();
			cg.tm.incrementReg();
			cg.encode("ADDI", item2.reg, 0, item2.value);
			item2.mode = ItemDesc.REG;
		}
		else if (item2.mode == ItemDesc.VAR)
			cg.loadItem(item2);
		
		
		if (operator == scanner._EQUAL) {
			cg.encode("CMP", item1.reg, item1.reg, item2.reg);
			cg.tm.decrementReg();
		}
		else if (operator == scanner._NOTEQ) {
			cg.encode("NEQ", item1.reg, item1.reg, item2.reg);
			cg.tm.decrementReg();
		}
		else if (operator == scanner._GREATER) {
			cg.encode("GT", item1.reg, item1.reg, item2.reg);
			cg.tm.decrementReg();
		}
		else if (operator == scanner._LESS) {
			cg.encode("LT", item1.reg, item1.reg, item2.reg);
			cg.tm.decrementReg();
		}
		else if (operator == scanner._LEQ) {
			cg.encode("LEQ", item1.reg, item1.reg, item2.reg);
			cg.tm.decrementReg();
		}
		else if (operator == scanner._GEQ) {
			cg.encode("GEQ", item1.reg, item1.reg, item2.reg);
			cg.tm.decrementReg();
		}
		else
			error("operator not allowed");
		
//		cg.encode("PSH", item1.reg, cg.tm.reg[30], item1.type.size);
		
		item1.type.type = SymbolTable.TYPE_BOOLEAN;
		return item1;
	}
	
	private ItemDesc subexpression() throws Exception {
		//subexpression = ["-"] term {("+" | "-") term}.
		int sign = 1;
		if (token == scanner._MINUS) {
			sign = -1;
			getNextToken();
		}
		
		ItemDesc item1 = term();
		item1.value *= sign;
		
		while (nextToken == scanner._PLUS || nextToken == scanner._MINUS || nextToken == scanner._OR) {
			getNextToken();
			int operator = token;
			getNextToken();
			ItemDesc item2 = term();
			
			if (item1.type.type == SymbolTable.TYPE_INT && item2.type.type == SymbolTable.TYPE_INT) {
				if (item1.mode == ItemDesc.CONST && item2.mode == ItemDesc.CONST) {
					if (operator == scanner._PLUS || operator == scanner._OR)
						item1.value += item2.value;
					else if (operator == scanner._MINUS)
						item1.value -= item2.value;
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.CONST && item2.mode == ItemDesc.VAR) {
					ItemDesc tmp = new ItemDesc();
					tmp = item1;
					item1 = item2;
					item2 = tmp;
					
					cg.loadItem(item1);
					if (operator == scanner._PLUS || operator == scanner._OR)
						cg.encode("ADDI", item1.reg, item1.reg, item2.value);
					
					else if (operator == scanner._MINUS) {
						item2.reg = cg.getFreeReg();
						cg.tm.incrementReg();
						cg.encode("ADDI", item2.reg, 0, item2.value);
						cg.encode("SUB", item1.reg, item2.reg, item1.reg);
						cg.tm.decrementReg();
					}
					
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.VAR && item2.mode == ItemDesc.CONST) {
					cg.loadItem(item1);
					if (operator == scanner._PLUS || operator == scanner._OR)
						cg.encode("ADDI", item1.reg, item1.reg, item2.value);
					
					else if (operator == scanner._MINUS)
						cg.encode("SUBI", item1.reg, item1.reg, item2.value);
					
					else
						error("operator not allowed");
				}
								
				else if (item1.mode == ItemDesc.VAR && item2.mode == ItemDesc.VAR) {
					cg.loadItem(item1);
					cg.loadItem(item2);
					if (operator == scanner._PLUS || operator == scanner._OR) {
						cg.encode("ADD", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._MINUS) {
						cg.encode("SUB", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.VAR && item2.mode == ItemDesc.REG) {
					cg.loadItem(item1);
					int saveReg = item1.reg;
					if (item1.reg > item2.reg) {
						saveReg = item2.reg;
					}
					
					if (operator == scanner._PLUS || operator == scanner._OR) {
						cg.encode("ADD", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._MINUS) {
						cg.encode("SUB", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else
						error ("operator not allowed");
					
					item1.reg = saveReg;
				}
				
				else if (item1.mode == ItemDesc.CONST && item2.mode == ItemDesc.REG) {
					ItemDesc tmp = new ItemDesc();
					tmp = item2;
					item2 = item1;
					item1 = tmp;
					
					cg.loadItem(item2);
					if (operator == scanner._PLUS || operator == scanner._OR)
						cg.encode("ADD", item1.reg, item1.reg, item2.reg);
					
					else if (operator == scanner._MINUS) {
						item2.reg = cg.tm.getFreeReg();
						cg.encode("ADDI", item2.reg, 0, item2.value);
						cg.encode("SUB", item1.reg, item2.reg, item1.reg);
						cg.tm.decrementReg();
					}
					
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.REG && item2.mode == ItemDesc.CONST) {
					if (operator == scanner._PLUS || operator == scanner._OR)
						cg.encode("ADDI", item1.reg, item1.reg, item2.value);
					
					else if (operator == scanner._MINUS)
						cg.encode("SUBI", item1.reg, item1.reg, item2.value);
					
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.REG && item2.mode == ItemDesc.VAR) {
					cg.loadItem(item2);
					
					if (operator == scanner._PLUS || operator == scanner._OR) {
						cg.encode("ADD", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._MINUS) {
						cg.encode("SUB", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.REG && item2.mode == ItemDesc.REG) {
					int saveReg = item1.reg;
					if (item1.reg > item2.reg) {
						saveReg = item2.reg;
					}
					
					if (operator == scanner._PLUS || operator == scanner._OR) {
						cg.encode("ADD", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._MINUS) {
						cg.encode("SUB", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else
						error("operator not allowed");
					
					item1.reg = saveReg;
				}
			}
			
			else {
				System.out.println("ERROR - - or + expected");
			}
			
//			getNextToken();
//			term();
		}
		
		return item1;
	}
	
	private ItemDesc term() throws Exception {
		//term = factor {("*" | "/" | "%") factor}.
		ItemDesc item1 = factor();
		
		while (nextToken == scanner._TIMES || nextToken == scanner._SLASH || nextToken == scanner._PERCENT || nextToken == scanner._AND) {
			getNextToken();
			int operator = token;
			
//			boolean lazy = false;
//			if (operator == scanner._AND) {
//				if (item1.mode == ItemDesc.CONST && item1.value == 0) {
//					lazy = true;
//				}
//				else if (item1.mode == ItemDesc.VAR) {
//					cg.loadItem(item1);
//				}
//			}
			
			getNextToken();
			ItemDesc item2 = factor();
			
			if (item1.type.type == SymbolTable.TYPE_INT && item2.type.type == SymbolTable.TYPE_INT) {
				if (item1.mode == ItemDesc.CONST && item2.mode == ItemDesc.CONST) {
					if (operator == scanner._TIMES || operator == scanner._AND)
						item1.value *= item2.value;
					else if (operator == scanner._SLASH)
						item1.value /= item2.value;
					else if (operator == scanner._PERCENT)
						item1.value %= item2.value;
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.CONST && item2.mode == ItemDesc.VAR) {
					ItemDesc tmp = new ItemDesc();
					tmp = item1;
					item1 = item2;
					item2 = tmp;
					cg.loadItem(item1);
					
					if (operator == scanner._TIMES || operator == scanner._AND) {
						cg.encode("MULI", item1.reg, item1.reg, item2.value);
					}
					else if (operator == scanner._SLASH) {
						item2.reg = cg.getFreeReg();
						cg.encode("ADDI", item2.reg, 0, item2.value);
						cg.encode("DIV", item1.reg, item2.reg, item1.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._PERCENT) {
						item2.reg = cg.getFreeReg();
						cg.encode("ADDI", item2.reg, 0, item2.value);
						cg.encode("MOD", item1.reg, item2.reg, item1.reg);
						cg.tm.decrementReg();
					}
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.VAR && item2.mode == ItemDesc.CONST) {
					cg.loadItem(item1);
					if (operator == scanner._TIMES || operator == scanner._AND)
						cg.encode("MULI", item1.reg, item1.reg, item2.value);
					
					else if (operator == scanner._SLASH) {
						cg.encode("DIVI", item1.reg, item1.reg, item2.value);
					}
					
					else if (operator == scanner._PERCENT)
						cg.encode("MODI", item1.reg, item1.reg, item2.value);
					
					else
						error("operator not allowed");
				}
								
				else if (item1.mode == ItemDesc.VAR && item2.mode == ItemDesc.VAR) {
					cg.loadItem(item1);
					cg.loadItem(item2);
					if (operator == scanner._TIMES || operator == scanner._AND) {
						cg.encode("MUL", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._SLASH) {
						cg.encode("DIV", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._PERCENT) {
						cg.encode("MOD", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else
						error("operator not allowed");
				}
				
				//TODO: decrement richtig ?
				else if (item1.mode == ItemDesc.VAR && item2.mode == ItemDesc.REG) {
					cg.loadItem(item1);
					int saveReg = item1.reg;
					if (item1.reg > item2.reg) {
						saveReg = item2.reg;
					}
					
					if (operator == scanner._TIMES || operator == scanner._AND) {
						cg.encode("MUL", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._SLASH) {
						cg.encode("DIV", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._PERCENT) {
						cg.encode("MOD", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else
						error("operator not allowed");
					
					item1.reg = saveReg;
				}
				
				else if (item1.mode == ItemDesc.CONST && item2.mode == ItemDesc.REG) {
					ItemDesc tmp = new ItemDesc();
					tmp = item1;
					item1 = item2;
					item2 = tmp;
					
					if (operator == scanner._TIMES || operator == scanner._AND) {
						cg.encode("MULI", item1.reg, item1.reg, item2.value);
					}
					else if (operator == scanner._SLASH) {
						item2.reg = cg.getFreeReg();
						cg.encode("ADDI", item2.reg, 0, item2.value);
						cg.encode("DIV", item1.reg, item2.reg, item1.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._PERCENT) {
						item2.reg = cg.getFreeReg();
						cg.encode("ADDI", item2.reg, 0, item2.value);
						cg.encode("MOD", item1.reg, item2.reg, item1.reg);
						cg.tm.decrementReg();
					}
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.REG && item2.mode == ItemDesc.CONST) {
					if (operator == scanner._TIMES || operator == scanner._AND) {
						cg.encode("MULI", item1.reg, item1.reg, item2.value);
					}
					else if (operator == scanner._SLASH) {
						cg.encode("DIVI", item1.reg, item1.reg, item2.value);
					}
					else if (operator == scanner._PERCENT) {
						cg.encode("MODI", item1.reg, item1.reg, item2.value);
					}
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.REG && item2.mode == ItemDesc.VAR) {
					cg.loadItem(item2);
					if (operator == scanner._TIMES || operator == scanner._AND) {
						cg.encode("MUL", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					
					else if (operator == scanner._SLASH) {
						cg.encode("DIV", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					
					else if (operator == scanner._PERCENT) {
						cg.encode("MOD", item1.reg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					
					else
						error("operator not allowed");
				}
				
				else if (item1.mode == ItemDesc.REG && item2.mode == ItemDesc.REG) {
					int saveReg = item1.reg;
					if (item1.reg > item2.reg) {
						saveReg = item2.reg;
					}
					
					if (operator == scanner._TIMES || operator == scanner._AND) {
						cg.encode("MUL", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._SLASH) {
						cg.encode("DIV", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else if (operator == scanner._PERCENT) {
						cg.encode("MOD", saveReg, item1.reg, item2.reg);
						cg.tm.decrementReg();
					}
					else
						error("operator not allowed");
					
					item1.reg = saveReg;
				}
				
				else {
					System.out.println("ERROR - * or / or % expected");
				}
			}
//			getNextToken();
//			factor();
		}
		
		return item1;
	}
	
	private ItemDesc factor() throws Exception {
		//factor = identifier {"." identifier} | number | string | char | null | "(" expression ")" | methodcall | ("!" factor).
		TypeDesc typeDesc = new TypeDesc();
		ItemDesc item = new ItemDesc();
		
		if (token == scanner._IDENTIFIER && nextToken != scanner._LPAREN) {
			identifier();
			Node tmp = st.find(scanner.getValue());
			if (tmp == null) {
				tmp = symTable.get(0).find(scanner.getValue());
				if (tmp == null)
					error("variable not found");
				else
					item.isGlobal = true;
			}
			
			if (tmp != null) {
				//System.out.println(tmp.offset);
				typeDesc.length = tmp.size;
				typeDesc.size = tmp.size;
				typeDesc.type = tmp.type;
				
				item.mode = ItemDesc.VAR;
				item.type = typeDesc;
				item.offset = tmp.offset;
			}
		}
		
		else if (token == scanner._NUMBER) {
			item.mode = ItemDesc.CONST;
			item.value = scanner.getIntValue();
			typeDesc.type = SymbolTable.TYPE_INT;
			
			item.type = typeDesc;
		}
		
		else if (token == scanner._QUOTE) {
			//TODO: string ??
			//parseString(scanner.getStringValue());
		}
		
		else if (token == scanner._SQUOTE) {
			squote();
		}
		
		else if (token == scanner._NULL) {
			System.out.println("null");
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
			item = expression();
			getNextToken();
			
			if (token != scanner._RPAREN) {
				error(')');
			}
		}
		
		return item;
	}
	
	private void parseString(String value, int offset) throws Exception {
		int charValue = 0;
		int counter = 0;
		int freeReg = cg.tm.getFreeReg();
		cg.encode("ADDI", freeReg, 0, 0);
		
		for (int i=0; i < value.length(); i++) {
			charValue = value.charAt(i);

			cg.encode("ADDI", freeReg, freeReg, charValue);
			
			if ((i+1) % 4 == 0) {
				cg.encode("STW", freeReg, 29, offset+counter++);
				cg.encode("ADDI", freeReg, 0, 0);
			}
			
			else if (i == value.length()-1) {
				int shift = 3-(i%4);
				cg.encode("LSHI", freeReg, freeReg, 8*shift);
				cg.encode("STW", freeReg, 29, offset+counter);
			}
			
			else
				cg.encode("LSHI", freeReg, freeReg, 8);
		}
	}	
	
	
	private void squote() {
		System.out.println("squote");
	}
	
	private void error(char ch) throws Exception {
		System.out.println("ERROR - '" + ch + "' expected (Line ~" + scanner.getLine() + ")");
		
		while(token != scanner._SEMIKOLON && token != scanner._IF && token != scanner._ELSEIF && token != scanner._ELSE && 
				token != scanner._WHILE && token != scanner._RBRACE && token != scanner._METHOD && token != scanner._EOF) {			
			getNextToken();
			
			if(token == scanner._SEMIKOLON || token == scanner._RBRACE) {
				getNextToken();
			}
			
		}
	}
	
	private void error(String e) throws Exception {
		System.out.println("ERROR - " + e + " at ~ " + scanner.getLine());
		
		while(token != scanner._SEMIKOLON && token != scanner._IF && token != scanner._ELSEIF && token != scanner._ELSE && 
				token != scanner._WHILE && token != scanner._RBRACE && token != scanner._METHOD && token != scanner._EOF) {			
			getNextToken();
			
			if(token == scanner._SEMIKOLON || token == scanner._RBRACE) {
				getNextToken();
			}
		}
	}
	
	public void printAll() throws Exception {
		for(SymbolTable s : symTable) {
			System.out.println(s.name);
			s.print();
		}
	}
}
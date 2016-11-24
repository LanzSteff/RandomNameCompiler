public class Node{
	String name;
	int type;
	String value;
	int visibility;
	Node next;
	SymbolTable parameter;
	int size;
	int offset;
	
	public Node(String n, int t, String v, int vis){
		name = n;
		type = t;
		value = v;
		visibility = vis;
		next = null;
		parameter = null;
		
		if (type == SymbolTable.TYPE_INT)
			size = 4;
		else if (type == SymbolTable.TYPE_CHAR)
			size = 1;
		else if (type == SymbolTable.TYPE_STRING)
			size = 30;
		else if (type == SymbolTable.TYPE_BOOLEAN)
			size = 1;
		else if (type == SymbolTable.TYPE_CLASS)
			//TODO: 100?!
			size = 100;
		//else if (type == )
	}
	
	public Node(String n, int t, String v, int vis, SymbolTable par) {
		this(n,t,v,vis);
		if (t < 10) {
			System.err.println("only function can have parameters");
			parameter = null;
		}

		else
			parameter = par;
	}

	public void printPar() {
		if (type%10 == 1)
			System.out.print("int");
		else if (type%10 == 2)
			System.out.print("char");
		else if (type%10 == 3)
			System.out.print("string");
		else if (type%10 == 4)
			System.out.print("boolean");
		else if (type == 5)
			System.out.print("class");
		
		System.out.print(" " + name + ", ");
	}
	
	public void print() {
		System.out.print(name + "\t" + value + "\t");
		if (type > 10 && type < 21)
			System.out.print("m-");
		else if (type > 20 && type < 31)
			System.out.print("a-");
		
		if (type%10 == 1)
			System.out.print("int");
		else if (type%10 == 2)
			System.out.print("char");
		else if (type%10 == 3)
			System.out.print("string");
		else if (type%10 == 4)
			System.out.print("boolean");
		else if (type == 5)
			System.out.print("class");
		else if (type == 15)
			System.out.print("void");
		
		if (size != 0)
			System.out.print("\t" + size);
		
		if (visibility == 1)
			System.out.println("\tpublic");
		else
			System.out.println("\tprivate");
		
		
		if (parameter != null) {
			//System.out.print("\t Paramter found: ");
				parameter.printPar();
				System.out.println();
			}
		}
		
		public void update(String n, int t, String v, int vis) {
			name = n;
			type = t;
			value = v;
			visibility = vis;
		}
		
		public void updateValue(String value) {
			this.value = value;
		}
		
		public void updateType(int t) {
			type = t;
		}
		
		public void updateVis(int v) {
			visibility = v;
		}
		
		public void updateSize(int s) {
			size = s;
		}
		
		public String getName() {
			return name;
		}
		
		public void createTable(int t) {
			parameter = new SymbolTable();
			addElement(t);
		}
		
		public void addElement(int t) {
			parameter.add(t);
		}
		
		public void updateLastParameter(String s) {
			parameter.last.name = s;
		}
	}
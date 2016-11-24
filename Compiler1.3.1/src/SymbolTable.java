public class SymbolTable{
	private Node start;
	public Node last;
	
	public final int
		TYPE_INT 			= 1,
		TYPE_CHAR 			= 2,
		TYPE_STRING 		= 3,
		TYPE_BOOLEAN		= 4,
		TYPE_CLASS			= 5,
		TYPE_INT_METH		= 11,
		TYPE_CHAR_METH		= 12,
		TYPE_STRING_METH	= 13,
		TYPE_BOOLEAN_METH	= 14,
		TYPE_VOID_METH		= 15;
	
	public final int
		VISIBILITY_PUBLIC 	= 1,
		VISIBILITY_PRIVATE	= 2;

	public SymbolTable() {
		start = null;
		last = null;
	}
	
	public void add(int t) {
		this.add(null, t, null, 1);
	}
	
	//name, type, value, visibility
	public void add(String n, int t, String value, int vis) {	
		if (empty()) {
			start = new Node(n, t, value, vis);
			last = start;
		}
		
		else {
			if (n != null) {
				Node tmp = find(n);
				
				if (tmp == null) {
					last.next = new Node(n, t, value, vis);
					last = last.next;
				}
				
				else {
					System.out.println("found that bitch!");
					tmp.update(n, t, value, vis);
				}
			}
			else {
				last.next = new Node(n, t, value, vis);
				last = last.next;
			}
		}
	}
	
	public void updateName(String n) {
		last.name = n;
	}
	
	public Node find(String n) {
		Node tmp = start;
		
		while (tmp != null) {
			if (tmp.name.compareTo(n) == 0)
				return tmp;
			
			tmp = tmp.next;
		}
		
		return null;
	}
	
	public void print() {
		int i=0;
		Node tmp = start;
		
		while (tmp != null) {
			System.out.print("#" + i++ + "\t");
			tmp.print();
			tmp = tmp.next;
		}
	}
	
	public void printPar() {
		Node tmp = start;
		System.out.print("-- ");
		
		while (tmp != null) {
			tmp.printPar();
			tmp = tmp.next;
		}
	}
	
	public void print(Node n) {
		n.print();
	}
	
	public boolean empty() {
		return start == null;
	}
	
	
	public class Node{
		String name;
		int type;
		String value;
		int visibility;
		Node next;
		SymbolTable parameter;
		
		public Node(String n, int t, String v, int vis){
			name = n;
			type = t;
			value = v;
			visibility = vis;
			next = null;
			parameter = null;
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
		
		private void printPar() {
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
		
		private void print() {
			System.out.print(name + "\t" + value + "\t");
			if (type > 10)
				System.out.print("m-");
			
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
		
		private void update(String n, int t, String v, int vis) {
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
}
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
		
		public Node(String n, int t, String v, int vis){
			name = n;
			type = t;
			value = v;
			visibility = vis;
			next = null;
		}
		
		private void print() {
			System.out.println(name + " " + value + " " + type + " " + visibility);
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
	}
}
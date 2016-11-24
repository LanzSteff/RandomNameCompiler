public class SymbolTable{
	private Node start;
	public Node last;
	public String name;
	public int size = 0;
	
	public static final int
		TYPE_INT 			= 1,
		TYPE_CHAR 			= 2,
		TYPE_STRING 		= 3,
		TYPE_BOOLEAN		= 4,
		TYPE_CLASS			= 5,
		
		TYPE_INT_METH		= 11,
		TYPE_CHAR_METH		= 12,
		TYPE_STRING_METH	= 13,
		TYPE_BOOLEAN_METH	= 14,
		TYPE_VOID_METH		= 15,
		
		TYPE_INT_ARRAY 		= 21,
		TYPE_CHAR_ARRAY 	= 22,
		TYPE_STRING_ARRAY 	= 23,
		TYPE_BOOLEAN_ARRAY	= 24,
		TYPE_CLASS_ARRAY	= 25;
	
	public final int
		VISIBILITY_PUBLIC 	= 1,
		VISIBILITY_PRIVATE	= 2;

	public SymbolTable() {
		start = null;
		last = null;
	}
	
	public SymbolTable(String n) {
		name = n;
		start = null;
		last = null;
	}
	
	public Node get(int p) {
		Node n = start;
		
		if(p >= size) {
			throw new IllegalStateException("out of bounds");
		}
		for(int i=0; i < p; i++) {
			n = n.next;
		}
		
		return n;
	}
	
	public void add(int t) {
		this.add(null, t, 1);
	}
	
	public void add(SymbolTable s) {
		Node n = s.start;
		
		while(n != null) {
			this.add(n);
			n = n.next;
		}
	}
	
	public void add(Node n) {
		this.add(n.name, n.type, n.visibility);
	}
	
	//name, type, value, visibility
	public void add(String n, int t, int vis) {
		if (empty()) {
			start = new Node(n, t, vis);
			start.offset = 0;
			last = start;
			size++;
		}
		
		else {
			if (n != null) {
				Node tmp = find(n);
				
				if (tmp == null) {
					last.next = new Node(n, t, vis);
					last.next.offset = last.offset + last.size;
				}
				
				else {
					System.out.println("found that bitch!");
					tmp.update(n, t, vis);
				}
			}
			else {
				last.next = new Node(n, t, vis);
				last.next.offset = last.offset + last.size;
				size++;
			}
			
			if (last.onHeap)
				getCorrectOffset();
			
			last = last.next;
		}
	}
	
	public void deleteLastNode() {
		Node tmp = start;
		while (tmp.next != last) {
			tmp = tmp.next;
		}
		
		last = tmp;
		size--;
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
			System.out.print("Nr " + i++ + "\t");
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
	
	private void getCorrectOffset() {
		last.next.offset = 0;
		Node tmp = start;
		
		while (tmp != null) {
			if (!tmp.onHeap)
				last.next.offset += tmp.size;
			
			tmp = tmp.next;
		}
	}
}
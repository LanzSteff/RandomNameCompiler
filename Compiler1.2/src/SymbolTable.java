public class SymbolTable{
	
	public class Node{
		String name;
		int type;
		String value;
		Node next;
		
		public Node(String n, int t, String v){
			name = n;
			type = t;
			value = v;
			next = null;
			
		}
	}
}
public class Main {
	public static void main(String[] args) {
		try {
			Parser p = new Parser("parser.txt");
			p.parse();
			p.st.print();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
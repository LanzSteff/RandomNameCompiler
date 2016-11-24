public class Main {
	public static void main(String[] args) {
		try {
			Parser p = new Parser("test3.txt");
			p.parse();
			p.st.print();
			/*Scanner s = new Scanner();
			s.openFile("test1.txt");
			s.testScanner();*/
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
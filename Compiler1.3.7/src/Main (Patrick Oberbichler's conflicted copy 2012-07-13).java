public class Main {
	public static void main(String[] args) {
		try {
			
			Parser p = new Parser("test3.txt");
			p.parse();
			p.printAll();
			System.out.println();
			p.cg.tm.printMemory();
			p.cg.tm.printInstructions();
			p.cg.tm.printRegisters();
			p.cg.closeFile();
			
			/*
			CodeGenerator cg = new CodeGenerator("blup.txt");
			cg.encode("ADDI", 1, 0, 2);
			cg.encode("SUB", 5, 4, 3);
			cg.closeFile();
			*/
			
			/*
			fib(38);
			TargetMachine t = new TargetMachine();
			t.readFile("fib.txt");
			t.execute();
			t.printInstructions();
			t.printRegisters();
			*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void fib(int n) {
		try {
			CodeGenerator cg = new CodeGenerator("fib.txt");
			cg.encode("ADDI", 1,0,1); 	//0 a=1
			cg.encode("ADDI", 2,0,1); 	//1 b=1
			// c = reg3
			cg.encode("ADDI", 5, 0, 3); //2 i = reg5
			cg.encode("ADDI", 6, 0, n+1);	//3 upper limit
			cg.encode("SUB", 7,6,5);	//4 reg7 = upper limit - i
			cg.encode("BEQ", 7,11);		//5 if
			
			cg.encode("ADD", 3,1,2); //6 c = a+b
			cg.encode("ADD", 1,0,2); //7 a = b
			cg.encode("ADD", 2,0,3); //8 b = c
			cg.encode("ADDI", 5,5,1); //9
			cg.encode("BR", 4); 	//10
			
			cg.encode("ADD", 10,0,2); //11
			System.out.println("Loesung ist in register 10");
			
			cg.closeFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
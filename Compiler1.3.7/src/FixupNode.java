public class FixupNode {
	String opCode;
	int reg1,b,c;
	int pc;
	String name;
	
	public FixupNode(String op, int a, int c, int counter) {
		opCode = op;
		reg1 = a;
		this.c = c;
		pc = counter;
		b = -1;
	}
	
	public FixupNode(String op, int a, int b, int c, int counter) {
		opCode = op;
		reg1=a;
		this.b=b;
		this.c=c;
		pc = counter;
	}
	
	public FixupNode(String op, int c, int counter, String name) {
		this.name = name;
		opCode = op;
		this.c = c;
		pc = counter;
		b = -1; 
	}
	
	public FixupNode(String op, int a, int b, int counter, String name) {
		this.name = name;
		opCode = op;
		this.reg1 = a;
		this.b = b;
		pc = counter;
	}
}
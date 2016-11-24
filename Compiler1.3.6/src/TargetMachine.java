import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class TargetMachine {
	
	public static final int
		// F1 register instructions:
		ADD = 0,
		SUB = 1,
		MUL = 2,
		DIV = 3,
		MOD = 4,
		CMP = 5,
		GT  = 6,
		GEQ = 7,
		LT  = 8,
		LEQ = 9,
		NEQ = 10,
		//CHK = 6,
		AND = 11,
		BIC = 12,
		OR  = 13,
		XOR = 14,
		LSH = 15,
		ASH = 16,
		
		// F2 register instructions:
		ADDI = 20,
		SUBI = 21,
		MULI = 22,
		DIVI = 23,
		MODI = 24,
		CMPI = 25,
		GTI  = 26,
		GEQI = 27,
		LTI  = 28,
		LEQI = 29,
		NEQI = 30,
		//CHKI = 31,
		ANDI = 32,
		BICI = 33,
		ORI  = 34,
		XORI = 35,
		LSHI = 36,
		ASHI = 37,
		
		// F1 load/store instructions:
		LDW = 40,
		LDB = 41,
		POP = 42,
		STW = 43,
		STB = 44,
		PSH = 45,
		
		// F1 control instructions:
		BEQ = 50,
		BNE = 51,
		BLT = 52,
		BLE = 53,
		BGT = 54,
		BGE = 55,
		BSR = 56,
		BR = 57;
	
	// Register
	public int[] reg = null;
	private int[] mem = null;
	
	private int progCounter;
	
	private final int maxReg = 32;
	private final int memSize = 4096;
	private final int varSize = 1024; 
	
	public ArrayList<Integer> instructions;
	public Instruction[] program;
	
	
	public TargetMachine() {
		reg = new int[maxReg];
		mem = new int[memSize];
		
		
		reg[0] = 0;
		reg[maxReg-2] = memSize-1;
		reg[maxReg-3] = memSize-varSize-1;
		
		
		progCounter = 0;
	}
	
	public void readFile(String fileName) throws Exception {
		instructions = new ArrayList<Integer>();
		
		//reading of the program file
		BufferedReader bReader = new BufferedReader(new FileReader(fileName));
		
		String line;
		while((line = bReader.readLine()) != null)
			if (line != "")
				instructions.add(Integer.parseInt(line));
		
		bReader.close();
		decode();
	}
	
	private void decode() throws Exception {
		if(instructions == null || instructions.size() == 0)
			throw new IllegalStateException("\nempty Instructions array -> close file first");
		else {
			program = new Instruction[instructions.size()];
			for(int i = 0; i < instructions.size(); i++) {
				program[i] = new Instruction();
				int instruction = instructions.get(i);
				
				program[i].opCode = (instruction >>> 26) & 63;
				
				program[i].a = (instruction >>> 21) & 31;
				program[i].b = (instruction >>> 16) & 31;
				program[i].c = instruction & 65535; //2^16
				if(program[i].c >= 32768)
					program[i].c -= 65536;
				
				/*
				if (program[i].opCode == 50) {
					int tmp = instruction & 65535;
					System.out.println(program[i].c + " " + tmp + " " + instruction);
				}*/
				
				if (program[i].opCode == BR)
					program[i].c = instruction & 0x2FFFFFF;
				
			}
		}
	}
	
	public void execute() {
		int a = program[progCounter].a;
		int b = program[progCounter].b;
		int c = program[progCounter].c;
		int opCode = program[progCounter].opCode;
		
		if(opCode < 13) {
			c = reg[c];
		}
		
		switch(opCode) {
			case ADD:
			case ADDI:
				reg[a] = reg[b] + c;
				break;
			case SUB:
			case SUBI:
				reg[a] = reg[b] - c;
				break;
			case MUL:
			case MULI:
				reg[a] = reg[b] * c;
				break;
			case DIV:
			case DIVI:
				reg[a] = reg[b] / c;
				break;
			case MOD:
			case MODI:
				reg[a] = reg[b] % c;
				break;
			case CMP:
			case CMPI:
				reg[a] = reg[b] - c;
				reg[a] = reg[a] == 0 ? 1 : 0;
				break;
			case GT:
			case GTI:
				reg[a] = reg[b] - c;
				reg[a] = reg[a] > 0 ? 1 : 0;
				break;
			case GEQ:
			case GEQI:
				reg[a] = reg[b] - c;
				reg[a] = reg[a] >= 0 ? 1 : 0;
				break;
			case LT:
			case LTI:
				reg[a] = reg[b] - c;
				reg[a] = reg[a] < 0 ? 1 : 0;
				break;
			case LEQ:
			case LEQI:
				reg[a] = reg[b] - c;
				reg[a] = reg[a] <= 0 ? 1 : 0;
				break;
			case NEQ:
			case NEQI:
				reg[a] = reg[b] - c;
				reg[a] = reg[a] != 0 ? 1 : 0;
				break;
			case AND:
			case ANDI:
				if (reg[b] == 1 && c == 1)
					reg[a] = 1;
				else
					reg[a] = 0;
			
				break;
			case BIC:
			case BICI:
				if (reg[b] == 1 && c == 0)
					reg[a] = 1;
				else
					reg[a] = 0;
				
				break;
			case OR:
			case ORI:
				if (reg[b] == 1 || c == 1)
					reg[a] = 1;
				else
					reg[a] = 0;
				break;
			case XOR:
			case XORI:
				reg[a] = reg[b]==c ? 1:0;
				break;
			case LSH:
			case LSHI:
				if (c<0)
					reg[a] = reg[b] >>> c*-1;
				else
					reg[a] = reg[b] << c;
				
				break;
			case ASH:
			case ASHI:
				System.out.println("arithmetic shift not yet implemented");
				//TODO: ??
				break;
			case LDW:
				reg[a] = mem[(reg[b] + c)/4];
				break;
			case STW:
				mem[(reg[b]+c)/4] = reg[a];
				break;
			case STB:
				//TODO: ??
				System.out.println("store byte not yet implemented");
				break;
			case LDB:
				//TODO: ??
				System.out.println("load byte not yet implemented");
				break;
			case POP:
				reg[a] = mem[reg[b]/4];
				reg[b] += c;
				break;
			case PSH:
				reg[b] = reg[b]-c;
				mem[reg[b]/4] = reg[a];
				break;
			case BEQ:
				if (reg[a] == 0)
					//progCounter += c;
					progCounter = c; // TODO: ??
				else
					progCounter += 1;
				break;
			case BNE:
				if (reg[a] != 0)
					progCounter += c;
				else
					progCounter += 1;
				break;
			case BLT:
				if (reg[a] < 0)
					progCounter += c;
				else
					progCounter += 1;
				break;
			case BLE:
				if (reg[a] <= 0)
					progCounter += c;
				else
					progCounter += 1;
				break;
			case BGT:
				if (reg[a] > 0)
					progCounter += c;
				else
					progCounter += 1;
				break;
			case BGE:
				if (reg[a] >= 0)
					progCounter += c;
				else
					progCounter += 1;
				break;
			case BSR:
				reg[31] = progCounter+1;
				progCounter += c;
				break;
			case BR:
				//progCounter += c;
				progCounter = c; //TODO: ??
				break;
		}
		
		if (opCode < BEQ)
			progCounter+=1;
		
		if (progCounter < program.length) {
			//System.out.println("blup - " + progCounter);
			execute();
		}
		else
			System.out.println("finished");
	}
	
	public void printInstructions() {
		System.out.println("instructions:");
		for (int i=0; i < program.length; i++)
			program[i].print(i);
	}
	
	public void printRegisters() {
		System.out.println("registers:");
		for(int i=0; i < maxReg; i++)
			System.out.println("\t#" + i + ": " + reg[i]);
	}
	
	public void printMemory() {
		
	}
	
	public class Instruction {
		public int opCode;
		public int a, b, c;
		
		public Instruction() {
			opCode=0;
			a=0; b=0; c=0;
		}
		
		private void print(int i) {
			System.out.println("\t#" + i + " - opCode:" + opCode + " - a:" + a + " - b:" + b + " - c:" + c);
		}
	}
}
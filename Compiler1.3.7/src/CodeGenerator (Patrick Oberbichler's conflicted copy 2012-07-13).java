import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;


public class CodeGenerator {
	//F1: 6op, 5a, 5b, 16c
	//F2: 6op, 5a, 5b, 11unused, 5c
	//F3: 6op, 26 absolut jump
	
	private RandomAccessFile output = null;
	public TargetMachine tm = new TargetMachine();
	
	CodeGenerator(String fName) throws Exception {
		output = new RandomAccessFile(fName, "rw");
	}
	
	CodeGenerator() throws Exception {
		output = new RandomAccessFile("outFile.txt", "rw");
	}
	
	public void closeFile() throws Exception {
		output.close();
	}
	
	public int encode(String op, int reg1, int reg2, int reg3) throws Exception {
		return encode(convertOpcode(op), reg1, reg2, reg3);
	}
	
	public int encode(String op, int reg1, int offset) throws IOException {
		return encode(convertOpcode(op), reg1, offset);
	}
	
	public int encode(String op, int offset) throws IOException {
		return encode(convertOpcode(op), offset);
	}
	
	public int encode(int op, int offset) throws IOException {
		if (op < 0) {
			System.out.println("ERROR - invalid opCode");
			return -1;
		}
		
		int returnValue = 0;
		returnValue += (op << 26);
		returnValue += offset & (int)(Math.pow(2,16)-1); //TODO: kein russisch
//		output.writeInt(returnValue);
		tm.instructions.add(returnValue);
		
		return returnValue;
	}
	
	//TODO: glaub loeschen
	public int encode(int op, int reg1, int offset) throws IOException {
		if (op < 0) {
			System.out.println("ERROR - invalid opCode");
			return -1;
		}
		
		int returnValue = 0;
		returnValue += (op << 26);
		returnValue += (reg1 << 21);
		returnValue += offset & (int)(Math.pow(2,16)-1); //TODO: kein russisch
		tm.instructions.add(returnValue);
//		output.writeInt(returnValue);
		
		return returnValue;
	}
	
	public int encode(int op, int reg1, int reg2, int reg3) throws IOException {
		if (op < 0) {
			System.out.println("ERROR - invalid opCode");
			return -1;
		}

		if (op == 77)
			System.out.println("LOL");
		
		int returnValue = 0;
		returnValue += (op << 26);		
		returnValue += (reg1 << 21);
		returnValue += (reg2 << 16);
		returnValue += reg3;

//		output.writeInt(returnValue);
		tm.instructions.add(returnValue);
		return returnValue;
	}
	
	public int encode(FixupNode node) throws Exception {
		String op = node.opCode;
		int a = node.reg1;
		int c = node.c;
		int b = node.b;
		
		if (b == -1)
			return encode(op, a, c);
		else
			return encode(op,a,b,c);
	}
	
	public int getFreeReg() {
		int fr = tm.getFreeReg();
		tm.incrementReg();
		
		return fr;
	}
	
	public int loadItem(ItemDesc item) throws Exception {
		if (item.type.type != SymbolTable.TYPE_BOOLEAN) {
			if (item.mode == ItemDesc.CONST) {
				int freeReg = tm.getFreeReg();
				encode("ADDI", freeReg, 0,item.value);
				item.reg = freeReg;
				item.mode = ItemDesc.REG;
				tm.incrementReg();
			}
			
			else if (item.mode == ItemDesc.VAR) {
				int freeReg = tm.getFreeReg();
				
				if (item.isGlobal)
					encode("LDW", freeReg, 0, item.offset);
				else 
					encode("LDW", freeReg, 28, item.offset);
				
				item.reg = freeReg;
				item.mode = ItemDesc.REG;
				tm.incrementReg();
			}
		}
		
		else {
			//TODO bool
		}
		
		return -1;
	}
	
	public void storeGlobal(int value, int offset) throws Exception {
		int freeReg = getFreeReg();
		encode("ADDI", freeReg, 0, value);
		encode("STW", freeReg, 28, offset);
	}
	
	private int convertOpcode(String op) {
		//F2 register
		if (op.compareTo("ADD") == 0) return 0;
		else if (op.compareTo("SUB") == 0) return 1;
		else if (op.compareTo("MUL") == 0) return 2;
		else if (op.compareTo("DIV") == 0) return 3;
		else if (op.compareTo("MOD") == 0) return 4;
		else if (op.compareTo("CMP") == 0) return 5;
		else if (op.compareTo("GT") == 0)  return 6;
		else if (op.compareTo("GEQ") == 0) return 7;
		else if (op.compareTo("LT") == 0)  return 8;
		else if (op.compareTo("LEQ") == 0) return 9;
		else if (op.compareTo("NEQ") == 0) return 10;
		else if (op.compareTo("AND") == 0) return 11;
		else if (op.compareTo("BIC") == 0) return 12;
		else if (op.compareTo("OR") == 0)  return 13;
		else if (op.compareTo("XOR") == 0) return 14;
		else if (op.compareTo("LSH") == 0) return 15;
		else if (op.compareTo("ASH") == 0) return 16;
		
		//F1 register
		else if (op.compareTo("ADDI") == 0) return 20;
		else if (op.compareTo("SUBI") == 0) return 21;
		else if (op.compareTo("MULI") == 0) return 22;
		else if (op.compareTo("DIVI") == 0) return 23;
		else if (op.compareTo("MODI") == 0) return 24;
		else if (op.compareTo("CMPI") == 0) return 25;
		else if (op.compareTo("GTI") == 0)  return 26;
		else if (op.compareTo("GEQI") == 0) return 27;
		else if (op.compareTo("LTI") == 0)  return 28;
		else if (op.compareTo("LEQI") == 0) return 29;
		else if (op.compareTo("NEQI") == 0) return 30;
//		else if (op.compareTo("CHKI") == 0) return 31;
		else if (op.compareTo("ANDI") == 0) return 32;
		else if (op.compareTo("BICI") == 0) return 33;
		else if (op.compareTo("ORI") == 0)  return 34;
		else if (op.compareTo("XORI") == 0) return 35;
		else if (op.compareTo("LSHI") == 0) return 36;
		else if (op.compareTo("ASHI") == 0) return 37;
		
		//F1 load/store
		else if (op.compareTo("LDW") == 0) return 40;
		else if (op.compareTo("LDB") == 0) return 41;
		else if (op.compareTo("POP") == 0) return 42;
		else if (op.compareTo("STW") == 0) return 43;
		else if (op.compareTo("PRT") == 0) return 44;
		else if (op.compareTo("PSH") == 0) return 45;
		
		//F1 control
		else if (op.compareTo("BEQ") == 0) return 50;
		else if (op.compareTo("BNE") == 0) return 51;
		else if (op.compareTo("BLT") == 0) return 52;
		else if (op.compareTo("BLE") == 0) return 53;
		else if (op.compareTo("BGT") == 0) return 54;
		else if (op.compareTo("BGE") == 0) return 55;
		else if (op.compareTo("BGR") == 0) return 56;
		
		//absolute jump
		else if (op.compareTo("BR") == 0) return 57;
		else if (op.compareTo("BRR") == 0) return 58;
		
		else if (op.compareTo("EXT") == 0) return 60;
		
		return -1;
		}
	
	
}

public class ItemDesc {
	public static final int CONST=1, VAR=2, REG=3, REF=4;
	
	public int mode;
	public int reg;
	public int offset;
	public int value;
	public TypeDesc type;
	public boolean isGlobal = false;
}
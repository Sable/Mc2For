package natlab.backends.Fortran.codegen_readable;

import java.util.HashMap;

public class FortranMapping {

	private static HashMap<String, String> FortranTypeMap = 
			new HashMap<String, String>();
	private static HashMap<String, String> FortranBinOperatorMap = 
			new HashMap<String, String>();
	private static HashMap<String, String> FortranDirectBuiltinMap = 
			new HashMap<String, String>();
	
	public FortranMapping() {
		makeFortranTypeMap();
		makeFortranBinaryOperatorMap();
		makeFortranDirectBuiltinMap();
	}
	
	private void makeFortranTypeMap() {
		/*
		 * TODO Fortran has kind keyword.
		 */
		FortranTypeMap.put("double", "DOUBLE PRECISION");
		FortranTypeMap.put("single", "REAL");
		FortranTypeMap.put("int8", "INTEGER(KIND=1)");
		FortranTypeMap.put("int16", "INTEGER(KIND=2)");
		FortranTypeMap.put("int32", "INTEGER(KIND=4)");
		FortranTypeMap.put("int64", "INTEGER(KIND=8)");
		FortranTypeMap.put("uint8", "INTEGER(KIND=8)");
		FortranTypeMap.put("uint16", "INTEGER");
		FortranTypeMap.put("uint32", "INTEGER");
		FortranTypeMap.put("uint64", "INTEGER");
		FortranTypeMap.put("char", "CHARACTER");
		FortranTypeMap.put("logical", "LOGICAL");
		FortranTypeMap.put("complex", "COMPLEX");
	}
	
	public String getFortranTypeMapping(String mclassasKey) {
		return FortranTypeMap.get(mclassasKey);
	}
	
	private void makeFortranBinaryOperatorMap() {
		// arithmetic operators
		FortranBinOperatorMap.put("plus", "+");
		FortranBinOperatorMap.put("minus", "-");
		FortranBinOperatorMap.put("times", "*");
		FortranBinOperatorMap.put("rdivide", "/");
		FortranBinOperatorMap.put("power", "**");
		// relational operators
		FortranBinOperatorMap.put("lt", ".LT.");
		FortranBinOperatorMap.put("le", ".LE.");
		FortranBinOperatorMap.put("gt", ".GT.");
		FortranBinOperatorMap.put("ge", ".GE.");
		FortranBinOperatorMap.put("eq", ".EQ.");
		FortranBinOperatorMap.put("ne", ".NE.");
		// logical operators
		FortranBinOperatorMap.put("and", ".AND.");
		FortranBinOperatorMap.put("or", ".OR.");
		FortranBinOperatorMap.put("not", ".NOT.");
		FortranBinOperatorMap.put("xor", ".XOR.");
	}
	
	private void makeFortranDirectBuiltinMap() {
		// arithmetic operators
		FortranDirectBuiltinMap.put("mtimes", "MATMUL");
		FortranDirectBuiltinMap.put("transpose", "TRANSPOSE");
		// commonly used mathematical built-ins
		FortranDirectBuiltinMap.put("sum", "SUM");
		FortranDirectBuiltinMap.put("ceil", "CEILING");
		FortranDirectBuiltinMap.put("floor", "FLOOR");
		FortranDirectBuiltinMap.put("mod", "MODULO");
		FortranDirectBuiltinMap.put("rem", "MOD");
		FortranDirectBuiltinMap.put("round", "NINT");
		FortranDirectBuiltinMap.put("sin", "SIN");
		FortranDirectBuiltinMap.put("asin", "ASIN");
		FortranDirectBuiltinMap.put("sinh", "SINH");
		FortranDirectBuiltinMap.put("cos", "COS");
		FortranDirectBuiltinMap.put("acos", "ACOS");
		FortranDirectBuiltinMap.put("cosh", "COSH");
		FortranDirectBuiltinMap.put("tan", "TAN");
		FortranDirectBuiltinMap.put("atan", "ATAN");
		FortranDirectBuiltinMap.put("tanh", "TANH");
		FortranDirectBuiltinMap.put("exp", "EXP");
		FortranDirectBuiltinMap.put("log", "LOG");
		FortranDirectBuiltinMap.put("log10", "LOG10");
		FortranDirectBuiltinMap.put("sqrt", "SQRT");
		FortranDirectBuiltinMap.put("abs", "ABS");
		FortranDirectBuiltinMap.put("conj", "CONJG");
		FortranDirectBuiltinMap.put("min", "MIN");
		FortranDirectBuiltinMap.put("max", "MAX");
		FortranDirectBuiltinMap.put("numel", "SIZE");
		FortranDirectBuiltinMap.put("size", "SHAPE");
		// logical operators
		FortranDirectBuiltinMap.put("any", "ANY");
		FortranDirectBuiltinMap.put("all", "ALL");
		FortranDirectBuiltinMap.put("bitand", "IAND");
		FortranDirectBuiltinMap.put("bitor", "IOR");
		FortranDirectBuiltinMap.put("bitcmp", "NOT");
		FortranDirectBuiltinMap.put("bitxor", "IEOR");
	}
	
	// TODO add no-directly-mapping built-in functions.
	
	public Boolean isFortranBinOperator(String operator) {
		if (FortranBinOperatorMap.containsKey(operator)) return true;
		else return false;
	}
	
	public String getFortranBinOpMapping(String operator) {
		return FortranBinOperatorMap.get(operator);
	}
	
	public Boolean isFortranDirectBuiltin(String builtinName) {
		if (FortranDirectBuiltinMap.containsKey(builtinName)) return true;
		else return false;
	}
	
	public String getFortranDirectBuiltinMapping (String builtinName) {
		 return FortranDirectBuiltinMap.get(builtinName);		
	}
}

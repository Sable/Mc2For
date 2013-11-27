package natlab.backends.Fortran.codegen_readable;

import java.util.*;

public class FortranMapping {

	private static Map<String, String> FortranTypeMap = 
			new HashMap<String, String>();
	private static Map<String, String> FortranBinOperatorMap = 
			new HashMap<String, String>();
	private static Map<String, String> FortranUnOperatorMap = 
			new HashMap<String, String>();
	private static Map<String, String> FortranDirectBuiltinMap = 
			new HashMap<String, String>();
	private static Set<String> FortranEasilyTransformedSet = 
			new HashSet<String>();
	
	public FortranMapping() {
		makeFortranTypeMap();
		makeFortranBinaryOperatorMap();
		makeFortranUnaryOperatorMap();
		makeFortranDirectBuiltinMap();
		makeFortranEasilyTransformedSet();
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
		FortranTypeMap.put("uint8", "INTEGER(KIND=2)");
		FortranTypeMap.put("uint16", "INTEGER(KIND=4)");
		FortranTypeMap.put("uint32", "INTEGER(KIND=8)");
		FortranTypeMap.put("uint64", "INTEGER(KIND=16)");
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
	
	public boolean isFortranBinOperator(String operator) {
		if (FortranBinOperatorMap.containsKey(operator)) return true;
		else return false;
	}
	
	public String getFortranBinOpMapping(String operator) {
		return FortranBinOperatorMap.get(operator);
	}
	
	private void makeFortranUnaryOperatorMap() {
		// arithmetic operators
		FortranUnOperatorMap.put("uplus", "+");
		FortranUnOperatorMap.put("uminus", "-");
	}
	
	public boolean isFortranUnOperator(String expType) {
		if (FortranUnOperatorMap.containsKey(expType)) return true;
		else return false;
	}
	
	public String getFortranUnOpMapping(String Operator) {
		return FortranUnOperatorMap.get(Operator);
	}
	
	private void makeFortranDirectBuiltinMap() {
		// arithmetic operators
		/* 
		 * FortranDirectBuiltinMap.put("mtimes", "MATMUL");
		 * comment off the builtin function mtimes, 
		 * since mtimes is highly overloaded in 
		 * benchmarks, and the easiest way I think 
		 * is to use a fortran interface to map it.
		 */
		FortranDirectBuiltinMap.put("transpose", "TRANSPOSE");
		// commonly used mathematical built-ins
		FortranDirectBuiltinMap.put("sum", "SUM");
		FortranDirectBuiltinMap.put("ceil", "CEILING");
		FortranDirectBuiltinMap.put("floor", "FLOOR");
		FortranDirectBuiltinMap.put("mod", "MODULO");
		FortranDirectBuiltinMap.put("rem", "MOD");
		FortranDirectBuiltinMap.put("round", "NINT");
		FortranDirectBuiltinMap.put("fix", "INT");
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
		FortranDirectBuiltinMap.put("length", "SIZE");
		// logical operators
		FortranDirectBuiltinMap.put("any", "ANY");
		FortranDirectBuiltinMap.put("all", "ALL");
		FortranDirectBuiltinMap.put("bitand", "IAND");
		FortranDirectBuiltinMap.put("bitor", "IOR");
		FortranDirectBuiltinMap.put("bitcmp", "NOT");
		FortranDirectBuiltinMap.put("bitxor", "IEOR");
	}
	
	public boolean isFortranDirectBuiltin(String builtinName) {
		if (FortranDirectBuiltinMap.containsKey(builtinName)) return true;
		else return false;
	}
	
	public String getFortranDirectBuiltinMapping (String builtinName) {
		 return FortranDirectBuiltinMap.get(builtinName);		
	}
	
	private void makeFortranEasilyTransformedSet() {
		FortranEasilyTransformedSet.add("colon");
		FortranEasilyTransformedSet.add("ldivide");
		FortranEasilyTransformedSet.add("randn");
		FortranEasilyTransformedSet.add("horzcat");
		FortranEasilyTransformedSet.add("vertcat");
	}
	
	public boolean isFortranEasilyTransformed(String builtinName) {
		if (FortranEasilyTransformedSet.contains(builtinName)) return true;
		else return false;
	}
	
	// TODO add no-directly-mapping built-in functions.
}

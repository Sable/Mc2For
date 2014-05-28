package natlab.backends.Fortran.codegen_simplified;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FortranMapping {
	private static HashMap<String, String> FortranTypeMap = 
			new HashMap<String, String>();
	private static HashMap<String, String> FortranBinOperatorMap = 
			new HashMap<String, String>();
	private static HashMap<String, String> FortranUnOperatorMap = 
			new HashMap<String, String>();
	private static HashMap<String, String> FortranDirectBuiltinMap = 
			new HashMap<String, String>();
	private static Set<String> FortranNoDirectBuiltinSet = 
			new HashSet<String>();
	private static HashMap<String, String> FortranBuiltinConstMap = 
			new HashMap<String, String>();
	private static HashMap<String, String> FortranIOOperationMap = 
			new HashMap<String, String>();
	
	public FortranMapping() {
		makeFortranTypeMap();
		makeFortranBinaryOperatorMap();
		makeFortranUnaryOperatorMap();
		makeFortranDirectBuiltinMap();
		makeFortranNoDirectBuiltinSet();
		makeFortranBuiltinConstMap();
		makeFortranIOOperationMap();
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
	
	private void makeFortranUnaryOperatorMap() {
		// arithmetic operators
		FortranUnOperatorMap.put("uplus", "+");
		FortranUnOperatorMap.put("uminus", "-");
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
		FortranDirectBuiltinMap.put("length", "SIZE");
		// logical operators
		FortranDirectBuiltinMap.put("any", "ANY");
		FortranDirectBuiltinMap.put("all", "ALL");
		FortranDirectBuiltinMap.put("bitand", "IAND");
		FortranDirectBuiltinMap.put("bitor", "IOR");
		FortranDirectBuiltinMap.put("bitcmp", "NOT");
		FortranDirectBuiltinMap.put("bitxor", "IEOR");
	}
	
	private void makeFortranNoDirectBuiltinSet() {
		/*
		 * for those numerous matlab built-in functions which 
		 * don't have directly mapping intrinsic fortran 
		 * functions, we leave the same "hole" in the generated 
		 * fortran code. By saying the same "hole", I mean 
		 * the same function signature in C++ jargon. We need 
		 * to build a separate Mc2For lib which is full of 
		 * user-defined functions in fortran, and those 
		 * functions have the same function signatures with 
		 * the built-in function calls in input matlab code.
		 * 
		 * this solution make the code generation framework 
		 * concise and not need to be updated when there comes 
		 * a new matlab built-in function. the only thing we 
		 * need to do is making a user-defined function by 
		 * ourselves or "find" one, and then update the Mc2For 
		 * lib. TODO shipped with Mc2For, we should at least 
		 * provide a significant number of user-defined fortran 
		 * functions to "fill" the "hole" of those commonly 
		 * used matlab built-in functions, like ones, zeros...
		 * 
		 * There are a lot of tutorials online about how to 
		 * make user-defined fortran lib and update lib.
		 * 
		 * actually, we can still make some function mappings 
		 * inlined, like .\ (left division), which can be 
		 * replaced by swapping operands and then use right 
		 * division, and : (colon operator), which can be 
		 * replaced by using implied DO loop in an array
		 * constructor. TODO this tmr.
		 */
		FortranNoDirectBuiltinSet.add("horzcat");
		FortranNoDirectBuiltinSet.add("vertcat");
		FortranNoDirectBuiltinSet.add("ldivide");
		FortranNoDirectBuiltinSet.add("colon");
		FortranNoDirectBuiltinSet.add("cellhorzcat");
	}
	
	private void makeFortranBuiltinConstMap() {
		FortranBuiltinConstMap.put("pi", "Math.PI");
		FortranBuiltinConstMap.put("true", ".TRUE.");
		FortranBuiltinConstMap.put("false", ".FALSE.");
	}
	
	// this should be deprecated, and goes to no direct built-in set.
	private void makeFortranIOOperationMap() {
		FortranIOOperationMap.put("disp", "PRINT *, ");	
	}
	
	public String getFortranTypeMapping(String mclassasKey) {
		return FortranTypeMap.get(mclassasKey);
	}
	
	public Boolean isFortranBinOperator(String expType) {
		if (FortranBinOperatorMap.containsKey(expType)) return true;
		else return false;
	}
	
	public String getFortranBinOpMapping(String Operator) {
		return FortranBinOperatorMap.get(Operator);
	}
	
	public Boolean isFortranUnOperator(String expType) {
		if (FortranUnOperatorMap.containsKey(expType)) return true;
		else return false;
	}
	
	public String getFortranUnOpMapping(String Operator) {
		return FortranUnOperatorMap.get(Operator);
	}
	
	public Boolean isFortranDirectBuiltin(String expType) {
		if (FortranDirectBuiltinMap.containsKey(expType)) return true;
		else return false;
	}
	
	public String getFortranDirectBuiltinMapping(String BuiltinName) {
		 return FortranDirectBuiltinMap.get(BuiltinName);		
	}
	
	public Boolean isFortranNoDirectBuiltin(String BuiltinName) {
		return FortranNoDirectBuiltinSet.contains(BuiltinName);
	}
	
	public Boolean isBuiltinConst(String expType) {
		if (FortranBuiltinConstMap.containsKey(expType)) return true;
		else return false;
	}
	
	public String getFortranBuiltinConstMapping(String BuiltinName) {
		 return FortranBuiltinConstMap.get(BuiltinName);		
	}
	
	public Boolean isFortranIOOperation(String expType) {
		if (FortranIOOperationMap.containsKey(expType))	return true;
		else return false;
	}
	
	public String getFortranIOOperationMapping(String Operator) {
		return FortranIOOperationMap.get(Operator);
	}
}


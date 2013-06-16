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
		FortranTypeMap.put("char", "CHARACTER");
		FortranTypeMap.put("logical", "LOGICAL");
		FortranTypeMap.put("complex", "COMPLEX");
		FortranTypeMap.put("single", "REAL");
		FortranTypeMap.put("double", "DOUBLE PRECISION");
		FortranTypeMap.put("int8", "INTEGER(Kind=1)");
		FortranTypeMap.put("int16", "INTEGER(Kind=2)");
		FortranTypeMap.put("int32", "INTEGER(Kind=4)");
		FortranTypeMap.put("int64", "INTEGER(Kind=8)");
		FortranTypeMap.put("uint8", "INTEGER");
		FortranTypeMap.put("uint16", "INTEGER");
		FortranTypeMap.put("uint32", "INTEGER");
		FortranTypeMap.put("uint64", "INTEGER");
	}
	
	public String getFortranTypeMapping(String mclassasKey) {
		return FortranTypeMap.get(mclassasKey);
	}
	
	private void makeFortranBinaryOperatorMap() {
		FortranBinOperatorMap.put("plus", "+");
		FortranBinOperatorMap.put("minus", "-");
		FortranBinOperatorMap.put("mtimes", "*");
		FortranBinOperatorMap.put("mrdivide", "/");
		FortranBinOperatorMap.put("mldivide", "\\");
		FortranBinOperatorMap.put("mpower", "**");
		FortranBinOperatorMap.put("times", "*");
		FortranBinOperatorMap.put("rdivide", "/");
		FortranBinOperatorMap.put("ldivide", "\\");
		FortranBinOperatorMap.put("power", "**");
		FortranBinOperatorMap.put("and", ".AND.");
		FortranBinOperatorMap.put("or", ".OR.");
		FortranBinOperatorMap.put("lt", ".LT.");
		FortranBinOperatorMap.put("gt", ".GT.");
		FortranBinOperatorMap.put("le", ".LE.");
		FortranBinOperatorMap.put("ge", ".GE.");
		FortranBinOperatorMap.put("eq", ".EQ.");
		FortranBinOperatorMap.put("ne", ".NE.");
		FortranBinOperatorMap.put("not", ".NOT.");
	}
	
	private void makeFortranDirectBuiltinMap() {
		/* 
		 * TODO create a categorical map here 
		 */
		FortranDirectBuiltinMap.put("sqrt", "SQRT");	
		FortranDirectBuiltinMap.put("sin", "SIN");	
		FortranDirectBuiltinMap.put("cos", "COS");
		FortranDirectBuiltinMap.put("sum", "SUM");
		FortranDirectBuiltinMap.put("size", "SIZE");
		FortranDirectBuiltinMap.put("length", "SIZE");
		FortranDirectBuiltinMap.put("exp", "EXP");
		FortranDirectBuiltinMap.put("transpose", "TRANSPOSE");
		FortranDirectBuiltinMap.put("ceil", "CEILING");
		FortranDirectBuiltinMap.put("abs", "ABS");
		FortranDirectBuiltinMap.put("round", "INT");
	}
	
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

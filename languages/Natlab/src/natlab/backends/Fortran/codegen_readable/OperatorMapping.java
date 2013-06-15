package natlab.backends.Fortran.codegen_readable;

import java.util.HashMap;

public class OperatorMapping {

	private static HashMap<String, String> FortranBinOperatorMap = 
			new HashMap<String, String>();
	private static HashMap<String, String> FortranDirectBuiltinMap = 
			new HashMap<String, String>();
	
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
	
	public OperatorMapping() {
		makeFortranBinaryOperatorMap();
		makeFortranDirectBuiltinMap();
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

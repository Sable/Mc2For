package natlab.backends.Fortran.codegen;

import java.util.*;

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
		FortranTypeMap.put("char", "CHAR");
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
	
	private void makeFortranUnaryOperatorMap() {
		FortranUnOperatorMap.put("uminus", "-");
		FortranUnOperatorMap.put("uplus", "+");
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
		FortranDirectBuiltinMap.put("exp", "EXP");
		FortranDirectBuiltinMap.put("transpose", "TRANSPOSE");
		FortranDirectBuiltinMap.put("ceil", "CEILING");
		FortranDirectBuiltinMap.put("abs", "ABS");
		FortranDirectBuiltinMap.put("round", "INT");
	}
	
	private void makeFortranNoDirectBuiltinSet() {
		FortranNoDirectBuiltinSet.add("horzcat");
		FortranNoDirectBuiltinSet.add("vertcat");
		FortranNoDirectBuiltinSet.add("ones");
		FortranNoDirectBuiltinSet.add("zeros");
		FortranNoDirectBuiltinSet.add("colon");
		FortranNoDirectBuiltinSet.add("randperm");
		// FortranNoDirectBuiltinSet.add("rand");
	}
	
	private void makeFortranBuiltinConstMap() {
		/* 
		 * TODO create a categorical map here 
		 */
		FortranBuiltinConstMap.put("pi", "Math.PI");
		FortranBuiltinConstMap.put("true", ".TRUE.");
		FortranBuiltinConstMap.put("false", ".FALSE.");
	}
	
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
	
	public String getFortranDirectBuiltinMapping (String BuiltinName) {
		 return FortranDirectBuiltinMap.get(BuiltinName);		
	}
	
	public Boolean isFortranNoDirectBuiltin(String BuiltinName) {
		return FortranNoDirectBuiltinSet.contains(BuiltinName);
	}
	
	public Boolean isBuiltinConst(String expType) {
		if (FortranBuiltinConstMap.containsKey(expType)) return true;
		else return false;
	}
	
	public String getFortranBuiltinConstMapping (String BuiltinName) {
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


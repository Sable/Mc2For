package natlab.backends.Fortran.codegen;

import java.util.*;

public class FortranMapping {
	private static HashMap<String, String> FortranTypeMap = new HashMap<String, String>();
	private static HashMap<String, String> FortranBinOperatorMap = new HashMap<String, String>();
	private static HashMap<String, String> FortranUnOperatorMap = new HashMap<String, String>();
	private static HashMap<String, String> FortranDirectBuiltinMap = new HashMap<String, String>();
	private static Set<String> FortranNoDirectBuiltinSet = new HashSet<String>();
	private static HashMap<String, String> FortranBuiltinConstMap = new HashMap<String, String>();
	private static HashMap<String, String> FortranIOOperationMap = new HashMap<String, String>();
	
	public FortranMapping(){
		makeFortranTypeMap();
		makeFortranBinOperatorMap();
		makeFortranUnOperatorMap();
		makeFortranDirectBuiltinMap();
		makeFortranNoDirectBuiltinSet();
		makeFortranBuiltinConstMap();
		makeFortranIOOperationMap();
	}
	
	private void makeFortranTypeMap()
	{
		FortranTypeMap.put("char", "char");
		FortranTypeMap.put("double", "real");
		FortranTypeMap.put("int8", "Integer");
		FortranTypeMap.put("logical", "logical");
		FortranTypeMap.put("COMPLEX", "complex");
		/*FortranTypeMap.put("single", "Float");
		FortranTypeMap.put("int8", "Byte");
		FortranTypeMap.put("int16", "Short");
		FortranTypeMap.put("int32", "Int");
		FortranTypeMap.put("int64", "Long");
		FortranTypeMap.put("uint8", "UByte");
		FortranTypeMap.put("uint16", "UShort");
		FortranTypeMap.put("uint32", "UInt");
		FortranTypeMap.put("uint64", "ULong");*/
		

	}
	
	private void makeFortranBinOperatorMap(){
		FortranBinOperatorMap.put("plus", "+");
		FortranBinOperatorMap.put("minus", "-");
		FortranBinOperatorMap.put("mtimes", "*");
		FortranBinOperatorMap.put("mrdivide", "/");
		FortranBinOperatorMap.put("mldivide", "\\");
		FortranBinOperatorMap.put("mpower", "^");
		FortranBinOperatorMap.put("times", "*");
		FortranBinOperatorMap.put("rdivide", "/");
		FortranBinOperatorMap.put("ldivide", "\\");
		FortranBinOperatorMap.put("power", "**");
		FortranBinOperatorMap.put("and", ".and.");
		FortranBinOperatorMap.put("or", ".or.");
		FortranBinOperatorMap.put("lt", ".lt.");
		FortranBinOperatorMap.put("gt", ".gt.");
		FortranBinOperatorMap.put("le", ".le.");
		FortranBinOperatorMap.put("ge", ".ge.");
		FortranBinOperatorMap.put("eq", ".eq.");
		FortranBinOperatorMap.put("ne", ".ne.");
		FortranBinOperatorMap.put("not", "~");
		
	}
	
	private void makeFortranUnOperatorMap(){
		FortranUnOperatorMap.put("uminus", "-");
		FortranUnOperatorMap.put("uplus", "+");
	}
	
	private void makeFortranDirectBuiltinMap(){
		//TODO create a categorical map here 
		FortranDirectBuiltinMap.put("sqrt", "sqrt");	
		FortranDirectBuiltinMap.put("sin", "sin");	
		FortranDirectBuiltinMap.put("cos", "cos");
		FortranDirectBuiltinMap.put("sum", "sum");
		FortranDirectBuiltinMap.put("size", "size");
		FortranDirectBuiltinMap.put("exp", "exp");
		FortranDirectBuiltinMap.put("transpose", "transpose");
	}
	
	private void makeFortranNoDirectBuiltinSet(){
		FortranNoDirectBuiltinSet.add("horzcat");
		FortranNoDirectBuiltinSet.add("vertcat");
		FortranNoDirectBuiltinSet.add("ones");
		FortranNoDirectBuiltinSet.add("zeros");
		FortranNoDirectBuiltinSet.add("colon");
		FortranNoDirectBuiltinSet.add("randperm");
	}
	
	private void makeFortranBuiltinConstMap(){
		//TODO create a categorical map here 
		FortranBuiltinConstMap.put("pi", "Math.PI");	
	}
	
	private void makeFortranIOOperationMap(){
		FortranIOOperationMap.put("disp", "print *, ");	
	}
	
	public String getFortranTypeMapping(String mclassasKey){
		return FortranTypeMap.get(mclassasKey);
	}
	
	public Boolean isFortranBinOperator(String expType){
		if (true == FortranBinOperatorMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public String getFortranBinOpMapping(String Operator){
		return FortranBinOperatorMap.get(Operator);
	}
	
	
	
	public Boolean isFortranUnOperator(String expType){
		if (true == FortranUnOperatorMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public String getFortranUnOpMapping(String Operator){
		return FortranUnOperatorMap.get(Operator);
	}
	
	public Boolean isFortranDirectBuiltin(String expType){
		if (true == FortranDirectBuiltinMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public String getFortranDirectBuiltinMapping (String BuiltinName){
		
		 return FortranDirectBuiltinMap.get(BuiltinName);
		
	}
	
	public Boolean isFortranNoDirectBuiltin(String BuiltinName){
		return FortranNoDirectBuiltinSet.contains(BuiltinName);
	}
	
	public Boolean isBuiltinConst(String expType){
		if (true == FortranBuiltinConstMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public String getFortranBuiltinConstMapping (String BuiltinName){
		
		 return FortranBuiltinConstMap.get(BuiltinName);
		
	}
	
	public Boolean isFortranIOOperation(String expType){
		if (true == FortranIOOperationMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public String getFortranIOOperationMapping(String Operator){
		return FortranIOOperationMap.get(Operator);
	}
}


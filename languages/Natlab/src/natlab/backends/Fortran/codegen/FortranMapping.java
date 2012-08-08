package natlab.backends.Fortran.codegen;

import java.util.HashMap;

public class FortranMapping {
	
	private static HashMap<String, String> FortranTypeMap = new HashMap();
	private static HashMap<String, String> FortranBinOperatorMap = new HashMap();
	private static HashMap<String, String> FortranUnOperatorMap = new HashMap();
	private static HashMap<String, String> FortranDirectBuiltinMap = new HashMap();
	private static HashMap<String, String> FortranBuiltinConstMap = new HashMap();
	private static HashMap<String, String> FortranMethodMap = new HashMap();
	private static HashMap<String, String> FortranIOOperationMap = new HashMap();
	
	
	public FortranMapping(){
		makeFortranTypeMap();
		makeFortranBinOperatorMap();
		makeFortranUnOperatorMap();
		makeFortranDirectBuiltinMap();
		makeFortranBuiltinConstMap();
		makeFortranMethodMap();
		makeFortranIOOperationMap();
	}
	
	private void makeFortranTypeMap()
	{
		FortranTypeMap.put("char", "char");
		FortranTypeMap.put("double", "real");
		FortranTypeMap.put("int8", "Integer");
		/*FortranTypeMap.put("single", "Float");
		FortranTypeMap.put("int8", "Byte");
		FortranTypeMap.put("int16", "Short");
		FortranTypeMap.put("int32", "Int");
		FortranTypeMap.put("int64", "Long");
		FortranTypeMap.put("uint8", "UByte");
		FortranTypeMap.put("uint16", "UShort");
		FortranTypeMap.put("uint32", "UInt");
		FortranTypeMap.put("uint64", "ULong");*/
		FortranTypeMap.put("logical", "logical");
		FortranTypeMap.put("COMPLEX", "complex");
		

	}
	
	private void makeFortranBinOperatorMap(){
		FortranBinOperatorMap.put("plus", "+");
		FortranBinOperatorMap.put("minus", "-");
		FortranBinOperatorMap.put("mtimes", "*");
		FortranBinOperatorMap.put("mrdivide", "/");
		FortranBinOperatorMap.put("mldivide", "\\");//may be as a method 
		FortranBinOperatorMap.put("mpower", "^");
		FortranBinOperatorMap.put("times", ".*");//may be as a method 
		FortranBinOperatorMap.put("rdivide", "./");//may be as a method 
		FortranBinOperatorMap.put("ldivide", ".\\");//may be as a method 
		FortranBinOperatorMap.put("power", ".^");//may be as a method 
		FortranBinOperatorMap.put("and", "&");
		FortranBinOperatorMap.put("or", "|");
		FortranBinOperatorMap.put("lt", "<");
		FortranBinOperatorMap.put("gt", ".gt.");
		FortranBinOperatorMap.put("le", "<=");
		FortranBinOperatorMap.put("ge", ">=");
		FortranBinOperatorMap.put("eq", "==");
		FortranBinOperatorMap.put("ne", "!=");
		FortranBinOperatorMap.put("transpose", ".'");//may be as a method 
		FortranBinOperatorMap.put("ctranspose", "'");//may be as a method 
		FortranBinOperatorMap.put("not", "~");
		FortranBinOperatorMap.put("colon", ":");//may be as a method 
		
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
	 
	}
	
	private void makeFortranBuiltinConstMap(){
		//TODO create a categorical map here 
		 
		  FortranBuiltinConstMap.put("pi", "Math.PI");	
		}
	
	private void makeFortranMethodMap(){
		//TODO
	}
	
	private void makeFortranIOOperationMap(){
		  FortranIOOperationMap.put("disp", "print *, ");	
	}
	
	public String getFortranTypeMapping(String mclassasKey){
		return FortranTypeMap.get(mclassasKey);
	}
	
	public Boolean isBinOperator(String expType){
		if (true == FortranBinOperatorMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public String getFortranBinOpMapping(String Operator){
		return FortranBinOperatorMap.get(Operator);
	}
	
	
	
	public Boolean isUnOperator(String expType){
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
	
	public Boolean isBuiltinConst(String expType){
		if (true == FortranBuiltinConstMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public String getFortranBuiltinConstMapping (String BuiltinName){
		
		 return FortranBuiltinConstMap.get(BuiltinName);
		
	}
	
	public Boolean isMethod(String expType){
		if (true == FortranMethodMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public static String getFortranMethodMapping(String MethodName){
		return FortranMethodMap.get(MethodName);
	}
	
	public Boolean isIOOperation(String expType){
		if (true == FortranIOOperationMap.containsKey(expType))
			return true;
		else
			return false;
	}
	
	public String getFortranIOOperationMapping(String Operator){
		return FortranIOOperationMap.get(Operator);
	}
}


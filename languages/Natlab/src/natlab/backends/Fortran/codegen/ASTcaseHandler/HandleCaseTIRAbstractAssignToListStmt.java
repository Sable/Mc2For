package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.constant.Constant;

public class HandleCaseTIRAbstractAssignToListStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRAbstractAssignToListStmt(){
		
	}
	/**
	 * AbstractAssignToListStmt: Statement ::= [RuntimeCheck] Variable* Expression;
	 * for each statement, currently, we need to insert two check:
	 * 1. for rhs, constant variable replacement check;
	 * 2. for lhs, do we need to inline allocate code check.
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
		if (Debug) System.out.println("in an abstractAssignToList  statement");
		
		AbstractAssignToListStmt stmt = new AbstractAssignToListStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		Expression exp = makeExpression(fcg, node);
		stmt.setExpression(exp);
		return stmt;
	}
	
	public static Expression makeExpression(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
		/** 
		 * Change for built-ins with n args.
		 * Currently it handles general case built-ins with one or two args only.
		 */
		int RHSCaseNumber;
		String RHSFortranOperator;
		String Operand1, Operand2, prefix="";
		String ArgsListasString;
		RHSCaseNumber = getRHSCaseNumber(fcg, node);
		RHSFortranOperator = getRHSMappingFortranOperator(fcg, node);
		//TODO
		Operand1 = getOperand1(node);
		Operand2 = getOperand2(node);
		ArrayList<String> Args = new ArrayList<String>();
		if(Operand2 != "" && Operand2 != null){
			prefix = ", ";
		}
		
		switch(RHSCaseNumber){
		case 1:
			BinaryExpr binExpr = new BinaryExpr();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				if(fcg.isSubroutine==true){
					/**
					 * if input argument on the LHS of assignment stmt, we assume that this input argument maybe modified.
					 */
					if(fcg.inArgs.contains(name.getID())){
						if (Debug) System.out.println("subroutine's input "+name.getID()+" has been modified!");
						fcg.inputHasChanged.add(name.getID());
						var.setName(name.getID()+"_backup");						
					}
					else{
						var.setName(name.getID());
					}
				}
				else{
					if(fcg.outRes.contains(name.getID())){
						var.setName(fcg.majorName);
					}
					else{
						var.setName(name.getID());
					}
				}
				binExpr.addVariable(var);
			}
			/**
			 * insert constant variable replacement check.
			 */
			if((((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(Operand1).getSingleton())).isConstant())
					&&(fcg.inArgs.contains(Operand1)==false)){
				Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(Operand1).getSingleton())).getConstant();
				binExpr.setOperand1(c.toString());
			}
			else{
				if(fcg.inputHasChanged.contains(Operand1))
				{
					binExpr.setOperand1(Operand1+"_backup");
				}
				else{
					binExpr.setOperand1(Operand1);		
				}		
			}
			if((((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(Operand2).getSingleton())).isConstant())
					&&(fcg.inArgs.contains(Operand2)==false)){
				Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(Operand2).getSingleton())).getConstant();
				binExpr.setOperand2(c.toString());
			}
			else{
				binExpr.setOperand2(Operand2);				
			}
			binExpr.setOperator(RHSFortranOperator);
			return binExpr;
		case 2:
			UnaryExpr unExpr = new UnaryExpr();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				if(fcg.isSubroutine==true){
					var.setName(name.getID());
				}
				else{
					if(fcg.outRes.contains(name.getID())){
						var.setName(fcg.majorName);
					}
					else{
						var.setName(name.getID());
					}
				}
				unExpr.addVariable(var);
			}
			/**
			 * insert constant variable replacement check.
			 */
			if((((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(Operand1).getSingleton())).isConstant())
					&&(fcg.inArgs.contains(Operand1)==false)){
				Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(Operand1).getSingleton())).getConstant();
				unExpr.setOperand(c.toString());
			}
			else{
				unExpr.setOperand(Operand1);				
			}
			unExpr.setOperator(RHSFortranOperator);
			return unExpr;
		case 3:
			Args = getArgsList(node);
			/**
			 * insert constant variable replacement check.
			 */
			for(int i=0;i<Args.size();i++){
				if((((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(Args.get(i)).getSingleton())).isConstant())
						&&(fcg.inArgs.contains(Args.get(i))==false)){
					Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(Args.get(i)).getSingleton())).getConstant();
					Args.remove(i);
					Args.add(i, c.toString());
				}
				else{
					//do nothing.				
				}
			}
			ArgsListasString = getArgsListAsString(Args);
			DirectBuiltinExpr dirBuiltinExpr = new DirectBuiltinExpr();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				if(fcg.isSubroutine==true){
					var.setName(name.getID());
				}
				else{
					if(fcg.outRes.contains(name.getID())){
						var.setName(fcg.majorName);
					}
					else{
						var.setName(name.getID());
					}
				}
				dirBuiltinExpr.addVariable(var);
			}
			dirBuiltinExpr.setBuiltinFunc(RHSFortranOperator);
			dirBuiltinExpr.setArgsList(ArgsListasString);
			return dirBuiltinExpr;
		case 4:
			NoDirectBuiltinExpr noDirBuiltinExpr = new NoDirectBuiltinExpr();
			/**
			 * insert constant variable replacement check in corresponding inline code.
			 */
			noDirBuiltinExpr = FortranCodeASTInliner.inline(fcg, node);
			return noDirBuiltinExpr;
		case 5:
			/**
			 * this is for assign an built-in constant to a variable, for example:
			 * a = pi, and because of we are doing constant variable replacement,
			 * we kind of need to ignore this expression, because this is also a 
			 * kind of constant assignment.
			 * 
			 * the question is how to ignore this, because we have to return some expression,
			 * so, we cannot do anything here. My solution is go to the FortranCodeASTGenerator class.
			 */
			BuiltinConstantExpr builtinConst = new BuiltinConstantExpr();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				if(fcg.isSubroutine==true){
					var.setName(name.getID());
				}
				else{
					if(fcg.outRes.contains(name.getID())){
						var.setName(fcg.majorName);
					}
					else{
						var.setName(name.getID());
					}
				}
				builtinConst.addVariable(var);
			}
			builtinConst.setBuiltinFunc(RHSFortranOperator);
			return builtinConst;
		case 6:
			Args = getArgsList(node);
			/**
			 * insert constant variable replacement check.
			 */
			for(int i=0;i<Args.size();i++){
				if((((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(Args.get(i)).getSingleton())).isConstant())
						&&(fcg.inArgs.contains(Args.get(i))==false)){
					Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(Args.get(i)).getSingleton())).getConstant();
					Args.remove(i);
					Args.add(i, c.toString());
				}
				else{
					//do nothing.				
				}
			}
			ArgsListasString = getArgsListAsString(Args);
			IOOperationExpr ioExpr = new IOOperationExpr();
			ioExpr.setArgsList(ArgsListasString);
			ioExpr.setIOOperator(RHSFortranOperator);
			return ioExpr;
		case 7:
			/**
			 * deal with user defined subprogram, apparently, there is no corresponding Fortran built-in function for this.
			 */
			Args = getArgsList(node);
			/**
			 * insert constant variable replacement check.
			 */
			for(int i=0;i<Args.size();i++){
				if((((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(Args.get(i)).getSingleton())).isConstant())
						&&(fcg.inArgs.contains(Args.get(i))==false)){
					Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(Args.get(i)).getSingleton())).getConstant();
					Args.remove(i);
					Args.add(i, c.toString());
				}
				else{
					//do nothing.				
				}
			}
			if((node.getTargets().asNameList().size()==1)&&(hasArrayAsInput(fcg,Args)==false)){
				/**
				 * this is for functions.
				 */
				ArgsListasString = getArgsListAsString(Args);
				UserDefinedFunction userDefFunc = new UserDefinedFunction();
				for(ast.Name name : node.getTargets().asNameList()){
					Variable var = new Variable();
					if(fcg.isSubroutine==true){
						var.setName(name.getID());
					}
					else{
						if(fcg.outRes.contains(name.getID())){
							var.setName(fcg.majorName);
						}
						else{
							var.setName(name.getID());
						}
					}
					userDefFunc.addVariable(var);
				}
				String funcName;
				funcName = node.getRHS().getVarName();
				userDefFunc.setFuncName(funcName);
				userDefFunc.setArgsList(ArgsListasString);
				if(fcg.funcNameRep.containsValue(funcName)){
					/**
					 * already has this function name, don't need to put it into the hashmap again.
					 */
					return userDefFunc;
				}
				else{
					String LHSName;
					LHSName = node.getLHS().getNodeString().replace("[", "").replace("]", "");
					fcg.funcNameRep.put(LHSName, funcName);
					return userDefFunc;
				}
			}
			else{
				/**
				 * this is for subroutines.
				 */
				ArgsListasString = getArgsListAsString(Args);
				Subroutines subroutine = new Subroutines();
				ArrayList<String> outputArgsList = new ArrayList<String>();
				for(ast.Name name : node.getTargets().asNameList()){
					outputArgsList.add(name.getID());
				}
				String funcName;
				funcName = node.getRHS().getVarName();
				subroutine.setFuncName(funcName);
				subroutine.setInputArgsList(ArgsListasString);
				subroutine.setOutputArgsList(outputArgsList.toString().replace("[", "").replace("]", ""));
				return subroutine;
			}
		default:
			System.err.println("this cannot happen...");
			return null;
		}
	}
	
	public static int getRHSCaseNumber(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if(true==fcg.FortranMap.isFortranBinOperator(RHSMatlabOperator)){
			return 1;
		}
		else if(true==fcg.FortranMap.isFortranUnOperator(RHSMatlabOperator)){
			return 2;
		}
		else if(true==fcg.FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)){
			return 3;
		}
		else if(true ==fcg.FortranMap.isFortranNoDirectBuiltin(RHSMatlabOperator)){
			return 4;
		}
		else if(true==fcg.FortranMap.isBuiltinConst(RHSMatlabOperator)){
			return 5;
		}
		else if(true==fcg.FortranMap.isFortranIOOperation(RHSMatlabOperator)){
			return 6;
		}
		else{
			return 7; // "user defined function";
		}
	}
	
	public static String getOperand1(TIRAbstractAssignToListStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 1)
			return node.getRHS().getChild(1).getChild(0).getNodeString();
		else
			return "";
	}
	
	public static String getOperand2(TIRAbstractAssignToListStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 2)
			return node.getRHS().getChild(1).getChild(1).getNodeString();
		else
			return "";
	}
	
	public static String getRHSMappingFortranOperator(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
		String RHSFortranOperator;
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if(true==fcg.FortranMap.isFortranBinOperator(RHSMatlabOperator)){
			RHSFortranOperator= fcg.FortranMap.getFortranBinOpMapping(RHSMatlabOperator);
		}
		else if(true==fcg.FortranMap.isFortranUnOperator(RHSMatlabOperator)){
			RHSFortranOperator= fcg.FortranMap.getFortranUnOpMapping(RHSMatlabOperator);
		}
		
		else if(true==fcg.FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)){
			RHSFortranOperator= fcg.FortranMap.getFortranDirectBuiltinMapping(RHSMatlabOperator);
		}
		else if(true==fcg.FortranMap.isBuiltinConst(RHSMatlabOperator)){
			RHSFortranOperator= fcg.FortranMap.getFortranBuiltinConstMapping(RHSMatlabOperator);
		}
		else if(true == fcg.FortranMap.isFortranIOOperation(RHSMatlabOperator)){
			RHSFortranOperator= fcg.FortranMap.getFortranIOOperationMapping(RHSMatlabOperator);
		}
		else{
			RHSFortranOperator = "//cannot process it yet";
		}
		return RHSFortranOperator;
	}
	
	public static ArrayList<String> getArgsList(TIRAbstractAssignToListStmt node){
		ArrayList<String> Args = new ArrayList<String>();
		int numArgs = node.getRHS().getChild(1).getNumChild();
		for (int i=0;i<numArgs;i++){
			Args.add(node.getRHS().getChild(1).getChild(i).getNodeString());
		}
		return Args;
	}

	public static String getArgsListAsString(ArrayList<String> Args){
		String prefix ="";
		String argListasString="";
		for(String arg : Args){
			argListasString = argListasString+prefix+arg;
			prefix=", ";
		}
		return argListasString;
	}

	public String makeFortranStringLiteral(String StrLit){		
		if(StrLit.charAt(0)=='\'' && StrLit.charAt(StrLit.length()-1)=='\''){			
			return "\""+(String) StrLit.subSequence(1, StrLit.length()-1)+"\"";		
		}
		else
		return StrLit;
	}
	
	public static boolean hasArrayAsInput(FortranCodeASTGenerator fcg, ArrayList<String> Args){
		boolean result = false;
		for(String inArg : Args){
			if((fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(inArg))==null){
				//do nothing
			}
			else if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(inArg)).getSingleton()).getShape().isScalar()==true){
				//do nothing
			}
			else{
				result = true;
			}
		}
		return result;
	}
}

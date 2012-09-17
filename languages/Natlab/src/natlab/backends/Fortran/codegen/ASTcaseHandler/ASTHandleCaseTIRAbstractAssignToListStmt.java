package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;

public class ASTHandleCaseTIRAbstractAssignToListStmt {

	static boolean Debug = false;
	
	public ASTHandleCaseTIRAbstractAssignToListStmt(){
		
	}
	/**
	 * AbstractAssignToListStmt: Statement ::= [RuntimeCheck] Variable* Expression;
	 */
	public FortranCodeASTGenerator getFortran(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
		if (Debug) System.out.println("in an abstractAssignToList  statement");
		
		AbstractAssignToListStmt stmt = new AbstractAssignToListStmt();
		Expression exp = makeExpression(fcg, node);
		stmt.setExpression(exp);
		fcg.SubProgram.getStatementSection().addStatement(stmt);
		return fcg;
	}
	
	/**********************HELPER METHODS***********************************/
	public Expression makeExpression(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
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
				var.setName(name.getID());
				binExpr.addVariable(var);
			}
			binExpr.setOperand1(Operand1);
			binExpr.setOperand2(Operand2);
			binExpr.setOperator(RHSFortranOperator);
			return binExpr;
		case 2:
			UnaryExpr unExpr = new UnaryExpr();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				var.setName(name.getID());
				unExpr.addVariable(var);
			}
			unExpr.setOperand(Operand1);
			unExpr.setOperator(RHSFortranOperator);
			return unExpr;
		case 3:
			Args = getArgsList(node);
			ArgsListasString = getArgsListAsString(Args);
			DirectBuiltinExpr dirBuiltinExpr = new DirectBuiltinExpr();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				var.setName(name.getID());
				dirBuiltinExpr.addVariable(var);
			}
			dirBuiltinExpr.setBuiltinFunc(RHSFortranOperator);
			dirBuiltinExpr.setArgsList(ArgsListasString);
			return dirBuiltinExpr;
		case 4:
			NoDirectBuiltinExpr noDirBuiltinExpr = new NoDirectBuiltinExpr();
			noDirBuiltinExpr = FortranCodeASTInliner.inline(fcg, node);
			return noDirBuiltinExpr;
		case 5:
			BuiltinConstantExpr builtinConst = new BuiltinConstantExpr();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				var.setName(name.getID());
				builtinConst.addVariable(var);
			}
			builtinConst.setBuiltinFunc(RHSFortranOperator);
			return builtinConst;
		case 6:
			Args = getArgsList(node);
			ArgsListasString = getArgsListAsString(Args);
			IOOperationExpr ioExpr = new IOOperationExpr();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				var.setName(name.getID());
				ioExpr.addVariable(var);
			}
			ioExpr.setIOOperator(RHSFortranOperator);
			return ioExpr;
		case 7:
			/**
			 * deal with user defined functions, apparently, there is no corresponding Fortran function for this.
			 */
			Args = getArgsList(node);
			ArgsListasString = getArgsListAsString(Args);
			UserDefinedFunction userDefFunc = new UserDefinedFunction();
			for(ast.Name name : node.getTargets().asNameList()){
				Variable var = new Variable();
				var.setName(name.getID());
				userDefFunc.addVariable(var);
			}
			/**
			 * the function is a user defined function, so cannot find a mapping name or operator.
			 * here, there are two situations:
			 * 1, the number of target variables is 1;
			 * 2, the number of target variables is 2.
			 * TODO
			 */
			String funcName;
			funcName = node.getRHS().getVarName();
			userDefFunc.setFuncName(funcName);
			userDefFunc.setArgsList(ArgsListasString);
			String LHSName;
			LHSName = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			//XU, a little bit trick, go back to IR to get a better solution
			fcg.funcNameRep.put(funcName, LHSName);
			return userDefFunc;
		default:
			System.err.println("this cannot happen...");
			return null;
		}
	}
	
	public int getRHSCaseNumber(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if(true==fcg.FortranMap.isFortranBinOperator(RHSMatlabOperator)){
			return 1; //"binop";
		}
		else if(true==fcg.FortranMap.isFortranUnOperator(RHSMatlabOperator)){
			return 2; //"unop";
		}
		else if(true==fcg.FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)){
			return 3; // "directBuiltin";
		}
		else if(true ==fcg.FortranMap.isFortranNoDirectBuiltin(RHSMatlabOperator)){
			return 4; // "noDirectBuiltin";
		}
		else if(true==fcg.FortranMap.isBuiltinConst(RHSMatlabOperator)){
			return 5; // "builtinConst";
		}
		else if(true==fcg.FortranMap.isFortranIOOperation(RHSMatlabOperator)){
			return 6; // "IOOPeration";
		}
		else{
			return 7; // "user defined function";
		}
	}
	
	public String getOperand1(TIRAbstractAssignToListStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 1)
			return node.getRHS().getChild(1).getChild(0).getNodeString();
		else
			return "";
	}
	
	public String getOperand2(TIRAbstractAssignToListStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 2)
			return node.getRHS().getChild(1).getChild(1).getNodeString();
		else
			return "";
	}
	
	public String getRHSMappingFortranOperator(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
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

	public String getArgsListAsString(ArrayList<String> Args){
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
}

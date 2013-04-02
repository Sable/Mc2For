package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class HandleCaseTIRAbstractAssignToListStmt {
	static boolean Debug = false;
	
	/**
	 * AbstractAssignToListStmt: Statement ::= [RuntimeCheck] Variable* Expression;
	 * for each statement, currently, we need to insert two check:
	 * 1. for rhs, constant folding check;
	 * 2. for lhs, variable allocation check.
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			TIRAbstractAssignToListStmt node) {
		if (Debug) System.out.println("in an abstractAssignToList statement");
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		/*
		 * TODO Change to support built-ins with multiple args.
		 * Currently it handles general case built-ins with one or two args only.
		 */
		int RHSCaseNumber = getRHSCaseNumber(fcg, node);
		String RHSFortranOperator = getRHSMappingFortranOperator(fcg, node);
		String Operand1 = getOperand1(node);
		String Operand2 = getOperand2(node);
		String ArgsListasString;
		ArrayList<String> Args = new ArrayList<String>();
		switch (RHSCaseNumber) {
		case 1:
			BinaryExpr binExpr = new BinaryExpr();
			binExpr.setIndent(indent);
			for (ast.Name name : node.getTargets().asNameList()) {
				Variable var = new Variable();
				if (fcg.isSubroutine) {
					/*
					 * if an input argument of the function is on the LHS of an assignment stmt, 
					 * we assume that this input argument maybe modified.
					 */
					if (fcg.inArgs.contains(name.getID())) {
						if (Debug) System.out.println("subroutine's input "+name.getID()
								+" has been modified!");
						/*
						 * here we need to detect whether it is the first time this variable 
						 * put in the set, because we only want to back up them once. but, 
						 * actually, the overhead for checking is similar to put it into set 
						 * twice.
						 */
						if (fcg.inputHasChanged.contains(name.getID())) {
							// do nothing.
							if (Debug) System.out.println("encounter "+name.getID()+" again.");
						}
						else {
							if (Debug) System.out.println("first time encounter "+name.getID());
							fcg.inputHasChanged.add(name.getID());
						}
						var.setName(name.getID()+"_copy");
					}
					else var.setName(name.getID());
				}
				else {
					if (fcg.outRes.contains(name.getID())) var.setName(fcg.majorName);
					else var.setName(name.getID());
				}
				binExpr.addVariable(var);
			}
			/*
			 * insert constant folding check.
			 */
			if (fcg.getMatrixValue(Operand1).hasConstant() 
					&& !fcg.inArgs.contains(Operand1)) {
				Constant c = fcg.getMatrixValue(Operand1).getConstant();
				binExpr.setOperand1(c.toString());
			}
			else {
				if (fcg.inputHasChanged.contains(Operand1))	binExpr.setOperand1(Operand1+"_copy");
				else binExpr.setOperand1(Operand1);	
			}
			if (fcg.getMatrixValue(Operand2).hasConstant() 
					&& !fcg.inArgs.contains(Operand2)) {
				Constant c = fcg.getMatrixValue(Operand2).getConstant();
				binExpr.setOperand2(c.toString());
			}
			else {
				if (fcg.inputHasChanged.contains(Operand2))	binExpr.setOperand2(Operand2+"_copy");
				else binExpr.setOperand2(Operand2);
			}
			binExpr.setOperator(RHSFortranOperator);
			return binExpr;
		case 2:
			UnaryExpr unExpr = new UnaryExpr();
			unExpr.setIndent(indent);
			for (ast.Name name : node.getTargets().asNameList()) {
				Variable var = new Variable();
				if (fcg.isSubroutine) {
					/*
					 * if an input argument of the function is on the LHS of an assignment stmt, 
					 * we assume that this input argument maybe modified.
					 */
					if (fcg.inArgs.contains(name.getID())) {
						if (Debug) System.out.println("subroutine's input "+name.getID()
								+" has been modified!");
						/*
						 * here we need to detect whether it is the first time this variable put 
						 * in the set, because we only want to back up them once.
						 */
						if (fcg.inputHasChanged.contains(name.getID())) {
							// do nothing.
							if (Debug) System.out.println("encounter "+name.getID()+" again.");
						}
						else {
							if (Debug) System.out.println("first time encounter "+name.getID());
							fcg.inputHasChanged.add(name.getID());
						}
						var.setName(name.getID()+"_copy");
					}
					else var.setName(name.getID());
				}
				else {
					if (fcg.outRes.contains(name.getID())) var.setName(fcg.majorName);
					else var.setName(name.getID());
				}
				unExpr.addVariable(var);
			}
			/*
			 * insert constant folding check.
			 */
			if (fcg.getMatrixValue(Operand1).hasConstant() && !fcg.inArgs.contains(Operand1)) {
				Constant c = fcg.getMatrixValue(Operand1).getConstant();
				unExpr.setOperand(c.toString());
			}
			else {
				if (fcg.inputHasChanged.contains(Operand1))	unExpr.setOperand(Operand1+"_copy");
				else unExpr.setOperand(Operand1);
			}
			unExpr.setOperator(RHSFortranOperator);
			return unExpr;
		case 3:
			DirectBuiltinExpr dirBuiltinExpr = new DirectBuiltinExpr();
			dirBuiltinExpr.setIndent(indent);
			for (ast.Name name : node.getTargets().asNameList()) {
				Variable var = new Variable();
				if (fcg.isSubroutine) {
					/*
					 * if an input argument of the function is on the LHS of an assignment stmt, 
					 * we assume that this input argument maybe modified.
					 */
					if (fcg.inArgs.contains(name.getID())) {
						if (Debug) System.out.println("subroutine's input "+name.getID()
								+" has been modified!");
						/*
						 * here we need to detect whether it is the first time this variable put 
						 * in the set, because we only want to back up them once.
						 */
						if (fcg.inputHasChanged.contains(name.getID())) {
							//do nothing.
							if (Debug) System.out.println("encounter "+name.getID()+" again.");
						}
						else {
							if (Debug) System.out.println("first time encounter "+name.getID());
							fcg.inputHasChanged.add(name.getID());
						}
						var.setName(name.getID()+"_copy");
					}
					else var.setName(name.getID());
				}
				else {
					if (fcg.outRes.contains(name.getID())) var.setName(fcg.majorName);
					else var.setName(name.getID());
				}
				dirBuiltinExpr.addVariable(var);
			}
			Args = getArgsList(node);
			/*
			 * insert constant folding check.
			 */
			for (int i=0;i<Args.size();i++) {
				if (fcg.getMatrixValue(Args.get(i)).hasConstant() 
						&& !fcg.inArgs.contains(Args.get(i))) {
					Constant c = fcg.getMatrixValue(Args.get(i)).getConstant();
					Args.remove(i);
					Args.add(i, c.toString());
				}
				else {
					if (fcg.inputHasChanged.contains(Args.get(i))) {
						String ArgsNew = Args.get(i)+"_copy";
						Args.remove(i);
						Args.add(i, ArgsNew);
					}
				}
			}
			ArgsListasString = getArgsListAsString(Args);
			dirBuiltinExpr.setBuiltinFunc(RHSFortranOperator);
			dirBuiltinExpr.setArgsList(ArgsListasString);
			return dirBuiltinExpr;
		case 4:
			NoDirectBuiltinExpr noDirBuiltinExpr = new NoDirectBuiltinExpr();
			/*
			 * insert constant folding check and variable allocation check 
			 * in corresponding in-lined code. insert indent in the in-lined 
			 * code, too.
			 */
			noDirBuiltinExpr = FortranCodeASTInliner.inline(fcg, node);
			return noDirBuiltinExpr;
		case 5:
			/*
			 * this is for assigning an built-in constant to a variable, for example:
			 * a = pi, and because of we are doing constant folding,
			 * we kind of need to ignore this expression, because this is also a 
			 * kind of constant assignment.
			 * 
			 * the question is how to ignore this, because we have to return some 
			 * expression, so, we cannot do anything here. My solution is go to the 
			 * FortranCodeASTGenerator class.
			 */
			BuiltinConstantExpr builtinConst = new BuiltinConstantExpr();
			builtinConst.setIndent(indent);
			for (ast.Name name : node.getTargets().asNameList()) {
				Variable var = new Variable();
				if (fcg.isSubroutine) {
					/*
					 * if an input argument of the function is on the LHS of an assignment stmt, 
					 * we assume that this input argument maybe modified.
					 */
					if (fcg.inArgs.contains(name.getID())) {
						if (Debug) System.out.println("subroutine's input "+name.getID()
								+" has been modified!");
						/*
						 * here we need to detect whether it is the first time this variable put 
						 * in the set, because we only want to back up them once.
						 */
						if (fcg.inputHasChanged.contains(name.getID())) {
							//do nothing.
							if (Debug) System.out.println("encounter "+name.getID()+" again.");
						}
						else {
							if (Debug) System.out.println("first time encounter "+name.getID());
							fcg.inputHasChanged.add(name.getID());
						}
						var.setName(name.getID()+"_copy");
					}
					else var.setName(name.getID());
				}
				else {
					if (fcg.outRes.contains(name.getID())) var.setName(fcg.majorName);
					else var.setName(name.getID());
				}
				builtinConst.addVariable(var);
			}
			builtinConst.setBuiltinFunc(RHSFortranOperator);
			return builtinConst;
		case 6:
			IOOperationExpr ioExpr = new IOOperationExpr();
			ioExpr.setIndent(indent);
			Args = getArgsList(node);
			/*
			 * insert constant folding check.
			 */
			for (int i=0;i<Args.size();i++) {
				if (fcg.getMatrixValue(Args.get(i)).hasConstant() 
						&& !fcg.inArgs.contains(Args.get(i))) {
					Constant c = fcg.getMatrixValue(Args.get(i)).getConstant();
					Args.remove(i);
					Args.add(i, c.toString());
				}
				else {
					if (fcg.inputHasChanged.contains(Args.get(i))) {
						String ArgsNew = Args.get(i)+"_copy";
						Args.remove(i);
						Args.add(i, ArgsNew);
					}
				}
			}
			ArgsListasString = getArgsListAsString(Args);
			ioExpr.setArgsList(ArgsListasString);
			ioExpr.setIOOperator(RHSFortranOperator);
			return ioExpr;
		case 7:
			/*
			 * deal with user defined subprogram, apparently, there is 
			 * no corresponding Fortran built-in function for this.
			 */
			Args = getArgsList(node);
			if (node.getTargets().asNameList().size()==1 && !hasArrayAsInput(fcg,Args)) {
				/*
				 * this is for functions.
				 */
				UserDefinedFunction userDefFunc = new UserDefinedFunction();
				userDefFunc.setIndent(indent);
				for (ast.Name name : node.getTargets().asNameList()) {
					Variable var = new Variable();
					if (fcg.isSubroutine) {
						/*
						 * if an input argument of the function is on the LHS of an assignment stmt, 
						 * we assume that this input argument maybe modified.
						 */
						if (fcg.inArgs.contains(name.getID())) {
							if (Debug) System.out.println("subroutine's input "+name.getID()
									+" has been modified!");
							/*
							 * here we need to detect whether it is the first time this variable 
							 * put in the set, because we only want to back up them once.
							 */
							if (fcg.inputHasChanged.contains(name.getID())) {
								// do nothing.
								if (Debug) System.out.println(
										"encounter "+name.getID()+" again.");
							}
							else {
								if (Debug) System.out.println(
										"first time encounter "+name.getID());
								fcg.inputHasChanged.add(name.getID());
							}
							var.setName(name.getID()+"_copy");
						}
						else var.setName(name.getID());
					}
					else {
						if (fcg.outRes.contains(name.getID())) var.setName(fcg.majorName);
						else var.setName(name.getID());
					}
					userDefFunc.addVariable(var);
				}
				/*
				 * insert constant folding check.
				 */
				for (int i=0;i<Args.size();i++) {
					if (fcg.getMatrixValue(Args.get(i)).hasConstant() 
							&& !fcg.inArgs.contains(Args.get(i))) {
						Constant c = fcg.getMatrixValue(Args.get(i)).getConstant();
						Args.remove(i);
						Args.add(i, c.toString());
					}
					else {
						if (fcg.inputHasChanged.contains(Args.get(i))) {
							String ArgsNew = Args.get(i)+"_copy";
							Args.remove(i);
							Args.add(i, ArgsNew);
						}
					}
				}
				ArgsListasString = getArgsListAsString(Args);
				String funcName;
				funcName = node.getRHS().getVarName();
				userDefFunc.setFuncName(funcName);
				userDefFunc.setArgsList(ArgsListasString);
				if (fcg.funcNameRep.containsValue(funcName)) {
					/*
					 * already has this function name, 
					 * don't need to put it into the hash map again.
					 */
					return userDefFunc;
				}
				else {
					String LHSName;
					LHSName = node.getLHS().getNodeString().replace("[", "").replace("]", "");
					fcg.funcNameRep.put(LHSName, funcName);
					return userDefFunc;
				}
			}
			else {
				/*
				 * this is for subroutines.
				 */
				Subroutines subroutine = new Subroutines();
				subroutine.setIndent(indent);
				ArrayList<String> outputArgsList = new ArrayList<String>();
				for (ast.Name name : node.getTargets().asNameList()) {
					/*
					 * if an input argument of the function is on the LHS of an assignment stmt, 
					 * we assume that this input argument maybe modified.
					 */
					if (fcg.inArgs.contains(name.getID())) {
						if (Debug) System.out.println("subroutine's input "+name.getID()
								+" has been modified!");
						/*
						 * here we need to detect whether it is the first time this variable 
						 * put in the set, because we only want to back up them once.
						 */
						if (fcg.inputHasChanged.contains(name.getID())) {
							// do nothing.
							if (Debug) System.out.println("encounter "+name.getID()+" again.");
						}
						else {
							if (Debug) System.out.println("first time encounter "+name.getID());
							fcg.inputHasChanged.add(name.getID());
						}
						outputArgsList.add(name.getID()+"_copy");
					}
					else outputArgsList.add(name.getID());
				}
				/*
				 * insert constant folding check.
				 */
				for (int i=0;i<Args.size();i++) {
					if (fcg.getMatrixValue(Args.get(i)).hasConstant() 
							&& !fcg.inArgs.contains(Args.get(i))) {
						Constant c = fcg.getMatrixValue(Args.get(i)).getConstant();
						Args.remove(i);
						Args.add(i, c.toString());
					}
					else {
						if (fcg.inputHasChanged.contains(Args.get(i))) {
							String ArgsNew = Args.get(i)+"_copy";
							Args.remove(i);
							Args.add(i, ArgsNew);
						}			
					}
				}
				ArgsListasString = getArgsListAsString(Args);
				String funcName;
				funcName = node.getRHS().getVarName();
				subroutine.setFuncName(funcName);
				subroutine.setInputArgsList(ArgsListasString);
				subroutine.setOutputArgsList(outputArgsList.toString().replace("[", "")
						.replace("]", ""));
				return subroutine;
			}
		default:
			System.err.println("this cannot happen...");
			return null;
		}
	}
	/***************************************helper methods****************************************/
	public static int getRHSCaseNumber(FortranCodeASTGenerator fcg, 
			TIRAbstractAssignToListStmt node) {
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if (fcg.FortranMap.isFortranBinOperator(RHSMatlabOperator)) return 1;
		else if (fcg.FortranMap.isFortranUnOperator(RHSMatlabOperator)) return 2;
		else if (fcg.FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)) return 3;
		else if(fcg.FortranMap.isFortranNoDirectBuiltin(RHSMatlabOperator))	return 4;
		else if(fcg.FortranMap.isBuiltinConst(RHSMatlabOperator)) return 5;
		else if(fcg.FortranMap.isFortranIOOperation(RHSMatlabOperator)) return 6;
		else return 7; // "user defined function";
	}
	
	public static String getOperand1(TIRAbstractAssignToListStmt node) {
		if (node.getRHS().getChild(1).getNumChild()>=1)
			return node.getRHS().getChild(1).getChild(0).getNodeString();
		else return "";
	}
	
	public static String getOperand2(TIRAbstractAssignToListStmt node) {
		if (node.getRHS().getChild(1).getNumChild() >= 2) 
			return node.getRHS().getChild(1).getChild(1).getNodeString();
		else return "";
	}
	
	public static String getRHSMappingFortranOperator(FortranCodeASTGenerator fcg, 
			TIRAbstractAssignToListStmt node) {
		String RHSFortranOperator;
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if (fcg.FortranMap.isFortranBinOperator(RHSMatlabOperator)) {
			RHSFortranOperator = fcg.FortranMap.getFortranBinOpMapping(RHSMatlabOperator);
		}
		else if (fcg.FortranMap.isFortranUnOperator(RHSMatlabOperator)) {
			RHSFortranOperator = fcg.FortranMap.getFortranUnOpMapping(RHSMatlabOperator);
		}
		else if (fcg.FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)) {
			RHSFortranOperator = fcg.FortranMap.getFortranDirectBuiltinMapping(RHSMatlabOperator);
		}
		else if (fcg.FortranMap.isBuiltinConst(RHSMatlabOperator)) {
			RHSFortranOperator = fcg.FortranMap.getFortranBuiltinConstMapping(RHSMatlabOperator);
		}
		else if (fcg.FortranMap.isFortranIOOperation(RHSMatlabOperator)) {
			RHSFortranOperator = fcg.FortranMap.getFortranIOOperationMapping(RHSMatlabOperator);
		}
		else RHSFortranOperator = "// cannot process it yet";
		return RHSFortranOperator;
	}
	
	public static ArrayList<String> getArgsList(TIRAbstractAssignToListStmt node) {
		ArrayList<String> Args = new ArrayList<String>();
		int numArgs = node.getRHS().getChild(1).getNumChild();
		for (int i=0;i<numArgs;i++) {
			Args.add(node.getRHS().getChild(1).getChild(i).getNodeString());
		}
		return Args;
	}

	public static String getArgsListAsString(ArrayList<String> Args) {
		String prefix ="";
		String argListasString="";
		for (String arg : Args) {
			argListasString = argListasString+prefix+arg;
			prefix=", ";
		}
		return argListasString;
	}

	public String makeFortranStringLiteral(String StrLit) {		
		if (StrLit.charAt(0)=='\'' && StrLit.charAt(StrLit.length()-1)=='\'') {			
			return "\""+StrLit.subSequence(1, StrLit.length()-1)+"\"";		
		}
		return StrLit;
	}
	
	public static boolean hasArrayAsInput(FortranCodeASTGenerator fcg, ArrayList<String> Args) {
		boolean result = false;
		for (String inArg : Args) {
			if (fcg.getCurrentOutSet().get(inArg)!=null
					&& !fcg.getMatrixValue(inArg).getShape().isScalar()) result = true;
		}
		return result;
	}
}

package natlab.backends.Fortran.codegen;

import java.util.ArrayList;
import java.util.HashMap;

import ast.ASTNode;

import natlab.tame.tir.TIRAbstractAssignStmt;
import natlab.tame.tir.TIRAbstractAssignToListStmt;
import natlab.tame.tir.TIRAbstractAssignToVarStmt;
import natlab.tame.tir.TIRAssignLiteralStmt;
import natlab.tame.tir.TIRFunction;
import natlab.tame.tir.TIRNode;
import natlab.tame.tir.analysis.TIRAbstractNodeCaseHandler;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.backends.Fortran.codegen.ASTcaseHandler.*;

public class FortranCodeASTGenerator extends TIRAbstractNodeCaseHandler{
	public ValueAnalysis<AggrValue<BasicMatrixValue>> analysis;
	public StringBuffer buf;
	public StringBuffer buf2;
	public FortranMapping FortranMap;
	public ArrayList<String> forStmtParameter;
	public ArrayList<String> arrayIndexParameter;
	public int callgraphSize;
	public int index;
	public String fileDir;
	public String majorName;
	public ArrayList<String> inArgs;
	public ArrayList<String> outRes;
	public HashMap<String, String> funcNameRep;//the key of this hashmap is the user defined function name, 
	                                           //and the value is the corresponding substitute variable name.
	public boolean indentIf;
	public boolean indentFW;
	public boolean isSubroutine;//this boolean value help the compiler to distinguish subroutine with function.
	public HashMap<String, BasicMatrixValue> tmpVariables;//to store those temporary variables which are used in Fortran code generation.
	                                                        //The key is name, and the value is its shape.
	public SubProgram SubProgram;
	static boolean Debug = false;
	
	public FortranCodeASTGenerator(ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir){
		this.analysis = analysis;
		this.buf = new StringBuffer();
		this.buf2 = new StringBuffer();
		this.FortranMap = new FortranMapping();
		this.forStmtParameter = new ArrayList<String>();
		this.arrayIndexParameter = new ArrayList<String>();
		this.callgraphSize = callgraphSize;
		this.index = index;
		this.fileDir = fileDir;
		this.majorName = "";
		this.inArgs = new ArrayList<String>();
		this.outRes = new ArrayList<String>();
		this.funcNameRep = new HashMap<String,String>();
		this.indentIf = false;
		this.indentFW = false;
		this.isSubroutine = false;
		this.tmpVariables = new HashMap<String,BasicMatrixValue>();
		((TIRNode)analysis.getNodeList().get(index).getAnalysis().getTree()).tirAnalyze(this);
	}
	
	public static SubProgram FortranProgramGen(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir){
		return new FortranCodeASTGenerator(analysis, callgraphSize, index, fileDir).SubProgram;
	}
	
	@Override
	public void caseASTNode(ASTNode node){
		
	}
	
	@Override
	public void caseTIRFunction(TIRFunction node){
		ASTHandleCaseTIRFunction functionStmt = new ASTHandleCaseTIRFunction();
		functionStmt.getFortran(this, node);
	}
	
	@Override
	public void caseTIRAssignLiteralStmt(TIRAssignLiteralStmt node){
		ASTHandleCaseTIRAssignLiteralStmt assignLiteralStmt = new ASTHandleCaseTIRAssignLiteralStmt();
		assignLiteralStmt.getFortran(this, node);
	}
	
	@Override
	public void caseTIRAbstractAssignToVarStmt(TIRAbstractAssignToVarStmt node){
		ASTHandleCaseTIRAbstractAssignToVarStmt abstractAssignToVarStmt = new ASTHandleCaseTIRAbstractAssignToVarStmt();
		abstractAssignToVarStmt.getFortran(this, node);
	}

	@Override
	public void caseTIRAbstractAssignToListStmt(TIRAbstractAssignToListStmt node){
		ASTHandleCaseTIRAbstractAssignToListStmt abstractAssignToListStmt = new ASTHandleCaseTIRAbstractAssignToListStmt();
		abstractAssignToListStmt.getFortran(this, node);
	}
	
	/**********************HELPER METHODS***********************************/
	public void makeExpression(TIRAbstractAssignStmt node){
		/** 
		 * Change for built-ins with n args.
		 * Currently it handles general case built-ins with one or two args only.
		 */
		int RHSCaseNumber;
		String RHSFortranOperator;
		String Operand1, Operand2, prefix="";
		String ArgsListasString;
		RHSCaseNumber = getRHSCaseNumber(node);
		RHSFortranOperator = getRHSMappingFortranOperator(node);
		//TODO
		Operand1 = getOperand1(node);
		Operand2 = getOperand2(node);
		ArrayList<String> Args = new ArrayList<String>();
		if(Operand2 != "" && Operand2 != null){prefix = ", ";}
		switch(RHSCaseNumber){
		case 1:
			buf.append(Operand1+" "+RHSFortranOperator+" "+Operand2+" ;");
			break;
		case 2:
			buf.append(RHSFortranOperator+""+Operand1+" ;"); //TODO test this
			break;
		case 3:
			Args = getArgsList(node);
			ArgsListasString = getArgsListAsString(Args);
			buf.append(RHSFortranOperator+"("+ArgsListasString+");");
			//buf.append("" + RHS + ArgsListasString);
			break;
		case 4:
			//TODO, add more similar "method"
			break;
		case 5:
			buf.append(RHSFortranOperator+";");
			break;
		case 6:
			Args = getArgsList(node);
			ArgsListasString = getArgsListAsString(Args);
			buf.append(""+RHSFortranOperator+ArgsListasString);
			break;
		case 7:
			/**
			 * deal with user defined functions, apparently, there is no corresponding Fortran function for this.
			 */
			String RHSName;
			RHSName = node.getRHS().getVarName();
			String LHSName;
			LHSName = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			//XU, a little bit trick, go back to IR to get a better solution
			funcNameRep.put(RHSName, LHSName);
			Args = getArgsList(node);
			ArgsListasString = getArgsListAsString(Args);
			buf.append(RHSName+"("+ArgsListasString+");");
			break;
		default:
			buf.append("//is it an error?");
			break;
		}
	}
	
	public String getOperand1(TIRAbstractAssignStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 1)
			return node.getRHS().getChild(1).getChild(0).getNodeString();
		else
			return "";
	}
	
	public String getOperand2(TIRAbstractAssignStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 2)
			return node.getRHS().getChild(1).getChild(1).getNodeString();
		else
			return "";
	}
	
	public int getRHSCaseNumber(TIRAbstractAssignStmt node){
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if(true==FortranMap.isBinOperator(RHSMatlabOperator)){
			return 1; //"binop";
		}
		else if(true==FortranMap.isUnOperator(RHSMatlabOperator)){
		   return 2; //"unop";
		}
		else if(true==FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)){
			return 3; // "builtin";
		}
		else if(true == FortranMap.isMethod(RHSMatlabOperator)){
			return 4; // "method";
		}
		else if(true==FortranMap.isBuiltinConst(RHSMatlabOperator)){
			return 5; // "builtin";
		}
		else if(true==FortranMap.isIOOperation(RHSMatlabOperator)){
			return 6; // "IO OPeration";
		}
		else{
			return 7; // "user defined function";
		}
	}
	
	public String getRHSMappingFortranOperator(TIRAbstractAssignStmt node){
		String RHSFortranOperator;
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if(true==FortranMap.isBinOperator(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranBinOpMapping(RHSMatlabOperator);
		}
		else if(true==FortranMap.isUnOperator(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranUnOpMapping(RHSMatlabOperator);
		}
		
		else if(true==FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranDirectBuiltinMapping(RHSMatlabOperator);
		}
		else if(true==FortranMap.isBuiltinConst(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranBuiltinConstMapping(RHSMatlabOperator);
		}
		else if(true == FortranMap.isMethod(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranMethodMapping(RHSMatlabOperator);
		}
		else if(true == FortranMap.isIOOperation(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranIOOperationMapping(RHSMatlabOperator);
		}
		else{
			RHSFortranOperator = "//cannot process it yet";
		}
		return RHSFortranOperator;
	}
	public ArrayList<String> getArgsList(TIRAbstractAssignStmt node){
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
	
	public void printStatements(ast.List<ast.Stmt> stmts){
		for(ast.Stmt stmt : stmts){
			if(indentIf == true){
				buf.append("  ");
			}
			else if(indentFW == true){
				buf.append(" ");
			}
			int length = buf.length();
			((TIRNode)stmt).tirAnalyze(this);
			if (buf.length() > length) buf.append('\n');
		}
	}
}

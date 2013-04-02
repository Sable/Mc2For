package natlab.backends.Fortran.codegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ast.ASTNode;

import natlab.tame.tir.*;
import natlab.tame.tir.analysis.TIRAbstractNodeCaseHandler;
import natlab.tame.valueanalysis.ValueFlowMap;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.backends.Fortran.codegen.ASTcaseHandler.*;

public class FortranCodeASTGenerator extends TIRAbstractNodeCaseHandler {
	static boolean Debug = false;
	int callgraphSize;
	String fileDir;
	ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet;
	public StringBuffer buf;
	public StringBuffer buf2;
	public FortranMapping FortranMap;
	public ArrayList<String> forStmtParameter;
	public ArrayList<String> arrayIndexParameter;
	public String majorName;
	public ArrayList<String> inArgs;
	public ArrayList<String> outRes;
	/* 
	 * funcNameRep, for function name replacement. 
	 * K: the user defined function name, 
	 * V: the corresponding substitute variable name.
	 */
	public HashMap<String, String> funcNameRep;
	/* 
	 * isSubroutine: this boolean value help the compiler 
	 * to distinguish subroutine with function.
	 */
	public boolean isSubroutine;
	public HashSet<String> inputHasChanged;
	public HashSet<String> arrayConvert;
	/* 
	 * tmpVariables, to store those temporary variables 
	 * which are used in Fortran code generation.
	 * K: the name, 
	 * V: its basic matrix value.
	 */
	public HashMap<String, BasicMatrixValue> tmpVariables;
	public int ifWhileForBlockNest;
	public StatementSection stmtSecForIfWhileForBlock;
	public SubProgram SubProgram;
	public int indentNum;
	public String indent;
	/* 
	 * tmpVarAsArrayIndex, 
	 * K: the name of the temp variable which is used as array index, 
	 * V: the range of those variables.
	 */
	public HashMap<String, ArrayList<String>> tmpVarAsArrayIndex;
	
	public FortranCodeASTGenerator(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, 
			int callgraphSize, 
			int index, String fileDir) {
		this.callgraphSize = callgraphSize;
		this.fileDir = fileDir;
		currentOutSet = analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet();
		FortranMap = new FortranMapping();
		forStmtParameter = new ArrayList<String>();
		arrayIndexParameter = new ArrayList<String>();
		majorName = "";
		inArgs = new ArrayList<String>();
		outRes = new ArrayList<String>();
		funcNameRep = new HashMap<String,String>();
		isSubroutine = false;
		inputHasChanged = new HashSet<String>();
		arrayConvert = new HashSet<String>();
		tmpVariables = new HashMap<String,BasicMatrixValue>();
		ifWhileForBlockNest = 0;
		stmtSecForIfWhileForBlock = new StatementSection();
		SubProgram = new SubProgram();
		indentNum = 0;
		indent = "   ";
		tmpVarAsArrayIndex = new HashMap<String, ArrayList<String>>();
		((TIRNode)analysis.getNodeList().get(index).getAnalysis().getTree()).tirAnalyze(this);
	}
	
	/*************************************HELPER METHODS******************************************/
	public static SubProgram FortranProgramGen(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, 
			int callgraphSize, 
			int index, String fileDir) {
		return new FortranCodeASTGenerator(analysis, callgraphSize, index, fileDir).SubProgram;
	}

	public void iterateStatements(ast.List<ast.Stmt> stmts) {
		for (ast.Stmt stmt : stmts) {
			((TIRNode)stmt).tirAnalyze(this);
		}
	}
	
	public boolean hasArrayAsInput() {
		boolean result = false;
		for (String inArg : inArgs) {
			if (!getMatrixValue(inArg).getShape().isScalar()) {
				result = true;
			}
		}
		return result;
	}
	
	public ValueFlowMap<AggrValue<BasicMatrixValue>> getCurrentOutSet() {
		return currentOutSet;
	}
	
	public BasicMatrixValue getMatrixValue(String variable) {
		return (BasicMatrixValue) currentOutSet.get(variable).getSingleton();
	}
	
	/**************************************AST NODE OVERRIDE**************************************/
	@Override
	public void caseASTNode(ASTNode node) {}
	
	@Override
	public void caseTIRFunction(TIRFunction node) {
		HandleCaseTIRFunction functionStmt = new HandleCaseTIRFunction();
		functionStmt.getFortran(this, node);
	}
	
	@Override
	public void caseTIRCommentStmt(TIRCommentStmt node) {
		HandleCaseTIRCommentStmt commentStmt = new HandleCaseTIRCommentStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(commentStmt.getFortran(this, node));
		else 
			SubProgram.getStatementSection().addStatement(commentStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRAssignLiteralStmt(TIRAssignLiteralStmt node) {
		/*
		 * insert constant variable replacement check.
		 */
		String targetName = node.getTargetName().getVarName();
		if (getMatrixValue(targetName).hasConstant() && !outRes.contains(targetName) 
				&& !inArgs.contains(targetName)) {
			if (Debug) System.out.println(targetName+" is a constant");
		}
		else {
			HandleCaseTIRAssignLiteralStmt assignLiteralStmt = 
					new HandleCaseTIRAssignLiteralStmt();
			if (ifWhileForBlockNest!=0) 
				stmtSecForIfWhileForBlock.addStatement(assignLiteralStmt.getFortran(this, node));
			else 
				SubProgram.getStatementSection().addStatement(assignLiteralStmt
						.getFortran(this, node));		
		}
	}
	
	@Override
	public void caseTIRAbstractAssignToVarStmt(TIRAbstractAssignToVarStmt node) {
		/*
		 * insert constant variable replacement check.
		 */
		String targetName = node.getTargetName().getVarName();
		if (getMatrixValue(targetName).hasConstant() && !this.outRes.contains(targetName)) {
			if (Debug) System.out.println(targetName+" is a constant");
		}
		else {
			HandleCaseTIRAbstractAssignToVarStmt abstractAssignToVarStmt = 
					new HandleCaseTIRAbstractAssignToVarStmt();
			if (ifWhileForBlockNest!=0) 
				stmtSecForIfWhileForBlock.addStatement(abstractAssignToVarStmt
						.getFortran(this, node));
			else 
				SubProgram.getStatementSection().addStatement(abstractAssignToVarStmt
						.getFortran(this, node));
		}
	}

	@Override
	public void caseTIRAbstractAssignToListStmt(TIRAbstractAssignToListStmt node) {
		/*
		 * insert constant variable replacement check.
		 * p.s. need to check whether the expression is io expression,
		 * because io expression doesn't have target variable
		 *
		 * one more problem, for this case, the lhs is a list of variable.
		 * And because node.getTargetName().getVarName() can only return the first variable,
		 * we need use node.getTargets().asNameList().
		 */
		if (HandleCaseTIRAbstractAssignToListStmt.getRHSCaseNumber(this, node)!=6) {
			String targetName = node.getTargetName().getVarName();
			if(getMatrixValue(targetName).hasConstant() && !outRes.contains(targetName)) {
				if (Debug) System.out.println(targetName+" is a constant");
			}
			else {
				HandleCaseTIRAbstractAssignToListStmt abstractAssignToListStmt = 
						new HandleCaseTIRAbstractAssignToListStmt();
				if (ifWhileForBlockNest!=0) 
					stmtSecForIfWhileForBlock.addStatement(abstractAssignToListStmt
							.getFortran(this, node));
				else 
					SubProgram.getStatementSection().addStatement(abstractAssignToListStmt
							.getFortran(this, node));
			}
		}
		else {
			HandleCaseTIRAbstractAssignToListStmt abstractAssignToListStmt = 
					new HandleCaseTIRAbstractAssignToListStmt();
			if (ifWhileForBlockNest!=0) 
				stmtSecForIfWhileForBlock.addStatement(abstractAssignToListStmt
						.getFortran(this, node));
			else 
				SubProgram.getStatementSection().addStatement(abstractAssignToListStmt
						.getFortran(this, node));
		}
	}
	
	@Override
	public void caseTIRIfStmt(TIRIfStmt node) {
		HandleCaseTIRIfStmt ifStmt = new HandleCaseTIRIfStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(ifStmt.getFortran(this, node));
		else 
			SubProgram.getStatementSection().addStatement(ifStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRWhileStmt(TIRWhileStmt node) {
		HandleCaseTIRWhileStmt whileStmt = new HandleCaseTIRWhileStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(whileStmt.getFortran(this, node));
		else 
			SubProgram.getStatementSection().addStatement(whileStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRForStmt(TIRForStmt node) {
		HandleCaseTIRForStmt forStmt = new HandleCaseTIRForStmt();
		if(ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(forStmt.getFortran(this, node));
		else 
			SubProgram.getStatementSection().addStatement(forStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRArrayGetStmt(TIRArrayGetStmt node) {
		HandleCaseTIRArrayGetStmt arrGetStmt = new HandleCaseTIRArrayGetStmt();
		if(ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(arrGetStmt.getFortran(this, node));
		else 
			SubProgram.getStatementSection().addStatement(arrGetStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRArraySetStmt(TIRArraySetStmt node) {
		HandleCaseTIRArraySetStmt arrSetStmt = new HandleCaseTIRArraySetStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(arrSetStmt.getFortran(this, node));
		else 
			SubProgram.getStatementSection().addStatement(arrSetStmt.getFortran(this, node));
	}
}

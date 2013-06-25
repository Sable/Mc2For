package natlab.backends.Fortran.codegen_simplified;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import ast.ASTNode;

import natlab.tame.tir.*;
import natlab.tame.tir.analysis.TIRAbstractNodeCaseHandler;
import natlab.tame.valueanalysis.ValueFlowMap;
import natlab.tame.valueanalysis.ValueSet;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.aggrvalue.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.*;
import natlab.backends.Fortran.codegen_simplified.astCaseHandler.*;

public class FortranCodeASTGenerator extends TIRAbstractNodeCaseHandler {
	static boolean Debug = false;
	int callgraphSize;
	String fileDir;
	ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet;
	public StringBuffer buf;
	public StringBuffer buf2;
	public FortranMapping FortranMapping;
	public String functionName;
	public ArrayList<String> inArgs;
	public ArrayList<String> outRes;
	public boolean isInSubroutine;
	public HashSet<String> inputHasChanged; // used to back up input argument.
	public HashSet<String> arrayConvert;
	public HashMap<String, BasicMatrixValue> tempVarsFortran; // temporary variables generated in Fortran code generation.
	public int ifWhileForBlockNest;
	public StatementSection stmtSecForIfWhileForBlock;
	public SubProgram subProgram;
	public int indentNum;
	public String standardIndent;
	/* 
	 * tmpVarAsArrayIndex, 
	 * K: the name of the temporary vector variable, 
	 * V: the range of those variables.
	 */
	public HashMap<String, ArrayList<String>> tempVectorAsArrayIndex;
	public HashSet<String> tempVarsBeforeF; // temporary variables generated during McSAF or Tamer simplification.
	public HashMap<String, ArrayList<BasicMatrixValue>> forCellArr; // not support nested cell array.
	public ArrayList<String> declaredCell;
	
	private FortranCodeASTGenerator(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, 
			int callgraphSize, 
			int index, String fileDir) {
		this.callgraphSize = callgraphSize;
		this.fileDir = fileDir;
		currentOutSet = analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet();
		FortranMapping = new FortranMapping();
		functionName = "";
		inArgs = new ArrayList<String>();
		outRes = new ArrayList<String>();
		isInSubroutine = false;
		inputHasChanged = new HashSet<String>();
		arrayConvert = new HashSet<String>();
		tempVarsFortran = new HashMap<String,BasicMatrixValue>();
		ifWhileForBlockNest = 0;
		stmtSecForIfWhileForBlock = new StatementSection();
		subProgram = new SubProgram();
		indentNum = 0;
		standardIndent = "   ";
		tempVectorAsArrayIndex = new HashMap<String, ArrayList<String>>();
		tempVarsBeforeF = new HashSet<String>();
		forCellArr = new HashMap<String, ArrayList<BasicMatrixValue>>();
		declaredCell = new ArrayList<String>();
		((TIRNode)analysis.getNodeList().get(index).getFunction().getAst()).tirAnalyze(this);
	}
	
	// ******************************ast node override*************************
	@Override
	@SuppressWarnings("rawtypes")
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
			subProgram.getStatementSection().addStatement(commentStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRAssignLiteralStmt(TIRAssignLiteralStmt node) {
		/*
		 * insert constant variable replacement check.
		 */
		String targetName = node.getTargetName().getVarName();
		if (hasSingleton(targetName) 
				&& getMatrixValue(targetName).hasConstant() 
				&& !outRes.contains(targetName) 
				&& !inArgs.contains(targetName) 
				&& node.getTargetName().tmpVar) {
			tempVarsBeforeF.add(targetName);
			if (Debug) System.out.println(targetName+" is a constant");
		}
		else {
			HandleCaseTIRAssignLiteralStmt assignLiteralStmt = 
					new HandleCaseTIRAssignLiteralStmt();
			if (ifWhileForBlockNest!=0) 
				stmtSecForIfWhileForBlock.addStatement(assignLiteralStmt.getFortran(this, node));
			else 
				subProgram.getStatementSection().addStatement(assignLiteralStmt
						.getFortran(this, node));		
		}
	}
	
	@Override
	public void caseTIRAbstractAssignToVarStmt(TIRAbstractAssignToVarStmt node) {
		/*
		 * insert constant variable replacement check.
		 */
		String targetName = node.getTargetName().getVarName();
		if (!isCell(targetName) && hasSingleton(targetName) 
				&& getMatrixValue(targetName).hasConstant() 
				&& !this.outRes.contains(targetName) 
				&& node.getTargetName().tmpVar) {
			tempVarsBeforeF.add(targetName);
			if (Debug) System.out.println(targetName+" is a constant");
		}
		else {
			HandleCaseTIRAbstractAssignToVarStmt abstractAssignToVarStmt = 
					new HandleCaseTIRAbstractAssignToVarStmt();
			if (ifWhileForBlockNest!=0) 
				stmtSecForIfWhileForBlock.addStatement(abstractAssignToVarStmt
						.getFortran(this, node));
			else 
				subProgram.getStatementSection().addStatement(abstractAssignToVarStmt
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
		 * And because node.getTargetName().getVarName() can only return 
		 * the first variable, we need use node.getTargets().asNameList().
		 */
		if (HandleCaseTIRAbstractAssignToListStmt.getRHSCaseNumber(this, node)!=6) {
			String targetName = node.getTargetName().getVarName();
			if(!isCell(targetName) && hasSingleton(targetName) 
					&& getMatrixValue(targetName).hasConstant() 
					&& !outRes.contains(targetName) 
					&& node.getTargetName().tmpVar) {
				// can a tmp var be tmp and constant scalar at the same time for this case?
				tempVarsBeforeF.add(targetName);
				if (Debug) System.out.println(targetName+" is a constant");
			}
			else {
				HandleCaseTIRAbstractAssignToListStmt abstractAssignToListStmt = 
						new HandleCaseTIRAbstractAssignToListStmt();
				if (ifWhileForBlockNest!=0) 
					stmtSecForIfWhileForBlock.addStatement(abstractAssignToListStmt
							.getFortran(this, node));
				else subProgram.getStatementSection().addStatement(
						abstractAssignToListStmt.getFortran(this, node));
			}
		}
		else {
			HandleCaseTIRAbstractAssignToListStmt abstractAssignToListStmt = 
					new HandleCaseTIRAbstractAssignToListStmt();
			if (ifWhileForBlockNest!=0) 
				stmtSecForIfWhileForBlock.addStatement(abstractAssignToListStmt
						.getFortran(this, node));
			else subProgram.getStatementSection().addStatement(
					abstractAssignToListStmt.getFortran(this, node));
		}
	}
	
	@Override
	public void caseTIRIfStmt(TIRIfStmt node) {
		HandleCaseTIRIfStmt ifStmt = new HandleCaseTIRIfStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(ifStmt.getFortran(this, node));
		else 
			subProgram.getStatementSection().addStatement(ifStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRWhileStmt(TIRWhileStmt node) {
		HandleCaseTIRWhileStmt whileStmt = new HandleCaseTIRWhileStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(whileStmt.getFortran(this, node));
		else 
			subProgram.getStatementSection().addStatement(whileStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRForStmt(TIRForStmt node) {
		HandleCaseTIRForStmt forStmt = new HandleCaseTIRForStmt();
		if(ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(forStmt.getFortran(this, node));
		else 
			subProgram.getStatementSection().addStatement(forStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRArrayGetStmt(TIRArrayGetStmt node) {
		HandleCaseTIRArrayGetStmt arrGetStmt = new HandleCaseTIRArrayGetStmt();
		if(ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(arrGetStmt.getFortran(this, node));
		else 
			subProgram.getStatementSection().addStatement(arrGetStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRArraySetStmt(TIRArraySetStmt node) {
		HandleCaseTIRArraySetStmt arrSetStmt = new HandleCaseTIRArraySetStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(arrSetStmt.getFortran(this, node));
		else 
			subProgram.getStatementSection().addStatement(arrSetStmt.getFortran(this, node));
	}
	
	// ******************************helper methods****************************
	public static SubProgram FortranProgramGen(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, 
			int callgraphSize, 
			int index, String fileDir) {
		return new FortranCodeASTGenerator(analysis, callgraphSize, index, fileDir).subProgram;
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
		if (variable.indexOf("_copy")!=-1) {
			int index = variable.indexOf("_copy");
			String originalVar = variable.substring(0, index);
			return (BasicMatrixValue) currentOutSet.get(originalVar).getSingleton();
		}
		return (BasicMatrixValue) currentOutSet.get(variable).getSingleton();
	}
	
	public boolean isCell(String variable) {
		if (currentOutSet.get(variable).getSingleton() instanceof CellValue) {
			return true;
		}
		else return false;
	}
	
	public boolean hasSingleton(String variable) {
		if (currentOutSet.get(variable).getSingleton()==null) return false;
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public ValueSet getValueSet(String variable) {
		return currentOutSet.get(variable);
	}
}

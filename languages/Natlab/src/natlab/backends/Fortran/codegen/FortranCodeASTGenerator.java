package natlab.backends.Fortran.codegen;

import java.util.ArrayList;
import java.util.HashMap;

import ast.ASTNode;

import natlab.tame.tir.TIRAbstractAssignToListStmt;
import natlab.tame.tir.TIRAbstractAssignToVarStmt;
import natlab.tame.tir.TIRArrayGetStmt;
import natlab.tame.tir.TIRArraySetStmt;
import natlab.tame.tir.TIRAssignLiteralStmt;
import natlab.tame.tir.TIRCommentStmt;
import natlab.tame.tir.TIRForStmt;
import natlab.tame.tir.TIRFunction;
import natlab.tame.tir.TIRIfStmt;
import natlab.tame.tir.TIRNode;
import natlab.tame.tir.TIRWhileStmt;
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
	
	public boolean isSubroutine;//this boolean value help the compiler to distinguish subroutine with function.
	public HashMap<String, BasicMatrixValue> tmpVariables;//to store those temporary variables which are used in Fortran code generation.
	public boolean isIfWhileForBlock;                                                        //The key is name, and the value is its shape.
	public StatementSection stmtSecForIfWhileForBlock;
	public SubProgram SubProgram;
	public int indentNum;
	public String indent;
	static boolean Debug = false;
	
	public FortranCodeASTGenerator(ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir){
		this.analysis = analysis;
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
		this.isSubroutine = false;
		this.tmpVariables = new HashMap<String,BasicMatrixValue>();
		this.isIfWhileForBlock = false;
		this.stmtSecForIfWhileForBlock = new StatementSection();
		this.SubProgram = new SubProgram();
		this.indentNum = 0;
		this.indent = "   ";
		((TIRNode)analysis.getNodeList().get(index).getAnalysis().getTree()).tirAnalyze(this);
	}
	
	public static SubProgram FortranProgramGen(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir){
		return new FortranCodeASTGenerator(analysis, callgraphSize, index, fileDir).SubProgram;
	}

	public void iterateStatements(ast.List<ast.Stmt> stmts){
		for(ast.Stmt stmt : stmts){
			((TIRNode)stmt).tirAnalyze(this);
		}
	}
	
	@Override
	public void caseASTNode(ASTNode node){
		
	}
	
	@Override
	public void caseTIRFunction(TIRFunction node){
		HandleCaseTIRFunction functionStmt = new HandleCaseTIRFunction();
		functionStmt.getFortran(this, node);
	}
	
	@Override
	public void caseTIRCommentStmt(TIRCommentStmt node){
		HandleCaseTIRCommentStmt commentStmt = new HandleCaseTIRCommentStmt();
		this.SubProgram.getStatementSection().addStatement(commentStmt.getFortran(this, node));
	}
	
	@Override
	public void caseTIRAssignLiteralStmt(TIRAssignLiteralStmt node){
		/**
		 * insert constant variable replacement check.
		 */
		if(((BasicMatrixValue)(this.analysis.getNodeList().get(this.index).getAnalysis().getCurrentOutSet().
				get(node.getTargetName().getVarName()).getSingleton())).isConstant()
				&&(this.outRes.contains(node.getTargetName().getVarName())==false)){
			if (Debug) System.out.println(node.getTargetName().getVarName()+" is a constant");
		}
		else{
			HandleCaseTIRAssignLiteralStmt assignLiteralStmt = new HandleCaseTIRAssignLiteralStmt();
			if(this.isIfWhileForBlock){
				this.stmtSecForIfWhileForBlock.addStatement(assignLiteralStmt.getFortran(this, node));
			}
			else{
				this.SubProgram.getStatementSection().addStatement(assignLiteralStmt.getFortran(this, node));			
			}			
		}
	}
	
	@Override
	public void caseTIRAbstractAssignToVarStmt(TIRAbstractAssignToVarStmt node){
		/**
		 * insert constant variable replacement check.
		 */
		if(((BasicMatrixValue)(this.analysis.getNodeList().get(this.index).getAnalysis().getCurrentOutSet().
				get(node.getTargetName().getVarName()).getSingleton())).isConstant()
				&&(this.outRes.contains(node.getTargetName().getVarName())==false)){
			if (Debug) System.out.println(node.getTargetName().getVarName()+" is a constant");
		}
		else{
			HandleCaseTIRAbstractAssignToVarStmt abstractAssignToVarStmt = new HandleCaseTIRAbstractAssignToVarStmt();
			if(this.isIfWhileForBlock){
				this.stmtSecForIfWhileForBlock.addStatement(abstractAssignToVarStmt.getFortran(this, node));
			}
			else{
				this.SubProgram.getStatementSection().addStatement(abstractAssignToVarStmt.getFortran(this, node));
			}
		}
	}

	@Override
	public void caseTIRAbstractAssignToListStmt(TIRAbstractAssignToListStmt node){
		/**
		 * insert constant variable replacement check.
		 * p.s. need to check whether the expression is io expression,
		 * because io expression doesn't have target variable
		 */
		/**
		 * one more problem, for this case, the lhs is a list of variable.
		 * And because node.getTargetName().getVarName() can only return the first variable,
		 * we need use node.getTargets().asNameList().
		 */
		if((HandleCaseTIRAbstractAssignToListStmt.makeExpression(this, node) instanceof IOOperationExpr)==false){
			if(((BasicMatrixValue)(this.analysis.getNodeList().get(this.index).getAnalysis().getCurrentOutSet().
					get(node.getTargetName().getVarName()).getSingleton())).isConstant()
					&&(this.outRes.contains(node.getTargetName().getVarName())==false)){
				if (Debug) System.out.println(node.getTargetName().getVarName()+" is a constant");
			}
			else{
				HandleCaseTIRAbstractAssignToListStmt abstractAssignToListStmt = new HandleCaseTIRAbstractAssignToListStmt();
				if(this.isIfWhileForBlock){
					this.stmtSecForIfWhileForBlock.addStatement(abstractAssignToListStmt.getFortran(this, node));
				}
				else{
					this.SubProgram.getStatementSection().addStatement(abstractAssignToListStmt.getFortran(this, node));
				}
			}
		}
		else{
			HandleCaseTIRAbstractAssignToListStmt abstractAssignToListStmt = new HandleCaseTIRAbstractAssignToListStmt();
			if(this.isIfWhileForBlock){
				this.stmtSecForIfWhileForBlock.addStatement(abstractAssignToListStmt.getFortran(this, node));
			}
			else{
				this.SubProgram.getStatementSection().addStatement(abstractAssignToListStmt.getFortran(this, node));
			}
		}
	}
	
	@Override
	public void caseTIRIfStmt(TIRIfStmt node){
		HandleCaseTIRIfStmt ifStmt = new HandleCaseTIRIfStmt();
		if(this.isIfWhileForBlock){
			this.stmtSecForIfWhileForBlock.addStatement(ifStmt.getFortran(this, node));
		}
		else{
			this.SubProgram.getStatementSection().addStatement(ifStmt.getFortran(this, node));
		}
	}
	
	@Override
	public void caseTIRWhileStmt(TIRWhileStmt node){
		HandleCaseTIRWhileStmt whileStmt = new HandleCaseTIRWhileStmt();
		if(this.isIfWhileForBlock){
			this.stmtSecForIfWhileForBlock.addStatement(whileStmt.getFortran(this, node));
		}
		else{
			this.SubProgram.getStatementSection().addStatement(whileStmt.getFortran(this, node));
		}
	}
	
	@Override
	public void caseTIRForStmt(TIRForStmt node){
		HandleCaseTIRForStmt forStmt = new HandleCaseTIRForStmt();
		if(this.isIfWhileForBlock){
			this.stmtSecForIfWhileForBlock.addStatement(forStmt.getFortran(this, node));
		}
		else{
			this.SubProgram.getStatementSection().addStatement(forStmt.getFortran(this, node));
		}
	}
	
	@Override
	public void caseTIRArrayGetStmt(TIRArrayGetStmt node){
		HandleCaseTIRArrayGetStmt arrGetStmt = new HandleCaseTIRArrayGetStmt();
		if(this.isIfWhileForBlock){
			this.stmtSecForIfWhileForBlock.addStatement(arrGetStmt.getFortran(this, node));
		}
		else{
			this.SubProgram.getStatementSection().addStatement(arrGetStmt.getFortran(this, node));
		}
	}
	
	@Override
	public void caseTIRArraySetStmt(TIRArraySetStmt node){
		HandleCaseTIRArraySetStmt arrSetStmt = new HandleCaseTIRArraySetStmt();
		if(this.isIfWhileForBlock){
			this.stmtSecForIfWhileForBlock.addStatement(arrSetStmt.getFortran(this, node));
		}
		else{
			this.SubProgram.getStatementSection().addStatement(arrSetStmt.getFortran(this, node));
		}
	}
}

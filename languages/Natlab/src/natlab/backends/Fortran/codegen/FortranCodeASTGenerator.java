package natlab.backends.Fortran.codegen;

import java.util.ArrayList;
import java.util.HashMap;

import ast.ASTNode;

import natlab.tame.tir.TIRAssignLiteralStmt;
import natlab.tame.tir.TIRFunction;
import natlab.tame.tir.TIRNode;
import natlab.tame.tir.analysis.TIRAbstractNodeCaseHandler;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.backends.Fortran.codegen.caseHandler.HandleCaseTIRAssignLiteralStmt;
import natlab.backends.Fortran.codegen.caseHandler.HandleCaseTIRFunction;

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
		HandleCaseTIRFunction functionStmt = new HandleCaseTIRFunction();
		functionStmt.getFortran(this, node);
	}
	
	@Override
	public void caseTIRAssignLiteralStmt(TIRAssignLiteralStmt node){
		HandleCaseTIRAssignLiteralStmt assignLiteralStmt = new HandleCaseTIRAssignLiteralStmt();
		assignLiteralStmt.getFortran(this, node);
	}
	
	/**********************HELPER METHODS***********************************/
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

package natlab.backends.Fortran.codegen;

import java.util.ArrayList;
import java.util.HashMap;

import ast.ASTNode;

import natlab.tame.tir.TIRNode;
import natlab.tame.tir.analysis.TIRAbstractNodeCaseHandler;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.backends.Fortran.codegen.FortranAST.*;

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
	public Program FortranProgram;
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
		this.FortranProgram = new Program();
		((TIRNode)analysis.getNodeList().get(index).getAnalysis().getTree()).tirAnalyze(this);
	}
	
	public Program FortranProgramGen(
			Program FortranProgram, ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir){
		return new FortranCodeASTGenerator(analysis, callgraphSize, index, fileDir).FortranProgram;
	}
	
	@Override
	public void caseASTNode(ASTNode node){
		
	}
	
}

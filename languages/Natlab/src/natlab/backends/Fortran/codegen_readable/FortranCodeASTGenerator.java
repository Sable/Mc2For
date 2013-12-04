package natlab.backends.Fortran.codegen_readable;

import java.util.*;

import ast.ASTNode;
import ast.List;
import ast.*;
import natlab.tame.tir.TIRCommentStmt;

import nodecases.AbstractNodeCaseHandler;
import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.valueanalysis.ValueFlowMap;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.aggrvalue.CellValue;
import natlab.tame.tamerplus.analysis.AnalysisEngine;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.*;
import natlab.tame.valueanalysis.components.isComplex.isComplexInfoFactory;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.*;
import natlab.backends.Fortran.codegen_readable.astCaseHandler.*;

public class FortranCodeASTGenerator extends AbstractNodeCaseHandler {
	static boolean Debug = false;
	// this currentOutSet is the out set at the end point of the program.
	ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet;
	public Set<String> remainingVars;
	public String entryPointFile;
	public Set<String> userDefinedFunctions;
	public AnalysisEngine analysisEngine;
	public boolean nocheck;
	public Set<String> allSubprograms;
	public Subprogram subprogram;
	public StringBuffer sb;
	public StringBuffer sbForRuntimeInline;
	public FortranMapping fortranMapping;
	public String functionName;
	public ArrayList<String> inArgs;
	public ArrayList<String> outRes;
	public boolean isInSubroutine;
	// used to back up input argument.
	public Set<String> inputHasChanged;
	public int ifWhileForBlockNest;
	public StatementSection stmtSecForIfWhileForBlock;
	public int indentNum;
	public String standardIndent;
	// ParameterizedExpr can be array index or function call, array index can be nested.
	public int insideArray;
	// public boolean isArray;
	public boolean randnFlag;
	public boolean leftOfAssign;
	public boolean rightOfAssign;
	public boolean storageAlloc;
	public boolean rhsArrayAssign;
	// temporary variables generated in Fortran code generation.
	public Map<String, BasicMatrixValue> fotranTemporaries;
	public boolean mustBeInt;
	public Set<String> forceToInt;
	public Set<String> inputsUsed;
	public Set<String> backupTempArrays;
	// not support nested cell array.
	public Map<String, ArrayList<BasicMatrixValue>> forCellArr;
	public ArrayList<String> declaredCell;
	
	/**
	 * private constructor, called by helper method generateFortran.
	 * @param fNode
	 * @param currentOutSet
	 * @param remainingVars
	 */
	private FortranCodeASTGenerator(
			Function fNode, 
			ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet, 
			Set<String> remainingVars, 
			String entryPointFile, 
			Set<String> userDefinedFunctions, 
			AnalysisEngine analysisEngine, 
			boolean nocheck) 
	{
		this.currentOutSet = currentOutSet;
		this.remainingVars = remainingVars;
		this.entryPointFile = entryPointFile;
		this.nocheck = nocheck;
		this.userDefinedFunctions = userDefinedFunctions;
		this.analysisEngine = analysisEngine;
		allSubprograms = new HashSet<String>();
		subprogram = new Subprogram();
		sb = new StringBuffer();
		sbForRuntimeInline = new StringBuffer();
		fortranMapping = new FortranMapping();
		functionName = "";
		inArgs = new ArrayList<String>();
		outRes = new ArrayList<String>();
		isInSubroutine = false;
		inputHasChanged = new HashSet<String>();
		ifWhileForBlockNest = 0;
		stmtSecForIfWhileForBlock = new StatementSection();
		indentNum = 0;
		standardIndent = "   ";
		insideArray = 0;
		// isArray = false;
		randnFlag = false;
		leftOfAssign = false;
		rightOfAssign = false;
		storageAlloc = false;
		rhsArrayAssign = false;
		fotranTemporaries = new HashMap<String,BasicMatrixValue>();
		mustBeInt = false;
		forceToInt = new HashSet<String>();
		inputsUsed = new HashSet<String>();
		backupTempArrays = new HashSet<String>();
		forCellArr = new HashMap<String, ArrayList<BasicMatrixValue>>();
		declaredCell = new ArrayList<String>();
		fNode.analyze(this);
	}
	// ******************************ast node override*************************
	@Override
	@SuppressWarnings("rawtypes")
	public void caseASTNode(ASTNode node) {}

	@Override
	public void caseFunction(Function node)	{
		HandleCaseFunction functionStmt = new HandleCaseFunction();
		functionStmt.getFortran(this, node);
    }
	
	@Override
	/**
	 * for comment statements.
	 */
	public void caseEmptyStmt(EmptyStmt node) {
		if (ifWhileForBlockNest != 0) {
			if (!node.getPrettyPrinted().equals("")) {
				String comment = node.getPrettyPrinted();
				if (Debug) System.out.println(comment);
				FCommentStmt fComment = new FCommentStmt();
				String indent = "";
				for (int i = 0; i < indentNum; i++) {
					indent = indent + standardIndent;
				}
				fComment.setIndent(indent);
				fComment.setFComment(comment.subSequence(1, comment.length()).toString());
				stmtSecForIfWhileForBlock.addStatement(fComment);
			}
		}
		else {
			if (!node.getPrettyPrinted().equals("")) {
				String comment = node.getPrettyPrinted();
				if (Debug) System.out.println(comment);
				FCommentStmt fComment = new FCommentStmt();
				String indent = "";
				for (int i = 0; i < indentNum; i++) {
					indent = indent + standardIndent;
				}
				fComment.setIndent(indent);
				fComment.setFComment(comment.subSequence(1, comment.length()).toString());
				subprogram.getStatementSection().addStatement(fComment);
			}
		}
	}
	
	@Override
	public void caseAssignStmt(AssignStmt node)	{
		if (ifWhileForBlockNest != 0) {
			FAssignStmt fAssignStmt = new FAssignStmt();
			String indent = "";
			for (int i = 0; i < indentNum; i++) {
				indent = indent + standardIndent;
			}
			fAssignStmt.setIndent(indent);
			/*
			 * translate matlab function with more than 
			 * one returns to subroutines in fortran.
			 */
			if (node.getLHS() instanceof MatrixExpr) {
				MatrixExpr lhsMatrix = (MatrixExpr)node.getLHS();
				if (lhsMatrix.getChild(0) instanceof List) {
					List lhsList = (List)lhsMatrix.getChild(0);
					if (lhsList.getChild(0) instanceof Row) {
						Row lhsRow = (Row)lhsList.getChild(0);
						if (lhsRow.getChild(0).getNumChild() > 1 || userDefinedFunctions.contains(
								(((NameExpr)((ParameterizedExpr)node.getRHS()).getChild(0)).getName().getID()))) {
							FSubroutines fSubroutines = new FSubroutines();
							fSubroutines.setIndent(indent);
							node.getRHS().analyze(this);
							sb.replace(sb.length()-1, sb.length(), "");
							sb.append(", ");
							for (int i = 0; i < lhsRow.getChild(0).getNumChild(); i++) {
								if (outRes.contains(lhsRow.getChild(0).getChild(i).getNodeString())) {
									sb.append(functionName);
								}
								else {
									sb.append(lhsRow.getChild(0).getChild(i).getNodeString());
								}
								if (i < lhsRow.getChild(0).getNumChild() - 1) {
									sb.append(", ");
								}
							}
							sb.append(")");
							if (Debug) System.out.println(sb);
							fSubroutines.setFunctionCall(sb.toString());
							sb.setLength(0);
							stmtSecForIfWhileForBlock.addStatement(fSubroutines);
							return;
						}
					}
				}
			}
			rightOfAssign = true;
			node.getRHS().analyze(this);
			fAssignStmt.setFRHS(sb.toString());
			sb.setLength(0);
			rightOfAssign = false;
			
			if (node.getRHS() instanceof ParameterizedExpr 
					&& analysisEngine.getTemporaryVariablesRemovalAnalysis()
						.getExprToTempVarTable().get(node.getRHS()) == null) {
				// this means the rhs is either array indexing or function call
				if (remainingVars.contains(
						((NameExpr)node.getRHS().getChild(0)).getName().getID())) {
					// rhs is array index
					String name = ((NameExpr)node.getRHS().getChild(0)).getName().getID();
					rhsArrayAssign = true;
				}
			}
			
			leftOfAssign = true;
			node.getLHS().analyze(this);
			leftOfAssign = false;
			rhsArrayAssign = false;
			String lhsName = sb.toString();
			if (randnFlag) {
				FSubroutines fSubroutines = new FSubroutines();
				fSubroutines.setIndent(indent);
				fSubroutines.setFunctionCall("RANDOM_NUMBER("+lhsName+")");
				randnFlag = false;
				sb.setLength(0);
				stmtSecForIfWhileForBlock.addStatement(fSubroutines);
			}
			else {
				// if there is runtime abc or allocate, add here.
				if (sbForRuntimeInline.length() != 0 && !storageAlloc) {
					RuntimeAllocate runtimeInline = new RuntimeAllocate();
					runtimeInline.setBlock(sbForRuntimeInline.toString());
					fAssignStmt.setRuntimeAllocate(runtimeInline);
					sbForRuntimeInline.setLength(0);
				}
				else if (lhsName.indexOf("(") == -1 
						&& !getMatrixValue(lhsName).getShape().isConstant() 
						&& storageAlloc) {
					sbForRuntimeInline.setLength(0);
					RuntimeAllocate runtimeInline = new RuntimeAllocate();
					sbForRuntimeInline.append("IF (.NOT. ALLOCATED(" 
							+ lhsName
							+ ")) THEN\n" + getSomeIndent(1));
					runtimeInline.setBlock(sbForRuntimeInline.toString());
					fAssignStmt.setRuntimeAllocate(runtimeInline);
					sbForRuntimeInline.setLength(0);
				}
				fAssignStmt.setFLHS(lhsName);
				sb.setLength(0);
				if (lhsName.indexOf("(") == -1 
						&& !getMatrixValue(lhsName).getShape().isConstant() 
						&& storageAlloc) {
					ExtraInlined extraInlined = new ExtraInlined();
					extraInlined.setBlock("END IF");
					fAssignStmt.setExtraInlined(extraInlined);
					storageAlloc = false;
				}
				stmtSecForIfWhileForBlock.addStatement(fAssignStmt);
			}
		}
		else {
			FAssignStmt fAssignStmt = new FAssignStmt();
			String indent = "";
			for (int i = 0; i < indentNum; i++) {
				indent = indent + standardIndent;
			}
			fAssignStmt.setIndent(indent);
			/*
			 * translate matlab function with more than 
			 * one returns to subroutines in fortran.
			 */
			if (node.getLHS() instanceof MatrixExpr) {
				MatrixExpr lhsMatrix = (MatrixExpr)node.getLHS();
				if (lhsMatrix.getChild(0) instanceof List) {
					List lhsList = (List)lhsMatrix.getChild(0);
					if (lhsList.getChild(0) instanceof Row) {
						Row lhsRow = (Row)lhsList.getChild(0);
						if (lhsRow.getChild(0).getNumChild() > 1 || userDefinedFunctions.contains(
								(((NameExpr)((ParameterizedExpr)node.getRHS()).getChild(0)).getName().getID()))) {
							FSubroutines fSubroutines = new FSubroutines();
							fSubroutines.setIndent(indent);
							node.getRHS().analyze(this);
							sb.replace(sb.length()-1, sb.length(), "");
							sb.append(", ");
							for (int i = 0; i < lhsRow.getChild(0).getNumChild(); i++) {
								if (outRes.contains(lhsRow.getChild(0).getChild(i).getNodeString())) {
									sb.append(functionName);
								}
								else {
									sb.append(lhsRow.getChild(0).getChild(i).getNodeString());
								}
								if (i < lhsRow.getChild(0).getNumChild() - 1) {
									sb.append(", ");
								}
							}
							sb.append(")");
							if (Debug) System.out.println(sb);
							
							fSubroutines.setFunctionCall(sb.toString());
							sb.setLength(0);
							subprogram.getStatementSection().addStatement(fSubroutines);
							return;
						}
					}
				}
			}
			rightOfAssign = true;
			node.getRHS().analyze(this);
			fAssignStmt.setFRHS(sb.toString());
			sb.setLength(0);
			rightOfAssign = false;
			
			if (node.getRHS() instanceof ParameterizedExpr 
					&& analysisEngine.getTemporaryVariablesRemovalAnalysis()
						.getExprToTempVarTable().get(node.getRHS()) == null) {
				// this means the rhs is either array indexing or function call
				if (remainingVars.contains(
						((NameExpr)node.getRHS().getChild(0)).getName().getID())) {
					// rhs is array index
					String name = ((NameExpr)node.getRHS().getChild(0)).getName().getID();
					rhsArrayAssign = true;
				}
			}
			
			leftOfAssign = true;
			node.getLHS().analyze(this);
			leftOfAssign = false;
			rhsArrayAssign = false;
			String lhsName = sb.toString();
			if (randnFlag) {
				FSubroutines fSubroutines = new FSubroutines();
				fSubroutines.setIndent(indent);
				fSubroutines.setFunctionCall("RANDOM_NUMBER("+lhsName+")");
				randnFlag = false;
				sb.setLength(0);
				if (lhsName.indexOf("(") == -1 
						&& !getMatrixValue(lhsName).getShape().isConstant()) {
					// need to inline run-time allocate.
					insideArray++; // this is a hack.
					node.getRHS().getChild(1).analyze(this);
					insideArray--;
					StringBuffer rtBuffer = new StringBuffer();
					rtBuffer.append(indent + "IF ((.NOT. ALLOCATED(" + lhsName + "))) THEN\n");
					rtBuffer.append(indent + standardIndent + "ALLOCATE(" + lhsName + "(" + sb.toString() + "))\n");
					rtBuffer.append(indent + "END IF\n");
					RuntimeAllocate rtAllocate = new RuntimeAllocate();
					rtAllocate.setBlock(rtBuffer.toString());
					fSubroutines.setRuntimeAllocate(rtAllocate);
					sb.setLength(0);
				}
				subprogram.getStatementSection().addStatement(fSubroutines);
			}
			else {
				// if there is runtime abc or allocate, add here.
				if (sbForRuntimeInline.length() != 0 && !storageAlloc) {
					RuntimeAllocate runtimeInline = new RuntimeAllocate();
					runtimeInline.setBlock(sbForRuntimeInline.toString());
					fAssignStmt.setRuntimeAllocate(runtimeInline);
					sbForRuntimeInline.setLength(0);
				}
				else if (lhsName.indexOf("(") == -1 
						&& !getMatrixValue(lhsName).getShape().isConstant() 
						&& storageAlloc) {
					sbForRuntimeInline.setLength(0);
					RuntimeAllocate runtimeInline = new RuntimeAllocate();
					sbForRuntimeInline.append("IF (.NOT. ALLOCATED(" 
							+ lhsName
							+ ")) THEN\n" + getSomeIndent(1));
					runtimeInline.setBlock(sbForRuntimeInline.toString());
					fAssignStmt.setRuntimeAllocate(runtimeInline);
					sbForRuntimeInline.setLength(0);
				}
				fAssignStmt.setFLHS(lhsName);
				sb.setLength(0);
				if (lhsName.indexOf("(") == -1 
						&& !getMatrixValue(lhsName).getShape().isConstant() 
						&& storageAlloc) {
					ExtraInlined extraInlined = new ExtraInlined();
					extraInlined.setBlock("END IF");
					fAssignStmt.setExtraInlined(extraInlined);
					storageAlloc = false;
				}
				subprogram.getStatementSection().addStatement(fAssignStmt);
			}	
		}
	}
	
	@Override
	/**
	 * in the readable version of fortran code generation, this is 
	 * one of the most important case handler and also the most 
	 * different part from the simplified version of fortran code 
	 * generation, together with the caseAssignStmt, they two cover 
	 * all the different assignments in simplified version, like 
	 * assignToVar, assignToList and so on.
	 */
	public void caseParameterizedExpr(ParameterizedExpr node) {
		// NameExpr(List), NameExpr is the first child, List is the second child.
		if (Debug) System.out.println("parameterized expr: " 
				+ node+", has "+node.getNumChild()+" children.");
		if (node.getChild(0) instanceof NameExpr) {
			String name = ((NameExpr) node.getChild(0)).getName().getID();
			if (remainingVars.contains(name)) {
				if (Debug) System.out.println("this is an array index.");
				/*
				 * TODO note that, need to find a way to distinguish the array 
				 * indexing is on which side. the array indexing on left hand 
				 * side must be inlined with runtime abc and reallocation, while 
				 * the runtime abc of the array indexing on right hand side is 
				 * optional.
				 */
				if (!getMatrixValue(name).getShape().isConstant() 
						&& rightOfAssign && !nocheck) {
					/*
					 * TODO add runtime abc.
					 */
					if (Debug) System.out.println("unknown shape array indexing " +
							"on right hand side, need run-time abc.");
					sbForRuntimeInline.append("! need run-time abc.\n");
				}
				else if (!getMatrixValue(name).getShape().isConstant() 
						&& leftOfAssign) {
					/*
					 * add runtime abc and reallocation, also add a back up 
					 * variable for the indexed array variable.
					 */
					if (Debug) System.out.println("unknown shape array indexing " +
							"on left hand side, need run-time abc and reallocation.");
					sbForRuntimeInline.append("! need run-time alloc/abc and realloc.\n");
					backupTempArrays.add(name);
					/*
					 * the name of array is node.getChild(0), 
					 * the index of array is node.getChild(1).getChild(i).
					 */
					insideArray++;
					int indexNum = node.getChild(1).getNumChild();
					for (int i = 0; i < indexNum; i++) {
						sbForRuntimeInline.append(getSomeIndent(0) + name + "_d" + (i+1) 
								+ " = SIZE(" + name + ", " + (i+1) + ");\n");
					}
					sbForRuntimeInline.append(getSomeIndent(0) + "IF (");
					for (int i = 0; i < indexNum; i++) {
						node.getChild(1).getChild(i).analyze(this);
						String indexCurrent = sb.toString();
						sb.setLength(0);
						if (!indexCurrent.equals(":")) {
							indexCurrent = indexCurrent.substring(indexCurrent.indexOf(":") + 1);
							try {
								sbForRuntimeInline.append(Integer.parseInt(indexCurrent) + " > " 
										+ name + "_d" + (i+1));
							} catch (Exception e) {
								sbForRuntimeInline.append("INT(" + indexCurrent + ") > " 
										+ name + "_d" + (i+1));
							}
						}
						if (i + 1 < indexNum 
								&& !node.getChild(1).getChild(i+1).getPrettyPrinted().equals(":")) {
							sbForRuntimeInline.append(" .OR. ");
						}
					}
					sbForRuntimeInline.append(") THEN\n");
					sbForRuntimeInline.append(getSomeIndent(1) + "IF (ALLOCATED(" 
							+ name + "_bk)) THEN\n");
					sbForRuntimeInline.append(getSomeIndent(2) + "DEALLOCATE(" + name 
							+ "_bk" + ");\n");
					sbForRuntimeInline.append(getSomeIndent(1) + "END IF\n");
					sbForRuntimeInline.append(getSomeIndent(1) + "ALLOCATE(" + name + "_bk(");
					for (int i = 0; i < indexNum; i++) {
						sbForRuntimeInline.append(name + "_d" + (i+1));
						if (i + 1 < indexNum) {
							sbForRuntimeInline.append(", ");
						}
					}
					sbForRuntimeInline.append("));\n");
					sbForRuntimeInline.append(getSomeIndent(1) + name + "_bk = " + name + ";\n");
					sbForRuntimeInline.append(getSomeIndent(1) + "DEALLOCATE(" + name + ");\n");
					for (int i = 0; i < indexNum; i++) {
						node.getChild(1).getChild(i).analyze(this);
						String indexCurrent = sb.toString();
						sb.setLength(0);
						if (!indexCurrent.equals(":")) {
							indexCurrent = indexCurrent.substring(indexCurrent.indexOf(":") + 1);
							try {
								sbForRuntimeInline.append(getSomeIndent(1) + name 
										+ "_d" + (i+1) + "max = MAX(" + name + "_d" + (i+1) + ", " 
										+ Integer.parseInt(indexCurrent) + ");\n");
							} catch (Exception e) {
								sbForRuntimeInline.append(getSomeIndent(1) + name 
										+ "_d" + (i+1) + "max = MAX(" + name + "_d" + (i+1) + ", INT(" 
										+ indexCurrent + "));\n");
							}
						}
						else {
							sbForRuntimeInline.append(getSomeIndent(1) + name 
									+ "_d" + (i+1) + "max = " + name + "_d" + (i+1) + ";\n");
						}
					}
					sbForRuntimeInline.append(getSomeIndent(1) + "ALLOCATE(" + name + "(");
					for (int i = 0; i < indexNum; i++) {
						sbForRuntimeInline.append(name + "_d" + (i+1) + "max");
						if (i + 1 < indexNum) {
							sbForRuntimeInline.append(", ");
						}
					}
					sbForRuntimeInline.append("));\n");
					sbForRuntimeInline.append(getSomeIndent(1) + name + "(");
					for (int i = 0; i < indexNum; i++) {
						sbForRuntimeInline.append("1:" + name + "_d" + (i+1));
						if (i + 1 < indexNum) {
							sbForRuntimeInline.append(", ");
						}
					}
					sbForRuntimeInline.append(") = " + name + "_bk;\n");
					sbForRuntimeInline.append(getSomeIndent(0) + "END IF\n");
					sbForRuntimeInline.append(getSomeIndent(0) + "!\n");
					for (int i = 0; i < indexNum; i++) {
						fotranTemporaries.put(name + "_d" + (i+1), new BasicMatrixValue(
								null, 
								PrimitiveClassReference.INT32, 
								new ShapeFactory<AggrValue<BasicMatrixValue>>().getScalarShape(), 
								null, 
								new isComplexInfoFactory<AggrValue<BasicMatrixValue>>()
								.newisComplexInfoFromStr("REAL")
								));
						fotranTemporaries.put(name + "_d" + (i+1) + "max", new BasicMatrixValue(
								null, 
								PrimitiveClassReference.INT32, 
								new ShapeFactory<AggrValue<BasicMatrixValue>>().getScalarShape(), 
								null, 
								new isComplexInfoFactory<AggrValue<BasicMatrixValue>>()
								.newisComplexInfoFromStr("REAL")
								));
					}
					insideArray--;
				}
				/*
				 * since we already know the name is the array's name, we 
				 * don't need to call analyze on node.getChild(0), and 
				 * this also separate the cases of the array have indices 
				 * and the arrays don't have indices. (the array has a 
				 * index list won't call caseNameExpr, and in caseNameExpr 
				 * we can add hacks to convert 2 ranks to 1 rank in Fortran.)
				 */
				sb.append(name + "(");
				insideArray++;
				/*
				 * add rigorous array indexing transformation.
				 */
				if (node.getChild(1) instanceof List 
						&& getMatrixValue(name).getShape().getDimensions().size() 
							!= ((List)node.getChild(1)).getNumChild() 
						&& getMatrixValue(name).getShape().isColVector()) {
					node.getChild(1).analyze(this);
					/*
					 * TODO this is a hack for n-by-1 vector linear indexing,
					 * won't work for multidimensional matrix linear indexing.
					 */ 
					sb.append(", 1");
				}
				else if (node.getChild(1) instanceof List 
						&& getMatrixValue(name).getShape().getDimensions().size() 
							!= ((List)node.getChild(1)).getNumChild() 
						&& getMatrixValue(name).getShape().isRowVector()) {
					/*
					 * TODO this is a hack for 1-by-n vector linear indexing,
					 * won't work for multidimensional matrix linear indexing.
					 */
					sb.append("1, ");
					node.getChild(1).analyze(this);
				}
				else {
					node.getChild(1).analyze(this);
				}
				insideArray--;
				sb.append(")");
			}
			else {
				/*
				 * for those numerous matlab built-in functions which 
				 * don't have directly mapping intrinsic fortran 
				 * functions, we leave the same "hole" in the generated 
				 * fortran code. By saying the same "hole", I mean 
				 * the same function signature in C++ jargon. We need 
				 * to build a separate Mc2For lib which is full of 
				 * user-defined functions in fortran, and those 
				 * functions have the same function signatures with 
				 * the built-in function calls in input matlab code.
				 * 
				 * this solution make the code generation framework 
				 * concise and not need to be updated when there comes 
				 * a new matlab built-in function. the only thing we 
				 * need to do is making a user-defined function by 
				 * ourselves or "find" one, and then update the Mc2For 
				 * lib. TODO shipped with Mc2For, we should at least 
				 * provide a significant number of user-defined fortran 
				 * functions to "fill" the "hole" of those commonly 
				 * used matlab built-in functions, like ones, zeros...
				 * 
				 * There are a lot of tutorials online about how to 
				 * make user-defined fortran lib and update lib.
				 * 
				 * actually, we can still make some function mappings 
				 * inlined, like .\ (left division), which can be 
				 * replaced by swapping operands and then use right 
				 * division, and : (colon operator), which can be 
				 * replaced by using implied DO loop in an array
				 * constructor. TODO this tmr.
				 */
				if (Debug) System.out.println("this is a function call");
				int inputNum = node.getChild(1).getNumChild();
				/*
				 * deal with storage allocation builtin, zeros and ones.
				 * TODO does rand and randn count?
				 */
				if (name.equals("zeros")) {
					storageAlloc = true;
				}
				/*
				 * functions with only one input or operand.
				 */
				if (inputNum == 0) {
					// for some constant.
					if (name.equals("pi")) {
						sb.append("3.14159265359");
					}
					else if (name.equals("i")) {
						sb.append("COMPLEX(0, 1)");
					}
					else {
						// no directly-mapping functions, leave the hole.
						sb.append(name + "()");
					}
				}
				else if (inputNum == 1) {
					if (fortranMapping.isFortranUnOperator(name)) {
						sb.append(fortranMapping.getFortranUnOpMapping(name));
						node.getChild(1).getChild(0).analyze(this);
					}
					else if (fortranMapping.isFortranDirectBuiltin(name)) {
						sb.append(fortranMapping.getFortranDirectBuiltinMapping(name));
						sb.append("(");
						node.getChild(1).getChild(0).analyze(this);
						sb.append(")");						
					}
					else if (fortranMapping.isFortranEasilyTransformed(name)) {
						if (Debug) System.out.println("******transformed function name: "+name+"******");
						if (name.equals("mean")) {
							sb.append("(SUM(");
							node.getChild(1).analyze(this);
							sb.append(") / SIZE(");
							node.getChild(1).analyze(this);
							sb.append("))");
						}
					}
					else {
						// no directly-mapping functions, leave the hole.
						sb.append(name + "(");
						node.getChild(1).analyze(this);
						sb.append(")");
						allSubprograms.add(node.getChild(0).getNodeString());
					}
				}
				/*
				 * functions with two inputs or operands.
				 */
				else if (inputNum == 2) {
					if (fortranMapping.isFortranBinOperator(name)) {
						sb.append("(");
						node.getChild(1).getChild(0).analyze(this);
						sb.append(" " + fortranMapping.getFortranBinOpMapping(name) + " ");
						node.getChild(1).getChild(1).analyze(this);
						sb.append(")");
					}
					else if (fortranMapping.isFortranDirectBuiltin(name)) {
						sb.append(fortranMapping.getFortranDirectBuiltinMapping(name));
						sb.append("(");
						node.getChild(1).getChild(0).analyze(this);
						sb.append(", ");
						node.getChild(1).getChild(1).analyze(this);
						sb.append(")");
					}
					else if (fortranMapping.isFortranEasilyTransformed(name)) {
						if (Debug) System.out.println("******transformed function name: "+name+"******");
						if (name.equals("colon")) {
							if (insideArray > 0) {
								/*
								 * back up the StringBuffer, test whether the start and end are integers.
								 */
								StringBuffer sb_bk = new StringBuffer();
								sb_bk.append(sb);
								sb.setLength(0);
								try {
									node.getChild(1).getChild(0).analyze(this);
									int start = Integer.parseInt(sb.toString());
									sb.setLength(0);
									sb.append(sb_bk);
									sb.append(start);
									
								} catch (Exception e) {
									sb.setLength(0);
									sb.append(sb_bk);
									sb.append("INT(");
									node.getChild(1).getChild(0).analyze(this);
									sb.append(")");
								}
								sb.append(":");
								sb_bk.setLength(0);
								sb_bk.append(sb);
								sb.setLength(0);
								try {
									node.getChild(1).getChild(1).analyze(this);
									int end = Integer.parseInt(sb.toString());
									sb.setLength(0);
									sb.append(sb_bk);
									sb.append(end);
									
								} catch (Exception e) {
									sb.setLength(0);
									sb.append(sb_bk);
									sb.append("INT(");
									node.getChild(1).getChild(1).analyze(this);
									sb.append(")");
								}
							}
							else {
								sb.append("[(I, I=INT(");
								node.getChild(1).getChild(0).analyze(this);
								sb.append("),INT(");
								node.getChild(1).getChild(1).analyze(this);
								sb.append("))]");
								fotranTemporaries.put("I", new BasicMatrixValue(
										null, 
										PrimitiveClassReference.INT32, 
										new ShapeFactory<AggrValue<BasicMatrixValue>>().getScalarShape(), 
										null, 
										new isComplexInfoFactory<AggrValue<BasicMatrixValue>>()
										.newisComplexInfoFromStr("REAL")
										));
							}
						}
						else if (name.equals("ldivide")) {
							sb.append("(");
							node.getChild(1).getChild(1).analyze(this);
							sb.append(" / ");
							node.getChild(1).getChild(0).analyze(this);
							sb.append(")");
						}
						else if (name.equals("randn")) {
							randnFlag = true;
						}
						if (name.equals("horzcat") || name.equals("vertcat")) {
							rhsArrayAssign = true;
							sb.append("[");
							for (int i = 0; i < inputNum; i++) {
								node.getChild(1).getChild(i).analyze(this);
								if (i < inputNum - 1) {
									sb.append(", ");
								}
							}
							sb.append("]");
						}
					}
					else if (fortranMapping.isFortranOverloadingInlineSet(name)) {
						/*
						 *  TODO determine which operator or function to inline 
						 *  depends on the shape of the oprands. 
						 */
						if (node.getChild(1).getChild(0) instanceof ParameterizedExpr 
								&& node.getChild(1).getChild(1) instanceof ParameterizedExpr) {
							Shape<AggrValue<BasicMatrixValue>> shapeOp1 = 
									getMatrixValue(((Name)analysisEngine
											.getTemporaryVariablesRemovalAnalysis()
											.getExprToTempVarTable()
											.get(node.getChild(1).getChild(0))).getID())
											.getShape();
							Shape<AggrValue<BasicMatrixValue>> shapeOp2 = 
									getMatrixValue(((Name)analysisEngine
											.getTemporaryVariablesRemovalAnalysis()
											.getExprToTempVarTable()
											.get(node.getChild(1).getChild(1))).getID())
											.getShape();
							if (name.equals("mtimes")) {
								if (shapeOp1.isRowVector() && shapeOp2.isColVector()) {
									// TODO use DOT_PRODUCT
									sb.append("DOT_PRODUCT(");
									node.getChild(1).getChild(0).analyze(this);
									sb.append(", ");
									node.getChild(1).getChild(1).analyze(this);
									sb.append(")");
								}
								else if (!shapeOp1.isConstant() && !shapeOp2.isConstant()) {
									// TODO use MATMUL, sometimes should make a shadow var.
									sb.append("MATMUL(");
									node.getChild(1).getChild(0).analyze(this);
									sb.append(", ");
									node.getChild(1).getChild(1).analyze(this);
									sb.append(")");
								}
								else {
									// use * operator
									sb.append("(");
									node.getChild(1).getChild(0).analyze(this);
									sb.append(" * ");
									node.getChild(1).getChild(1).analyze(this);
									sb.append(")");
								}
							}
							else if (name.equals("mrdivide")) {
								if (shapeOp1.isRowVector() && shapeOp2.isColVector()) {
									// TODO
								}
								else if (!shapeOp1.isConstant() && !shapeOp2.isConstant()) {
									// TODO
								}
								else {
									// use / operator
									sb.append("(");
									node.getChild(1).getChild(0).analyze(this);
									sb.append(" / ");
									node.getChild(1).getChild(1).analyze(this);
									sb.append(")");
								}
							}
							else if (name.equals("mpower")) {
								if (shapeOp1.isRowVector() && shapeOp2.isColVector()) {
									// TODO
								}
								else if (!shapeOp1.isConstant() && !shapeOp2.isConstant()) {
									// TODO
								}
								else {
									// use ** operator
									sb.append("(");
									node.getChild(1).getChild(0).analyze(this);
									sb.append(" ** ");
									node.getChild(1).getChild(1).analyze(this);
									sb.append(")");
								}
							}
						}
						else {
							// at least one of the operands is literal, which means it's a scalar.
							if (name.equals("mtimes")) {
								// use * operator
								sb.append("(");
								node.getChild(1).getChild(0).analyze(this);
								sb.append(" * ");
								node.getChild(1).getChild(1).analyze(this);
								sb.append(")");
							}
							else if (name.equals("mrdivide")) {
								// use / operator
								sb.append("(");
								node.getChild(1).getChild(0).analyze(this);
								sb.append(" / ");
								node.getChild(1).getChild(1).analyze(this);
								sb.append(")");
							}
							else if (name.equals("mpower")) {
								// use ** operator
								sb.append("(");
								node.getChild(1).getChild(0).analyze(this);
								sb.append(" ** ");
								node.getChild(1).getChild(1).analyze(this);
								sb.append(")");
							}
						}
						
					}
					else {
						// no directly-mapping functions, also leave the hole.
						sb.append(name + "(");
						node.getChild(1).getChild(0).analyze(this);
						sb.append(", ");
						node.getChild(1).getChild(1).analyze(this);
						sb.append(")");
						allSubprograms.add(node.getChild(0).getNodeString());
					}
				}
				/*
				 * functions with more than two inputs, leave the hole.
				 */
				else {
					if (fortranMapping.isFortranEasilyTransformed(name)) {
						if (name.equals("horzcat") || name.equals("vertcat")) {
							rhsArrayAssign = true;
							sb.append("[");
							for (int i = 0; i < inputNum; i++) {
								node.getChild(1).getChild(i).analyze(this);
								if (i < inputNum - 1) {
									sb.append(", ");
								}
							}
							sb.append("]");
						}
					}
					else {
						sb.append(name + "(");
						for (int i = 0; i < inputNum; i++) {
							node.getChild(1).getChild(i).analyze(this);
							if (i < inputNum - 1) {
								sb.append(", ");
							}
						}
						sb.append(")");
						allSubprograms.add(node.getChild(0).getNodeString());						
					}
				}
			}
		}
		else {
			System.err.println("how does this happen?");
			System.exit(0);
		}
	}
	
	@Override
	public void caseName(Name node) {
		// what is the difference from cseNameExpr?
		System.err.println(node.getID());
	}
	
	@Override
	public void caseNameExpr(NameExpr node) {
		String name = node.getName().getID();
		if (Debug) System.out.println("nameExpr:" + name);
		if (remainingVars.contains(name)) {
			if (Debug) System.out.println(name+" is a variable.");
			
			if (inArgs.contains(name) && leftOfAssign) {
				inputHasChanged.add(name);
			}
						
			if (mustBeInt) {
				forceToInt.add(name);
			}
			
			if (!functionName.equals(entryPointFile) 
					&& !isInSubroutine 
					&& outRes.contains(name)) {
				sb.append(functionName);
			}
			else if (functionName.equals(entryPointFile) && inArgs.contains(name)) {
				inputsUsed.add(name);
				sb.append(name);
			}
			else {
				if (!getMatrixValue(name).getShape().isConstant() && leftOfAssign) {
					sbForRuntimeInline.append("! seems need runtime allocate before assigning.\n");
				}
				/*
				 * hack to convert 2 ranks array to 1 rank vector in Fortran, 
				 * only add these trailings when both sides of the assignment
				 * are arrays.
				 */
				// for lhs.
				if (getMatrixValue(name).getShape().isRowVector() 
						&& leftOfAssign 
						&& insideArray == 0 
						&& rhsArrayAssign) {
					sb.append(name + "(1, :)");
				}
				else if (getMatrixValue(name).getShape().isColVector() 
						&& leftOfAssign 
						&& insideArray == 0 
						&& rhsArrayAssign) {
					sb.append(name + "(:, 1)");
				}
				// for rhs.
				else if (getMatrixValue(name).getShape().isRowVector() 
						&& rightOfAssign 
						&& insideArray == 0) {
					sb.append(name + "(1, :)");
					rhsArrayAssign = true;
				}
				else if (getMatrixValue(name).getShape().isColVector() 
						&& rightOfAssign 
						&& insideArray == 0) {
					sb.append(name + "(:, 1)");
					rhsArrayAssign = true;
				}
				else {
					sb.append(name);
				}
			}
			
		}
		else {
			if (Debug) System.out.println(name+" is a function name.");
			sb.append(name);
		}
	}
	
	@Override
	public void caseLiteralExpr(LiteralExpr node) {
		if (Debug) System.out.println("liter: "+node.getPrettyPrinted());
		sb.append(node.getPrettyPrinted());
	}
	
	@Override
	public void caseIntLiteralExpr(IntLiteralExpr node) {
		if (Debug) System.out.print("intLiter: "+node.getPrettyPrinted());
		sb.append(node.getPrettyPrinted());
	}
	
	@Override
	public void caseForStmt(ForStmt node) {
		HandleCaseForStmt forStmt = new HandleCaseForStmt();
		if(ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(forStmt.getFortran(this, node));
		else 
			subprogram.getStatementSection().addStatement(forStmt.getFortran(this, node));
    }
	
	@Override
	public void caseRangeExpr(RangeExpr node) {
		if (node.getNumChild() == 3) {
			if (Debug) System.out.println("has increment.");
			/*
			 * start
			 */
			if (node.getChild(0) instanceof NameExpr 
					&& !forceToInt.contains(((NameExpr)node.getChild(0)).getName().getID())
					&& !getMatrixValue(((NameExpr)node.getChild(0)).getName().getID())
					.getMatlabClass().equals(PrimitiveClassReference.INT32)) {
				sb.append("INT(");
				node.getChild(0).analyze(this);
				sb.append(")");
			}
			else if (node.getChild(0) instanceof ParameterizedExpr) {
				/*
				 * back up the StringBuffer, test whether the step is integer.
				 */
				StringBuffer sb_bk = new StringBuffer();
				sb_bk.append(sb);
				sb.setLength(0);
				try {
					node.getChild(0).analyze(this);
					int step = Integer.parseInt(sb.toString());
					sb.setLength(0);
					sb.append(sb_bk);
					sb.append(step);
				} catch (Exception e) {
					sb.setLength(0);
					sb.append(sb_bk);
					sb.append("INT(");
					node.getChild(0).analyze(this);
					sb.append(")");
				}
			}
			else {
				node.getChild(0).analyze(this);
			}
			sb.append(", ");
			/*
			 * end
			 */
			if (node.getChild(2) instanceof NameExpr 
					&& !forceToInt.contains(((NameExpr)node.getChild(2)).getName().getID())
					&& !getMatrixValue(((NameExpr)node.getChild(2)).getName().getID())
					.getMatlabClass().equals(PrimitiveClassReference.INT32)) {
				sb.append("INT(");
				node.getChild(2).analyze(this);
				sb.append(")");
			}
			else if (node.getChild(2) instanceof ParameterizedExpr) {
				/*
				 * back up the StringBuffer, test whether the step is integer.
				 */
				StringBuffer sb_bk = new StringBuffer();
				sb_bk.append(sb);
				sb.setLength(0);
				try {
					node.getChild(2).analyze(this);
					int step = Integer.parseInt(sb.toString());
					sb.setLength(0);
					sb.append(sb_bk);
					sb.append(step);
				} catch (Exception e) {
					sb.setLength(0);
					sb.append(sb_bk);
					sb.append("INT(");
					node.getChild(2).analyze(this);
					sb.append(")");
				}
			}
			else {
				node.getChild(2).analyze(this);
			}
			/*
			 * step
			 */
			if (node.getChild(1).getNumChild() != 0) {
				sb.append(", ");
				if (node.getChild(1).getChild(0) instanceof NameExpr 
						&& !forceToInt.contains(((NameExpr)node.getChild(1).getChild(0)).getName().getID())
						&& !getMatrixValue(((NameExpr)node.getChild(1).getChild(0)).getName().getID())
						.getMatlabClass().equals(PrimitiveClassReference.INT32)) {
					sb.append("INT(");
					node.getChild(1).getChild(0).analyze(this);
					sb.append(")");
				}
				else if (node.getChild(1).getChild(0) instanceof ParameterizedExpr) {
					/*
					 * back up the StringBuffer, test whether the step is integer.
					 */
					StringBuffer sb_bk = new StringBuffer();
					sb_bk.append(sb);
					sb.setLength(0);
					try {
						node.getChild(1).getChild(0).analyze(this);
						int step = Integer.parseInt(sb.toString());
						sb.setLength(0);
						sb.append(sb_bk);
						sb.append(step);
					} catch (Exception e) {
						sb.setLength(0);
						sb.append(sb_bk);
						sb.append("INT(");
						node.getChild(1).getChild(0).analyze(this);
						sb.append(")");
					}
				}
				else {
					node.getChild(1).getChild(0).analyze(this);
				}
			}
		}
		else {
			System.err.println("how does this happen?");
			System.exit(0);
		}
	}
	
	@Override
	public void caseIfStmt(IfStmt node) {
		HandleCaseIfStmt ifStmt = new HandleCaseIfStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(ifStmt.getFortran(this, node));
		else 
			subprogram.getStatementSection().addStatement(ifStmt.getFortran(this, node));
	}
	
	@Override
	public void caseWhileStmt(WhileStmt node) {
		HandleCaseWhileStmt whileStmt = new HandleCaseWhileStmt();
		if (ifWhileForBlockNest!=0) 
			stmtSecForIfWhileForBlock.addStatement(whileStmt.getFortran(this, node));
		else 
			subprogram.getStatementSection().addStatement(whileStmt.getFortran(this, node));
	}
	
	@Override
	public void caseMatrixExpr(MatrixExpr node) {
		for (int i=0; i<node.getNumChild(); i++) {
			node.getChild(i).analyze(this);
		}
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void caseList(List node) {
		for (int i=0; i<node.getNumChild(); i++) {
			if (Debug) System.out.println(node.getNumChild());
			if (!(node.getChild(i) instanceof EmptyStmt)) {
				if (node.getChild(i) instanceof NameExpr 
						&& insideArray > 0 
						&& !forceToInt.contains(((NameExpr)node.getChild(i)).getName().getID())
						&& !getMatrixValue(((NameExpr)node.getChild(i)).getName().getID())
						.getMatlabClass().equals(PrimitiveClassReference.INT32)) {
					if (Debug) System.out.println("I am a variable index!");
					sb.append("INT(");
					node.getChild(i).analyze(this);
					sb.append(")");
				}
				else if (node.getChild(i) instanceof ParameterizedExpr 
						&& !((NameExpr) node.getChild(i).getChild(0)).getName().getID().equals("colon") 
						&& insideArray > 0) {
					if (Debug) System.out.println("I am a variable index!");
					sb.append("INT(");
					node.getChild(i).analyze(this);
					sb.append(")");
				}
				else {
					node.getChild(i).analyze(this);
				}
				if (insideArray > 0 && i < node.getNumChild()-1) sb.append(", ");
			}
			else {
				// for comment statements, which are instances of EmpyStmt.
				node.getChild(i).analyze(this);
			}
		}
	}
	
	@Override
	public void caseColonExpr(ColonExpr node) {
		sb.append(":");
	}
	
	@Override
	public void caseRow(Row node) {
		node.getElementList().analyze(this);
	}
	
	// ******************************helper methods****************************
	public static Subprogram generateFortran(
			Function fNode, 
			ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet, 
			Set<String> remainingVars, 
			String entryPointFile, 
			Set<String> userDefinedFunctions, 
			AnalysisEngine analysisEngine, 
			boolean nocheck) 
	{
		return new FortranCodeASTGenerator(
				fNode, 
				currentOutSet, 
				remainingVars, 
				entryPointFile, 
				userDefinedFunctions, 
				analysisEngine, 
				nocheck).subprogram;
	}

	public void iterateStatements(ast.List<ast.Stmt> stmts) {
		for (ast.Stmt stmt : stmts) {
			if (!(stmt instanceof TIRCommentStmt))
				stmt.analyze(this);
			else {
				stmt.analyze(this);
			}
		}
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
	
	public String getSomeIndent(int n) {
		String res = "";
		n += indentNum;
		while (n > 0) {
			res += standardIndent;
			n--;
		}
		return res;
	}
}

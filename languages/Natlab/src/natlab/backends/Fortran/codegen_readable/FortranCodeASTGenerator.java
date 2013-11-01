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
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.ShapeFactory;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.*;
import natlab.backends.Fortran.codegen_readable.astCaseHandler.*;

public class FortranCodeASTGenerator extends AbstractNodeCaseHandler {
	static boolean Debug = false;
	// this currentOutSet is the out set at the end point of the program.
	ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet;
	public Set<String> remainingVars;
	public String entryPointFile;
	public Set<String> userDefinedFunctions;
	public boolean nocheck;
	public Set<String> allSubprograms;
	public Subprogram subprogram;
	public StringBuffer sb;
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
	public boolean colonFlag;
	public boolean randnFlag;
	// temporary variables generated in Fortran code generation.
	public Map<String, BasicMatrixValue> fotranTemporaries;
	public boolean mustBeInt;
	public Set<String> forceToInt;
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
			boolean nocheck) 
	{
		this.currentOutSet = currentOutSet;
		this.remainingVars = remainingVars;
		this.entryPointFile = entryPointFile;
		this.nocheck = nocheck;
		this.userDefinedFunctions = userDefinedFunctions;
		allSubprograms = new HashSet<String>();
		subprogram = new Subprogram();
		sb = new StringBuffer();
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
		colonFlag = false;
		randnFlag = false;
		fotranTemporaries = new HashMap<String,BasicMatrixValue>();
		mustBeInt = false;
		forceToInt = new HashSet<String>();
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
	public void caseAssignStmt(AssignStmt node)	{
		if (ifWhileForBlockNest != 0) {
			FAssignStmt fAssignStmt = new FAssignStmt();
			String indent = "";
			for (int i = 0; i < indentNum; i++) {
				indent = indent + this.standardIndent;
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
								if (this.outRes.contains(lhsRow.getChild(0).getChild(i).getNodeString())) {
									sb.append(this.functionName);
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
			node.getRHS().analyze(this);
			fAssignStmt.setFRHS(sb.toString());
			sb.setLength(0);
			node.getLHS().analyze(this);
			if (colonFlag) {
				fAssignStmt.setFLHS(sb.toString()+"(1, :)");
				colonFlag = false;
				sb.setLength(0);
				stmtSecForIfWhileForBlock.addStatement(fAssignStmt);
			}
			else if (randnFlag) {
				FSubroutines fSubroutines = new FSubroutines();
				fSubroutines.setIndent(indent);
				fSubroutines.setFunctionCall("RANDOM_NUMBER("+sb.toString()+")");
				randnFlag = false;
				sb.setLength(0);
				stmtSecForIfWhileForBlock.addStatement(fSubroutines);
			}
			else {
				fAssignStmt.setFLHS(sb.toString());
				sb.setLength(0);
				stmtSecForIfWhileForBlock.addStatement(fAssignStmt);
			}
		}
		else {
			FAssignStmt fAssignStmt = new FAssignStmt();
			String indent = "";
			for (int i = 0; i < indentNum; i++) {
				indent = indent + this.standardIndent;
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
								if (this.outRes.contains(lhsRow.getChild(0).getChild(i).getNodeString())) {
									sb.append(this.functionName);
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
			node.getRHS().analyze(this);
			fAssignStmt.setFRHS(sb.toString());
			sb.setLength(0);
			node.getLHS().analyze(this);
			if (colonFlag) {
				fAssignStmt.setFLHS(sb.toString()+"(1, :)");
				colonFlag = false;
				sb.setLength(0);
				subprogram.getStatementSection().addStatement(fAssignStmt);
			}
			else if (randnFlag) {
				FSubroutines fSubroutines = new FSubroutines();
				fSubroutines.setIndent(indent);
				fSubroutines.setFunctionCall("RANDOM_NUMBER("+sb.toString()+")");
				randnFlag = false;
				sb.setLength(0);
				subprogram.getStatementSection().addStatement(fSubroutines);
			}
			else {
				fAssignStmt.setFLHS(sb.toString());
				sb.setLength(0);
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
			if (this.remainingVars.contains(name)) {
				if (Debug) System.out.println("this is an array index.");
				// TODO add rigorous array indexing transformation and runtime abc.
				node.getChild(0).analyze(this);
				if (!this.getMatrixValue(name).getShape().isConstant()) {
					System.out.println("unknown shape, need run-time abc.");
				}
				sb.append("(");
				insideArray++;
				node.getChild(1).analyze(this);
				if (node.getChild(1) instanceof List 
						&& this.getMatrixValue(name).getShape().getDimensions().size() 
							!= ((List)node.getChild(1)).getNumChild()) {
					// TODO this is a hack for n-by-1 vectors.
					sb.append(", 1");
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
				/*
				 * functions with only one input or operand.
				 */
				if (node.getChild(1).getNumChild() == 1) {
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
					else {
						// no directly-mapping functions, leave the hole.
						node.getChild(0).analyze(this);
						sb.append("(");
						node.getChild(1).analyze(this);
						sb.append(")");
						this.allSubprograms.add(node.getChild(0).getNodeString());
					}
				}
				/*
				 * functions with two inputs or operands.
				 */
				else if (node.getChild(1).getNumChild() == 2) {
					if (fortranMapping.isFortranBinOperator(name)) {
						sb.append("(");
						node.getChild(1).getChild(0).analyze(this);
						sb.append(" " + fortranMapping.getFortranBinOpMapping(name) + " ");
						node.getChild(1).getChild(1).analyze(this);
						sb.append(")");
					}
					else if (fortranMapping.isFortranDirectBuiltin(name)) {
						if (name.equals("mtimes")) {
							if (node.getChild(1).getChild(0) instanceof ParameterizedExpr) {
								String op1 = ((NameExpr)((ParameterizedExpr)node.getChild(1)
										.getChild(0)).getChild(0)).getName().getID();
								String op2 = ((NameExpr)((ParameterizedExpr)node.getChild(1)
										.getChild(1)).getChild(0)).getName().getID();
								if (getMatrixValue(op1).getShape().maybeVector() 
										|| getMatrixValue(op2).getShape().maybeVector()) {
									sb.append("DOT_PRODUCT");
								}
							}
								
						}
						else {
							sb.append(fortranMapping.getFortranDirectBuiltinMapping(name));
						}
						sb.append("(");
						node.getChild(1).getChild(0).analyze(this);
						sb.append(", ");
						node.getChild(1).getChild(1).analyze(this);
						sb.append(")");
					}
					else if (fortranMapping.isFortranEasilyTransformed(name)) {
						if (name.equals("colon")) {
							if (insideArray > 0) {
								// sb.append("INT(");
								node.getChild(1).getChild(0).analyze(this);
								// sb.append(")");
								sb.append(":");
								sb.append("INT(");
								node.getChild(1).getChild(1).analyze(this);
								sb.append(")");
							}
							else {
								sb.append("(/(I, I=INT(");
								node.getChild(1).getChild(0).analyze(this);
								sb.append("),INT(");
								node.getChild(1).getChild(1).analyze(this);
								sb.append("))/)");
								colonFlag = true;
								fotranTemporaries.put("I", new BasicMatrixValue(
										null, 
										PrimitiveClassReference.INT32, 
										new ShapeFactory<AggrValue<BasicMatrixValue>>().getScalarShape(), 
										null));
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
					}
					else {
						// no directly-mapping functions, also leave the hole.
						node.getChild(0).analyze(this);
						sb.append("(");
						node.getChild(1).getChild(0).analyze(this);
						sb.append(", ");
						node.getChild(1).getChild(1).analyze(this);
						sb.append(")");
						this.allSubprograms.add(node.getChild(0).getNodeString());
					}
				}
				/*
				 * functions with more than two inputs, leave the hole.
				 */
				else {
					node.getChild(0).analyze(this);
					sb.append("(");
					for (int i = 0; i < node.getChild(1).getNumChild(); i++) {
						node.getChild(1).getChild(i).analyze(this);
						if (i < node.getChild(1).getNumChild() - 1) {
							sb.append(", ");
						}
					}
					sb.append(")");
					this.allSubprograms.add(node.getChild(0).getNodeString());
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
		if (Debug) System.out.println("nameExpr:" + node.getName().getID());
		if (this.remainingVars.contains(node.getName().getID())) {
			if (Debug) System.out.println(node.getName().getID()+" is a variable.");
			
			if (mustBeInt) {
				forceToInt.add(node.getName().getID());
			}
			
			if (!this.functionName.equals(this.entryPointFile) 
					&& !this.isInSubroutine 
					&& this.outRes.contains(node.getName().getID())) {
				sb.append(this.functionName);
			}
			else {
				sb.append(node.getName().getID());
			}
			
		}
		else {
			if (Debug) System.out.println(node.getName().getID()+" is a function name.");
			sb.append(node.getName().getID());
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
			if (node.getChild(0) instanceof NameExpr 
					&& !forceToInt.contains(((NameExpr)node.getChild(0)).getName().getID())
					&& !getMatrixValue(((NameExpr)node.getChild(0)).getName().getID())
					.getMatlabClass().equals(PrimitiveClassReference.INT32)) {
				sb.append("INT(");
				node.getChild(0).analyze(this);
				sb.append(")");
			}
			else if (node.getChild(0) instanceof ParameterizedExpr) {
				sb.append("INT(");
				node.getChild(0).analyze(this);
				sb.append(")");
			}
			else {
				node.getChild(0).analyze(this);
			}
			sb.append(", ");
			node.getChild(2).analyze(this);
			if (node.getChild(1).getNumChild() != 0) {
				sb.append(", ");
				node.getChild(1).getChild(0).analyze(this);
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
				else {
					node.getChild(i).analyze(this);
				}
				if (insideArray > 0 && i < node.getNumChild()-1) 
					sb.append(", ");
			}
		}
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
			boolean nocheck) 
	{
		return new FortranCodeASTGenerator(
				fNode, 
				currentOutSet, 
				remainingVars, 
				entryPointFile, 
				userDefinedFunctions, 
				nocheck).subprogram;
	}

	public void iterateStatements(ast.List<ast.Stmt> stmts) {
		for (ast.Stmt stmt : stmts) {
			if (!(stmt instanceof TIRCommentStmt))
				stmt.analyze(this);
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
}

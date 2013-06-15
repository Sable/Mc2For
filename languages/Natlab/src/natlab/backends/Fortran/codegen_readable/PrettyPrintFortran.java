package natlab.backends.Fortran.codegen_readable;

import java.util.Set;

import ast.*;
import nodecases.AbstractNodeCaseHandler;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.ValueFlowMap;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;

public class PrettyPrintFortran extends AbstractNodeCaseHandler {
	static boolean Debug = false;
	ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet;
	Set<String> remainingVars;
	OperatorMapping operatorMapping;
	int scope;
	String indent;
	
	public PrettyPrintFortran(Function fNode, 
			ValueFlowMap<AggrValue<BasicMatrixValue>> currentOutSet, 
			Set<String> remainingVars) {
		this.currentOutSet = currentOutSet;
		this.remainingVars = remainingVars;
		operatorMapping = new OperatorMapping();
		scope = 0;
		indent = "   ";
		fNode.analyze(this);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void caseASTNode(ASTNode node) {}

	@Override
	public void caseFunction(Function node)
    {
		System.out.print("SUBROUTINE "+node.getName());
		System.out.print("(");
		for (String inArg : node.getInParamSet()) {
			System.out.print(inArg+",");
		}
		int counter = 0;
		for (String outArg : node.getOutParamSet()) {
			System.out.print(outArg);
			counter++;
			if (counter < node.getOutParamSet().size()) {
				System.out.print(",");
			}
		}
		System.out.println(")");
		System.out.println("IMPLICIT NONE");
		System.out.println("TODO insert declaration from value analysis result.");
		for (Stmt stmt : node.getStmtList()) {
			if (!(stmt instanceof TIRCommentStmt)) {
				if (Debug) System.out.println(stmt.getClass());
				stmt.analyze(this);
			}
		}
		System.out.println("END SUBROUTINE");
    }
	
	@Override
	public void caseAssignStmt(AssignStmt node) {
		System.out.print(getCurrentIndent(scope, indent));
		node.getLHS().analyze(this);
		System.out.print(" = ");
		node.getRHS().analyze(this);
		System.out.print(";\n");
	}
	
	@Override
	public void caseParameterizedExpr(ParameterizedExpr node) {
		// NameExpr(List), NameExpr is the first child, List is the second child.
		if (Debug) System.out.println("parameterized expr: " 
				+ node+", has "+node.getNumChild()+" children.");
		if (node.getChild(0) instanceof NameExpr) {
			String name = ((NameExpr) node.getChild(0)).getName().getID();
			if (this.remainingVars.contains(name)) {
				if (Debug) System.out.println("this is an array index.");
				node.getChild(0).analyze(this);
				System.out.print("(");
				node.getChild(1).analyze(this);
				System.out.print(")");
			}
			else {
				if (Debug) System.out.println("this is a function call");
				if (node.getChild(1).getNumChild()==1) {
					if (operatorMapping.isFortranDirectBuiltin(name)) {
						System.out.print(" "+operatorMapping
								.getFortranDirectBuiltinMapping(name));
						System.out.print("(");
						node.getChild(1).getChild(0).analyze(this);
						System.out.print(")");						
					}
					else {
						node.getChild(0).analyze(this);
						System.out.print("(");
						node.getChild(1).analyze(this);
						System.out.print(")");						
					}
				}
				else if (node.getChild(1).getNumChild()==2) {
					if (operatorMapping.isFortranBinOperator(name)) {
						System.out.print("(");
						node.getChild(1).getChild(0).analyze(this);
						System.out.print(" "+operatorMapping
								.getFortranBinOpMapping(name)+" ");
						node.getChild(1).getChild(1).analyze(this);
						System.out.print(")");						
					}
					else if (operatorMapping.isFortranDirectBuiltin(name)) {
						System.out.print(" "+operatorMapping
								.getFortranDirectBuiltinMapping(name));
						System.out.print("(");
						node.getChild(1).getChild(0).analyze(this);
						System.out.print(" ,");
						node.getChild(1).getChild(1).analyze(this);
						System.out.print(")");						
					}
					else {
						node.getChild(0).analyze(this);
						System.out.print("(");
						node.getChild(1).getChild(0).analyze(this);
						System.out.print(" ,");
						node.getChild(1).getChild(1).analyze(this);
						System.out.print(")");
					}
				}
			}
		}
		else {
			System.err.println("can this happen?");
			System.exit(0);
		}
	}
	
	@Override
	public void caseName(Name node) {
		System.out.println(node.getID());
	}
	
	@Override
	public void caseNameExpr(NameExpr node) {
		// System.out.println("nameExpr:" + node.getName().getID());
		if (this.remainingVars.contains(node.getName().getID())) {
			if (Debug) System.out.println(node.getName().getID()+" is a variable.");
			System.out.print(node.getName().getID());
		}
		else {
			if (Debug) System.out.println(node.getName().getID()+" is a function name.");
			System.out.print(node.getName().getID());
		}
	}
	
	@Override
	public void caseLiteralExpr(LiteralExpr node) {
		if (Debug) System.out.println("liter: "+node.getPrettyPrinted());
		System.out.print(node.getPrettyPrinted());
	}
	
	@Override
	public void caseIntLiteralExpr(IntLiteralExpr node) {
		if (Debug) System.out.print("intLiter: "+node.getPrettyPrinted());
		System.out.print(node.getPrettyPrinted());
	}
	
	@Override
	public void caseForStmt(ForStmt node)
    {
		if (Debug) System.out.println("for stmt~");
		System.out.print(getCurrentIndent(scope, indent));
		System.out.print("DO ");
		node.getChild(0).analyze(this);
		scope++;
        for (Stmt stmt : node.getStmtList()) {
        	if (!(stmt instanceof TIRCommentStmt)) {
        		if (Debug) System.out.println(stmt.getClass());
        		stmt.analyze(this);
        	}
        }
        scope--;
		System.out.print(getCurrentIndent(scope, indent));
        System.out.println("ENDDO");
    }
	
	@Override
	public void caseRangeExpr(RangeExpr node) {
		if (node.getNumChild()==3) {
			if (Debug) System.out.println("has increment.");
			node.getChild(0).analyze(this);
			System.out.print(", ");
			node.getChild(2).analyze(this);
			System.out.print(", ");
			node.getChild(1).getChild(0).analyze(this);
		}
	}
	
	@Override
	public void caseIfStmt(IfStmt node) {
		if (Debug) System.out.println("if stmt~");
		System.out.print(getCurrentIndent(scope, indent));
		System.out.print("IF ");
		node.getIfBlock(0).getCondition().analyze(this);
		System.out.println(" THEN");
		scope++;
		for (Stmt stmt : node.getIfBlock(0).getStmtList()) {
			stmt.analyze(this);
		}
		scope--;
		System.out.print(getCurrentIndent(scope, indent));
		System.out.println("ELSE");
		scope++;
		node.getElseBlock().analyze(this);
		scope--;
		System.out.print(getCurrentIndent(scope, indent));
		System.out.println("ENDIF");
	}
	
	// ****************useless, but important cases****************************
	@Override
	public void caseMatrixExpr(MatrixExpr node) {
		for (int i=0; i<node.getNumChild(); i++) {
			node.getChild(i).analyze(this);
		}
	}
	
	@Override
	public void caseList(List node) {
		for (int i=0; i<node.getNumChild(); i++) {
			// System.out.println(node.getChild(i));
			node.getChild(i).analyze(this);
		}
	}
	
	@Override
	public void caseRow(Row node) {
		node.getElementList().analyze(this);
	}
	
	// ******************************helper methods****************************
	private String getCurrentIndent(int scope, String indent) {
		String currentIndent = "";
		while (scope>0) {
			currentIndent += indent;
			scope--;
		}
		return currentIndent;
	}
}

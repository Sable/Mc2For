package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import ast.FPLiteralExpr;
import ast.IntLiteralExpr;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;

public class ASTHandleCaseTIRAssignLiteralStmt {

	static boolean Debug = false;
	
	public ASTHandleCaseTIRAssignLiteralStmt(){
		
	}
	
	public FortranCodeASTGenerator getFortran(FortranCodeASTGenerator fcg, TIRAssignLiteralStmt node){
		if (Debug) System.out.println("in an assignLiteral statement");
		AssignStmt stmt = new AssignStmt();
		LiteralExp literalExp = new LiteralExp();
		
		String LHS;
		LHS = node.getTargetName().getVarName();
		Variable var = new Variable();
		var.setName(LHS);
		stmt.setVariable(var);
		
		Variable expVar = new Variable();
		if(node.getRHS().getRValue() instanceof IntLiteralExpr){
			expVar.setName((((IntLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString()));
		}
		else{
			expVar.setName(((FPLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString());
		}
		literalExp.setVariable(expVar);
		stmt.setExp(literalExp);
		/**
		 * literal assignment target variable should be a constant, if it's not a constant, we need allocate it as a 1 by 1 array.
		 */
		if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).isConstant()){
			if (Debug) System.out.println(LHS+" is a constant");
		}
		else{
			ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).getShape().getDimensions());
			try{
				for(Integer intgr : dim){
					String test = intgr.toString();
				}
			}
			catch(Exception e){
				stmt.setRuntimeCheck("allocate("+LHS+"(1, 1));");
			}
		}
		fcg.SubProgram.getStatementSection().addStatement(stmt);
		return fcg;
	}
}

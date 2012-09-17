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
	/**
	 * AssignLiteralStmt: Statement ::= <RuntimeCheck> Variable <Literal>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRAssignLiteralStmt node){
		if (Debug) System.out.println("in an assignLiteral statement");
		
		AssignLiteralStmt stmt = new AssignLiteralStmt();
		Variable var = new Variable();
		var.setName(node.getTargetName().getVarName());
		stmt.setVariable(var);
		
		if(node.getRHS().getRValue() instanceof IntLiteralExpr){
			stmt.setLiteral(((IntLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString());
		}
		else{
			stmt.setLiteral(((FPLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString());
		}
		
		/**
		 * literal assignment target variable should be a constant, if it's not a constant, we need allocate it as a 1 by 1 array.
		 */
		if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getTargetName().getVarName()).getSingleton())).isConstant()){
			if (Debug) System.out.println(node.getTargetName().getVarName()+" is a constant");
		}
		else{
			ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getTargetName().getVarName()).getSingleton())).getShape().getDimensions());
			try{
				for(Integer intgr : dim){
					String test = intgr.toString();
				}
			}
			catch(Exception e){
				RuntimeCheck rtc = new RuntimeCheck();
				rtc.setName("allocate("+node.getTargetName().getVarName()+"(1, 1));");
				stmt.setRuntimeCheck(rtc);
			}
		}
		return stmt;
	}
}

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
		
		String LHS;
		LHS = node.getTargetName().getVarName();
		String RHS;
		if(node.getRHS().getRValue() instanceof IntLiteralExpr){
			RHS = ((IntLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString();
		}
		else{
			RHS = ((FPLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString();
		}
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
				fcg.buf.append("allocate("+LHS+"(1, 1));\n  ");
			}
			fcg.buf.append(LHS+" = "+RHS+";");
		}
		fcg.SubProgram.getStatementSection().addStatement(stmt);
		return fcg;
	}
}

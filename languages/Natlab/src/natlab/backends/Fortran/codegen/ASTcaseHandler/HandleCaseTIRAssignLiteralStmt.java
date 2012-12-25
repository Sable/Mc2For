package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import ast.FPLiteralExpr;
import ast.IntLiteralExpr;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.Shape;

public class HandleCaseTIRAssignLiteralStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRAssignLiteralStmt(){
		
	}
	/**
	 * AssignLiteralStmt: Statement ::= <RuntimeCheck> Variable <Literal>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRAssignLiteralStmt node){
		if (Debug) System.out.println("in an assignLiteral statement");
		
		AssignLiteralStmt stmt = new AssignLiteralStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		Variable var = new Variable();
		//TODO need more consideration, what if the input argument is an array, but inside subroutine, 
		//it has been assigned a constant, this problem also relates to different shape var merging problem.
		if(fcg.isSubroutine==true){
			/**
			 * if input argument on the LHS of assignment stmt, we assume that this input argument maybe modified.
			 */
			if(fcg.inArgs.contains(node.getTargetName().getVarName())){
				if (Debug) System.out.println("subroutine's input "+node.getTargetName().getVarName()+" has been modified!");
				/**
				 * here we need to detect whether it is the first time this variable put in the set,
				 * because we only want to back up them once.
				 */
				if(fcg.inputHasChanged.contains(node.getTargetName().getVarName())){
					//do nothing.
					if (Debug) System.out.println("encounter "+node.getTargetName().getVarName()+" again.");
				}
				else{
					if (Debug) System.out.println("first time encounter "+node.getTargetName().getVarName());
					fcg.inputHasChanged.add(node.getTargetName().getVarName());
					BackupVar backupVar = new BackupVar();
					backupVar.setName(node.getTargetName().getVarName()+"_backup = "+node.getTargetName().getVarName()+";\n");
					stmt.setBackupVar(backupVar);
				}
				var.setName(node.getTargetName().getVarName()+"_backup");
			}
			else{
				var.setName(node.getTargetName().getVarName());
			}
		}
		else{
			var.setName(node.getTargetName().getVarName());
		}
		stmt.setVariable(var);
		
		stmt.setLiteral(node.getRHS().getNodeString());
		/*if(node.getRHS().getRValue() instanceof IntLiteralExpr){
			stmt.setLiteral(((IntLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString());
		}
		else{
			stmt.setLiteral(((FPLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString());
		}*/
		
		/**
		 * literal assignment target variable should be a constant, if it's not a constant, we need allocate it as a 1 by 1 array.
		 */
		ArrayList<Integer> dims = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis()
				.getCurrentOutSet().get(node.getTargetName().getVarName()).getSingleton())).getShape().getDimensions());
		if(Shape.isDimensionExactlyKnow(dims)){
			
		}
		else{
			RuntimeCheck rtc = new RuntimeCheck();
			rtc.setName("allocate("+node.getTargetName().getVarName()+"(1, 1));");
			stmt.setRuntimeCheck(rtc);
		}
		return stmt;
	}
}

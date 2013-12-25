package natlab.backends.Fortran.codegen_readable.astCaseHandler;

import ast.ForStmt;

import natlab.backends.Fortran.codegen_readable.*;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.*;
import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.isComplex.isComplexInfoFactory;
import natlab.tame.valueanalysis.components.shape.ShapeFactory;

public class HandleCaseForStmt {
	static boolean Debug = false;
	
	/**
	 * ForStmt: Statement 
	 * ::= <LoopVar> <LowBoundary> <UpperBoundary> [Inc] ForBlock: StatementSection;
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			ForStmt node) 
	{
		if (Debug) System.out.println("in for statement.");
		FForStmt stmt = new FForStmt();
		stmt.setIndent(fcg.getMoreIndent(0));
		String varName = node.getChild(0).getChild(0).getPrettyPrinted();
		String[] checks = node.getChild(0).getChild(1).getPrettyPrinted().split(":");
		String[] rangeVars = new String[3];
		if (checks.length == 3 && checks[1].indexOf("uminus") == -1) {
			fcg.forLoopTransform = true;
			node.getChild(0).getChild(1).analyze(fcg);
			rangeVars = fcg.sb.toString().replace(" ", "").split(",");
			/*
			 * add for loop transformation.
			 */
			try {
				int start = Integer.parseInt(rangeVars[0]);
				stmt.setFForCondition(varName + "_rangeVar = " + start 
						+ ", INT((" + rangeVars[1] + " - " + rangeVars[0] 
								+ ") / " + rangeVars[2] + " + 1)");
			} catch (Exception e) {
				stmt.setFForCondition(varName + "_rangeVar = INT(" + rangeVars[0] 
						+ "), INT((" + rangeVars[1] + " - " + rangeVars[0] 
								+ ") / " + rangeVars[2] + " + 1)");
			}
			fcg.fotranTemporaries.put(varName + "_rangeVar", new BasicMatrixValue(
					null, 
					PrimitiveClassReference.INT32, 
					new ShapeFactory<AggrValue<BasicMatrixValue>>().getScalarShape(), 
					null, 
					new isComplexInfoFactory<AggrValue<BasicMatrixValue>>()
					.newisComplexInfoFromStr("REAL")
					));
			FAssignStmt fAssign = new FAssignStmt();
			fAssign.setIndent(fcg.getMoreIndent(0));
			fAssign.setFLHS(varName);
			fAssign.setFRHS(rangeVars[0]);
			if (fcg.ifWhileForBlockNest != 0) {
				fcg.stmtSecForIfWhileForBlock.addStatement(fAssign);
			}
			else {
				fcg.subprogram.getStatementSection().addStatement(fAssign);
			}
			fcg.forLoopTransform = false;
		}
		else {
			fcg.mustBeInt = true;
			node.getChild(0).getChild(0).analyze(fcg);
			fcg.mustBeInt = false;
			fcg.sb.append(" = ");
			node.getChild(0).getChild(1).analyze(fcg);
			stmt.setFForCondition(fcg.sb.toString());
		}
		fcg.sb.setLength(0);
		/*
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock 
		 * point back after iterate for block.
		 */
		StatementSection backup = fcg.stmtSecForIfWhileForBlock;
		
		fcg.ifWhileForBlockNest++;
		StatementSection forStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = forStmtSec;
		fcg.indentNum++;
		node.getStmtList().analyze(fcg);
		if (checks.length == 3 && checks[1].indexOf("uminus") == -1) {
			FAssignStmt fAssign = new FAssignStmt();
			fAssign.setIndent(fcg.getMoreIndent(0));
			fAssign.setFLHS(varName);
			fAssign.setFRHS(varName + " + " + rangeVars[2]);
			forStmtSec.addStatement(fAssign);
		}
		stmt.setFForBlock(forStmtSec);
		fcg.indentNum--;
		fcg.ifWhileForBlockNest--;
		
		fcg.stmtSecForIfWhileForBlock = backup;
		
		return stmt;
	}
}

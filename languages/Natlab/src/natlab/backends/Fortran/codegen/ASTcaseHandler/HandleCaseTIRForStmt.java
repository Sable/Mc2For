package natlab.backends.Fortran.codegen.ASTcaseHandler;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.tame.valueanalysis.components.constant.HasConstant;

public class HandleCaseTIRForStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRForStmt(){
		
	}
	/**
	 * ForStmt: Statement ::= <LoopVar> <LowBoundary> [Inc] <UpperBoundary> ForBlock: StatementSection;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRForStmt node){
		if (Debug) System.out.println("in for statement.");
		
		ForStmt stmt = new ForStmt();
		String indent = "";
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		stmt.setLoopVar(node.getLoopVarName().getVarName());
		fcg.forStmtParameter.add(node.getLoopVarName().getVarName());
		/**
		 * lower bound constant replacement
		 */
		if(((HasConstant)(fcg.analysis.getNodeList()
				.get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getLowerName().getVarName()).getSingleton())).getConstant()!=null){
			Constant lower = ((HasConstant)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getLowerName().getVarName()).getSingleton())).getConstant();
			double doubleLower = (Double) lower.getValue();
			int intLower = (int) doubleLower;
			stmt.setLowBoundary(Integer.toString(intLower));
		}
		else{
			stmt.setLowBoundary(node.getLowerName().getVarName());		
		}
		fcg.forStmtParameter.add(node.getLowerName().getVarName());
		/**
		 * upper bound constant replacement
		 */
		if(((HasConstant)(fcg.analysis.getNodeList()
				.get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getUpperName().getVarName()).getSingleton())).getConstant()!=null){
			Constant upper = ((HasConstant)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getUpperName().getVarName()).getSingleton())).getConstant();
			double doubleUpper = (Double) upper.getValue();
			int intUpper = (int) doubleUpper;
			stmt.setUpperBoundary(Integer.toString(intUpper));
		}
		else{
			stmt.setUpperBoundary(node.getUpperName().getVarName());		
		}
		fcg.forStmtParameter.add(node.getUpperName().getVarName());
		/**
		 * if has increment variable
		 */
		if(node.getIncName()!=null){
			Inc inc = new Inc();
			/**
			 * increment variable constant replacement
			 */
			if(((HasConstant)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getIncName().getVarName()).getSingleton())).getConstant()!=null){
				Constant increment = ((HasConstant)(fcg.analysis.getNodeList()
						.get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getIncName().getVarName()).getSingleton())).getConstant();
				double doubleInc = (Double) increment.getValue();
				int intInc = (int) doubleInc;
				inc.setName(Integer.toBinaryString(intInc));
			}
			else{
				inc.setName(node.getIncName().getVarName());		
			}
			fcg.forStmtParameter.add(node.getIncName().getVarName());
			stmt.setInc(inc);
		}
		/**
		 * backup this pointer! and make fcg.stmtSecForIFWhileForBlock point back after iterate for block.
		 */
		StatementSection backup = fcg.stmtSecForIfWhileForBlock;
		
		fcg.inIfWhileForBlock = true;
		StatementSection forStmtSec = new StatementSection();
		fcg.stmtSecForIfWhileForBlock = forStmtSec;
		fcg.indentNum++;
		fcg.iterateStatements(node.getStatements());
		stmt.setForBlock(forStmtSec);
		fcg.indentNum--;
		fcg.inIfWhileForBlock = false;
		
		fcg.stmtSecForIfWhileForBlock = backup;
		
		return stmt;
	}
}

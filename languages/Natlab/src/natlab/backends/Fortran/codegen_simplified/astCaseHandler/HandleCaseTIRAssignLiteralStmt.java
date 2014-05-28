package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen_simplified.FortranCodeASTGenerator;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.AssignLiteralStmt;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.RuntimeAllocate;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Statement;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Variable;
import natlab.tame.tir.TIRAssignLiteralStmt;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.tame.valueanalysis.components.shape.Shape;

public class HandleCaseTIRAssignLiteralStmt {
	static boolean Debug = false;
	
	/**
	 * AssignLiteralStmt: Statement ::= <RuntimeAllocate> Variable <Literal>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRAssignLiteralStmt node) {
		if (Debug) System.out.println("in an assignLiteral statement");
		AssignLiteralStmt stmt = new AssignLiteralStmt();
		String indent = new String();
		for (int i = 0; i < fcg.indentNum; i++) {
			indent = indent + fcg.standardIndent;
		}
		stmt.setIndent(indent);
		Variable var = new Variable();
		String targetName = node.getTargetName().getVarName();
		/*
		 * TODO need more consideration, what if the input argument is an array, 
		 * but inside subroutine, it has been assigned a constant, this problem 
		 * also relates to different shape var merging problem.
		 */
		if (fcg.hasSingleton(targetName)) {
			/*
			 * if input argument on the LHS of assignment stmt, 
			 * we assume that this input argument maybe modified.
			 */
			if (fcg.isInSubroutine && fcg.inArgs.contains(targetName)) {
				if (Debug) System.out.println("subroutine's input " + targetName 
						+ " has been modified!");
				fcg.inputHasChanged.add(targetName);
				var.setName(targetName+"_copy");
			}
			else if (fcg.outRes.contains(targetName))
				var.setName(fcg.functionName);
			else 
				var.setName(targetName);
			stmt.setVariable(var);
			stmt.setLiteral(node.getRHS().getNodeString());
			/*
			 * literal assignment target variable should be a constant, if it's not a constant, 
			 * we need allocate it as a 1 by 1 array.
			 */
			@SuppressWarnings("rawtypes")
			Shape targetVar= fcg.getMatrixValue(targetName).getShape();
			if (!targetVar.isConstant()) {
				RuntimeAllocate rtc = new RuntimeAllocate();
				rtc.setBlock("ALLOCATE("+targetName+"(1, 1));");
				stmt.setRuntimeAllocate(rtc);			
			}
		}
		else {
			/*
			 * assign different type value to the same variable, 
			 * we need to generate a derived type in Fortran for 
			 * this variable, the fields in the derived type 
			 * correspond to different type.
			 */
			if (fcg.forCellArr.keySet().contains(targetName)) {
				int fieldNum = 0, i = 0;
				for (BasicMatrixValue fieldValue : fcg.forCellArr.get(targetName)) {
					Constant constant = Constant.get(node.getRHS());
					if (fieldValue.getMatlabClass().equals(constant.getMatlabClass()) 
							&& fieldValue.getShape().equals(constant.getShape())) {
						fieldNum = i;
					}
					else i++;
				}
				var.setName(targetName+"%f"+fieldNum);
				stmt.setVariable(var);
				stmt.setLiteral(node.getRHS().getNodeString());
			}
			else {
				ArrayList<BasicMatrixValue> valueList = new ArrayList<BasicMatrixValue>();
				int length = fcg.getValueSet(targetName).values().toArray().length;
				for (int i=0; i<length; i++) {
					valueList.add((BasicMatrixValue)fcg.getValueSet(targetName).values().toArray()[i]);
				}
				fcg.forCellArr.put(targetName, valueList);
			}
		}
		return stmt;
	}
}
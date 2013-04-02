package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.List;
import java.util.ArrayList;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.*;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.tame.valueanalysis.components.shape.*;
import natlab.tame.classes.reference.*;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class HandleCaseTIRAbstractAssignToVarStmt {
	static boolean Debug = false;
	static boolean lhsShapeIsknown = true;
	static boolean rhsShapeIsKnown = true;
	
	/**
	 * AbstractAssignToVarStmt: Statement ::= <RuntimeAllocate> <TargetVariable> <SourceVariable>;
	 * for each statement, currently, we need to insert two check:
	 * 1. for rhs, constant folding check;
	 * 2. for lhs, do we need to inline allocate code check.
	 */
	public Statement getFortran(
			FortranCodeASTGenerator fcg, 
			TIRAbstractAssignToVarStmt node) {
		if (Debug) System.out.println("in an abstractAssignToVar statement");
		AbstractAssignToVarStmt stmt = new AbstractAssignToVarStmt();
		String indent = new String();
		for (int i=0; i<fcg.indentNum; i++) {
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		String targetName = node.getTargetName().getID();
		/*
		 * for lhs, insert function name variable replacement check.
		 */
		if (fcg.isSubroutine) {
			/*
			 * if an input argument of the function is on the LHS of an assignment stmt, 
			 * we assume that this input argument maybe modified.
			 */
			if (fcg.inArgs.contains(targetName)) {
				if (Debug) System.out.println("subroutine's input "+targetName
						+" has been modified!");
				/*
				 * here we need to detect whether it is the first time this variable put in the set,
				 * because we only want to back up them once.
				 */
				if (fcg.inputHasChanged.contains(targetName)) {
					// do nothing.
					if (Debug) System.out.println("encounter "+targetName+" again.");
				}
				else {
					if (Debug) System.out.println("first time encounter "+targetName);
					fcg.inputHasChanged.add(targetName);
				}
				stmt.setTargetVariable(targetName+"_copy");
			}
			else stmt.setTargetVariable(targetName);
		}
		else {
			if (fcg.outRes.contains(targetName)) stmt.setTargetVariable(fcg.majorName);
			else stmt.setTargetVariable(targetName);
		}
		/*
		 * for rhs, insert constant folding check.
		 */
		String rhsNodeString = node.getRHS().getNodeString();
		if (fcg.getMatrixValue(rhsNodeString).hasConstant() 
				&& (!fcg.inArgs.contains(rhsNodeString))) {
			if (Debug) System.out.println(targetName+" is a constant");
			Constant c = fcg.getMatrixValue(rhsNodeString).getConstant();
			stmt.setSourceVariable(c.toString());
		}
		else {
			if (fcg.inputHasChanged.contains(rhsNodeString)) 
				stmt.setSourceVariable(rhsNodeString+"_copy");
			else stmt.setSourceVariable(rhsNodeString);
		}
		/*
		 * for lhs, insert runtime shape allocate check.
		 * TODO need more concerns.
		 */
		if (fcg.getMatrixValue(rhsNodeString).hasConstant()) {
			if (Debug) System.out.println(targetName+" is a constant");
		}
		else {
			List<DimValue> lhsVariableDimension = fcg.getMatrixValue(targetName)
					.getShape().getDimensions();
			List<DimValue> rhsVariableDimension = fcg.getMatrixValue(rhsNodeString)
					.getShape().getDimensions();
			try {
				for (DimValue dimValue : lhsVariableDimension) {
					// if lhs variable's shape is not exactly known, we need allocate it first.
					if (dimValue==null) {
						System.out.println("The shape of "+targetName+" is not exactly known, " +
								"we need allocate it first");
						lhsShapeIsknown = false;
					}
				}
				for (DimValue dimValue : rhsVariableDimension) {
					if (dimValue==null) {
						System.out.println("The shape of "+rhsNodeString+" is not exactly konwn, "
								+"we cannot allocate "+targetName+" with "+rhsNodeString
								+"'s shape, "+"we need runtime check "+rhsNodeString
								+"'s shape first.");
						rhsShapeIsKnown = false;
					}
				}
				// TODO inline runtime check code for RHS
				if (!lhsShapeIsknown && !rhsShapeIsKnown) {
					RuntimeAllocate rtc1 = new RuntimeAllocate();
					rtc1.setBlock(targetName+"_RTC = shape("+rhsNodeString+");\n");
					stmt.setRuntimeAllocate(rtc1);
					RuntimeAllocate rtc2 = new RuntimeAllocate();
					rtc2.setBlock("allocate("+targetName+"("+targetName+"_RTC(1),"+targetName
							+"_RTC(2)));\n");
					stmt.setRuntimeAllocate(rtc2);
					List<DimValue> shape = new ArrayList<DimValue>();
					shape.add(new DimValue(2, null));
					BasicMatrixValue tmp = new BasicMatrixValue(null, PrimitiveClassReference.INT8, 
							(new ShapeFactory()).newShapeFromDimValues(shape), null);
					fcg.tmpVariables.put(targetName+"_RTC",tmp);
				}
				else if (!lhsShapeIsknown) {
					RuntimeAllocate rtc = new RuntimeAllocate();
					rtc.setBlock("allocate("+targetName+"("+rhsVariableDimension.toString()
							.replace("[", "").replace("]", "")+"));\n  ");
					stmt.setRuntimeAllocate(rtc);
				}
			} catch (Exception e) {
				System.err.println("error in HandleCaseTIRAbstractAssignToVarStmt.java");
			}
		}
		return stmt;
	}
}

package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.*;
import natlab.tame.valueanalysis.components.constant.Constant;
import natlab.tame.valueanalysis.components.shape.ShapeFactory;
import natlab.tame.classes.reference.*;

public class HandleCaseTIRAbstractAssignToVarStmt {

	static boolean Debug = false;
	static boolean lhsShapeIsknown = true;
	static boolean rhsShapeIsKnown = true;
	
	public HandleCaseTIRAbstractAssignToVarStmt(){
		
	}
	/**
	 * AbstractAssignToVarStmt: Statement ::= <RuntimeCheck> <TargetVariable> <SourceVariable>;
	 * for each statement, currently, we need to insert two check:
	 * 1. for rhs, constant variable replacement check;
	 * 2. for lhs, do we need to inline allocate code check.
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRAbstractAssignToVarStmt node){
		if (Debug) System.out.println("in an abstractAssignToVar statement");
		
		AbstractAssignToVarStmt stmt = new AbstractAssignToVarStmt();
		String indent = new String();
		for(int i=0; i<fcg.indentNum; i++){
			indent = indent + fcg.indent;
		}
		stmt.setIndent(indent);
		/**
		 * for lhs, insert function name variable replacement check.
		 */
		if(fcg.isSubroutine==true){
			/**
			 * if input argument on the LHS of assignment stmt, we assume that this input argument maybe modified.
			 */
			if(fcg.inArgs.contains(node.getTargetName().getID())){
				if (Debug) System.out.println("subroutine's input "+node.getTargetName().getID()+" has been modified!");
				/**
				 * here we need to detect whether it is the first time this variable put in the set,
				 * because we only want to back up them once.
				 */
				if(fcg.inputHasChanged.contains(node.getTargetName().getID())){
					//do nothing.
					if (Debug) System.out.println("encounter "+node.getTargetName().getID()+" again.");
				}
				else{
					if (Debug) System.out.println("first time encounter "+node.getTargetName().getID());
					fcg.inputHasChanged.add(node.getTargetName().getID());
				}
				stmt.setTargetVariable(node.getTargetName().getID()+"_copy");
			}
			else{
				stmt.setTargetVariable(node.getTargetName().getID());
			}
		}
		else{
			if(fcg.outRes.contains(node.getTargetName().getID())){
				stmt.setTargetVariable(fcg.majorName);
			}
			else{
				stmt.setTargetVariable(node.getTargetName().getID());
			}
		}
		/**
		 * for rhs, insert constant variable replacement check.
		 */
		if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet()
				.get(node.getRHS().getNodeString()).getSingleton())).isConstant()
				&&(fcg.inArgs.contains(node.getRHS().getNodeString())==false)){
			if (Debug) System.out.println(node.getTargetName().getID()+" is a constant");
			Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(node.getRHS().getNodeString()).getSingleton())).getConstant();
			stmt.setSourceVariable(c.toString());
		}
		else{
			if(fcg.inputHasChanged.contains(node.getRHS().getNodeString())){
				stmt.setSourceVariable(node.getRHS().getNodeString()+"_copy");
			}
			else{
				stmt.setSourceVariable(node.getRHS().getNodeString());
			}
		}
		/**
		 * for lhs, insert runtime shape allocate check.
		 * TODO need more concerns.
		 */
		if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet()
				.get(node.getTargetName().getID()).getSingleton())).isConstant()){
			if (Debug) System.out.println(node.getTargetName().getID()+" is a constant");
		}
		else{
			ArrayList<Integer> lhsVariableDimension = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getTargetName().getID()).getSingleton())).getShape().getDimensions());
			ArrayList<Integer> rhsVariableDimension = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList()
					.get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getRHS().getNodeString()).getSingleton())).getShape().getDimensions());
			try{
				for(Integer intgrL : lhsVariableDimension){
					//if lhs variable's shape is not exactly known, we need allocate it first.
					if(intgrL==null){
						System.out.println("The shape of "+node.getTargetName().getID()+" is not exactly known, we need allocate it first");
						lhsShapeIsknown = false;
					}
				}
				for(Integer intgrR : rhsVariableDimension){
					if(intgrR==null){
						System.out.println("The shape of "+node.getRHS().getNodeString()+" is not exactly konwn, we cannot allocate "+node.getTargetName().getID()+" with "+node.getRHS().getNodeString()+"'s shape, " +
								"we need runtime check "+node.getRHS().getNodeString()+"'s shape first.");
						rhsShapeIsKnown = false;
					}
				}
				//TODO inline runtime check code for RHS
				if((lhsShapeIsknown == false)&&(rhsShapeIsKnown == false)){
					RuntimeCheck rtc1 = new RuntimeCheck();
					rtc1.setBlock(node.getTargetName().getID()+"_RTC = shape("+node.getRHS().getNodeString()+");\n");
					stmt.setRuntimeCheck(rtc1);
					RuntimeCheck rtc2 = new RuntimeCheck();
					rtc2.setBlock("allocate("+node.getTargetName().getID()+"("+node.getTargetName().getID()+"_RTC(1),"+node.getTargetName().getID()+"_RTC(2)));\n");
					stmt.setRuntimeCheck(rtc2);
					ArrayList<Integer> shape = new ArrayList<Integer>();
					shape.add(2);
					BasicMatrixValue tmp = 
							new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory<AggrValue<BasicMatrixValue>>()).newShapeFromIntegers(shape));
					fcg.tmpVariables.put(node.getTargetName().getID()+"_RTC",tmp);
				}
				else if(lhsShapeIsknown == false){
					RuntimeCheck rtc = new RuntimeCheck();
					rtc.setBlock("allocate("+node.getTargetName().getID()+"("+rhsVariableDimension.toString().replace("[", "").replace("]", "")+"));\n  ");
					stmt.setRuntimeCheck(rtc);
				}
			}
			catch(Exception e){
				System.err.println("error in HandleCaseTIRAbstractAssignToVarStmt.java");
			}
			/*String type = fcg.FortranMap.getFortranTypeMapping(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getTargetName().getID()).getSingleton())).getMatlabClass().toString());
			if(type == "String"){
				//type = makeFortranStringLiteral(type);
				fcg.buf.append(fcg.makeFortranStringLiteral(node.getRHS().getNodeString()) + ";");
			}
			else
				fcg.buf.append(node.getRHS().getNodeString() + ";");*/
		}
		return stmt;
	}
}

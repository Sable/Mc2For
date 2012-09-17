package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.ShapeFactory;
import natlab.tame.classes.reference.*;

public class ASTHandleCaseTIRAbstractAssignToVarStmt {

	static boolean Debug = false;
	static boolean lhsShapeIsknown = true;
	static boolean rhsShapeIsKnown = true;
	
	public ASTHandleCaseTIRAbstractAssignToVarStmt(){
		
	}
	/**
	 * AbstractAssignToVarStmt: Statement ::= <RuntimeCheck> <TargetVariable> <SourceVariable>;
	 */
	public Statement getFortran(FortranCodeASTGenerator fcg, TIRAbstractAssignToVarStmt node){
		if (Debug) System.out.println("in an abstractAssignToVar statement");
		
		AbstractAssignToVarStmt stmt = new AbstractAssignToVarStmt();
		stmt.setTargetVariable(node.getTargetName().getID());
		stmt.setSourceVariable(node.getRHS().getNodeString());
		
		if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getTargetName().getID()).getSingleton())).isConstant()){
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
					rtc1.setName(node.getTargetName().getID()+"_RTC = shape("+node.getRHS().getNodeString()+");\n");
					stmt.setRuntimeCheck(rtc1);
					RuntimeCheck rtc2 = new RuntimeCheck();
					rtc2.setName("allocate("+node.getTargetName().getID()+"("+node.getTargetName().getID()+"_RTC(1),"+node.getTargetName().getID()+"_RTC(2)));\n");
					stmt.setRuntimeCheck(rtc2);
					ArrayList<Integer> shape = new ArrayList<Integer>();
					shape.add(2);
					BasicMatrixValue tmp = 
							new BasicMatrixValue(new BasicMatrixValue(PrimitiveClassReference.INT8),(new ShapeFactory()).newShapeFromIntegers(shape));
					fcg.tmpVariables.put(node.getTargetName().getID()+"_RTC",tmp);
				}
				else if(lhsShapeIsknown == false){
					RuntimeCheck rtc = new RuntimeCheck();
					rtc.setName("allocate("+node.getTargetName().getID()+"("+rhsVariableDimension.toString().replace("[", "").replace("]", "")+"));\n  ");
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

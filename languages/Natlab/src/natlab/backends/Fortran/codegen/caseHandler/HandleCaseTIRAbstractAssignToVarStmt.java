package natlab.backends.Fortran.codegen.caseHandler;

import java.util.ArrayList;

import natlab.backends.Fortran.codegen.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;

public class HandleCaseTIRAbstractAssignToVarStmt {

	static boolean Debug = false;
	
	public HandleCaseTIRAbstractAssignToVarStmt(){
		
	}
	
	public FortranCodeGenerator getFortran(FortranCodeGenerator fcg, TIRAbstractAssignToVarStmt node){
		if (Debug) System.out.println("in an abstractAssignToVar statement");
		String LHS;
		//ArrayList<String> vars = new ArrayList<String>();
		LHS = node.getTargetName().getID();
		if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).isConstant()){
			if (Debug) System.out.println(LHS+" is a constant");
		}
		else{
			ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).getShape().getDimensions());
			ArrayList<Integer> dimRHS = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(node.getRHS().getNodeString()).getSingleton())).getShape().getDimensions());
			try{
				for(Integer intgr : dim){
					String test = intgr.toString();
				}
			}
			catch(Exception e){
				fcg.buf.append("      allocate("+LHS+"("+dimRHS.toString().replace("[", "").replace("]", "")+"));\n  ");
			}
			String type = fcg.FortranMap.getFortranTypeMapping(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).getMatlabClass().toString());
			fcg.buf.append("      "+node.getLHS().getNodeString()+" = ");
			if(type == "String"){
				//type = makeFortranStringLiteral(type);
				fcg.buf.append(fcg.makeFortranStringLiteral(node.getRHS().getNodeString()) + ";");
			}
			else
				fcg.buf.append(node.getRHS().getNodeString() + ";");
			//TODO check for expression on RHS
			//TODO check for built-ins
			//TODO check for operators
		}
		return fcg;
	}
}

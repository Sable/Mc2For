package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ast.Name;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.backends.Fortran.codegen.*;

public class CaseNewSubroutine {
	static boolean Debug = false;
	
	public CaseNewSubroutine(){
		
	}
	
	public FortranCodeASTGenerator newSubroutine(FortranCodeASTGenerator fcg, TIRFunction node){
		fcg.isSubroutine = true;
		if (Debug) System.out.println("this is a subroutine");
		fcg.iterateStatements(node.getStmts());
		fcg.buf.append("return\nend");
		
		fcg.buf2.append("subroutine ");
		fcg.buf2.append(fcg.majorName);
		fcg.buf2.append("(");
		boolean first = true;
		for(Name param : node.getInputParams()){
			if(!first) {
				fcg.buf2.append(", ");
			}
			fcg.buf2.append(param.getVarName());
			first = false;
		}
		fcg.buf2.append(", ");
		first = true;
		for(Name res : node.getOutputParams()){
			if(!first) {
				fcg.buf2.append(", ");
			}
			fcg.buf2.append(res.getVarName());
			first = false;
		}
		fcg.buf2.append(")");
		fcg.buf2.append("\nimplicit none");
		
		if (Debug) System.out.println(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().keySet()+"\n");
		
		for(String variable : fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().keySet()){
			/**
			 * first, deal with for-loop statement variables...Fortran must declare them as integer.
			 */
			
			if (Debug) System.out.println("the parameters in for stmt: "+fcg.forStmtParameter);
			
			if(fcg.forStmtParameter.contains(variable)){
				if (Debug) System.out.println("variable "+variable+" is a for stmt parameter.");
				if (Debug) System.out.println(variable + " = " + fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\ncomplex");
				}
				else{
					buf.append("\n" + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				fcg.buf2.append("\n" + fcg.FortranMap.getFortranTypeMapping("int8"));
				//parameter
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
					if (Debug) System.out.println("add parameter here!");
					fcg.buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}
				else{
					fcg.buf2.append(" :: " + variable);
				}
			}
			/**
			 * general situations...
			 */
			else{
				if (Debug) System.out.println(variable + " = " + fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\ncomplex");
				}
				else{
					buf.append("\n" + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				fcg.buf2.append("\n" + fcg.FortranMap.getFortranTypeMapping(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				//parameter
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()&&(fcg.inArgs.contains(variable)==false)&&(fcg.outRes.contains(variable)==false)){
					if (Debug) System.out.println("add parameter here!");
					fcg.buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}
				else{
					//dimension
					if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
						if (Debug) System.out.println("add dimension here!");
						fcg.buf2.append(" , dimension(");
						ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
						if (Debug) System.out.println(dim);
						boolean conter = false;
						boolean variableShapeIsKnown = true;
						for(Integer intgr : dim){
							if(intgr==null){
								if (Debug) System.out.println("The shape of "+variable+" is not exactly known, we need allocate it first");
								variableShapeIsKnown = false;
							}
						}
						/**
						 * if one of the dimension is unknown, which value is null, goes to if block.
						 */
						if(variableShapeIsKnown==false){
							for(int i=1; i<=dim.size(); i++){
								if(conter){
									fcg.buf2.append(",");
								}
								fcg.buf2.append(":");
								conter = true;
							}
							fcg.buf2.append(") , allocatable :: " + variable);
						}
						
						/**
						 * if all the dimension is exactly known, which values are all integer, goes to else block.
						 */
						else{
							for(Integer inte : dim){
								if(conter){
									fcg.buf2.append(",");
								}
								fcg.buf2.append(inte.toString());
								conter = true;
							}
							fcg.buf2.append(")");
							if(fcg.outRes.contains(variable)){
								fcg.buf2.append(" :: " + fcg.majorName);
							}
							else{
								fcg.buf2.append(" :: " + variable);
							}
						}
					}
					else{
						/**
						 * for subroutines, it's different from which in functions.
						 */
						if(fcg.inArgs.contains(variable)){
							fcg.buf2.append(" , intent(in)");
						}
						else if(fcg.outRes.contains(variable)){
							fcg.buf2.append(" , intent(out)");
						}
						fcg.buf2.append(" :: " + variable);
					}
				}
			}
		}
		fcg.buf2.append("\n");
		fcg.buf2.append(fcg.buf);
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(fcg.fileDir+node.getName()+".f95"));
			out.write(fcg.buf2.toString());
			out.close();
		}
		catch(IOException e){
			System.err.println("Exception ");

		}
		return fcg;
	}
}

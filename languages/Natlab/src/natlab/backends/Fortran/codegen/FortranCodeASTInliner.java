package natlab.backends.Fortran.codegen;

import java.util.ArrayList;

import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.tir.TIRAbstractAssignToListStmt;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.shape.ShapeFactory;
import natlab.tame.valueanalysis.components.constant.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.backends.Fortran.codegen.ASTcaseHandler.*;

public class FortranCodeASTInliner {

	public FortranCodeASTInliner(){
		
	}
	
	public static NoDirectBuiltinExpr inline(FortranCodeASTGenerator fcg, TIRAbstractAssignToListStmt node){
		NoDirectBuiltinExpr noDirBuiltinExpr = new NoDirectBuiltinExpr();
		
		if(node.getRHS().getVarName().equals("horzcat")){
			String indent = new String();
			for(int i=0; i<fcg.indentNum; i++){
				indent = indent + fcg.indent;
			}
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			StringBuffer tmpBuf = new StringBuffer();
			for(int i=1; i<=argsNum; i++){
				/**
				 * need constant variable replacement check.
				 */
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(i-1)).getSingleton())).isConstant()){
					Constant c = ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(args.get(i-1)).getSingleton())).getConstant();
					tmpBuf.append(indent+LHS+"(1,"+i+") = "+c+";");
					if(i<argsNum){
						tmpBuf.append("\n");	
					}	
				}
				else{
					tmpBuf.append(indent+LHS+"(1,"+i+") = "+args.get(i-1)+";");
					if(i<argsNum){
						tmpBuf.append("\n");	
					}					
				}
			}
			noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
		}
		
		else if(node.getRHS().getVarName().equals("vertcat")){
			String indent = new String();
			for(int i=0; i<fcg.indentNum; i++){
				indent = indent + fcg.indent;
			}
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			StringBuffer tmpBuf = new StringBuffer();
			for(int i=1; i<=argsNum; i++){
				/**
				 * need constant variable replacement check.
				 */
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(i-1)).getSingleton())).isConstant()){
					DoubleConstant c = (DoubleConstant) ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(args.get(i-1)).getSingleton())).getConstant();
					tmpBuf.append(indent+LHS+"("+i+",1) = "+c.toString()+";");
				}
				else{
					tmpBuf.append(indent+LHS+"("+i+",:) = "+args.get(i-1)+"(1,:);");					
				}
				if(i<argsNum){
					tmpBuf.append("\n");
				}
			}
			noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
		}
		
		else if(node.getRHS().getVarName().equals("ones")){
			String indent = new String();
			for(int i=0; i<fcg.indentNum; i++){
				indent = indent + fcg.indent;
			}
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			StringBuffer tmpBuf = new StringBuffer();
			/*do i = 1 , 10
			    do j = 1 , 5
		  	      a(i,j) = 1;
		   	    enddo
		      enddo
		     */
			/**
			 * need constant variable replacement check.
			 */
			if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(args.get(0)).getSingleton())).isConstant()){
				DoubleConstant c = (DoubleConstant) ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(0)).getSingleton())).getConstant();
				int ci = c.getValue().intValue();
				tmpBuf.append(indent+"do tmp_"+LHS+"_i = 1 , "+ci+"\n");
			}
			else{
				tmpBuf.append(indent+"do tmp_"+LHS+"_i = 1 , "+args.get(0)+"\n");
			}
			if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(args.get(1)).getSingleton())).isConstant()){
				DoubleConstant c = (DoubleConstant)((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(1)).getSingleton())).getConstant();
				int ci = c.getValue().intValue();
				tmpBuf.append(indent+fcg.indent+"do tmp_"+LHS+"_j = 1 , "+ci+"\n");
			}
			else{
				tmpBuf.append(indent+fcg.indent+"do tmp_"+LHS+"_j = 1 , "+args.get(1)+"\n");
			}
			tmpBuf.append(indent+fcg.indent+fcg.indent+LHS+"(tmp_"+LHS+"_i,tmp_"+LHS+"_j) = 1;\n");
			tmpBuf.append(indent+fcg.indent+"enddo\n");
			tmpBuf.append(indent+"enddo");
			
			ArrayList<Integer> shape = new ArrayList<Integer>();
			shape.add(1);
			shape.add(1);
			BasicMatrixValue tmp = 
					new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory()).newShapeFromIntegers(shape));
			fcg.tmpVariables.put("tmp_"+LHS+"_i", tmp);
			fcg.tmpVariables.put("tmp_"+LHS+"_j", tmp);
			fcg.forStmtParameter.add(args.get(0));
			fcg.forStmtParameter.add(args.get(1));
			noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
		}
		
		else if(node.getRHS().getVarName().equals("zeros")){
			String indent = new String();
			for(int i=0; i<fcg.indentNum; i++){
				indent = indent + fcg.indent;
			}
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			StringBuffer tmpBuf = new StringBuffer();
			/*do i = 1 , 10
			    do j = 1 , 5
		  	      a(i,j) = 0;
		   	    enddo
		      enddo
		     */
			/**
			 * need constant variable replacement check.
			 */
			if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(args.get(0)).getSingleton())).isConstant()){
				DoubleConstant c = (DoubleConstant) ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(0)).getSingleton())).getConstant();
				int ci = c.getValue().intValue();
				tmpBuf.append(indent+"do tmp_"+LHS+"_i = 1 , "+ci+"\n");
			}
			else{
				tmpBuf.append(indent+"do tmp_"+LHS+"_i = 1 , "+args.get(0)+"\n");
			}
			if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
					get(args.get(1)).getSingleton())).isConstant()){
				DoubleConstant c = (DoubleConstant)((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(1)).getSingleton())).getConstant();
				int ci = c.getValue().intValue();
				tmpBuf.append(indent+fcg.indent+"do tmp_"+LHS+"_j = 1 , "+ci+"\n");
			}
			else{
				tmpBuf.append(indent+fcg.indent+"do tmp_"+LHS+"_j = 1 , "+args.get(1)+"\n");
			}
			tmpBuf.append(indent+fcg.indent+fcg.indent+LHS+"(tmp_"+LHS+"_i,tmp_"+LHS+"_j) = 0;\n");
			tmpBuf.append(indent+fcg.indent+"enddo\n");
			tmpBuf.append(indent+"enddo");
			
			ArrayList<Integer> shape = new ArrayList<Integer>();
			shape.add(1);
			shape.add(1);
			BasicMatrixValue tmp = 
					new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory()).newShapeFromIntegers(shape));
			fcg.tmpVariables.put("tmp_"+LHS+"_i", tmp);
			fcg.tmpVariables.put("tmp_"+LHS+"_j", tmp);
			fcg.forStmtParameter.add(args.get(0));
			fcg.forStmtParameter.add(args.get(1));
			noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
		}
		
		else if(node.getRHS().getVarName().equals("colon")){
			/*
			 * a=1:10 -> a=[1,2,3,...,10]
			 * a=1:2:10 -> a=[1,3,5,...,9]
			 * so, depends on the number of input parameters, there are two transformations.
			 */
			String indent = new String();
			for(int i=0; i<fcg.indentNum; i++){
				indent = indent + fcg.indent;
			}
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			if(argsNum==2){
				StringBuffer tmpBuf = new StringBuffer();
				/* a = arg1:arg2
				 * -->
				 * do tmp_a_i = arg1,arg2
			  	 *   a(1,tmp_a_i) = tmp_a_i;
			     * enddo
			     */
				/**
				 * need constant variable replacement check.
				 */
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(0)).getSingleton())).isConstant()){
					DoubleConstant c = (DoubleConstant) ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(args.get(0)).getSingleton())).getConstant();
					int ci = c.getValue().intValue();
					tmpBuf.append(indent+"do tmp_"+LHS+"_i = "+ci);
				}
				else{
					tmpBuf.append(indent+"do tmp_"+LHS+"_i = "+args.get(0));
				}
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(1)).getSingleton())).isConstant()){
					DoubleConstant c = (DoubleConstant)((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(args.get(1)).getSingleton())).getConstant();
					int ci = c.getValue().intValue();
					tmpBuf.append(","+ci+"\n");
				}
				else{
					tmpBuf.append(","+args.get(1)+"\n");
				}
				tmpBuf.append(fcg.indent+fcg.indent+LHS+"(1,tmp_"+LHS+"_i) = tmp_"+LHS+"_i;\n");
				tmpBuf.append(indent+"enddo");
				
				ArrayList<Integer> shape = new ArrayList<Integer>();
				shape.add(1);
				shape.add(1);
				BasicMatrixValue tmp = 
						new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory()).newShapeFromIntegers(shape));
				fcg.tmpVariables.put("tmp_"+LHS+"_i", tmp);
				fcg.forStmtParameter.add(args.get(0));
				fcg.forStmtParameter.add(args.get(1));
				noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
			}
			else if(argsNum==3){
				StringBuffer tmpBuf = new StringBuffer();
				/* a = lower:inc:upper
				 * -->
				 * tmp_a_index = 1;
				 * do tmp_a_i = lower,upper,inc
			  	 *   a(1,tmp_a_index) = tmp_a_i;
			  	 *   tmp_a_index=tmp_a_index+1;
			     * enddo
			     */
				tmpBuf.append("tmp_"+LHS+"_index = 1;\n");
				/**
				 * need constant variable replacement check.
				 */
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(0)).getSingleton())).isConstant()){
					DoubleConstant c = (DoubleConstant) ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(args.get(0)).getSingleton())).getConstant();
					int ci = c.getValue().intValue();
					tmpBuf.append(indent+"do tmp_"+LHS+"_i = "+ci);
				}
				else{
					tmpBuf.append(indent+"do tmp_"+LHS+"_i = "+args.get(0));
				}
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(2)).getSingleton())).isConstant()){
					DoubleConstant c = (DoubleConstant)((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(args.get(2)).getSingleton())).getConstant();
					int ci = c.getValue().intValue();
					tmpBuf.append(","+ci);
				}
				else{
					tmpBuf.append(","+args.get(1));
				}
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
						get(args.get(1)).getSingleton())).isConstant()){
					DoubleConstant c = (DoubleConstant)((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().
							get(args.get(1)).getSingleton())).getConstant();
					int ci = c.getValue().intValue();
					tmpBuf.append(","+ci+"\n");
				}
				else{
					tmpBuf.append(","+args.get(1)+"\n");
				}
				tmpBuf.append(fcg.indent+fcg.indent+LHS+"(1,tmp_"+LHS+"_index) = tmp_"+LHS+"_i;\n");
				tmpBuf.append(fcg.indent+fcg.indent+"tmp_"+LHS+"_index = tmp_"+LHS+"_index+1;\n");
				tmpBuf.append(indent+"enddo");
				
				ArrayList<Integer> shape = new ArrayList<Integer>();
				shape.add(1);
				shape.add(1);
				BasicMatrixValue tmp = 
						new BasicMatrixValue(PrimitiveClassReference.INT8,(new ShapeFactory()).newShapeFromIntegers(shape));
				fcg.tmpVariables.put("tmp_"+LHS+"_i", tmp);
				fcg.tmpVariables.put("tmp_"+LHS+"_index", tmp);
				fcg.forStmtParameter.add(args.get(0));
				fcg.forStmtParameter.add(args.get(1));
				fcg.forStmtParameter.add(args.get(2));
				noDirBuiltinExpr.setCodeInline(tmpBuf.toString());
			}
		}
		
		else if(node.getRHS().getVarName().equals("randperm")){
			/*
			 * a=randperm(6) will get a=[1,4,3,6,5,2],
			 * 
			 */
			String indent = new String();
			for(int i=0; i<fcg.indentNum; i++){
				indent = indent + fcg.indent;
			}
			String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			ArrayList<String> args = new ArrayList<String>();
			args = HandleCaseTIRAbstractAssignToListStmt.getArgsList(node);
			int argsNum = args.size();
			StringBuffer tmpBuf = new StringBuffer();
		}
		
		else{
			/**
			 * for those no direct builtins which not be implemented yet 
			 */
			noDirBuiltinExpr.setCodeInline("!    the built-in function \""+node.getRHS().getVarName()+"\" has not been implemented yet, fix it!");
		}
		return noDirBuiltinExpr;
	}
}

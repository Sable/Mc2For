package natlab.backends.Fortran.codegen;

import ast.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


import natlab.tame.classes.reference.ClassReference;
import natlab.tame.tir.*;

import natlab.tame.tir.analysis.TIRAbstractNodeCaseHandler;
import natlab.tame.valueanalysis.ValueAnalysis;
import natlab.tame.valueanalysis.ValueAnalysisPrinter;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.backends.Fortran.codegen.FortranMapping;

public class FortranCodeGenerator extends TIRAbstractNodeCaseHandler{
	ValueAnalysis<AggrValue<BasicMatrixValue>> analysis;
	private StringBuffer buf;
	private StringBuffer buf2;
	private FortranMapping FortranMap;
	private ArrayList<String> forStmtParameter;
	private ArrayList<String> arrayIndexParameter;
	private int callgraphSize;
	private int index;
	private String fileDir;
	private String majorName;
	private ArrayList<String> inArgs;
	private ArrayList<String> outRes;
	private HashMap<String, String> funcNameRep;//the key of this hashmap is the user defined function name, and the value is the corresponding substitute variable name.
	private boolean indentIf;
	private boolean indentFW;
	private boolean isSubroutine;//this boolean value help the compiler to distinguish subroutine with function.
	static boolean Debug = false;
	
	private FortranCodeGenerator(ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir){
		this.analysis = analysis;
		this.buf = new StringBuffer();
		this.buf2 = new StringBuffer();
		this.FortranMap = new FortranMapping();
		this.forStmtParameter = new ArrayList<String>();
		this.arrayIndexParameter = new ArrayList<String>();
		this.callgraphSize = callgraphSize;
		this.index = index;
		this.fileDir = fileDir;
		this.majorName = "";
		this.inArgs = new ArrayList<String>();
		this.outRes = new ArrayList<String>();
		this.funcNameRep = new HashMap<String,String>();
		this.indentIf = false;
		this.indentFW = false;
		this.isSubroutine = false;
		((TIRNode)analysis.getNodeList().get(index).getAnalysis().getTree()).tirAnalyze(this);
	}
	
	
	public static String FortranCodePrinter(
			ValueAnalysis<AggrValue<BasicMatrixValue>> analysis, int callgraphSize, int index, String fileDir){
		return new FortranCodeGenerator(analysis, callgraphSize, index, fileDir).buf2.toString();
	}
	
	
	@Override
	public void caseASTNode(ASTNode node){}
	
	@Override
	public void caseTIRFunction(TIRFunction node){
		this.majorName = node.getName();
		for(Name param : node.getInputParams()){
			inArgs.add(param.getVarName());
		}
		for(Name result : node.getOutputParams()){
			outRes.add(result.getVarName());
		}
		/**
		 *deal with main entry point, main program, actually, sometimes, a subroutine can also be with 0 output...
		 *TODO think of a better way to distinguish whether it is a main entry point or a 0-output subroutine... 
		 */
		if(outRes.size()==0){
			String indent = node.getIndent();
			boolean first = true;;
			//buf.append(indent + "public static def " );
			printStatements(node.getStmts());
			//Write code for nested functions here
			//buf.append(indent + "}//end of function\n}//end of class\n");
			buf.append(indent + "      stop\n      end");
			
			if (Debug) System.out.println("the parameters in for stmt: "+forStmtParameter);
			
			buf2.append(indent + "      program ");
			// TODO - CHANGE IT TO DETERMINE RETURN TYPE		
			buf2.append(majorName);
			buf2.append("\n      implicit none");
			/*buf.append("(");
			first = true;
			for(Name param : node.getInputParams()){
				if(!first){
					buf.append(", ");
				}
				buf.append(param.getPrettyPrinted()+": "+FortranMap.getFortranTypeMapping(getArgumentType(analysis, node, param.getID())) );
				symbolMap.put(param.getID().toString(), getAnalysisValue(analysis, node, param.getID()));
				first = false;
			}*/
			
			//System.out.println(this.analysis.getNodeList().get(index).getAnalysis().getOutFlowSets());
			if (Debug) System.out.println(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()+"\n");
			
			for(String variable : this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()){
				if(forStmtParameter.contains(variable)||arrayIndexParameter.contains(variable)){
					if (Debug) System.out.println("variable "+variable+" is a for stmt parameter.");
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping("int8"));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						buf2.append(" :: " + variable);
					}
				}
				else{
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						//dimension
						if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
							if (Debug) System.out.println("add dimension here!");
							buf2.append(" , dimension(");
							ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
							boolean conter = false;
							/**
							 * if one of the dimension is unknown, which value is null, goes to catch.
							 */
							try{
								for(Integer intgr : dim){
									String test = intgr.toString();
								}
							}
							catch(Exception e){
								for(int i=1; i<=dim.size(); i++){
									if(conter){
										buf2.append(",");
									}
									buf2.append(":");
									conter = true;
								}
								buf2.append(") , allocatable :: " + variable);
								break;
							}
							/**
							 * if all the dimension is exactly known, which values are all integer, goes to here.
							 */
							for(Integer intgr : dim){
								if(conter){
									buf2.append(",");
								}
								buf2.append(intgr.toString());
								conter = true;
							}
							buf2.append(")");
							buf2.append(" :: " + variable);
						}
						else{
							buf2.append(" :: " + variable);
						}
					}
				}
			}
			/**
			 * at the end of declaration, declare those user defined function.
			 */
			for(String key : funcNameRep.keySet()){
				System.out.println(key);
				String variable = funcNameRep.get(key);
				if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\n      complex");
				}
				else{
					buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				buf2.append("\n      " + FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				//parameter
				if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
					if (Debug) System.out.println("add parameter here!");
					buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}
				else{
					//dimension
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
						if (Debug) System.out.println("add dimension here!");
						buf2.append(" , dimension(");
						ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
						if (Debug) System.out.println(dim);
						boolean conter = false;
						/**
						 * if one of the dimension is unknown, which value is null, goes to catch.
						 */
						try{
							for(Integer intgr : dim){
								String test = intgr.toString();
							}
						}
						catch(Exception e){
							for(int i=1; i<=dim.size(); i++){
								if(conter){
									buf2.append(",");
								}
								buf2.append(":");
								conter = true;
							}
							buf2.append(") , allocatable :: " + variable);
							break;
						}
						/**
						 * if all the dimension is exactly known, which values are all integer, goes to here.
						 */
						for(Integer inte : dim){
							if(conter){
								buf2.append(",");
							}
							buf2.append(inte.toString());
							conter = true;
						}
						buf2.append(")");
						buf2.append(" :: " + key);
					}
					else{
						buf2.append(" :: " + key);
					}
				}
			}
			
			buf2.append("\n");
			buf2.append(buf);
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(fileDir+majorName+".f"));
				out.write(buf2.toString());
				out.close();
			}
			catch(IOException e){
				System.out.println("Exception ");

			}
		}
		/**
		 * deal with functions, not subroutine, because in Fortran, functions can essentially only return one value.
		 * actually, I can also convert 1-output functions in Matlab to subroutines...
		 */
		else if(outRes.size()==1){
			String indent = node.getIndent();
			boolean first = true;
			
			printStatements(node.getStmts());
			buf.append(indent + "      return\n      end");
			
			if (Debug) System.out.println("the parameters in for stmt: "+forStmtParameter);
			
			buf2.append(indent + "      function ");
			buf2.append(majorName);
			buf2.append("(");
			first = true;
			for(Name param : node.getInputParams()) {
				if(!first) {
					buf2.append(", ");
				}
				buf2.append(param.getVarName());
				first = false;
			}
			buf2.append(")");
			buf2.append("\n      implicit none");
			
			if (Debug) System.out.println(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()+"\n");
			
			for(String variable : this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()){
				/**
				 * deal with for statement variables...Fortran must declare them integer.
				 */
				if(forStmtParameter.contains(variable)){
					if (Debug) System.out.println("variable "+variable+" is a for stmt parameter.");
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping("int8"));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						buf2.append(" :: " + variable);
					}
				}
				/**
				 * general situations...
				 */
				else{
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()&&(inArgs.contains(variable)==false)&&(outRes.contains(variable)==false)){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						//dimension
						if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
							if (Debug) System.out.println("add dimension here!");
							buf2.append(" , dimension(");
							ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
							if (Debug) System.out.println(dim);
							boolean conter = false;
							/**
							 * if one of the dimension is unknown, which value is null, goes to catch.
							 */
							try{
								for(Integer intgr : dim){
									String test = intgr.toString();
								}
							}
							catch(Exception e){
								for(int i=1; i<=dim.size(); i++){
									if(conter){
										buf2.append(",");
									}
									buf2.append(":");
									conter = true;
								}
								buf2.append(") , allocatable :: " + variable);
								break;
							}
							/**
							 * if all the dimension is exactly known, which values are all integer, goes to here.
							 */
							for(Integer inte : dim){
								if(conter){
									buf2.append(",");
								}
								buf2.append(inte.toString());
								conter = true;
							}
							buf2.append(")");
							if(outRes.contains(variable)){
								buf2.append(" :: " + majorName);
							}
							else{
								buf2.append(" :: " + variable);
							}
						}
						else{
							if (Debug) System.out.println("Is this variable in the output parameters set: "+outRes.contains(variable));
							if(outRes.contains(variable)){
								buf2.append(" :: " + majorName);
							}
							else{
								buf2.append(" :: " + variable);
							}
						}
					}
				}
			}
			buf2.append("\n");
			buf2.append(buf);
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(fileDir+node.getName()+".f"));
				out.write(buf2.toString());
				out.close();
			}
			catch(IOException e){
				System.out.println("Exception ");

			}
		}
		/**
		 * deal with subroutines, which output can be more than one.
		 */
		else{
			isSubroutine = true;
			if (Debug) System.out.println("this is a subroutine");
			printStatements(node.getStmts());
			buf.append("      return\n      end");
			
			buf2.append("      subroutine ");
			buf2.append(majorName);
			buf2.append("(");
			boolean first = true;
			for(Name param : node.getInputParams()){
				if(!first) {
					buf2.append(", ");
				}
				buf2.append(param.getVarName());
				first = false;
			}
			buf2.append(", ");
			first = true;
			for(Name res : node.getOutputParams()){
				if(!first) {
					buf2.append(", ");
				}
				buf2.append(res.getVarName());
				first = false;
			}
			buf2.append(")");
			buf2.append("\n      implicit none");
			
			if (Debug) System.out.println(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()+"\n");
			
			for(String variable : this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().keySet()){
				/**
				 * first, deal with for-loop statement variables...Fortran must declare them as integer.
				 */
				
				if (Debug) System.out.println("the parameters in for stmt: "+forStmtParameter);
				
				if(forStmtParameter.contains(variable)){
					if (Debug) System.out.println("variable "+variable+" is a for stmt parameter.");
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping("int8"));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						buf2.append(" :: " + variable);
					}
				}
				/**
				 * general situations...
				 */
				else{
					if (Debug) System.out.println(variable + " = " + this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable));
					
					//complex or not others, like real, integer or something else
					/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
						if (Debug) System.out.println("COMPLEX here!");
						buf.append("\n      complex");
					}
					else{
						buf.append("\n      " + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					}*/
					buf2.append("\n      " + FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
					//parameter
					if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()&&(inArgs.contains(variable)==false)&&(outRes.contains(variable)==false)){
						if (Debug) System.out.println("add parameter here!");
						buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
					}
					else{
						//dimension
						if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
							if (Debug) System.out.println("add dimension here!");
							buf2.append(" , dimension(");
							ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
							if (Debug) System.out.println(dim);
							boolean conter = false;
							/**
							 * if one of the dimension is unknown, which value is null, goes to catch.
							 */
							try{
								for(Integer intgr : dim){
									String test = intgr.toString();
								}
							}
							catch(Exception e){
								for(int i=1; i<=dim.size(); i++){
									if(conter){
										buf2.append(",");
									}
									buf2.append(":");
									conter = true;
								}
								buf2.append(") , allocatable :: " + variable);
								break;
							}
							/**
							 * if all the dimension is exactly known, which values are all integer, goes to here.
							 */
							for(Integer inte : dim){
								if(conter){
									buf2.append(",");
								}
								buf2.append(inte.toString());
								conter = true;
							}
							buf2.append(")");
							if(outRes.contains(variable)){
								buf2.append(" :: " + majorName);
							}
							else{
								buf2.append(" :: " + variable);
							}
						}
						else{
							/**
							 * for subroutines, it's different from which in functions.
							 */
							if(inArgs.contains(variable)){
								buf2.append(" , intent(in)");
							}
							else if(outRes.contains(variable)){
								buf2.append(" , intent(out)");
							}
							buf2.append(" :: " + variable);
						}
					}
				}
			}
			buf2.append("\n");
			buf2.append(buf);
			try{
				BufferedWriter out = new BufferedWriter(new FileWriter(fileDir+node.getName()+".f"));
				out.write(buf2.toString());
				out.close();
			}
			catch(IOException e){
				System.out.println("Exception ");

			}
		}
	}
	
	@Override
	public void caseTIRAssignLiteralStmt(TIRAssignLiteralStmt node){
		if (Debug) System.out.println("in an assignLiteral statement");
		String LHS;
		LHS = node.getTargetName().getVarName();
		String RHS;
		if(node.getRHS().getRValue() instanceof IntLiteralExpr){
			RHS = ((IntLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString();
		}
		else{
			RHS = ((FPLiteralExpr)node.getRHS().getRValue()).getValue().getValue().toString();
		}
		if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).isConstant()){
			if (Debug) System.out.println(LHS+" is a constant");
		}
		else{
			ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).getShape().getDimensions());
			try{
				for(Integer intgr : dim){
					String test = intgr.toString();
				}
			}
			catch(Exception e){
				buf.append("      allocate("+LHS+"(1, 1));\n  ");
			}
			buf.append("      "+LHS+" = "+RHS+";");
		}
	}
	
	@Override
	public void caseTIRAbstractAssignToListStmt(TIRAbstractAssignToListStmt node){
		if (Debug) System.out.println("in an abstractAssignToList  statement");
		if(FortranMap.isFortranNoDirectBuiltin(node.getRHS().getVarName())){
			if (Debug) System.out.println("the function \""+node.getRHS().getVarName()+"\" has no corresponding builtin function in Fortran...");
			if(node.getRHS().getVarName().equals("horzcat")){
				String LHS = node.getLHS().getNodeString().replace("[", "").replace("]", "");
				ArrayList<String> Args = new ArrayList<String>();
				Args = GetArgs(node);
				ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).getShape().getDimensions());
				int i=1;
				int length = Args.size();
				for(String Arg : Args){
					buf.append("      "+LHS+"(1,"+i+") = "+Arg+";");
					i = i+1;
					if(i<=length){
					buf.append("\n");	
					}
				}
			}
			//TODO add more no direct mapping built-ins
		}
		else{
			String LHS;
			ArrayList<String> vars = new ArrayList<String>();
			for(ast.Name name : node.getTargets().asNameList()){
				vars.add(name.getID());
			}
			/**
			 * deal with difference number of output.
			 */
			if(vars.size()>1){
				/**
				 * this should be a call statement to call a subroutine.
				 */
				ArrayList<String> Args = new ArrayList<String>();
				String ArgsListasString, OutputsListasString;
				Args = GetArgs(node);
				ArgsListasString = GetArgsListasString(Args);
				OutputsListasString = GetArgsListasString(vars);
				buf.append("      call "+node.getRHS().getVarName()+"("+ArgsListasString+", "+OutputsListasString+")");
			}
			else if(1==vars.size()){
				LHS = vars.get(0);
				if(isSubroutine==true){//which means this statement is in an subroutine
					buf.append("      "+LHS+" = ");
				}
				else{
					if(outRes.contains(LHS)){
						buf.append("      "+majorName + " = ");
					}
					else{
						buf.append("      "+LHS+" = ");
					}
				}
				//use varname to get the name of the method/operator/Var
				makeExpression(node);
			}
			else if(0==vars.size()){
				//TODO
				makeExpression(node);
			}
		}		
	}
	
	@Override
	public void caseTIRAbstractAssignToVarStmt(TIRAbstractAssignToVarStmt node){
		if (Debug) System.out.println("in an abstractAssignToVar statement");
		String LHS;
		//ArrayList<String> vars = new ArrayList<String>();
		LHS = node.getTargetName().getID();
		if(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).isConstant()){
			if (Debug) System.out.println(LHS+" is a constant");
		}
		else{
			ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).getShape().getDimensions());
			ArrayList<Integer> dimRHS = new ArrayList<Integer>(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(node.getRHS().getNodeString()).getSingleton())).getShape().getDimensions());
			try{
				for(Integer intgr : dim){
					String test = intgr.toString();
				}
			}
			catch(Exception e){
				buf.append("      allocate("+LHS+"("+dimRHS.toString().replace("[", "").replace("]", "")+"));\n  ");
			}
			String type = FortranMap.getFortranTypeMapping(((BasicMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(LHS).getSingleton())).getMatlabClass().toString());
			buf.append("      "+node.getLHS().getNodeString()+" = ");
			if(type == "String"){
				//type = makeFortranStringLiteral(type);
				buf.append(makeFortranStringLiteral(node.getRHS().getNodeString()) + ";");
			}
			else
			buf.append(node.getRHS().getNodeString() + ";");
			//TODO check for expression on RHS
			//TODO check for built-ins
			//TODO check for operators
		}
	}
	
	@Override
	public void caseTIRIfStmt(TIRIfStmt node){
		if (Debug) System.out.println("in if statement.");
		if (Debug) System.out.println(node.getConditionVarName().getID());
		buf.append("      if ("+node.getConditionVarName().getID()+") then\n");
		indentIf = true;
		printStatements(node.getIfStameents());
		indentIf = false;
		buf.append("      else\n");
		indentIf = true;
		printStatements(node.getElseStatements());
		indentIf = false;
		buf.append("      endif");
		return;
	}
	
	@Override
	public void caseTIRWhileStmt(TIRWhileStmt node){
		if (Debug) System.out.println("in while statement.");
		if (Debug) System.out.println(node.getCondition().getVarName());
		buf.append("      do while ("+node.getCondition().getVarName()+")\n");
		indentFW = true;
		printStatements(node.getStatements());
		indentFW = false;
		buf.append("      enddo");
		return;
	}
	
	@Override
	public void caseTIRForStmt(TIRForStmt node){
		if (Debug) System.out.println("in for statement.");
		if (Debug) System.out.println(node.getLoopVarName().getVarName());
		buf.append("      do "+node.getLoopVarName().getVarName()+" = "+node.getLowerName().getVarName()+" , "+node.getUpperName().getVarName()+"\n");
		indentFW = true;
		printStatements(node.getStatements());
		indentFW = false;
		buf.append("      enddo");
		forStmtParameter.add(node.getLoopVarName().getVarName());
		forStmtParameter.add(node.getLowerName().getVarName());
		forStmtParameter.add(node.getUpperName().getVarName());
	}
	
	@Override
	public void caseTIRArrayGetStmt(TIRArrayGetStmt node){
		if (Debug) System.out.println("in an arrayget statement!");
		String indexList = node.getIndizes().toString();
		String[] tokens = indexList.replace("[", "").replace("]", "").split("[,]");
		ArrayList<String> tokensAsArray = new ArrayList<String>();
		for(String indexName : tokens){
			tokensAsArray.add(indexName);
		}
		if(tokensAsArray.contains(":")){
			buf.append("      "+node.getLHS().getNodeString().replace("[", "").replace("]", "")+
					"("+node.getIndizes().toString().replace("[", "").replace("]", "")+")"+
					" = "+node.getArrayName().getVarName()+"("+node.getIndizes().toString().replace("[", "").replace("]", "")+")");
		}
		else{
			buf.append("      "+node.getLHS().getNodeString().replace("[", "").replace("]", "")+
					" = "+node.getArrayName().getVarName()+"("+node.getIndizes().toString().replace("[", "").replace("]", "")+")");
		}
		for(String indexName : tokens){
			if(indexName.equals(":")){
				//ignore this
			}
			else{
				arrayIndexParameter.add(indexName);
			}
		}
	}
	
	@Override
	public void caseTIRArraySetStmt(TIRArraySetStmt node){
		if (Debug) System.out.println("in an arrayset statement!");
		buf.append("      "+node.getArrayName().getVarName()+"("+node.getIndizes().toString().replace("[", "").replace("]", "")+")"+" = "+node.getValueName().getVarName()+";");
		for(Name index : node.getIndizes().asNameList()){
			arrayIndexParameter.add(index.getVarName());
		}
	}
	
	@Override
	public void caseTIRCommentStmt(TIRCommentStmt node){
		if (Debug) System.out.println("in a comment statement");
		/**
		 * for Natlab, it consider blank line is also a comment statement.
		 */
		if(node.getNodeString().contains("%")){
			buf.append("c     "+node.getNodeString().replace("%", ""));			
		}
	}

	/**********************HELPER METHODS***********************************/
	private void makeExpression(TIRAbstractAssignStmt node){
		/** 
		 * Change for built-ins with n args.
		 * Currently it handles general case built-ins with one or two args only.
		 */
		int RHSCaseNumber;
		String RHSFortranOperator;
		String Operand1, Operand2, prefix="";
		String ArgsListasString;
		RHSCaseNumber = getRHSCaseNumber(node);
		RHSFortranOperator = getRHSMappingFortranOperator(node);
		//TODO
		Operand1 = getOperand1(node);
		Operand2 = getOperand2(node);
		ArrayList<String> Args = new ArrayList<String>();
		if(Operand2 != "" && Operand2 != null){prefix = ", ";}
		switch(RHSCaseNumber){
		case 1:
			buf.append(Operand1+" "+RHSFortranOperator+" "+Operand2+" ;");
			break;
		case 2:
			buf.append(RHSFortranOperator+""+Operand1+" ;"); //TODO test this
			break;
		case 3:
			Args = GetArgs(node);
			ArgsListasString = GetArgsListasString(Args);
			buf.append(RHSFortranOperator+"("+ArgsListasString+");");
			//buf.append("      " + RHS + ArgsListasString);
			break;
		case 4:
			//TODO, add more similar "method"
			break;
		case 5:
			buf.append(RHSFortranOperator+";");
			break;
		case 6:
			Args = GetArgs(node);
			ArgsListasString = GetArgsListasString(Args);
			buf.append("      "+RHSFortranOperator+ArgsListasString);
			break;
		case 7:
			/**
			 * deal with user defined functions, apparently, there is no corresponding Fortran function for this.
			 */
			String RHSName;
			RHSName = node.getRHS().getVarName();
			String LHSName;
			LHSName = node.getLHS().getNodeString().replace("[", "").replace("]", "");
			//XU, a little bit trick, go back to IR to get a better solution
			funcNameRep.put(RHSName, LHSName);
			Args = GetArgs(node);
			ArgsListasString = GetArgsListasString(Args);
			buf.append(RHSName+"("+ArgsListasString+");");
			break;
		default:
			buf.append("//is it an error?");
			break;
		}
	}
	
	private String getOperand1(TIRAbstractAssignStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 1)
			return node.getRHS().getChild(1).getChild(0).getNodeString();
		else
			return "";
	}
	
	private String getOperand2(TIRAbstractAssignStmt node){
		if(node.getRHS().getChild(1).getNumChild() >= 2)
			return node.getRHS().getChild(1).getChild(1).getNodeString();
		else
			return "";
	}
	
	private int getRHSCaseNumber(TIRAbstractAssignStmt node){
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if(true==FortranMap.isBinOperator(RHSMatlabOperator)){
			return 1; //"binop";
		}
		else if(true==FortranMap.isUnOperator(RHSMatlabOperator)){
		   return 2; //"unop";
		}
		else if(true==FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)){
			return 3; // "builtin";
		}
		else if(true == FortranMap.isMethod(RHSMatlabOperator)){
			return 4; // "method";
		}
		else if(true==FortranMap.isBuiltinConst(RHSMatlabOperator)){
			return 5; // "builtin";
		}
		else if(true==FortranMap.isIOOperation(RHSMatlabOperator)){
			return 6; // "IO OPeration";
		}
		else{
			return 7; // "user defined function";
		}
	}
	
	private String getRHSMappingFortranOperator(TIRAbstractAssignStmt node){
		String RHSFortranOperator;
		String RHSMatlabOperator;
		RHSMatlabOperator = node.getRHS().getVarName();
		if(true==FortranMap.isBinOperator(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranBinOpMapping(RHSMatlabOperator);
		}
		else if(true==FortranMap.isUnOperator(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranUnOpMapping(RHSMatlabOperator);
		}
		
		else if(true==FortranMap.isFortranDirectBuiltin(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranDirectBuiltinMapping(RHSMatlabOperator);
		}
		else if(true==FortranMap.isBuiltinConst(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranBuiltinConstMapping(RHSMatlabOperator);
		}
		else if(true == FortranMap.isMethod(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranMethodMapping(RHSMatlabOperator);
		}
		else if(true == FortranMap.isIOOperation(RHSMatlabOperator)){
			RHSFortranOperator= FortranMap.getFortranIOOperationMapping(RHSMatlabOperator);
		}
		else{
			RHSFortranOperator = "//cannot process it yet";
		}
		return RHSFortranOperator;
	}
	private ArrayList<String> GetArgs(TIRAbstractAssignStmt node){
		ArrayList<String> Args = new ArrayList<String>();
		int numArgs = node.getRHS().getChild(1).getNumChild();
		for (int i=0;i<numArgs;i++){
			Args.add(node.getRHS().getChild(1).getChild(i).getNodeString());
		}
		return Args;
	}

	private String GetArgsListasString(ArrayList<String> Args){
		String prefix ="";
		String ArgListasString="";
		for(String arg : Args){
			ArgListasString = ArgListasString+prefix+arg;
			prefix=", ";
		}
		return ArgListasString;
	}

	private String makeFortranStringLiteral(String StrLit){		
		if(StrLit.charAt(0)=='\'' && StrLit.charAt(StrLit.length()-1)=='\''){			
			return "\""+(String) StrLit.subSequence(1, StrLit.length()-1)+"\"";		
		}
		else
		return StrLit;
	}
	
	private void printStatements(ast.List<ast.Stmt> stmts){
		for(ast.Stmt stmt : stmts){
			if(indentIf == true){
				buf.append("  ");
			}
			else if(indentFW == true){
				buf.append(" ");
			}
			int length = buf.length();
			((TIRNode)stmt).tirAnalyze(this);
			if (buf.length() > length) buf.append('\n');
		}
	}
}


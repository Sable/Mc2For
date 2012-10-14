package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class CaseNewUserDefinedFunc {
	static boolean Debug = false;
	
	public CaseNewUserDefinedFunc(){
		
	}
	
	public FortranCodeASTGenerator newUserDefinedFunc(FortranCodeASTGenerator fcg, TIRFunction node){
		/*
		 * SubProgram ::= ProgramTitle DeclarationSection StatementSection;
		 * and user defined function is like this, (return type) function name(inputArgs) + declaration section + stmt section.
		 * so 
		 * 1. we try to go through the stmt section first, set the stmt section;
		 * 2. set the title section;
		 * 3. and then we can set the declaration sectionn,
		 * because there may be some shadow variable we generated during the stmt transformation.
		 * the difference between user defined function and main function is that we need inputArgs following the function name,
		 * and declare a variable whose name is the same as the function's name to return the result.
		 */
		SubProgram subUserDefFunc = new SubProgram();
		fcg.SubProgram = subUserDefFunc;
		StatementSection stmtSection = new StatementSection();
		subUserDefFunc.setStatementSection(stmtSection);
		/**
		 * go through all the statements.
		 */
		//boolean first = true;
		fcg.iterateStatements(node.getStmts());
		
		ProgramTitle title = new ProgramTitle();
		title.setProgramType("function");
		title.setProgramName(fcg.majorName);
		ProgramParameterList inputArgsList = new ProgramParameterList();
		for(String arg : fcg.inArgs){
			Parameter para = new Parameter();
			para.setName(arg);
			inputArgsList.addParameter(para);
		}
		title.setProgramParameterList(inputArgsList);
		subUserDefFunc.setProgramTitle(title);
		
		DeclarationSection declSection = new DeclarationSection();
				
		for(String variable : fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().keySet()){
			DeclStmt declStmt = new DeclStmt();
			//type is already a token, don't forget.
			KeywordList keywordList = new KeywordList();
			ShapeInfo shapeInfo = new ShapeInfo();
			VariableList varList = new VariableList();
			
			if(fcg.forStmtParameter.contains(variable)||fcg.arrayIndexParameter.contains(variable)){
				if (Debug) System.out.println(variable + " = " + fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\ncomplex");
				}
				else{
					buf.append("\n" + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				declStmt.setType(fcg.FortranMap.getFortranTypeMapping("int8"));
				//parameter
				/*if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
					if (Debug) System.out.println("add parameter here!");
					fcg.buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}*/
				//else{
					Variable var = new Variable();
					var.setName(variable);
					varList.addVariable(var);
					//declStmt.setKeywordList(keywordList);
					declStmt.setVariableList(varList);
				//}
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
				declStmt.setType(fcg.FortranMap.getFortranTypeMapping(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				//parameter
				/*if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()&&(fcg.inArgs.contains(variable)==false)&&(fcg.outRes.contains(variable)==false)){
					if (Debug) System.out.println("add parameter here!");
					fcg.buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}*/
				//else{
					//dimension
					if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
						if (Debug) System.out.println("add dimension here!");
						Keyword keyword = new Keyword();
						ArrayList<Integer> dim = new ArrayList<Integer>(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions());
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
							StringBuffer tempBuf = new StringBuffer();
							tempBuf.append("dimension(");
							for(int i=1; i<=dim.size(); i++){
								if(conter){
									tempBuf.append(",");
								}
								tempBuf.append(":");
								conter = true;
							}
							tempBuf.append(") , allocatable :: ");
							keyword.setName(tempBuf.toString());
							keywordList.addKeyword(keyword);
							Variable var = new Variable();
							var.setName(variable);
							varList.addVariable(var);
							if(fcg.funcNameRep.containsKey(variable)){
								Variable varFunc = new Variable();
								varFunc.setName(fcg.funcNameRep.get(variable));
								varList.addVariable(varFunc);								
							}
							declStmt.setKeywordList(keywordList);
							declStmt.setVariableList(varList);
						}
						
						/**
						 * if all the dimension is exactly known, which values are all integer, goes to else block.
						 */
						//currently, I put shapeInfo with the keyword dimension together, it's okay now, keep an eye on this.
						else{
							StringBuffer tempBuf = new StringBuffer();
							tempBuf.append("dimension(");
							for(Integer inte : dim){
								if(conter){
									tempBuf.append(",");
								}
								tempBuf.append(inte.toString());
								conter = true;
							}
							tempBuf.append(")");
							if(fcg.outRes.contains(variable)){
								keyword.setName(tempBuf.toString());
								keywordList.addKeyword(keyword);
								Variable var = new Variable();
								var.setName(fcg.majorName);
								varList.addVariable(var);
								if(fcg.funcNameRep.containsKey(variable)){
									Variable varFunc = new Variable();
									varFunc.setName(fcg.funcNameRep.get(variable));
									varList.addVariable(varFunc);								
								}
							}
							else{
								keyword.setName(tempBuf.toString());
								keywordList.addKeyword(keyword);
								Variable var = new Variable();
								var.setName(variable);
								varList.addVariable(var);
								if(fcg.funcNameRep.containsKey(variable)){
									Variable varFunc = new Variable();
									varFunc.setName(fcg.funcNameRep.get(variable));
									varList.addVariable(varFunc);								
								}
							}
							declStmt.setKeywordList(keywordList);
							declStmt.setVariableList(varList);
						}
					}
					else{
						if (Debug) System.out.println("Is this variable in the output parameters set: "+fcg.outRes.contains(variable));
						if(fcg.outRes.contains(variable)){
							Variable var = new Variable();
							var.setName(fcg.majorName);
							varList.addVariable(var);
							if(fcg.funcNameRep.containsKey(variable)){
								Variable varFunc = new Variable();
								varFunc.setName(fcg.funcNameRep.get(variable));
								varList.addVariable(varFunc);								
							}
							declStmt.setVariableList(varList);
						}
						else{
							Variable var = new Variable();
							var.setName(variable);
							varList.addVariable(var);
							if(fcg.funcNameRep.containsKey(variable)){
								Variable varFunc = new Variable();
								varFunc.setName(fcg.funcNameRep.get(variable));
								varList.addVariable(varFunc);								
							}
							declStmt.setVariableList(varList);
						}
					}
				//}
			}
			declSection.addDeclStmt(declStmt);
		}
		/**
		 * declare those variables generated during the code generation,
		 * like extra variables for runtime shape check
		 */
		for(String tmpVariable : fcg.tmpVariables.keySet()){
			DeclStmt declStmt = new DeclStmt();
			//type is already a token, don't forget.
			ShapeInfo shapeInfo = new ShapeInfo();
			VariableList varList = new VariableList();
			declStmt.setType(fcg.FortranMap.getFortranTypeMapping(fcg.tmpVariables.get(tmpVariable).getMatlabClass().toString()));
			if(fcg.tmpVariables.get(tmpVariable).getShape().isScalar()){

				Variable var = new Variable();
				var.setName(tmpVariable);
				varList.addVariable(var);
				declStmt.setVariableList(varList);
				declSection.addDeclStmt(declStmt);
			}
			else{
				KeywordList keywordList = new KeywordList();
				Keyword keyword = new Keyword();
				keyword.setName("dimension("+fcg.tmpVariables.get(tmpVariable).getShape().toString().replace(" ", "").replace("[", "").replace("]", "")+")");
				keywordList.addKeyword(keyword);
				declStmt.setKeywordList(keywordList);
				Variable var = new Variable();
				var.setName(tmpVariable);
				varList.addVariable(var);
				declStmt.setVariableList(varList);
				declSection.addDeclStmt(declStmt);
			}
		}
		subUserDefFunc.setDeclarationSection(declSection);
		subUserDefFunc.setProgramEnd("return\nend");
		return fcg;
	}
}

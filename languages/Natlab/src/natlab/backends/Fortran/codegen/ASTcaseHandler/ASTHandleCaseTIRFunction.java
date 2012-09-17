package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import ast.Name;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;
import natlab.tame.tir.*;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;

public class ASTHandleCaseTIRFunction {
	static boolean Debug =false;
	
	public ASTHandleCaseTIRFunction(){
		
	}
	
	public FortranCodeASTGenerator getFortran(FortranCodeASTGenerator fcg, TIRFunction node){
		fcg.majorName = node.getName();
		for(Name param : node.getInputParams()){
			fcg.inArgs.add(param.getVarName());
		}
		for(Name result : node.getOutputParams()){
			fcg.outRes.add(result.getVarName());
		}
		/**
		 *deal with main entry point, main program, actually, sometimes, a subroutine can also be with 0 output...
		 *TODO think of a better way to distinguish whether it is a main entry point or a 0-output subroutine... 
		 */
		if(fcg.outRes.size()==0){
			SubProgram subMain = new SubProgram();
			fcg.SubProgram = subMain;
			StatementSection stmtSection = new StatementSection();
			subMain.setStatementSection(stmtSection);
			/**
			 * go through all the statements.
			 */
			fcg.printStatements(node.getStmts());			
			
			ProgramTitle title = new ProgramTitle();
			//ProgramParameterList paraList = new ProgramParameterList();
			title.setProgramType("program");
			title.setProgramName(fcg.majorName);
			/*for(String inputVar:fcg.inArgs){
				Parameter para = new Parameter();
				para.setName(inputVar);
				paraList.addParameter(para);
			}*/
			//title.setProgramParameterList(paraList);
			subMain.setProgramTitle(title);
			
			DeclarationSection declSection = new DeclarationSection();
			
			if (Debug) System.out.println(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().keySet()+"\n");
			for(String variable : fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().keySet()){
				DeclStmt declStmt = new DeclStmt();
				//type is already a token, don't forget.
				KeywordList keywordList = new KeywordList();
				ShapeInfo shapeInfo = new ShapeInfo();
				VariableList varList = new VariableList();
				
				if(fcg.forStmtParameter.contains(variable)||fcg.arrayIndexParameter.contains(variable)){
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
					declStmt.setType(fcg.FortranMap.getFortranTypeMapping("int8"));
					//parameter
					/*if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						Keyword keyword = new Keyword();
						keyword.setName("parameter");
						keywordList.addKeyword(keyword);//TODO did Fortran support constant array or matrix?
						Variable var = new Variable();
						var.setName(variable + "=" + ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
						varList.addVariable(var);
						declStmt.setKeywordList(keywordList);
						declStmt.setVariableList(varList);
					}*/
					//else{
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						declStmt.setKeywordList(keywordList);
						declStmt.setVariableList(varList);
					//}
				}
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
					/*if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
						if (Debug) System.out.println("add parameter here!");
						Keyword keyword = new Keyword();
						keyword.setName("parameter");
						keywordList.addKeyword(keyword);//TODO did Fortran support constant array or matrix?
						Variable var = new Variable();
						var.setName(variable + "=" + ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
						varList.addVariable(var);
						declStmt.setKeywordList(keywordList);
						declStmt.setVariableList(varList);
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
									declStmt.setKeywordList(keywordList);
									declStmt.setVariableList(varList);
								}
								else{
									keyword.setName(tempBuf.toString());
									keywordList.addKeyword(keyword);
									Variable var = new Variable();
									var.setName(variable);
									varList.addVariable(var);
									declStmt.setKeywordList(keywordList);
									declStmt.setVariableList(varList);
								}
							}
						}
						else{
							Variable var = new Variable();
							var.setName(variable);
							varList.addVariable(var);
							declStmt.setVariableList(varList);
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
				KeywordList keywordList = new KeywordList();
				ShapeInfo shapeInfo = new ShapeInfo();
				VariableList varList = new VariableList();
				declStmt.setType(fcg.FortranMap.getFortranTypeMapping(fcg.tmpVariables.get(tmpVariable).getMatlabClass().toString()));
				Keyword keyword = new Keyword();
				keyword.setName("dimension("+fcg.tmpVariables.get(tmpVariable).getShape().toString().replace(" ", "").replace("[", "").replace("]", "")+")");
				keywordList.addKeyword(keyword);
				Variable var = new Variable();
				var.setName(tmpVariable);
				varList.addVariable(var);
				declStmt.setKeywordList(keywordList);
				declStmt.setVariableList(varList);
				declSection.addDeclStmt(declStmt);
			}
			/**
			 * at the end of declaration, declare those user defined function.
			 */
			for(String key : fcg.funcNameRep.keySet()){
				String variable = fcg.funcNameRep.get(key);
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
				if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()){
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
						fcg.buf2.append(" :: " + key);
					}
				}
			}
			
			subMain.setDeclarationSection(declSection);
		}
		/**
		 * deal with functions, not subroutine, because in Fortran, functions can essentially only return one value.
		 * actually, I can also convert 1-output functions in Matlab to subroutines...
		 */
		else if(fcg.outRes.size()==1){
			String indent = node.getIndent();
			boolean first = true;
			
			fcg.printStatements(node.getStmts());
			fcg.buf.append(indent + "return\nend");
			
			if (Debug) System.out.println("the parameters in for stmt: "+fcg.forStmtParameter);
			
			fcg.buf2.append(indent + "function ");
			fcg.buf2.append(fcg.majorName);
			fcg.buf2.append("(");
			first = true;
			for(Name param : node.getInputParams()) {
				if(!first) {
					fcg.buf2.append(", ");
				}
				fcg.buf2.append(param.getVarName());
				first = false;
			}
			fcg.buf2.append(")");
			fcg.buf2.append("\nimplicit none");
			
			if (Debug) System.out.println(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().keySet()+"\n");
			
			for(String variable : fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().keySet()){
				/**
				 * deal with for statement variables...Fortran must declare them integer.
				 */
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
							if (Debug) System.out.println("Is this variable in the output parameters set: "+fcg.outRes.contains(variable));
							if(fcg.outRes.contains(variable)){
								fcg.buf2.append(" :: " + fcg.majorName);
							}
							else{
								fcg.buf2.append(" :: " + variable);
							}
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
		}
		/**
		 * deal with subroutines, which output can be more than one.
		 */
		else{
			fcg.isSubroutine = true;
			if (Debug) System.out.println("this is a subroutine");
			fcg.printStatements(node.getStmts());
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
		}
		return fcg;
	}
}

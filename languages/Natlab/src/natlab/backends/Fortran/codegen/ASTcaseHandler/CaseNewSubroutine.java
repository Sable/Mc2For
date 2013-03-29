package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.List;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.constant.*;
import natlab.tame.valueanalysis.components.shape.*;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class CaseNewSubroutine {
	static boolean Debug = false;
	
	public CaseNewSubroutine(){
		
	}
	
	public FortranCodeASTGenerator newSubroutine(FortranCodeASTGenerator fcg, TIRFunction node){
		/*
		 * SubProgram ::= ProgramTitle DeclarationSection StatementSection;
		 * and subroutine is like this, subroutine name(inputArgs+outputArgs) + declaration section + stmt section.
		 * so 
		 * 1. we try to go through the stmt section first, set the stmt section;
		 * 2. set the title section;
		 * 3. and then we can set the declaration sectionn,
		 * because there may be some shadow variable we generated during the stmt transformation.
		 * the difference between subroutine and main function is that we need inputArgs following the function name,
		 * and declare the input and output variables with the keyword intent(in) and intent(out).
		 */
		if (Debug) System.out.println("this is a subroutine");
		fcg.isSubroutine = true;
		//first pass of all the statements, collect information.
		SubProgram preSubroutine = new SubProgram();
		fcg.SubProgram = preSubroutine;
		StatementSection preStmtSection = new StatementSection();
		preSubroutine.setStatementSection(preStmtSection);
		fcg.iterateStatements(node.getStmts());
		//second pass of all the statements, using information collected from the first pass.
		SubProgram subroutine = new SubProgram();
		fcg.SubProgram = subroutine;
		StatementSection stmtSection = new StatementSection();
		subroutine.setStatementSection(stmtSection);
		fcg.iterateStatements(node.getStmts());
		
		ProgramTitle title = new ProgramTitle();
		title.setProgramType("subroutine");
		title.setProgramName(fcg.majorName);
		ProgramParameterList argsList = new ProgramParameterList();
		for(String arg : fcg.inArgs){
			Parameter para = new Parameter();
			para.setName(arg);
			argsList.addParameter(para);
		}
		for(String arg : fcg.outRes){
			Parameter para = new Parameter();
			para.setName(arg);
			argsList.addParameter(para);
		}
		title.setProgramParameterList(argsList);
		subroutine.setProgramTitle(title);
		
		DeclarationSection declSection = new DeclarationSection();
		
		for(String variable : fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().keySet()){
			
			if((((HasConstant)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet()
					.get(variable).getSingleton())).getConstant()!=null&&(fcg.inArgs.contains(variable)==false)&&(fcg.outRes.contains(variable)==false))
					||(fcg.tmpVarAsArrayIndex.containsKey(variable))){
				if (Debug) System.out.println("do constant folding, no declaration.");
			}
			else if(fcg.forStmtParameter.contains(variable)||fcg.arrayIndexParameter.contains(variable)){
				DeclStmt declStmt = new DeclStmt();
				//type is already a token, don't forget.
				KeywordList keywordList = new KeywordList();
				ShapeInfo shapeInfo = new ShapeInfo();
				VariableList varList = new VariableList();
				if (Debug) System.out.println(variable + " = " + fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\ncomplex");
				}
				else{
					buf.append("\n" + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				declStmt.setType("\n" + fcg.FortranMap.getFortranTypeMapping("int8"));
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
					declSection.addDeclStmt(declStmt);
			}
			/**
			 * general situations...
			 */
			else{
				DeclStmt declStmt = new DeclStmt();
				//type is already a token, don't forget.
				KeywordList keywordList = new KeywordList();
				ShapeInfo shapeInfo = new ShapeInfo();
				VariableList varList = new VariableList();
				if (Debug) System.out.println(variable + " = " + fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable));
				
				//complex or not others, like real, integer or something else
				/*if(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getisComplexInfo().geticType().equals("COMPLEX")){
					if (Debug) System.out.println("COMPLEX here!");
					buf.append("\ncomplex");
				}
				else{
					buf.append("\n" + FortranMap.getFortranTypeMapping(((AdvancedMatrixValue)(this.analysis.getNodeList().get(index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				}*/
				declStmt.setType(fcg.FortranMap.getFortranTypeMapping(((fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
				//parameter
				/*if(((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).isConstant()&&(fcg.inArgs.contains(variable)==false)&&(fcg.outRes.contains(variable)==false)){
					if (Debug) System.out.println("add parameter here!");
					fcg.buf2.append(" , parameter :: " + variable + "=" + ((BasicMatrixValue)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getConstant().toString());
				}*/
				//else{
					//dimension
					if(((HasShape)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().isScalar()==false){
						if (Debug) System.out.println("add dimension here!");
						Keyword keyword = new Keyword();
						List<DimValue> dim = ((HasShape)(fcg.analysis.getNodeList().get(fcg.index).getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getShape().getDimensions();
						boolean conter = false;
						boolean variableShapeIsKnown = true;
						for(DimValue dimValue : dim){
							if(dimValue.hasIntValue()){
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
							tempBuf.append(") , allocatable");
							keyword.setName(tempBuf.toString());
							keywordList.addKeyword(keyword);
							if((fcg.inArgs.contains(variable))&&(fcg.inputHasChanged.contains(variable)==false)){
								Keyword keyword2 = new Keyword();
								keyword2.setName("intent(in)");
								keywordList.addKeyword(keyword2);
							}
							else if(fcg.outRes.contains(variable)){
								Keyword keyword2 = new Keyword();
								keyword2.setName("intent(out)");
								keywordList.addKeyword(keyword2);
							}
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
							for(DimValue dimValue : dim){
								if(conter){
									tempBuf.append(",");
								}
								tempBuf.append(dimValue.toString());
								conter = true;
							}
							tempBuf.append(")");
							keyword.setName(tempBuf.toString());
							keywordList.addKeyword(keyword);
							if((fcg.inArgs.contains(variable))&&(fcg.inputHasChanged.contains(variable)==false)){
								Keyword keyword2 = new Keyword();
								keyword2.setName("intent(in)");
								keywordList.addKeyword(keyword2);
							}
							else if(fcg.outRes.contains(variable)){
								Keyword keyword2 = new Keyword();
								keyword2.setName("intent(out)");
								keywordList.addKeyword(keyword2);
							}
							Variable var = new Variable();
							var.setName(variable);
							varList.addVariable(var);
							if(fcg.inputHasChanged.contains(variable)){
								Variable varBackup = new Variable();
								varBackup.setName(variable+"_copy");
								varList.addVariable(varBackup);
							}
							declStmt.setKeywordList(keywordList);
							declStmt.setVariableList(varList);
						}
					}
					else{
						/**
						 * for subroutines, it's different from which in functions.
						 */
						if((fcg.inArgs.contains(variable))&&(fcg.inputHasChanged.contains(variable)==false)){
							Keyword keyword = new Keyword();
							keyword.setName("intent(in)");
							keywordList.addKeyword(keyword);
							declStmt.setKeywordList(keywordList);
						}
						else if(fcg.outRes.contains(variable)){
							Keyword keyword = new Keyword();
							keyword.setName("intent(out)");
							keywordList.addKeyword(keyword);
							declStmt.setKeywordList(keywordList);
						}
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						declStmt.setVariableList(varList);
					}
				//}
					declSection.addDeclStmt(declStmt);
			}
		}
		/**
		 * declare the function name
		 */
		for(String variable : fcg.funcNameRep.keySet()){
			DeclStmt declStmt = new DeclStmt();
			VariableList varList = new VariableList();
			declStmt.setType(fcg.FortranMap.getFortranTypeMapping(((fcg.analysis.getNodeList().get(fcg.index)
					.getAnalysis().getCurrentOutSet().get(variable).getSingleton())).getMatlabClass().toString()));
			Variable var = new Variable();
			var.setName(fcg.funcNameRep.get(variable));
			varList.addVariable(var);
			declStmt.setVariableList(varList);
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
		subroutine.setDeclarationSection(declSection);
		subroutine.setProgramEnd("return\nend");
		
		if(fcg.inputHasChanged.isEmpty()==false){
			for(String Stmt : fcg.inputHasChanged){
				BackupVar backupStmt = new BackupVar();
				backupStmt.setStmt(Stmt+"_copy = "+Stmt+";");
				subroutine.addBackupVar(backupStmt);
			}
		}
		
		fcg.isSubroutine = false;
		return fcg;
	}
}

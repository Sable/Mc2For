package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.List;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.shape.DimValue;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class CaseNewSubroutine {
	static boolean Debug = false;

	/**
	 * SubProgram ::= ProgramTitle DeclarationSection StatementSection;
	 * and subroutine is like this, 
	 * subroutine name(inputArgs+outputArgs) + declaration section + stmt section.
	 * so 
	 * 1. we try to go through the stmt section first, set the stmt section;
	 * 2. set the title section;
	 * 3. and then we can set the declaration sectionn,
	 * because there may be some shadow variable we generated during the stmt transformation.
	 * the difference between subroutine and main function is that we need inputArgs 
	 * following the function name,and declare the input and output variables with the 
	 * keyword intent(in) and intent(out).
	 */
	public FortranCodeASTGenerator newSubroutine(
			FortranCodeASTGenerator fcg, 
			TIRFunction node) {
		if (Debug) System.out.println("this is a subroutine");
		fcg.isSubroutine = true;
		/* 
		 * first pass of all the statements, collect information.
		 */
		SubProgram preSubroutine = new SubProgram();
		fcg.SubProgram = preSubroutine;
		StatementSection preStmtSection = new StatementSection();
		preSubroutine.setStatementSection(preStmtSection);
		fcg.iterateStatements(node.getStmts());
		/* 
		 * second pass of all the statements, using information collected from the first pass.
		 */
		SubProgram subroutine = new SubProgram();
		fcg.SubProgram = subroutine;
		StatementSection stmtSection = new StatementSection();
		subroutine.setStatementSection(stmtSection);
		fcg.iterateStatements(node.getStmts());
		/*
		 * set the title.
		 */
		ProgramTitle title = new ProgramTitle();
		title.setProgramType("subroutine");
		title.setProgramName(fcg.majorName);
		/*
		 * set the program parameter list.
		 */
		ProgramParameterList argsList = new ProgramParameterList();
		for (String arg : fcg.inArgs) {
			Parameter para = new Parameter();
			para.setName(arg);
			argsList.addParameter(para);
		}
		for (String arg : fcg.outRes) {
			Parameter para = new Parameter();
			para.setName(arg);
			argsList.addParameter(para);
		}
		title.setProgramParameterList(argsList);
		subroutine.setProgramTitle(title);
		/*
		 * set the declaration section.
		 */		
		DeclarationSection declSection = new DeclarationSection();
		for (String variable : fcg.getCurrentOutSet().keySet()) {
			/*
			 * has a constant value. do constant folding, no declaration.
			 */
			if ((fcg.getMatrixValue(variable).hasConstant() && !fcg.inArgs.contains(variable) 
					&& !fcg.outRes.contains(variable)) 
					|| fcg.tmpVarAsArrayIndex.containsKey(variable)) {
				if (Debug) System.out.println("do constant folding, no declaration.");
			}
			/*
			 * in Fortran, for loop variables and array indices must be integer.
			 */
			else if (fcg.forStmtParameter.contains(variable) 
					|| fcg.arrayIndexParameter.contains(variable)) {
				DeclStmt declStmt = new DeclStmt();
				// type is already a token, don't forget.
				KeywordList keywordList = new KeywordList();
				ShapeInfo shapeInfo = new ShapeInfo();
				VariableList varList = new VariableList();
				if (Debug) System.out.println(variable + " = " + fcg.getMatrixValue(variable));
				declStmt.setType("\n" + fcg.FortranMap.getFortranTypeMapping("int8"));
				Variable var = new Variable();
				var.setName(variable);
				varList.addVariable(var);
				declStmt.setVariableList(varList);
				declSection.addDeclStmt(declStmt);
			}
			/*
			 * general case.
			 */
			else {
				DeclStmt declStmt = new DeclStmt();
				// type is already a token, don't forget.
				KeywordList keywordList = new KeywordList();
				ShapeInfo shapeInfo = new ShapeInfo();
				VariableList varList = new VariableList();
				if (Debug) System.out.println(variable + " = " + fcg.getMatrixValue(variable));
				declStmt.setType(fcg.FortranMap.getFortranTypeMapping(
						fcg.getMatrixValue(variable).getMatlabClass().toString()));
				/*
				 * declare arrays.
				 */
				if (!fcg.getMatrixValue(variable).getShape().isScalar()) {
					if (Debug) System.out.println("add dimension here!");
					Keyword keyword = new Keyword();
					List<DimValue> dim = fcg.getMatrixValue(variable).getShape().getDimensions();
					boolean counter = false;
					boolean variableShapeIsKnown = true;
					for (DimValue dimValue : dim) {
						if (!dimValue.hasIntValue()) {
							if (Debug) System.out.println("The shape of " + variable 
									+ " is not exactly known, we need allocate it first");
							variableShapeIsKnown = false;
						}
					}
					/*
					 * if the shape is not exactly known, get into if block.
					 */
					if (!variableShapeIsKnown) {
						StringBuffer tempBuf = new StringBuffer();
						tempBuf.append("dimension(");
						for (int i=1; i<=dim.size(); i++) {
							if (counter) tempBuf.append(",");
							tempBuf.append(":");
							counter = true;
						}
						tempBuf.append(") , allocatable");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						if (fcg.inArgs.contains(variable) 
								&& !fcg.inputHasChanged.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("intent(in)");
							keywordList.addKeyword(keyword2);
						}
						else if (fcg.outRes.contains(variable)) {
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
					/*
					 * if the shape is exactly known, get into else block. 
					 * currently, I put shapeInfo with the keyword dimension 
					 * together, it's okay now, but keep an eye on this.
					 */
					else {
						StringBuffer tempBuf = new StringBuffer();
						tempBuf.append("dimension(");
						for (DimValue dimValue : dim) {
							if (counter) {
								tempBuf.append(",");
							}
							tempBuf.append(dimValue.toString());
							counter = true;
						}
						tempBuf.append(")");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						if (fcg.inArgs.contains(variable) 
								&& !fcg.inputHasChanged.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("intent(in)");
							keywordList.addKeyword(keyword2);
						}
						else if (fcg.outRes.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("intent(out)");
							keywordList.addKeyword(keyword2);
						}
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						if (fcg.inputHasChanged.contains(variable)) {
							Variable varBackup = new Variable();
							varBackup.setName(variable+"_copy");
							varList.addVariable(varBackup);
						}
						declStmt.setKeywordList(keywordList);
						declStmt.setVariableList(varList);
					}
				}
				/*
				 * declare scalars.
				 */
				else {
					/*
					 * for subroutines, it's different from which in functions.
					 */
					if (fcg.inArgs.contains(variable) 
							&& !fcg.inputHasChanged.contains(variable)) {
						Keyword keyword = new Keyword();
						keyword.setName("intent(in)");
						keywordList.addKeyword(keyword);
						declStmt.setKeywordList(keywordList);
					}
					else if (fcg.outRes.contains(variable)) {
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
				declSection.addDeclStmt(declStmt);
			}
		}
		/*
		 * declare the function name
		 */
		for (String variable : fcg.funcNameRep.keySet()) {
			DeclStmt declStmt = new DeclStmt();
			VariableList varList = new VariableList();
			declStmt.setType(fcg.FortranMap.getFortranTypeMapping(
					fcg.getMatrixValue(variable).getMatlabClass().toString()));
			Variable var = new Variable();
			var.setName(fcg.funcNameRep.get(variable));
			varList.addVariable(var);
			declStmt.setVariableList(varList);
			declSection.addDeclStmt(declStmt);
		}
		/*
		 * declare those variables generated during the code generation,
		 * like extra variables for runtime shape check
		 */
		for (String tmpVariable : fcg.tmpVariables.keySet()) {
			DeclStmt declStmt = new DeclStmt();
			// type is already a token, don't forget.
			ShapeInfo shapeInfo = new ShapeInfo();
			VariableList varList = new VariableList();
			declStmt.setType(fcg.FortranMap.getFortranTypeMapping(
					fcg.tmpVariables.get(tmpVariable).getMatlabClass().toString()));
			if (!fcg.tmpVariables.get(tmpVariable).getShape().isScalar()) {
				KeywordList keywordList = new KeywordList();
				Keyword keyword = new Keyword();
				keyword.setName("dimension("+fcg.tmpVariables.get(tmpVariable).getShape()
						.toString().replace(" ", "").replace("[", "").replace("]", "")+")");
				keywordList.addKeyword(keyword);
				declStmt.setKeywordList(keywordList);
			}
			Variable var = new Variable();
			var.setName(tmpVariable);
			varList.addVariable(var);
			declStmt.setVariableList(varList);
			declSection.addDeclStmt(declStmt);
		}
		subroutine.setDeclarationSection(declSection);
		subroutine.setProgramEnd("return\nend");
		
		if (!fcg.inputHasChanged.isEmpty()) {
			for (String Stmt : fcg.inputHasChanged) {
				BackupVar backupStmt = new BackupVar();
				backupStmt.setStmt(Stmt+"_copy = "+Stmt+";");
				subroutine.addBackupVar(backupStmt);
			}
		}
		
		fcg.isSubroutine = false;
		return fcg;
	}
}

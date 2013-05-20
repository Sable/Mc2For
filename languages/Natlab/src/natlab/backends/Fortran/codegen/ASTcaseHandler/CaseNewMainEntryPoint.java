package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.List;

import natlab.tame.tir.TIRFunction;
import natlab.tame.valueanalysis.components.shape.DimValue;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class CaseNewMainEntryPoint {
	static boolean Debug = false;
	
	/**
	 * main entry program is one kind of sub-program, and it is like this, 
	 * 		program name
	 * 		declaration section
	 * 		stmt section
	 * 		end program
	 * 1. we try to go through the stmt section first, set the stmt section;
	 * 2. set the title section;
	 * 3. and then we can set the declaration section,
	 * because there may be some shadow variable we generated during the stmt 
	 * transformation.
	 */
	public FortranCodeASTGenerator newMain(FortranCodeASTGenerator fcg, TIRFunction node) {
		/* 
		 * first pass of all the statements, collect information.
		 */
		SubProgram preSubMain = new SubProgram();
		fcg.SubProgram = preSubMain;
		StatementSection preStmtSection = new StatementSection();
		preSubMain.setStatementSection(preStmtSection);
		fcg.iterateStatements(node.getStmts());
		/* 
		 * second pass of all the statements, using information collected from the first pass.
		 */
		SubProgram subMain = new SubProgram();
		fcg.SubProgram = subMain;
		StatementSection stmtSection = new StatementSection();
		subMain.setStatementSection(stmtSection);
		fcg.iterateStatements(node.getStmts());
		/*
		 *  set the title.
		 */
		ProgramTitle title = new ProgramTitle();
		title.setProgramType("PROGRAM");
		title.setProgramName(fcg.functionName);
		subMain.setProgramTitle(title);
		/*
		 *  set the declaration section.
		 */
		DeclarationSection declSection = new DeclarationSection();
		for (String variable : fcg.getCurrentOutSet().keySet()) {
			if ((fcg.getMatrixValue(variable).hasConstant() 
					&& !fcg.inArgs.contains(variable)) 
					&& fcg.tamerTmpVar.contains(variable) 
					|| fcg.tmpVectorAsArrayIndex.containsKey(variable)) {
				if (Debug) System.out.println("do constant folding, no declaration.");
			}
			else {
				DeclStmt declStmt = new DeclStmt();
				// type is already a token, don't forget.
				KeywordList keywordList = new KeywordList();
				ShapeInfo shapeInfo = new ShapeInfo();
				VariableList varList = new VariableList();
				if (Debug) System.out.println(variable + "'s value is " + fcg.getMatrixValue(variable));
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
						tempBuf.append("DIMENSION(");
						for (int i=1; i<=dim.size(); i++) {
							if (counter) {
								tempBuf.append(",");
							}
							tempBuf.append(":");
							counter = true;
						}
						tempBuf.append(") , ALLOCATABLE");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
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
						tempBuf.append("DIMENSION(");
						for (DimValue dimValue : dim) {
							if (counter) tempBuf.append(",");
							tempBuf.append(dimValue.toString());
							counter = true;
						}
						tempBuf.append(")");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						declStmt.setKeywordList(keywordList);
						declStmt.setVariableList(varList);
					}
				}
				/*
				 * declare scalars.
				 */
				else {
					Variable var = new Variable();
					var.setName(variable);
					varList.addVariable(var);
					declStmt.setVariableList(varList);
				}
				declSection.addDeclStmt(declStmt);
			}
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
				keyword.setName("DIMENSION("+fcg.tmpVariables.get(tmpVariable).getShape()
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
		subMain.setDeclarationSection(declSection);
		subMain.setProgramEnd("END PROGRAM");
		return fcg;
	}
}

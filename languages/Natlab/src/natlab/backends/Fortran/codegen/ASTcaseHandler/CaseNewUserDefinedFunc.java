package natlab.backends.Fortran.codegen.ASTcaseHandler;

import java.util.List;

import natlab.tame.tir.*;
import natlab.tame.valueanalysis.components.shape.DimValue;
import natlab.backends.Fortran.codegen.*;
import natlab.backends.Fortran.codegen.FortranAST.*;

public class CaseNewUserDefinedFunc {
	static boolean Debug = false;

	/**
	 * SubProgram ::= ProgramTitle DeclarationSection StatementSection;
	 * and user defined function is like this, 
	 * (return type) function name(inputArgs) + declaration section + stmt section.
	 * so 
	 * 1. we try to go through the stmt section first, set the stmt section;
	 * 2. set the title section;
	 * 3. and then we can set the declaration sectionn,
	 * because there may be some shadow variable we generated during the stmt transformation.
	 * the difference between user defined function and main function is that we need 
	 * inputArgs following the function name, and declare a variable whose name is the 
	 * same as the function's name to return the result.
	 */
	public FortranCodeASTGenerator newUserDefinedFunc(
			FortranCodeASTGenerator fcg, 
			TIRFunction node) {
		SubProgram subUserDefFunc = new SubProgram();
		fcg.SubProgram = subUserDefFunc;
		StatementSection stmtSection = new StatementSection();
		subUserDefFunc.setStatementSection(stmtSection);
		/*
		 * go through all the statements.
		 */
		fcg.iterateStatements(node.getStmts());
		/*
		 * set the title.
		 */
		ProgramTitle title = new ProgramTitle();
		title.setProgramType("function");
		title.setProgramName(fcg.majorName);
		ProgramParameterList inputArgsList = new ProgramParameterList();
		for (String arg : fcg.inArgs) {
			Parameter para = new Parameter();
			para.setName(arg);
			inputArgsList.addParameter(para);
		}
		title.setProgramParameterList(inputArgsList);
		subUserDefFunc.setProgramTitle(title);
		/*
		 * set the declaration section.
		 */
		DeclarationSection declSection = new DeclarationSection();
		for (String variable : fcg.getCurrentOutSet().keySet()) {
			/*
			 * has a constant value. do constant folding, no declaration.
			 */
			if ((fcg.getMatrixValue(variable).hasConstant() && !fcg.inArgs.contains(variable))
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
				declStmt.setType(fcg.FortranMap.getFortranTypeMapping("int8"));
				Variable var = new Variable();
				var.setName(variable);
				varList.addVariable(var);
				declStmt.setVariableList(varList);
				declSection.addDeclStmt(declStmt);
			}
			/*
			 * general cases.
			 */
			else {
				DeclStmt declStmt = new DeclStmt();
				//type is already a token, don't forget.
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
							if (counter) {
								tempBuf.append(",");
							}
							tempBuf.append(":");
							counter = true;
						}
						tempBuf.append(") , allocatable");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						if (fcg.funcNameRep.containsKey(variable)) {
							Variable varFunc = new Variable();
							varFunc.setName(fcg.funcNameRep.get(variable));
							varList.addVariable(varFunc);								
						}
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
							if (counter) tempBuf.append(",");
							tempBuf.append(dimValue.toString());
							counter = true;
						}
						tempBuf.append(")");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						Variable var = new Variable();
						if (fcg.outRes.contains(variable)) var.setName(fcg.majorName);
						else var.setName(variable);
						varList.addVariable(var);
						if (fcg.funcNameRep.containsKey(variable)) {
							Variable varFunc = new Variable();
							varFunc.setName(fcg.funcNameRep.get(variable));
							varList.addVariable(varFunc);								
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
					 * for user defined functions, it's different from which in subroutines.
					 */
					Variable var = new Variable();
					if (fcg.outRes.contains(variable)) var.setName(fcg.majorName);
					else var.setName(variable);
					varList.addVariable(var);
					if(fcg.funcNameRep.containsKey(variable)){
						Variable varFunc = new Variable();
						varFunc.setName(fcg.funcNameRep.get(variable));
						varList.addVariable(varFunc);
					}
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
		subUserDefFunc.setDeclarationSection(declSection);
		subUserDefFunc.setProgramEnd("return\nend");
		return fcg;
	}
}

package natlab.backends.Fortran.codegen_readable.astCaseHandler;

import java.util.List;

import ast.Function;

import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.valueanalysis.components.shape.DimValue;
import natlab.backends.Fortran.codegen_readable.*;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.*;

public class GenerateSubroutine {
	static boolean Debug = false;
	
	/**
	 * subroutine is one kind of subprograms, and it is like this,
	 * 		SUBROUTINE name(inputArgs+outputArgs)
	 * 		USE modules
	 * 		declaration section
	 * 		stmt section.
	 * 		END SUBROUTINE
	 * 
	 * 1. we try to go through the stmt section first, set the stmt section;
	 * 2. set the title section;
	 * 3. and then we can set the declaration section,
	 * 
	 * because there may be some temporary variable we generated during 
	 * the stmt transformation. the difference between subroutine and 
	 * other two subprograms is that we need input parameters and output 
	 * parameters following the function name.
	 */
	public FortranCodeASTGenerator newSubroutine(
			FortranCodeASTGenerator fcg, 
			Function node) 
	{
		fcg.isInSubroutine = true;
		/* 
		 * first pass of all the statements, collect information.
		 */
		Subprogram preSubroutine = new Subprogram();
		fcg.subprogram = preSubroutine;
		StatementSection preStmtSection = new StatementSection();
		preSubroutine.setStatementSection(preStmtSection);
		fcg.iterateStatements(node.getStmts());
		fcg.passCounter++;
		/* 
		 * second pass of all the statements, using information collected from the first pass.
		 */
		Subprogram subroutine = new Subprogram();
		fcg.subprogram = subroutine;
		StatementSection stmtSection = new StatementSection();
		subroutine.setStatementSection(stmtSection);
		fcg.iterateStatements(node.getStmts());
		/*
		 * set the title.
		 */
		ProgramTitle title = new ProgramTitle();
		title.setProgramType("SUBROUTINE");
		title.setProgramName(fcg.functionName);
		/*
		 * set the program parameter list.
		 */
		ProgramParameterList argsList = new ProgramParameterList();
		for (String arg : fcg.inArgs) {
			Parameter parameter = new Parameter();
			if (fcg.inputHasChanged.contains(arg)) {
				parameter.setName(arg + "_cp");
			}
			else parameter.setName(arg);
			argsList.addParameter(parameter);
		}
		for (String arg : fcg.outRes) {
			Parameter parameter = new Parameter();
			parameter.setName(arg);
			argsList.addParameter(parameter);
		}
		title.setProgramParameterList(argsList);
		subroutine.setProgramTitle(title);
		/*
		 * declare modules
		 */
		for (String builtin : fcg.allSubprograms) {
			Module module = new Module();
			module.setName(builtin);
			title.addModule(module);
		}
		/*
		 * set the declaration section.
		 */		
		DeclarationSection declSection = new DeclarationSection();
		DerivedTypeList derivedTypeList = new DerivedTypeList();
		for (String variable : fcg.remainingVars) {
			if (fcg.isCell(variable) || !fcg.hasSingleton(variable)) {
				/*
				 * cell array declaration, mapping to derived type in Fortran.
				 */
				DerivedType derivedType = new DerivedType();
				StringBuffer sb = new StringBuffer();
				boolean skip = false;
				for (String cell : fcg.declaredCell) {
					if (fcg.forCellArr.get(cell).equals(fcg.forCellArr.get(variable))) {
						// these two cell arrays should be with the same derived type.
						sb.append("TYPE (cellStruct_"+cell+") "+variable+"\n");
						skip = true;
					}
				}
				if (!skip) {
					sb.append("TYPE "+"cellStruct_"+variable+"\n");
					for (int i=0; i<fcg.forCellArr.get(variable).size(); i++) {
						sb.append("   "+fcg.fortranMapping.getFortranTypeMapping(
								fcg.forCellArr.get(variable).get(i).getMatlabClass().toString()));
						if (!fcg.forCellArr.get(variable).get(i).getShape().isScalar()) {
							if (fcg.forCellArr.get(variable).get(i).getMatlabClass()
									.equals(PrimitiveClassReference.CHAR)) {
								sb.append("("+fcg.forCellArr.get(variable).get(i)
										.getShape().getDimensions().get(1)+")");
							}
							else {
								sb.append(", DIMENSION("+fcg.forCellArr.get(variable)
										.get(i).getShape().getDimensions().toString()
										.replace("[", "").replace("]", "")+")");
							}
						}
						sb.append(" :: "+"f"+i+"\n");
					}
					sb.append("END TYPE "+"cellStruct_"+variable+"\n");
					sb.append("TYPE (cellStruct_"+variable+") "+variable+"\n");					
				}
				fcg.declaredCell.add(variable);
				derivedType.setBlock(sb.toString());
				derivedTypeList.addDerivedType(derivedType);
				declSection.setDerivedTypeList(derivedTypeList);
			}
			/*
			 * normal case.
			 */
			else {
				DeclStmt declStmt = new DeclStmt();
				// type is already a token, don't forget.
				KeywordList keywordList = new KeywordList();
				ShapeInfo shapeInfo = new ShapeInfo();
				VariableList varList = new VariableList();
				if (Debug) System.out.println(variable + "'s value is " + fcg.getMatrixValue(variable));
				/*
				 * declare types, especially character string.
				 */
				if (fcg.getMatrixValue(variable).hasisComplexInfo() 
						&& (fcg.getMatrixValue(variable)
								.getisComplexInfo().geticType().equals("ANY") 
								|| fcg.getMatrixValue(variable)
								.getisComplexInfo().geticType().equals("COMPLEX"))) {
					if (fcg.getMatrixValue(variable).getMatlabClass()
							.equals(PrimitiveClassReference.DOUBLE)) 
						declStmt.setType("COMPLEX(KIND=8)");
					else 
						declStmt.setType("COMPLEX");
				}
				else if (fcg.getMatrixValue(variable).getMatlabClass().equals(PrimitiveClassReference.CHAR) 
						&& !fcg.getMatrixValue(variable).getShape().isScalar()) {
					declStmt.setType(fcg.fortranMapping.getFortranTypeMapping("char")
							+"(LEN=*)");
				}
				else if (fcg.forceToInt.contains(variable)) {
					declStmt.setType("INTEGER(KIND=4)");
				}
				else {
					declStmt.setType(fcg.fortranMapping.getFortranTypeMapping(
						fcg.getMatrixValue(variable).getMatlabClass().toString()));
				}
				/*
				 * declare arrays, but not character string.
				 */
				if (!fcg.getMatrixValue(variable).getMatlabClass().equals(PrimitiveClassReference.CHAR) 
						&& !fcg.getMatrixValue(variable).getShape().isScalar()) {
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
						for (int i = 0; i < dim.size(); i++) {
							if (counter) tempBuf.append(",");
							tempBuf.append(":");
							counter = true;
						}
						tempBuf.append("), ALLOCATABLE");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						/*if (fcg.inArgs.contains(variable) 
								&& !fcg.inputHasChanged.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("INTENT(IN)");
							keywordList.addKeyword(keyword2);
						}
						else if (fcg.outRes.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("INTENT(OUT)");
							keywordList.addKeyword(keyword2);
						}*/
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						if (fcg.inputHasChanged.contains(variable)) {
							Variable varCopy = new Variable();
							varCopy.setName(variable+"_cp");
							varList.addVariable(varCopy);
						}
						// need extra temporaries for runtime reallocate variables.
						if (fcg.backupTempArrays.contains(variable)) {
							Variable varBackup = new Variable();
							varBackup.setName(variable+"_bk");
							varList.addVariable(varBackup);
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
						tempBuf.append("DIMENSION(");
						for (int i = 0; i < dim.size(); i++) {
							if (counter) tempBuf.append(",");
							tempBuf.append(dim.get(i).toString());
							counter = true;
						}
						tempBuf.append(")");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						/*
						 * for subroutines, we should care about whether 
						 * the input has been modified, but for main 
						 * programs or functions, we don't need to care.
						 */
						/*if (fcg.inArgs.contains(variable) 
								&& !fcg.inputHasChanged.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("INTENT(IN)");
							keywordList.addKeyword(keyword2);
						}
						else if (fcg.outRes.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("INTENT(OUT)");
							keywordList.addKeyword(keyword2);
						}*/
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						if (fcg.inputHasChanged.contains(variable)) {
							Variable varCopy = new Variable();
							varCopy.setName(variable+"_cp");
							varList.addVariable(varCopy);
						}
						declStmt.setKeywordList(keywordList);
						declStmt.setVariableList(varList);
					}
				}
				/*
				 * declare scalars.
				 */
				else {
					/*if (fcg.inArgs.contains(variable) 
							&& !fcg.inputHasChanged.contains(variable)) {
						Keyword keyword = new Keyword();
						keyword.setName("INTENT(IN)");
						keywordList.addKeyword(keyword);
						declStmt.setKeywordList(keywordList);
					}
					else if (fcg.outRes.contains(variable)) {
						Keyword keyword = new Keyword();
						keyword.setName("INTENT(OUT)");
						keywordList.addKeyword(keyword);
						declStmt.setKeywordList(keywordList);
					}*/
					Variable var = new Variable();
					var.setName(variable);
					varList.addVariable(var);
					if (fcg.inputHasChanged.contains(variable)) {
						Variable varBackup = new Variable();
						varBackup.setName(variable+"_cp");
						varList.addVariable(varBackup);
					}
					declStmt.setVariableList(varList);
				}
				/* 
				 * if several variables have the same type declaration, 
				 * we should declare them in one line (for readability).
				 * we need a method to compare declStmt.
				 */
				boolean redundant = false;
				for (int i = 0; i < declSection.getDeclStmtList().getNumChild(); i++) {
					if (GenerateMainEntryPoint.compareDecl(declSection.getDeclStmt(i), declStmt)) {
						if (Debug) System.out.println(declStmt.getVariableList().getNumChild());
						for (int j = 0; j < declStmt.getVariableList().getNumVariable(); j++) {
							if (Debug) System.out.println(declStmt.getVariableList().getVariable(j).getName());
							declSection.getDeclStmt(i).getVariableList().addVariable(
									declStmt.getVariableList().getVariable(j));
						}
						redundant = true;
					}
				}
				if (!redundant) declSection.addDeclStmt(declStmt);
			}
		}
		/*
		 * declare those variables generated during the code generation,
		 * like extra variables for runtime shape check
		 */
		for (String tmpVariable : fcg.fotranTemporaries.keySet()) {
			DeclStmt declStmt = new DeclStmt();
			// type is already a token, don't forget.
			ShapeInfo shapeInfo = new ShapeInfo();
			VariableList varList = new VariableList();
			declStmt.setType(fcg.fortranMapping.getFortranTypeMapping(
					fcg.fotranTemporaries.get(tmpVariable).getMatlabClass().toString()));
			if (!fcg.fotranTemporaries.get(tmpVariable).getShape().isScalar()) {
				KeywordList keywordList = new KeywordList();
				Keyword keyword = new Keyword();
				List<DimValue> dim = fcg.fotranTemporaries.get(tmpVariable).getShape().getDimensions();
				boolean counter = false;
				StringBuffer tempBuf = new StringBuffer();
				tempBuf.append("DIMENSION(");
				for (int i = 0; i < dim.size(); i++) {
					if (counter) tempBuf.append(",");
					tempBuf.append(":");
					counter = true;
				}
				tempBuf.append("), ALLOCATABLE");
				keyword.setName(tempBuf.toString());
				keywordList.addKeyword(keyword);
				declStmt.setKeywordList(keywordList);
			}
			Variable var = new Variable();
			var.setName(tmpVariable);
			varList.addVariable(var);
			declStmt.setVariableList(varList);
			/* 
			 * if several variables have the same type declaration, 
			 * we should declare them in one line (for readability).
			 * we need a method to compare declStmt.
			 */
			boolean redundant = false;
			for (int i = 0; i < declSection.getDeclStmtList().getNumChild(); i++) {
				if (GenerateMainEntryPoint.compareDecl(declSection.getDeclStmt(i), declStmt)) {
					for (int j = 0; j < declStmt.getVariableList().getNumChild(); j++) {
						declSection.getDeclStmt(i).getVariableList().addVariable(
								declStmt.getVariableList().getVariable(j));
					}
					redundant = true;
				}
			}
			if (!redundant) declSection.addDeclStmt(declStmt);
		}
		
		for (String preallocVar : fcg.horzVertPrealloc) {
			DeclStmt declStmt = new DeclStmt();
			// type is already a token, don't forget.
			ShapeInfo shapeInfo = new ShapeInfo();
			VariableList varList = new VariableList();
			declStmt.setType("DOUBLE PRECISION");
			KeywordList keywordList = new KeywordList();
			Keyword keyword = new Keyword();
			keyword.setName("DIMENSION(:), ALLOCATABLE");
			keywordList.addKeyword(keyword);
			declStmt.setKeywordList(keywordList);
			Variable var = new Variable();
			var.setName(preallocVar);
			varList.addVariable(var);
			declStmt.setVariableList(varList);
			/* 
			 * if several variables have the same type declaration, 
			 * we should declare them in one line (for readability).
			 * we need a method to compare declStmt.
			 */
			boolean redundant = false;
			for (int i = 0; i < declSection.getDeclStmtList().getNumChild(); i++) {
				if (GenerateMainEntryPoint.compareDecl(declSection.getDeclStmt(i), declStmt)) {
					for (int j = 0; j < declStmt.getVariableList().getNumChild(); j++) {
						declSection.getDeclStmt(i).getVariableList().addVariable(
								declStmt.getVariableList().getVariable(j));
					}
					redundant = true;
				}
			}
			if (!redundant) declSection.addDeclStmt(declStmt);
		}
		subroutine.setDeclarationSection(declSection);
		subroutine.setProgramEnd("END SUBROUTINE");
		/* 
		 * back up input arguments for subroutines.
		 */
		if (!fcg.inputHasChanged.isEmpty()) {
			for (String Stmt : fcg.inputHasChanged) {
				BackupVar backupStmt = new BackupVar();
				backupStmt.setStmt(Stmt + " = " + Stmt + "_cp;");
				subroutine.addBackupVar(backupStmt);
			}
		}
		fcg.isInSubroutine = false;
		fcg.inputHasChanged.clear();
		return fcg;
	}
}

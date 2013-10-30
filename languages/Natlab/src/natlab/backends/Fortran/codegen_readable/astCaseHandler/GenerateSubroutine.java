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
								sb.append(" , DIMENSION("+fcg.forCellArr.get(variable)
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
				if (fcg.getMatrixValue(variable).getMatlabClass().equals(PrimitiveClassReference.CHAR) 
						&& !fcg.getMatrixValue(variable).getShape().isScalar()) {
					declStmt.setType(fcg.fortranMapping.getFortranTypeMapping("char")
							+"("+fcg.getMatrixValue(variable).getShape().getDimensions().get(1)+")");
				}
				else declStmt.setType(fcg.fortranMapping.getFortranTypeMapping(
						fcg.getMatrixValue(variable).getMatlabClass().toString()));
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
						tempBuf.append(") , ALLOCATABLE");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						if (fcg.inArgs.contains(variable) 
								&& !fcg.inputHasChanged.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("INTENT(IN)");
							keywordList.addKeyword(keyword2);
						}
						else if (fcg.outRes.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("INTENT(OUT)");
							keywordList.addKeyword(keyword2);
						}
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						// need extra temporaries for runtime allocate variables.
						Variable var_bk = new Variable();
						var_bk.setName(variable+"_bk");
						varList.addVariable(var_bk);
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
						if (fcg.inArgs.contains(variable) 
								&& !fcg.inputHasChanged.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("INTENT(IN)");
							keywordList.addKeyword(keyword2);
						}
						else if (fcg.outRes.contains(variable)) {
							Keyword keyword2 = new Keyword();
							keyword2.setName("INTENT(OUT)");
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
					if (fcg.inArgs.contains(variable) 
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
					}
					Variable var = new Variable();
					var.setName(variable);
					varList.addVariable(var);
					if (fcg.inputHasChanged.contains(variable)) {
						Variable varBackup = new Variable();
						varBackup.setName(variable+"_copy");
						varList.addVariable(varBackup);
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
				keyword.setName("DIMENSION("+fcg.fotranTemporaries.get(tmpVariable).getShape()
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
		subroutine.setProgramEnd("END SUBROUTINE");
		if (!fcg.inputHasChanged.isEmpty()) {
			for (String Stmt : fcg.inputHasChanged) {
				BackupVar backupStmt = new BackupVar();
				backupStmt.setStmt(Stmt+"_copy = "+Stmt+";");
				subroutine.addBackupVar(backupStmt);
			}
		}
		fcg.isInSubroutine = false;
		fcg.inputHasChanged.clear();
		return fcg;
	}
}

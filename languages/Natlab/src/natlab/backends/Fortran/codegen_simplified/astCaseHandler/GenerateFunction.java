package natlab.backends.Fortran.codegen_simplified.astCaseHandler;

import java.util.List;

import natlab.backends.Fortran.codegen_simplified.FortranCodeASTGenerator;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.BackupVar;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.DeclStmt;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.DeclarationSection;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.DerivedType;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.DerivedTypeList;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Keyword;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.KeywordList;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Module;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Parameter;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.ProgramParameterList;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.ProgramTitle;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.ShapeInfo;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.StatementSection;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Subprogram;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.Variable;
import natlab.backends.Fortran.codegen_simplified.FortranAST_simplified.VariableList;
import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.tir.TIRFunction;
import natlab.tame.valueanalysis.components.shape.DimValue;

public class GenerateFunction {
	static boolean Debug = false;
	
	/**
	 * function is one kind of subprograms, and it is like this,
	 * 		FUNCTION name(inputArgs)
	 * 		USE modules
	 * 		declaration section
	 * 		stmt section.
	 * 		END FUNCTION name
	 * 
	 * 1. we try to go through the stmt section first, set the stmt section;
	 * 2. set the title section;
	 * 3. and then we can set the declaration section,
	 * 
	 * because there may be some temporary variable we generated during 
	 * the stmt transformation. the difference between function and 
	 * other two sub-programs is that we need input parameters following 
	 * the function name, and also declare the function name as the return 
	 * variable (recall that, function in fortran also return one value).
	 */
	public FortranCodeASTGenerator newFunction(
			FortranCodeASTGenerator fcg, 
			TIRFunction node) 
	{
		/* 
		 * first pass of all the statements, collecting information.
		 */
		Subprogram preFunction = new Subprogram();
		fcg.subprogram = preFunction;
		StatementSection preStmtSection = new StatementSection();
		preFunction.setStatementSection(preStmtSection);
		fcg.iterateStatements(node.getStmts());
		/* 
		 * second pass of all the statements, using information 
		 * collected from the first pass.
		 */
		Subprogram function = new Subprogram();
		fcg.subprogram = function;
		StatementSection stmtSection = new StatementSection();
		function.setStatementSection(stmtSection);
		fcg.iterateStatements(node.getStmts());
		/*
		 * set the title.
		 */
		ProgramTitle title = new ProgramTitle();
		title.setProgramType("FUNCTION");
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
		title.setProgramParameterList(argsList);
		function.setProgramTitle(title);
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
		for (String variable : fcg.getCurrentOutSet().keySet()) {
			/* 
			 * cell array declaration, mapping to derived type in Fortran.
			 */
			if (fcg.isCell(variable) || !fcg.hasSingleton(variable)) {
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
			else if (fcg.getMatrixValue(variable).hasConstant() 
					&& !fcg.inArgs.contains(variable) 
					&& !fcg.outRes.contains(variable) 
					&& fcg.tempVarsBeforeF.contains(variable) 
					|| fcg.tempVectorAsArrayIndex.containsKey(variable)) {
				if (Debug) System.out.println("constant variable replacement, no declaration.");
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
				 * declare types.
				 */
				if (fcg.getMatrixValue(variable).getMatlabClass().equals(PrimitiveClassReference.CHAR) 
						&& !fcg.getMatrixValue(variable).getShape().isScalar()) {
					declStmt.setType(fcg.fortranMapping.getFortranTypeMapping("char")
							+"("+fcg.getMatrixValue(variable).getShape().getDimensions().get(1)+")");
				}
				else 
					declStmt.setType(fcg.fortranMapping.getFortranTypeMapping(
						fcg.getMatrixValue(variable).getMatlabClass().toString()));
				/*
				 * declare arrays, but not character strings.
				 */
				if (!fcg.getMatrixValue(variable).getShape().isScalar() 
						&& !fcg.getMatrixValue(variable).getMatlabClass().equals(PrimitiveClassReference.CHAR)) {
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
						/*
						 * add return variable replacement, using function name to replace.
						 */
						Variable var = new Variable();
						if (fcg.outRes.contains(variable)) {
							var.setName(fcg.functionName);
						}
						else {
							var.setName(variable);
						}
						varList.addVariable(var);
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
						 * add return variable replacement, using function name to replace.
						 */
						Variable var = new Variable();
						if (fcg.outRes.contains(variable)) {
							var.setName(fcg.functionName);
						}
						else {
							var.setName(variable);
						}
						varList.addVariable(var);
						declStmt.setKeywordList(keywordList);
						declStmt.setVariableList(varList);
					}
				}
				/*
				 * declare scalars.
				 */
				else {
					/*
					 * add return variable replacement, using function name to replace.
					 */
					Variable var = new Variable();
					if (fcg.outRes.contains(variable)) {
						var.setName(fcg.functionName);
					}
					else {
						var.setName(variable);
					}
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
		for (String tmpVariable : fcg.fortranTemporaries.keySet()) {
			DeclStmt declStmt = new DeclStmt();
			// type is already a token, don't forget.
			ShapeInfo shapeInfo = new ShapeInfo();
			VariableList varList = new VariableList();
			declStmt.setType(fcg.fortranMapping.getFortranTypeMapping(
					fcg.fortranTemporaries.get(tmpVariable).getMatlabClass().toString()));
			if (!fcg.fortranTemporaries.get(tmpVariable).getShape().isScalar()) {
				KeywordList keywordList = new KeywordList();
				Keyword keyword = new Keyword();
				keyword.setName("DIMENSION("+fcg.fortranTemporaries.get(tmpVariable).getShape()
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
		function.setDeclarationSection(declSection);
		function.setProgramEnd("END FUNCTION");
		if (!fcg.inputHasChanged.isEmpty()) {
			for (String Stmt : fcg.inputHasChanged) {
				BackupVar backupStmt = new BackupVar();
				backupStmt.setStmt(Stmt+"_copy = "+Stmt+";");
				function.addBackupVar(backupStmt);
			}
		}
		fcg.isInSubroutine = false;
		fcg.inputHasChanged.clear();
		return fcg;
	}
}

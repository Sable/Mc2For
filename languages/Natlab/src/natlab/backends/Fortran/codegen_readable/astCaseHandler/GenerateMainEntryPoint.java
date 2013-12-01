package natlab.backends.Fortran.codegen_readable.astCaseHandler;

import java.util.List;
import java.util.ArrayList;

import ast.Function;

import natlab.tame.classes.reference.PrimitiveClassReference;
import natlab.tame.valueanalysis.aggrvalue.AggrValue;
import natlab.tame.valueanalysis.basicmatrix.BasicMatrixValue;
import natlab.tame.valueanalysis.components.isComplex.isComplexInfoFactory;
import natlab.tame.valueanalysis.components.shape.DimValue;
import natlab.tame.valueanalysis.components.shape.ShapeFactory;
import natlab.backends.Fortran.codegen_readable.*;
import natlab.backends.Fortran.codegen_readable.FortranAST_readable.*;

public class GenerateMainEntryPoint {
	static boolean Debug = false;
	
	/**
	 * main entry program is one kind of subprograms, and it is like this, 
	 * 		PROGRAM name
	 * 		USE modules
	 * 		IMPLICIT NONE
	 * 		declaration section
	 * 		stmt section
	 * 		END PROGRAM name
	 * 
	 * 1. we try to go through the stmt section first, set the stmt section;
	 * 2. set the title section;
	 * 3. and then we can set the declaration section,
	 * 
	 * because there may be some temporary variables we generated during 
	 * the stmt transformation.
	 */
	public FortranCodeASTGenerator newMain(
			FortranCodeASTGenerator fcg, 
			Function node) 
	{
		/* 
		 * first pass of all the statements, collecting information.
		 */
		Subprogram preMainEntry = new Subprogram();
		fcg.subprogram = preMainEntry;
		StatementSection preStmtSection = new StatementSection();
		preMainEntry.setStatementSection(preStmtSection);
		// the inputs of the main program in Fortran don't have to be INTEGER.
		fcg.iterateStatements(node.getStmts());
		/* 
		 * second pass of all the statements, using 
		 * information collected from the first pass.
		 */
		Subprogram mainEntry = new Subprogram();
		fcg.subprogram = mainEntry;
		StatementSection stmtSection = new StatementSection();
		mainEntry.setStatementSection(stmtSection);
		
		GetInput getInput = new GetInput();
		StringBuffer temp = new StringBuffer();
		// TODO currently, we only support one input.
		if (fcg.inArgs.size() == 1 && fcg.inputsUsed.contains(fcg.inArgs.get(0))) {
			temp.append("\nint_tmpvar = 0\n");
			temp.append("arg_buffer = '0000000000'\n");
			temp.append("DO int_tmpvar = 1 , IARGC()\n");
			temp.append(fcg.standardIndent + "CALL GETARG(int_tmpvar, arg_buffer)\n");
			temp.append(fcg.standardIndent + "IF ((int_tmpvar == 1)) THEN\n");
			temp.append(fcg.standardIndent + fcg.standardIndent + "READ(arg_buffer, *) "+ fcg.inArgs.get(0)  +"\n");
			temp.append(fcg.standardIndent + "END IF\n");
			temp.append("END DO\n");
			
			fcg.fotranTemporaries.put("int_tmpvar", new BasicMatrixValue(
					null, 
					PrimitiveClassReference.INT32, 
					new ShapeFactory<AggrValue<BasicMatrixValue>>().getScalarShape(), 
					null, 
					new isComplexInfoFactory<AggrValue<BasicMatrixValue>>()
					.newisComplexInfoFromStr("REAL")));
			ArrayList<Integer> tempShape = new ArrayList<Integer>();
			tempShape.add(1);
			tempShape.add(10);
			fcg.fotranTemporaries.put("arg_buffer", new BasicMatrixValue(
					null, 
					PrimitiveClassReference.CHAR, 
					new ShapeFactory<AggrValue<BasicMatrixValue>>().newShapeFromIntegers(tempShape), 
					null, 
					new isComplexInfoFactory<AggrValue<BasicMatrixValue>>()
					.newisComplexInfoFromStr("REAL")));
		}
		// here is a hack to add timing TODO find a better way.
		temp.append("\nCALL CPU_TIME(ftime1);\n");
		fcg.fotranTemporaries.put("ftime1", new BasicMatrixValue(
				null, 
				PrimitiveClassReference.DOUBLE, 
				new ShapeFactory<AggrValue<BasicMatrixValue>>().getScalarShape(), 
				null, 
				new isComplexInfoFactory<AggrValue<BasicMatrixValue>>()
				.newisComplexInfoFromStr("REAL")));
		fcg.fotranTemporaries.put("ftime2", new BasicMatrixValue(
				null, 
				PrimitiveClassReference.DOUBLE, 
				new ShapeFactory<AggrValue<BasicMatrixValue>>().getScalarShape(), 
				null, 
				new isComplexInfoFactory<AggrValue<BasicMatrixValue>>()
				.newisComplexInfoFromStr("REAL")));
		getInput.setBlock(temp.toString());		
		mainEntry.setGetInput(getInput);
		
		fcg.iterateStatements(node.getStmts());
		/*
		 *  set the title.
		 */
		ProgramTitle title = new ProgramTitle();
		title.setProgramType("PROGRAM");
		title.setProgramName(fcg.functionName);
		mainEntry.setProgramTitle(title);
		/*
		 * declare modules
		 */
		for (String builtin : fcg.allSubprograms) {
			Module module = new Module();
			module.setName(builtin);
			title.addModule(module);
		}
		/*
		 *  set the declaration section.
		 */
		DeclarationSection declSection = new DeclarationSection();
		DerivedTypeList derivedTypeList = new DerivedTypeList();
		for (String variable : fcg.remainingVars) {
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
						&& fcg.getMatrixValue(variable).getisComplexInfo().geticType().equals("COMPLEX")) {
					// do all the variables have iscomplex information?
					declStmt.setType("COMPLEX");
				}
				else if (fcg.getMatrixValue(variable).getMatlabClass().equals(PrimitiveClassReference.CHAR) 
						&& !fcg.getMatrixValue(variable).getShape().isScalar()) {
					declStmt.setType(fcg.fortranMapping.getFortranTypeMapping("char")
							+"("+fcg.getMatrixValue(variable).getShape().getDimensions().get(1)+")");
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
						tempBuf.append(") , ALLOCATABLE");
						keyword.setName(tempBuf.toString());
						keywordList.addKeyword(keyword);
						Variable var = new Variable();
						var.setName(variable);
						varList.addVariable(var);
						// need extra temporaries for runtime reallocate variables.
						if (fcg.backupTempArrays.contains(variable)) {
							Variable var_bk = new Variable();
							var_bk.setName(variable+"_bk");
							varList.addVariable(var_bk);
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
				/* 
				 * if several variables have the same type declaration, 
				 * we should declare them in one line (for readability).
				 * we need a method to compare declStmt.
				 */
				boolean redundant = false;
				for (int i = 0; i < declSection.getDeclStmtList().getNumChild(); i++) {
					if (compareDecl(declSection.getDeclStmt(i), declStmt)) {
						for (int j = 0; j < declStmt.getVariableList().getNumChild(); j++) {
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
			/*
			 * declare types, especially character string.
			 */
			if (fcg.fotranTemporaries.get(tmpVariable).getMatlabClass().equals(PrimitiveClassReference.CHAR) 
					&& !fcg.fotranTemporaries.get(tmpVariable).getShape().isScalar()) {
				declStmt.setType(fcg.fortranMapping.getFortranTypeMapping("char")
						+"("+fcg.fotranTemporaries.get(tmpVariable).getShape().getDimensions().get(1)+")");
			}
			else 
				declStmt.setType(fcg.fortranMapping.getFortranTypeMapping(
					fcg.fotranTemporaries.get(tmpVariable).getMatlabClass().toString()));
			if (!fcg.fotranTemporaries.get(tmpVariable).getMatlabClass().equals(PrimitiveClassReference.CHAR) 
					&& !fcg.fotranTemporaries.get(tmpVariable).getShape().isScalar()) {
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
		mainEntry.setDeclarationSection(declSection);
		// here a hack to add timing TODO find a better way.
		StringBuffer timeEnd = new StringBuffer();
		timeEnd.append("\nCALL CPU_TIME(ftime2);\n");
		timeEnd.append("PRINT '(\"Time = \", f6.3, \" seconds.\")', ftime2-ftime1;\n\n");
		mainEntry.setProgramEnd(timeEnd + "END PROGRAM");
		return fcg;
	}
	
	/**
	 *  helper method to compare declStmt. since 
	 *  DeclStmt ::= <Type> [KeywordList] [ShapeInfo] VariableList
	 *  we should compare each of the components.
	 */
	public static boolean compareDecl(DeclStmt declStmt1, DeclStmt declStmt2) {
		if (!declStmt1.getType().equals(declStmt2.getType())) {
			return false;
		}
		if (declStmt1.hasKeywordList() ^ declStmt2.hasKeywordList()) {
			return false;
		}
		if (declStmt1.hasKeywordList() && declStmt2.hasKeywordList()) {
			if (declStmt1.getKeywordList().getNumChild() 
					!= declStmt2.getKeywordList().getNumChild()) {
				return false;
			}
			else {
				for (int i = 0; i < declStmt1.getKeywordList().getNumChild(); i++) {
					if (!declStmt1.getKeywordList().getKeyword(i).getName().equals(
							declStmt2.getKeywordList().getKeyword(i).getName())) {
						return false;
					}			
				}
			}
		}		
		return true;
	}
}

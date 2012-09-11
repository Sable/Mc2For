cd ~/Project/languages/Natlab/src
rm -rf natlab/backends/Fortran/codegen/FortranAST
java -jar natlab/backends/Fortran/codegen/jastadd2.jar --package=natlab.backends.Fortran.codegen.FortranAST natlab/backends/Fortran/codegen/FortranIR.ast

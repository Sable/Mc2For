rm ~/mclab/languages/Natlab/src/natlab/backends/Fortran/codegen/FortranAST/*.java
cd ~/mclab/languages/Natlab/src
java -jar natlab/backends/Fortran/codegen/jastadd2.jar --package=natlab.backends.Fortran.codegen.FortranAST natlab/backends/Fortran/codegen/FortranIR.ast natlab/backends/Fortran/codegen/PrettyPrinter.jadd

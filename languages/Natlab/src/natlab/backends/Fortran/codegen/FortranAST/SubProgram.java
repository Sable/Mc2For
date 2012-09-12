package natlab.backends.Fortran.codegen.FortranAST;


public abstract class SubProgram extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 2

    public SubProgram() {
        super();

    }

    public Object clone() throws CloneNotSupportedException {
        SubProgram node = (SubProgram)super.clone();
    return node;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 0;
  }
    // Declared in PrettyPrinter.jadd at line 4

   public void pp() {}

}

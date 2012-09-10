package natlab.backends.Fortran.codegen.FortranAST;
public abstract class subProgram extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 2

    public subProgram() {
        super();

    }

    public Object clone() throws CloneNotSupportedException {
        subProgram node = (subProgram)super.clone();
    return node;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 0;
  }
}

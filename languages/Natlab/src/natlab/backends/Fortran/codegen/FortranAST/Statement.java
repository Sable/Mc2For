package natlab.backends.Fortran.codegen.FortranAST;


public abstract class Statement extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 17

    public Statement() {
        super();

    }

    public Object clone() throws CloneNotSupportedException {
        Statement node = (Statement)super.clone();
    return node;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 0;
  }
}

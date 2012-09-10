package natlab.backends.Fortran.codegen.FortranAST;
public abstract class Program extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 1

    public Program() {
        super();

    }

    public Object clone() throws CloneNotSupportedException {
        Program node = (Program)super.clone();
    return node;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 0;
  }
}

package natlab.backends.Fortran.codegen.FortranAST;
public abstract class Exp extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 21

    public Exp() {
        super();

    }

    public Object clone() throws CloneNotSupportedException {
        Exp node = (Exp)super.clone();
    return node;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 0;
  }
}

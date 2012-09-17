package natlab.backends.Fortran.codegen.FortranAST;


public abstract class Expression extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 25

    public Expression() {
        super();

    }

    public Object clone() throws CloneNotSupportedException {
        Expression node = (Expression)super.clone();
    return node;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 0;
  }
    // Declared in PrettyPrinter.jadd at line 104

    public void pp() {}

}

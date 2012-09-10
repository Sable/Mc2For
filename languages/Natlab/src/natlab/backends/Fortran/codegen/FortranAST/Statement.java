package FortranAST;
public abstract class Statement extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 15

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

package natlab.backends.Fortran.codegen.FortranAST;


public class LiteralExp extends Exp implements Cloneable {
    // Declared in FortranIR.ast line 20

    public LiteralExp() {
        super();

        setChild(null, 0);
    }

    // Declared in FortranIR.ast line 20
    public LiteralExp(Variable p0) {
        setChild(p0, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        LiteralExp node = (LiteralExp)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          LiteralExp node = (LiteralExp)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        LiteralExp res = (LiteralExp)copy();
        for(int i = 0; i < getNumChild(); i++) {
          ASTNode node = getChildNoTransform(i);
          if(node != null) node = node.fullCopy();
          res.setChild(node, i);
        }
        return res;
    }
    public void flushCache() {
        super.flushCache();
    }
  protected int numChildren() {
    return 1;
  }
    // Declared in FortranIR.ast line 20
    public void setVariable(Variable node) {
        setChild(node, 0);
    }
    public Variable getVariable() {
        return (Variable)getChild(0);
    }

    public Variable getVariableNoTransform() {
        return (Variable)getChildNoTransform(0);
    }


}

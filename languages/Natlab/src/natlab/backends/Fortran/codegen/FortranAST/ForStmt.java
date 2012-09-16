package natlab.backends.Fortran.codegen.FortranAST;


public class ForStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 39

    public ForStmt() {
        super();

        setChild(new Opt(), 0);
        setChild(null, 1);
    }

    // Declared in FortranIR.ast line 39
    public ForStmt(String p0, String p1, Opt p2, String p3, StatementSection p4) {
        setLoopVar(p0);
        setLowBoundary(p1);
        setChild(p2, 0);
        setHighBoundary(p3);
        setChild(p4, 1);
    }

    public Object clone() throws CloneNotSupportedException {
        ForStmt node = (ForStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          ForStmt node = (ForStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        ForStmt res = (ForStmt)copy();
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
    return 2;
  }
    // Declared in FortranIR.ast line 39
    private String tokenString_LoopVar;
    public void setLoopVar(String value) {
        tokenString_LoopVar = value;
    }
    public String getLoopVar() {
        return tokenString_LoopVar;
    }


    // Declared in FortranIR.ast line 39
    private String tokenString_LowBoundary;
    public void setLowBoundary(String value) {
        tokenString_LowBoundary = value;
    }
    public String getLowBoundary() {
        return tokenString_LowBoundary;
    }


    // Declared in FortranIR.ast line 39
    public void setIncOpt(Opt opt) {
        setChild(opt, 0);
    }

    public boolean hasInc() {
        return getIncOpt().getNumChild() != 0;
    }

    public Inc getInc() {
        return (Inc)getIncOpt().getChild(0);
    }

    public void setInc(Inc node) {
        getIncOpt().setChild(node, 0);
    }
    public Opt getIncOpt() {
        return (Opt)getChild(0);
    }

    public Opt getIncOptNoTransform() {
        return (Opt)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 39
    private String tokenString_HighBoundary;
    public void setHighBoundary(String value) {
        tokenString_HighBoundary = value;
    }
    public String getHighBoundary() {
        return tokenString_HighBoundary;
    }


    // Declared in FortranIR.ast line 39
    public void setForBlock(StatementSection node) {
        setChild(node, 1);
    }
    public StatementSection getForBlock() {
        return (StatementSection)getChild(1);
    }

    public StatementSection getForBlockNoTransform() {
        return (StatementSection)getChildNoTransform(1);
    }


}

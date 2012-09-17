package natlab.backends.Fortran.codegen.FortranAST;


public class WhileStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 38

    public WhileStmt() {
        super();

        setChild(null, 0);
    }

    // Declared in FortranIR.ast line 38
    public WhileStmt(String p0, StatementSection p1) {
        setCondition(p0);
        setChild(p1, 0);
    }

    public Object clone() throws CloneNotSupportedException {
        WhileStmt node = (WhileStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          WhileStmt node = (WhileStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        WhileStmt res = (WhileStmt)copy();
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
    // Declared in FortranIR.ast line 38
    private String tokenString_Condition;
    public void setCondition(String value) {
        tokenString_Condition = value;
    }
    public String getCondition() {
        return tokenString_Condition;
    }


    // Declared in FortranIR.ast line 38
    public void setWhileBlock(StatementSection node) {
        setChild(node, 0);
    }
    public StatementSection getWhileBlock() {
        return (StatementSection)getChild(0);
    }

    public StatementSection getWhileBlockNoTransform() {
        return (StatementSection)getChildNoTransform(0);
    }


    // Declared in PrettyPrinter.jadd at line 174

    public void pp() {
    	System.out.print("do while ("+getCondition()+")");
    	System.out.print("\n");
    	getWhileBlock().pp();
    	System.out.print("enddo");
    	
    }

}

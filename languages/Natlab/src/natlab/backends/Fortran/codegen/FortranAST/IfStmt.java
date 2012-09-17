package natlab.backends.Fortran.codegen.FortranAST;


public class IfStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 36

    public IfStmt() {
        super();

        setChild(null, 0);
        setChild(new Opt(), 1);
    }

    // Declared in FortranIR.ast line 36
    public IfStmt(String p0, StatementSection p1, Opt p2) {
        setCondition(p0);
        setChild(p1, 0);
        setChild(p2, 1);
    }

    public Object clone() throws CloneNotSupportedException {
        IfStmt node = (IfStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          IfStmt node = (IfStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        IfStmt res = (IfStmt)copy();
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
    // Declared in FortranIR.ast line 36
    private String tokenString_Condition;
    public void setCondition(String value) {
        tokenString_Condition = value;
    }
    public String getCondition() {
        return tokenString_Condition;
    }


    // Declared in FortranIR.ast line 36
    public void setIfBlock(StatementSection node) {
        setChild(node, 0);
    }
    public StatementSection getIfBlock() {
        return (StatementSection)getChild(0);
    }

    public StatementSection getIfBlockNoTransform() {
        return (StatementSection)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 36
    public void setElseBlockOpt(Opt opt) {
        setChild(opt, 1);
    }

    public boolean hasElseBlock() {
        return getElseBlockOpt().getNumChild() != 0;
    }

    public StatementSection getElseBlock() {
        return (StatementSection)getElseBlockOpt().getChild(0);
    }

    public void setElseBlock(StatementSection node) {
        getElseBlockOpt().setChild(node, 0);
    }
    public Opt getElseBlockOpt() {
        return (Opt)getChild(1);
    }

    public Opt getElseBlockOptNoTransform() {
        return (Opt)getChildNoTransform(1);
    }


    // Declared in PrettyPrinter.jadd at line 164

    public void pp() {
    	System.out.print("if ("+getCondition()+") then");
    	System.out.print("\n");
    	getIfBlock().pp();
    	if(hasElseBlock()) {
    		System.out.print("else\n");
    		getElseBlock().pp();
    	}
    	System.out.print("endif");
    }

}

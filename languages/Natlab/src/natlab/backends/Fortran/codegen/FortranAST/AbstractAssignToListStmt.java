package natlab.backends.Fortran.codegen.FortranAST;


public class AbstractAssignToListStmt extends Statement implements Cloneable {
    // Declared in FortranIR.ast line 24

    public AbstractAssignToListStmt() {
        super();

        setChild(new Opt(), 0);
        setChild(null, 1);
    }

    // Declared in FortranIR.ast line 24
    public AbstractAssignToListStmt(Opt p0, Expression p1) {
        setChild(p0, 0);
        setChild(p1, 1);
    }

    public Object clone() throws CloneNotSupportedException {
        AbstractAssignToListStmt node = (AbstractAssignToListStmt)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          AbstractAssignToListStmt node = (AbstractAssignToListStmt)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        AbstractAssignToListStmt res = (AbstractAssignToListStmt)copy();
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
    // Declared in FortranIR.ast line 24
    public void setRuntimeCheckOpt(Opt opt) {
        setChild(opt, 0);
    }

    public boolean hasRuntimeCheck() {
        return getRuntimeCheckOpt().getNumChild() != 0;
    }

    public RuntimeCheck getRuntimeCheck() {
        return (RuntimeCheck)getRuntimeCheckOpt().getChild(0);
    }

    public void setRuntimeCheck(RuntimeCheck node) {
        getRuntimeCheckOpt().setChild(node, 0);
    }
    public Opt getRuntimeCheckOpt() {
        return (Opt)getChild(0);
    }

    public Opt getRuntimeCheckOptNoTransform() {
        return (Opt)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 24
    public void setExpression(Expression node) {
        setChild(node, 1);
    }
    public Expression getExpression() {
        return (Expression)getChild(1);
    }

    public Expression getExpressionNoTransform() {
        return (Expression)getChildNoTransform(1);
    }


    // Declared in PrettyPrinter.jadd at line 98

    public void pp() {
    	if(hasRuntimeCheck()) {
    		System.out.println(getRuntimeCheck());
    	}
    	getExpression().pp();
    }

}

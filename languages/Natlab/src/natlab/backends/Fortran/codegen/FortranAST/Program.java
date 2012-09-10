package natlab.backends.Fortran.codegen.FortranAST;
public class Program extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 1

    public Program() {
        super();

        setChild(null, 0);
        setChild(null, 1);
        setChild(null, 2);
    }

    // Declared in FortranIR.ast line 1
    public Program(ProgramMain p0, ProgramUserDefFunc p1, ProgramUserDefSubroutine p2) {
        setChild(p0, 0);
        setChild(p1, 1);
        setChild(p2, 2);
    }

    public Object clone() throws CloneNotSupportedException {
        Program node = (Program)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          Program node = (Program)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        Program res = (Program)copy();
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
    return 3;
  }
    // Declared in FortranIR.ast line 1
    public void setProgramMain(ProgramMain node) {
        setChild(node, 0);
    }
    public ProgramMain getProgramMain() {
        return (ProgramMain)getChild(0);
    }

    public ProgramMain getProgramMainNoTransform() {
        return (ProgramMain)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 1
    public void setProgramUserDefFunc(ProgramUserDefFunc node) {
        setChild(node, 1);
    }
    public ProgramUserDefFunc getProgramUserDefFunc() {
        return (ProgramUserDefFunc)getChild(1);
    }

    public ProgramUserDefFunc getProgramUserDefFuncNoTransform() {
        return (ProgramUserDefFunc)getChildNoTransform(1);
    }


    // Declared in FortranIR.ast line 1
    public void setProgramUserDefSubroutine(ProgramUserDefSubroutine node) {
        setChild(node, 2);
    }
    public ProgramUserDefSubroutine getProgramUserDefSubroutine() {
        return (ProgramUserDefSubroutine)getChild(2);
    }

    public ProgramUserDefSubroutine getProgramUserDefSubroutineNoTransform() {
        return (ProgramUserDefSubroutine)getChildNoTransform(2);
    }


}

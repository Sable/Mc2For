package natlab.backends.Fortran.codegen.FortranAST;


public class SubProgram extends ASTNode implements Cloneable {
    // Declared in FortranIR.ast line 2

    public SubProgram() {
        super();

        setChild(null, 0);
        setChild(null, 1);
        setChild(null, 2);
    }

    // Declared in FortranIR.ast line 2
    public SubProgram(ProgramTitle p0, DeclarationSection p1, StatementSection p2) {
        setChild(p0, 0);
        setChild(p1, 1);
        setChild(p2, 2);
    }

    public Object clone() throws CloneNotSupportedException {
        SubProgram node = (SubProgram)super.clone();
    return node;
    }
    public ASTNode copy() {
      try {
          SubProgram node = (SubProgram)clone();
          if(children != null) node.children = (ASTNode[])children.clone();
          return node;
      } catch (CloneNotSupportedException e) {
      }
      System.err.println("Error: Could not clone node of type " + getClass().getName() + "!");
      return null;
    }
    public ASTNode fullCopy() {
        SubProgram res = (SubProgram)copy();
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
    // Declared in FortranIR.ast line 2
    public void setProgramTitle(ProgramTitle node) {
        setChild(node, 0);
    }
    public ProgramTitle getProgramTitle() {
        return (ProgramTitle)getChild(0);
    }

    public ProgramTitle getProgramTitleNoTransform() {
        return (ProgramTitle)getChildNoTransform(0);
    }


    // Declared in FortranIR.ast line 2
    public void setDeclarationSection(DeclarationSection node) {
        setChild(node, 1);
    }
    public DeclarationSection getDeclarationSection() {
        return (DeclarationSection)getChild(1);
    }

    public DeclarationSection getDeclarationSectionNoTransform() {
        return (DeclarationSection)getChildNoTransform(1);
    }


    // Declared in FortranIR.ast line 2
    public void setStatementSection(StatementSection node) {
        setChild(node, 2);
    }
    public StatementSection getStatementSection() {
        return (StatementSection)getChild(2);
    }

    public StatementSection getStatementSectionNoTransform() {
        return (StatementSection)getChildNoTransform(2);
    }


    // Declared in PrettyPrinter.jadd at line 4

	public void pp() {
		getProgramTitle().pp();
		getDeclarationSection().pp();
		getStatementSection().pp();
	}

}

package at.dms.kjc.sir;

import at.dms.kjc.*;
import at.dms.compiler.*;

/**
 * This represents a push expression (can only be used as a statement).
 */
public class SIRPushExpression extends JExpression {

    /**
     * The argument to the push expression--the item to push.
     */
    protected JExpression arg;

    /**
     * Type of the item to push.
     */
    protected CType tapeType;

    /**
     * Construct a node in the parsing tree
     * @param	where		the line of this node in the source code
     * @param	arg		the argument of the call
     */
    public SIRPushExpression(TokenReference where, JExpression arg)
    {
	super(where);
        this.tapeType = null;
	this.arg = arg;
    }

    /**
     * Construct a node in the parsing tree
     * @param	where		the line of this node in the source code
     * @param	arg		the argument of the call
     */
    public SIRPushExpression(JExpression arg)
    {
	super(null);
        this.tapeType = null;
	this.arg = arg;
    }

    /**
     * Sets the type of the tape being pushed to
     * @param   type             the type of the tape
     */
    public void setTapeType(CType type)
    {
        this.tapeType = type;
    }

    // ----------------------------------------------------------------------
    // ACCESSORS
    // ----------------------------------------------------------------------

    /**
     * @return the type of this expression
     */
    public CType getType() {
	return CStdType.Void;
    }

    /**
     * Returns true iff this expression can be used as a statement (JLS 14.8)
     */
    public boolean isStatementExpression() {
	return true;
    }

    // ----------------------------------------------------------------------
    // SEMANTIC ANALYSIS
    // ----------------------------------------------------------------------

    /**
     * Throws an exception (NOT SUPPORTED YET)
     */
    public JExpression analyse(CExpressionContext context) throws PositionedError {
	at.dms.util.Utils.fail("Analysis of SIR nodes not supported yet.");
	return this;
    }

    // ----------------------------------------------------------------------
    // CODE GENERATION
    // ----------------------------------------------------------------------

    /**
     * Accepts the specified visitor.
     */
    public void accept(KjcVisitor p) {
	if (p instanceof SLIRVisitor) {
	    ((SLIRVisitor)p).visitPushExpression(this, tapeType, arg);
	} else {
	    // otherwise, visit the argument
	    arg.accept(p);
	}
    }

    /**
     * Accepts the specified attribute visitor.  NOT SUPPORTED YET.
     * @param   p               the visitor
     */
    public Object accept(AttributeVisitor p) {
        at.dms.util.Utils.fail("Visitors to SIRE nodes not supported yet.");
        return null;
    }

    /*
     * Generates JVM bytecode to evaluate this expression.  NOT SUPPORTED YET.
     *
     * @param	code		the bytecode sequence
     * @param	discardValue	discard the result of the evaluation ?
     */
    public void genCode(CodeSequence code, boolean discardValue) {
	at.dms.util.Utils.fail("Codegen of SIR nodes not supported yet.");
    }
}

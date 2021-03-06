/*
 * Copyright (C) 1990-2001 DMS Decision Management Systems Ges.m.b.H.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id: JIntLiteral.java,v 1.14 2006-10-27 20:48:54 dimock Exp $
 */

package at.dms.kjc;

import at.dms.classfile.PushLiteralInstruction;
import at.dms.compiler.NumberParser;
import at.dms.compiler.PositionedError;
import at.dms.compiler.TokenReference;
import at.dms.util.InconsistencyException;

/**
 * JLS 3.10.1 Integer Literals. This class represents int literals.
 */
public class JIntLiteral extends JLiteral {

    // ----------------------------------------------------------------------
    // CONSTRUCTORS
    // ----------------------------------------------------------------------

    protected JIntLiteral() {} // for cloner only

    /**
     * Constructs a literal expression from a textual representation.
     * @param   where       the line of this node in the source code
     * @param   image       the textual representation of this literal
     */
    public JIntLiteral(TokenReference where, String image)
        throws PositionedError
    {
        super(where);
        if (image.startsWith("0")) {
            // octal or hexadecimal
            try {
                this.value = NumberParser.decodeInt(image);
            } catch (NumberFormatException e) {
                throw new PositionedError(where, KjcMessages.INVALID_INT_LITERAL, image);
            }
            this.invert = false;
        } else {
            // decimal
            int value;

            try {
                value = NumberParser.decodeInt("-" + image);
            } catch (NumberFormatException e) {
                throw new PositionedError(where, KjcMessages.INVALID_INT_LITERAL, image);
            }
            if (value == Integer.MIN_VALUE) {
                this.value = value;
                this.invert = true;
            } else {
                this.value = -value;
                this.invert = false;
            }
        }
    }

    /**
     * Constructs a literal expression from a constant value.
     * @param   where       the line of this node in the source code
     * @param   value       the constant value
     */
    public JIntLiteral(TokenReference where, int value) {
        super(where);
        this.value = value;
        this.invert = false;
    }

    public JIntLiteral(int value) {
        this(null, value);
    }

    // ----------------------------------------------------------------------
    // ACCESSORS
    // ----------------------------------------------------------------------

    /**
     * Returns a literal with the sign inverted.
     * This is needed to handle 2147483648 which cannot be stored
     * in a variable of type int.
     *
     * JLS 3.10.1 :
     * The largest decimal literal of type int is 2147483648 (2^31). All
     * decimal literals from 0 to 2147483647 may appear anywhere an int
     * literal may appear, but the literal 2147483648 may appear only as the
     * operand of the unary negation operator -.
     */
    public JIntLiteral getOppositeLiteral() throws PositionedError {
        return new JIntLiteral(getTokenReference(), invert ? Integer.MIN_VALUE : -value);
    }

    /**
     * Returns the type of this expression.
     */
    public CType getType() {
        return CStdType.Integer;
    }

    /**
     * Returns the constant value of the expression.
     */
    public int intValue() {
        if (this.invert) {
            throw new InconsistencyException();
        }
        return value;
    }

    /**
     * Returns true iff the value of this literal is the
     * default value for this type (JLS 4.5.5).
     */
    public boolean isDefault() {
        return value == 0;
    }

    /**
     * Returns a string representation of this literal.
     */
    public String toString() {
        StringBuffer    buffer = new StringBuffer();

        buffer.append("JIntLiteral[");
        if (invert) {
            buffer.append("2147483648 (= 2^31)");
        } else {
            buffer.append(value);
        }
        buffer.append("]");
        return buffer.toString();
    }

    public String convertToString() {
        return ""+value;
    }

    // ----------------------------------------------------------------------
    // SEMANTIC ANALYSIS
    // ----------------------------------------------------------------------

    /**
     * Analyses the expression (semantically).
     * @param   context     the analysis context
     * @return  an equivalent, analysed expression
     * @exception   PositionedError the analysis detected an error
     */
    public JExpression analyse(CExpressionContext context) throws PositionedError {
        check(context, !this.invert, KjcMessages.INVALID_INT_LITERAL, "2147483648 (= 2^31)");
        return this;
    }

    /**
     * Can this expression be converted to the specified type by assignment conversion (JLS 5.2) ?
     * @param   dest        the destination type
     * @return  true iff the conversion is valid
     */
    public boolean isAssignableTo(CType dest) {
        switch (dest.getTypeID()) {
        case TID_BYTE:
            return (byte)value == value;
        case TID_SHORT:
            return (short)value == value;
        case TID_CHAR:
            return (char)value == value;
        default:
            return CStdType.Integer.isAssignableTo(dest);
        }
    }

    /**
     * convertType
     * changes the type of this expression to an other
     * @param  dest the destination type
     */
    public JExpression convertType(CType dest, CExpressionContext context) {
        if (this.invert) {
            throw new InconsistencyException();
        }

        switch (dest.getTypeID()) {
        case TID_BYTE:
            return new JByteLiteral(getTokenReference(), (byte)value);
        case TID_SHORT:
            return new JShortLiteral(getTokenReference(), (short)value);
        case TID_CHAR:
            return new JCharLiteral(getTokenReference(), (char)value);
        case TID_INT:
            return this;
        case TID_LONG:
            return new JLongLiteral(getTokenReference(), (long)value);
        case TID_FLOAT:
            return new JFloatLiteral(getTokenReference(), (float)value);
        case TID_DOUBLE:
            return new JDoubleLiteral(getTokenReference(), (double)value);
        case TID_CLASS:
            if (dest != CStdType.String) {
                throw new InconsistencyException("cannot convert from int to " + dest);
            }
            return new JStringLiteral(getTokenReference(), "" + value);
        default:
            throw new InconsistencyException("cannot convert from int to " + dest);
        }
    }

    // ----------------------------------------------------------------------
    // CODE GENERATION
    // ----------------------------------------------------------------------

    /**
     * Accepts the specified visitor
     * @param   p       the visitor
     */
    public void accept(KjcVisitor p) {
        p.visitIntLiteral(value);
    }

    /**
     * Accepts the specified attribute visitor
     * @param   p       the visitor
     */
    public Object accept(AttributeVisitor p) {
        return    p.visitIntLiteral(this, value);
    }

    /**
     * Accepts the specified visitor
     * @param p the visitor
     * @param o object containing extra data to be passed to visitor
     * @return object containing data generated by visitor 
     */
    @Override
    public <S,T> S accept(ExpressionVisitor<S,T> p, T o) {
        return p.visitIntLiteral(this,o);
    }


    /**
     * Generates JVM bytecode to evaluate this expression.
     *
     * @param   code        the bytecode sequence
     * @param   discardValue    discard the result of the evaluation ?
     */
    public void genCode(CodeSequence code, boolean discardValue) {
        if (! discardValue) {
            setLineNumber(code);
            code.plantInstruction(new PushLiteralInstruction(value));
        }
    }

    /**
     * Returns whether or <pre>o</pre> this represents a literal with the same
     * value as this.
     */
    public boolean equals(Object o) {
        return (o!=null && 
                (o instanceof JIntLiteral) &&
                ((JIntLiteral)o).value==this.value);
    }

    // ----------------------------------------------------------------------
    // DATA MEMBERS
    // ----------------------------------------------------------------------

    // value = MAX_VALUE + 1, valid only as argument to unary minus
    private /* final */ boolean     invert;  // removed final for cloner
    private /* final */ int     value;  // removed final for cloner

    /** THE FOLLOWING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */

    /** Returns a deep clone of this object. */
    public Object deepClone() {
        at.dms.kjc.JIntLiteral other = new at.dms.kjc.JIntLiteral();
        at.dms.kjc.AutoCloner.register(this, other);
        deepCloneInto(other);
        return other;
    }

    /** Clones all fields of this into <pre>other</pre> */
    protected void deepCloneInto(at.dms.kjc.JIntLiteral other) {
        super.deepCloneInto(other);
        other.invert = this.invert;
        other.value = this.value;
    }

    /** THE PRECEDING SECTION IS AUTO-GENERATED CLONING CODE - DO NOT MODIFY! */
}

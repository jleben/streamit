/*
 * DoComplexProp.java: perform constant propagation on function bodies
 * David Maze <dmaze@cag.lcs.mit.edu>
 * $Id: DoComplexProp.java,v 1.9 2002-11-20 20:43:56 dmaze Exp $
 */

package streamit.frontend.tojava;

import streamit.frontend.nodes.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.ArrayList;

/**
 * DoComplexProp does all of the work required to convert complex
 * expressions to real ones in front-end code.  It builds up symbol
 * tables for variables, and then runs VarToComplex to replace
 * references to complex variables with complex expressions referencing
 * the fields of the variables.  ComplexProp is then run to cause an
 * expression to be either purely real or be an ExprComplex at the
 * top level.  Finally, this pass inserts statements as necessary to
 * cause all statements to deal with purely real values.
 *
 * Things this is known to punt on:
 * -- Complex parameters to function calls not handled directly
 *    by ComplexProp need to be temporary variables.
 * -- Initialized complex variables should have their value separated
 *    out.
 * -- Semantics of for loops (for(complex c = 1+1i; abs(c) < 5; c += 1i))
 */
public class DoComplexProp extends FEReplacer
{
    private StreamType streamType;
    private LinkedList oldStreamTypes;
    private SymbolTable symTab;
    private GetExprType getExprType;
    private VarToComplex varToComplex;
    private ComplexProp cplxProp;
    private TempVarGen varGen;
    
    public DoComplexProp(TempVarGen varGen)
    {
        streamType = null;
        oldStreamTypes = new LinkedList();
        symTab = null;
        varToComplex = null;
        cplxProp = new ComplexProp();
        this.varGen = varGen;
    }

    private void pushSymTab()
    {
        symTab = new SymbolTable(symTab);
        varToComplex = new VarToComplex(symTab, streamType);
        getExprType = new GetExprType(symTab, streamType);
    }
    
    private void popSymTab()
    {
        symTab = symTab.getParent();
        varToComplex = new VarToComplex(symTab, streamType);
        getExprType = new GetExprType(symTab, streamType);
    }

    private void paramListToSymTab(List params)
    {
        for (Iterator iter = params.iterator(); iter.hasNext(); )
        {
            Parameter param = (Parameter)iter.next();
            symTab.registerVar(param.getName(), param.getType());
        }
    }

    private void pushStreamType(StreamType st)
    {
        oldStreamTypes.addFirst(streamType);
        streamType = st;
        varToComplex = new VarToComplex(symTab, streamType);
        getExprType = new GetExprType(symTab, streamType);
    }
    
    private void popStreamType()
    {
        streamType = (StreamType)oldStreamTypes.removeFirst();
        varToComplex = new VarToComplex(symTab, streamType);
        getExprType = new GetExprType(symTab, streamType);
    }

    private Expression doExprProp(Expression expr)
    {
        expr = (Expression)expr.accept(this);
        expr = (Expression)expr.accept(varToComplex);
        expr = (Expression)expr.accept(cplxProp);
        return expr;
    }

    /**
     * Create an initialized temporary variable to handle a
     * complex expression.  If expr is an ExprComplex, uses addStatement()
     * to add statements to declare a new temporary variable and
     * separately initialize its real and complex parts, and return
     * an ExprVar corresponding to the temporary.  Otherwise, return
     * expr.
     */
    private Expression makeComplexTemporary(Expression expr)
    {
        if (!(expr instanceof ExprComplex))
            return expr;
        ExprComplex cplx = (ExprComplex)expr;
        // This code path almost certainly isn't going to follow
        // the path tojava.TempVarGen was written for, so we can
        // ignore the type parameter.
        String tempVar = varGen.varName(varGen.nextVar(null));
        Expression exprVar = new ExprVar(expr.getContext(), tempVar);
        Type type = new TypePrimitive(TypePrimitive.TYPE_COMPLEX);
        addStatement(new StmtVarDecl(expr.getContext(), type, tempVar, null));
        symTab.registerVar(tempVar, type);
        addStatement(new StmtAssign(expr.getContext(),
                                    new ExprField(expr.getContext(),
                                                  exprVar, "real"),
                                    cplx.getReal()));
        addStatement(new StmtAssign(expr.getContext(),
                                    new ExprField(expr.getContext(),
                                                  exprVar, "imag"),
                                    cplx.getImag()));
        return exprVar;
    }

    /**
     * Create an initialized temporary variable to hold an arbitrary
     * expression.  Uses addStatement() to add an initializer for
     * the variable, and returns the variable.
     */
    private Expression makeAnyTemporary(Expression expr)
    {
        String tempVar = varGen.varName(varGen.nextVar(null));
        Expression exprVar = new ExprVar(expr.getContext(), tempVar);
        Type type = (Type)expr.accept(getExprType);
        addStatement(new StmtVarDecl(expr.getContext(), type, tempVar, expr));
        symTab.registerVar(tempVar, type);
        return exprVar;
    }

    protected Expression doExpression(Expression expr)
    {
        return doExprProp(expr);
    }

    public Object visitExprPeek(ExprPeek expr)
    {
        Expression x = (Expression)super.visitExprPeek(expr);
        // If the stream's input type is complex, we want a temporary
        // instead.
        if (streamType.getIn().isComplex())
            x = makeAnyTemporary(x);
        return x;
    }

    public Object visitExprPop(ExprPop expr)
    {
        Expression x = (Expression)super.visitExprPop(expr);
        // If the stream's input type is complex, we want a temporary
        // instead.
        // if (streamType.getIn().isComplex())
        //     x = makeAnyTemporary(x);
        return x;
    }

    public Object visitFunction(Function func)
    {
        pushSymTab();
        paramListToSymTab(func.getParams());
        Object result = super.visitFunction(func);
        popSymTab();
        return result;
    }

    public Object visitFuncWork(FuncWork func)
    {
        pushSymTab();
        paramListToSymTab(func.getParams());
        Object result = super.visitFuncWork(func);
        popSymTab();
        return result;
    }

    public Object visitStmtAssign(StmtAssign stmt)
    {
        Expression lhs = stmt.getLHS();
        Expression rhs = doExprProp(stmt.getRHS());
        if (rhs instanceof ExprComplex)
        {
            ExprComplex cplx = (ExprComplex)rhs;
            addStatement(new StmtAssign(stmt.getContext(),
                                        new ExprField(lhs.getContext(),
                                                      lhs, "real"),
                                        cplx.getReal()));
            addStatement(new StmtAssign(stmt.getContext(),
                                        new ExprField(lhs.getContext(),
                                                      lhs, "imag"),
                                        cplx.getImag()));
            return null;
        }
        else if (((Type)lhs.accept(getExprType)).isComplex() &&
                 !((Type)rhs.accept(getExprType)).isComplex())
        {
            addStatement(new StmtAssign(stmt.getContext(),
                                        new ExprField(lhs.getContext(),
                                                     lhs, "real"),
                                        rhs));
            addStatement(new StmtAssign(stmt.getContext(),
                                        new ExprField(lhs.getContext(),
                                                      lhs, "imag"),
                                        new ExprConstInt(lhs.getContext(),
                                                         0)));
            return null;
        }
        else if (rhs != stmt.getRHS())
            return new StmtAssign(stmt.getContext(), lhs, rhs);
        else
            return stmt;
    }

    public Object visitStmtBlock(StmtBlock block)
    {
        pushSymTab();
        Statement result = (Statement)super.visitStmtBlock(block);
        popSymTab();
        return result;
    }

    public Object visitStmtEnqueue(StmtEnqueue stmt)
    {
        Expression value = stmt.getValue();
        value = doExprProp(value);
        value = makeComplexTemporary(value);
        return new StmtEnqueue(stmt.getContext(), value);
    }

    public Object visitStmtExpr(StmtExpr stmt)
    {
        Expression newExpr = doExprProp(stmt.getExpression());
        if (newExpr instanceof ExprComplex)
        {
            ExprComplex cplx = (ExprComplex)newExpr;
            addStatement(new StmtExpr(stmt.getContext(), cplx.getReal()));
            addStatement(new StmtExpr(stmt.getContext(), cplx.getImag()));
            return null;
        }
        if (newExpr == stmt.getExpression())
            return stmt;
        return new StmtExpr(stmt.getContext(), newExpr);
    }

    public Object visitStmtPush(StmtPush stmt)
    {
        Expression value = stmt.getValue();
        value = doExprProp(value);
        value = makeComplexTemporary(value);
        return new StmtPush(stmt.getContext(), value);
    }

    public Object visitStmtReturn(StmtReturn stmt)
    {
        Expression value = stmt.getValue();
        if (value == null) return stmt;
        value = doExprProp(value);
        value = makeComplexTemporary(value);
        return new StmtReturn(stmt.getContext(), value);
    }

    public Object visitStmtVarDecl(StmtVarDecl stmt)
    {
        // Go ahead and do propagation:
        stmt = (StmtVarDecl)super.visitStmtVarDecl(stmt);
        symTab.registerVar(stmt.getName(), stmt.getType());
        // Now check to see if this is initialized,
        if (stmt.getInit() == null) return stmt;
        // and if so, if the type is complex,
        if (!stmt.getType().isComplex()) return stmt;
        // and if then, if the right-hand side is complex too,
        Expression exprVar = new ExprVar(stmt.getContext(), stmt.getName());
        if (stmt.getInit() instanceof ExprComplex)
        {
            // Right.  Create the separate initialization statements.
            ExprComplex cplx = (ExprComplex)stmt.getInit();
            addStatement(new StmtVarDecl(stmt.getContext(), stmt.getType(),
                                         stmt.getName(), null));
            addStatement(new StmtAssign(stmt.getContext(),
                                        new ExprField(stmt.getContext(),
                                                      exprVar, "real"),
                                        cplx.getReal()));
            addStatement(new StmtAssign(stmt.getContext(),
                                        new ExprField(stmt.getContext(),
                                                      exprVar, "imag"),
                                        cplx.getImag()));
            return null;
        }
        // or if the right-hand side isn't complex at all.
        if (!((Type)stmt.getInit().accept(getExprType)).isComplex())
        {
            addStatement(new StmtVarDecl(stmt.getContext(), stmt.getType(),
                                         stmt.getName(), null));
            addStatement(new StmtAssign(stmt.getContext(),
                                        new ExprField(stmt.getContext(),
                                                      exprVar, "real"),
                                        stmt.getInit()));
            addStatement(new StmtAssign(stmt.getContext(),
                                        new ExprField(stmt.getContext(),
                                                      exprVar, "imag"),
                                        new ExprConstInt(stmt.getContext(),
                                                         0)));
            return null;
        }
        // Otherwise, we have complex foo = (complex)bar(), which is fine.
        return stmt;
    }

    public Object visitStreamSpec(StreamSpec spec)
    {
        pushSymTab();
        pushStreamType(spec.getStreamType());
        paramListToSymTab(spec.getParams());
        
        Object result = super.visitStreamSpec(spec);

        popStreamType();
        popSymTab();

        return result;
    }
}

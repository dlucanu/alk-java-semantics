package execution.state.symbolic;

import ast.AST;
import ast.attr.BuiltInMethodASTAttr;
import ast.attr.IdASTAttr;
import ast.enums.Operator;
import ast.enums.Primitive;
import ast.expr.*;
import ast.expr.fol.ForAllExprAST;
import ast.expr.fol.ImpliesAST;
import ast.symbolic.IdDeclAST;
import ast.symbolic.SelectAST;
import ast.type.ArrayDataTypeAST;
import ast.type.DataTypeAST;
import ast.type.IntDataTypeAST;
import execution.ExecutionPayload;
import execution.exhaustive.SplitMapper;
import execution.parser.exceptions.AlkException;
import execution.state.ExecutionState;
import execution.state.expression.IterableWithIntervalState;
import execution.types.alkInt.AlkInt;
import symbolic.SymbolicValue;
import util.types.Storable;
import visitor.BuiltInMethodHelper;

import java.util.ArrayList;
import java.util.List;

import static execution.parser.exceptions.AlkException.ERR_LIMIT;

public class ArraySymbolicIntervalState
extends IterableWithIntervalState
{
    public ArraySymbolicIntervalState(Primitive primitive, AST ast, ExecutionPayload payload)
    {
        super(primitive, ast, payload);
    }

    @Override
    public Storable getFinalResult()
    {
        Storable x = limits.get(0);
        Storable y = limits.get(1);

        if (x instanceof AlkInt && y instanceof AlkInt)
        {
            return super.getFinalResult();
        }

        SymbolicValue left = SymbolicValue.toSymbolic(x);
        SymbolicValue right = SymbolicValue.toSymbolic(y);

        List<AST> children = new ArrayList<>();
        children.add(left.toAST());
        children.add(right.toAST());
        AST check = RelationalAST.createBinary(Operator.LOWEREQ, children);
        if (!getExec().getPathCondition().asserts(new SymbolicValue(check)))
        {
            super.handle(new AlkException(ERR_LIMIT));
        }

        // TODO: check both are ints

        // generate symbolic value
        AST symAST = SymIDAST.generate("[ival]");
        SymbolicValue value = new SymbolicValue(symAST);
        DataTypeAST dataType = new ArrayDataTypeAST(null);
        dataType.addChild(new IntDataTypeAST(null));
        getExec().getPathCondition().setType(symAST.getText(), dataType, true);

        // check size is preserved: $ap_0.size() == y - x + 1
        children.clear();
        children.add(right.toAST());
        children.add(left.toAST());
        AST diff = AdditiveAST.createBinary(Operator.SUBTRACT, children);

        children.clear();
        children.add(diff);
        children.add(new IntAST("1"));
        AST newSize = AdditiveAST.createBinary(Operator.ADD, children);

        FactorPointMethodAST oldSize = new FactorPointMethodAST(null);
        BuiltInMethodASTAttr attr = new BuiltInMethodASTAttr(BuiltInMethodHelper.parse("size"));
        oldSize.addAttribute(BuiltInMethodASTAttr.class, attr);
        oldSize.addChild(value.toAST());

        children.clear();
        children.add(oldSize);
        children.add(newSize);

        AST checkSize = EqualityAST.createBinary(Operator.EQUAL, children);
        getExec().getPathCondition().add(new SymbolicValue(checkSize));

        // do the value mapping: forall i : int :: 0 <= i && i < y - x + 1 ==> $ap_0[i] == x + i
        AST bound = SymIDAST.generate("[iter]");

        children.clear();
        children.add(new IntAST("0"));
        children.add(bound);
        AST lowerBound = RelationalAST.createBinary(Operator.LOWEREQ, children);

        children.clear();
        children.add(bound);
        children.add(newSize);
        AST upperBound = RelationalAST.createBinary(Operator.LOWER, children);

        children.clear();
        children.add(lowerBound);
        children.add(upperBound);
        AST conjunction = LogicalAndAST.createBinary(Operator.LOGICALAND, children);

        children.clear();
        children.add(left.toAST());
        children.add(bound);
        AST toMap = AdditiveAST.createBinary(Operator.ADD, children);

        AST fromMap = new SelectAST(null);
        fromMap.addChild(value.toAST());
        fromMap.addChild(bound);

        children.clear();
        children.add(fromMap);
        children.add(toMap);
        AST body = EqualityAST.createBinary(Operator.EQUAL, children);

        AST impliesAST = new ImpliesAST(null);
        impliesAST.addChild(conjunction);
        impliesAST.addChild(body);

        ForAllExprAST mapping = new ForAllExprAST(null);
        mapping.addChild(impliesAST);

        IdDeclAST idDecl = new IdDeclAST(null);
        IdASTAttr boundAttr = new IdASTAttr("$" + bound.getText());
        idDecl.addAttribute(IdASTAttr.class, boundAttr);
        idDecl.addChild(new IntDataTypeAST(null));
        mapping.addChild(idDecl);

        getExec().getPathCondition().setType(bound.getText(), new IntDataTypeAST(null), true);
        getExec().getPathCondition().add(new SymbolicValue(mapping));

        return generator.generate(value);
    }

    @Override
    public ExecutionState clone(SplitMapper sm) {
        ArraySymbolicIntervalState copy = new ArraySymbolicIntervalState(primitive, tree, payload.clone(sm));
        for (Storable value : limits)
        {
            copy.limits.add(value.weakClone(sm.getLocationMapper()));
        }
        return super.decorate(copy, sm);
    }
}
package execution.state.expression;

import execution.ExecutionResult;
import execution.state.GuardedGeneratorState;
import grammar.alkParser;
import parser.types.AlkValue;
import parser.visitors.expression.ExpressionVisitor;
import util.CtxState;
import util.Payload;

@CtxState(ctxClass = alkParser.InExpressionContext.class)
public class InExpressionState extends GuardedGeneratorState<AlkValue> {

    public InExpressionState(alkParser.InExpressionContext tree, Payload payload) {
        super(tree, payload, tree.equality_expression(), ExpressionVisitor.class);
    }

    @Override
    protected AlkValue interpretResult(AlkValue current, AlkValue next) {
        return current.in(next);
    }

}
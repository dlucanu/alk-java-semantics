package execution.state.expression;

import execution.state.ExecutionState;
import execution.state.GuardedGeneratorState;
import grammar.alkParser;
import execution.types.AlkValue;
import parser.env.LocationMapper;
import parser.exceptions.AlkException;
import parser.visitors.expression.ExpressionVisitor;
import util.CtxState;
import util.Payload;
import util.SplitMapper;

@CtxState(ctxClass = alkParser.InExpressionContext.class)
public class InExpressionState extends GuardedGeneratorState<AlkValue> {

    private alkParser.InExpressionContext ctx;

    public InExpressionState(alkParser.InExpressionContext tree, Payload payload) {
        super(tree, payload, tree.equality_expression(), ExpressionVisitor.class);
        this.ctx = tree;
    }

    @Override
    protected AlkValue interpretResult(AlkValue current, AlkValue next) {

        if (current == null || next == null)
        {
            throw new AlkException(ctx.start.getLine(), "Undefined variable used for in expression.");
        }

        try
        {
            return current.in(next);
        }
        catch (AlkException e)
        {
            super.handle(e);
        }
        return null;
    }

    @Override
    public ExecutionState clone(SplitMapper sm) {
        InExpressionState copy = new InExpressionState((alkParser.InExpressionContext) tree, sm.getPayload());
        return super.decorate(copy, sm);
    }

}
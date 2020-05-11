package execution.state.statement;

import execution.state.ExecutionState;
import execution.state.IndependentSingleState;
import grammar.alkParser;
import parser.visitors.StmtVisitor;
import util.CtxState;
import util.Payload;
import util.SplitMapper;

@CtxState(ctxClass = alkParser.ToForAllContext.class)
public class ToForAllState extends IndependentSingleState
{
    public ToForAllState(alkParser.ToForAllContext tree, Payload payload)
    {
        super(tree, payload, tree.forall_struct(), StmtVisitor.class);
    }

    @Override
    public ExecutionState clone(SplitMapper sm) {
        ToForAllState copy = new ToForAllState((alkParser.ToForAllContext) tree, sm.getPayload());
        return super.decorate(copy, sm);
    }
}

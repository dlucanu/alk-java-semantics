package execution.state.statement;

import execution.state.ExecutionState;
import execution.state.IndependentSingleState;
import grammar.alkParser;
import parser.types.AlkValue;
import parser.visitors.StmtVisitor;
import util.CtxState;
import util.Payload;

@CtxState(ctxClass = alkParser.ToAssignmentStmtContext.class)
public class ToAssignmentStmtState extends IndependentSingleState
{
    public ToAssignmentStmtState(alkParser.ToAssignmentStmtContext tree, Payload payload)
    {
        super(tree, payload, tree.assignment(), StmtVisitor.class);
    }

    @Override
    public ExecutionState clone(Payload payload) {
        ToAssignmentStmtState copy = new ToAssignmentStmtState((alkParser.ToAssignmentStmtContext) tree, payload);
        return super.decorate(copy);
    }

}

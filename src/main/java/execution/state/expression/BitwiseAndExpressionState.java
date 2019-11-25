package execution.state.expression;

import execution.ExecutionResult;
import execution.state.GuardedGeneratorState;
import grammar.alkParser;
import parser.types.AlkValue;
import parser.visitors.expression.ExpressionVisitor;

public class BitwiseAndExpressionState extends GuardedGeneratorState<AlkValue> {
    public BitwiseAndExpressionState(alkParser.BitwiseAndExpressionContext tree, ExpressionVisitor visitor) {
        super(tree, visitor, tree.shift_expression());
    }

    @Override
    protected AlkValue interpretResult(AlkValue current, AlkValue next) {
        return current.bitwiseAnd(next);
    }
}
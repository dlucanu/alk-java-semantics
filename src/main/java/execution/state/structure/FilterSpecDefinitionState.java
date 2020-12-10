package execution.state.structure;

import execution.ExecutionResult;
import execution.state.ExecutionState;
import grammar.alkParser;
import execution.parser.env.EnvironmentProxy;
import execution.parser.env.Location;
import execution.parser.exceptions.AlkException;
import execution.types.AlkIterableValue;
import execution.types.AlkValue;
import execution.types.alkArray.AlkArray;
import execution.types.alkBool.AlkBool;
import execution.parser.visitors.expression.ExpressionVisitor;
import ast.CtxState;
import execution.ExecutionPayload;
import execution.exhaustive.SplitMapper;
import util.types.Value;

import java.util.List;

import static execution.parser.exceptions.AlkException.ERR_SPEC_BOOL;
import static execution.parser.exceptions.AlkException.ERR_SPEC_ITERABLE_REQUIRED;

@CtxState(ctxClass = alkParser.FilterSpecDefinitionContext.class)
public class FilterSpecDefinitionState extends ExecutionState<AlkValue, Value> {

    private List<Location> source;
    private int step;
    private alkParser.FilterSpecDefinitionContext ctx;
    private AlkArray array = new AlkArray();
    private AlkValue validatingValue;
    private EnvironmentProxy env;

    public FilterSpecDefinitionState(alkParser.FilterSpecDefinitionContext tree, ExecutionPayload executionPayload) {
        super(tree, executionPayload);
        ctx = tree;
    }

    @Override
    public ExecutionState makeStep()
    {
        if (source == null)
            return super.request(ExpressionVisitor.class, ctx.expression(0));

        if (step == source.size())
        {
            setResult(new ExecutionResult<>(array));
            return null;
        }

        validatingValue = (AlkValue) source.get(step++).toRValue();

        env = new EnvironmentProxy(getEnv());
        env.addTempEntry(ctx.ID().toString(), validatingValue);
        return super.request(ExpressionVisitor.class, ctx.expression(1), env);
    }

    @Override
    public void assign(ExecutionResult executionResult) {
        if (source == null)
        {
            AlkValue resultVal = (AlkValue) executionResult.getValue().toRValue();

            if (!(resultVal instanceof AlkIterableValue))
                super.handle(new AlkException(ERR_SPEC_ITERABLE_REQUIRED));

            source = ((AlkIterableValue) resultVal).toArray(generator);
            step = 0;
        }
        else
        {
            if (!(executionResult.getValue().toRValue() instanceof AlkBool))
                super.handle(new AlkException(ERR_SPEC_BOOL));
            if (((AlkBool) executionResult.getValue().toRValue()).isTrue())
                array.push(generator.generate(validatingValue));
        }
    }

    @Override
    public ExecutionState clone(SplitMapper sm) {
        FilterSpecDefinitionState copy = new FilterSpecDefinitionState((alkParser.FilterSpecDefinitionContext) tree, sm.getExecutionPayload());
        copy.step = step;

        if (source != null)
        {
            for (Location value : source)
            {
                copy.source.add(sm.getLocationMapper().get(value));
            }
        }


        if (array != null)
        {
            copy.array = array.weakClone(sm.getLocationMapper());
        }

        if (this.validatingValue != null)
        {
            copy.validatingValue = this.validatingValue.weakClone(sm.getLocationMapper());
        }

        if (this.env != null)
        {
            copy.env = (EnvironmentProxy) sm.getEnvironmentMapper().get(this.env);
        }
        return super.decorate(copy, sm);
    }
}
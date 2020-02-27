package execution.state.structure;

import execution.state.ExecutionState;
import execution.state.SingleState;
import org.antlr.v4.runtime.tree.ParseTree;
import execution.types.AlkIterableValue;
import execution.types.AlkValue;
import execution.types.alkInt.AlkInt;
import parser.visitors.structure.DataStructureVisitor;
import util.Payload;
import util.exception.InternalException;
import util.types.IntervalValue;

import java.util.ArrayList;
import java.util.List;

public class IterableWithIntervalState extends SingleState<AlkIterableValue, IntervalValue>
{
    private Class<? extends AlkIterableValue> clazz;

    public IterableWithIntervalState(ParseTree tree,
                                     Payload payload,
                                     ParseTree interval,
                                     Class<? extends AlkIterableValue> clazz)
    {
        super(tree, payload, interval, DataStructureVisitor.class);
        this.clazz = clazz;
    }

    @Override
    protected AlkIterableValue interpretResult(IntervalValue value)
    {
        try {
            AlkIterableValue iterableValue = clazz.newInstance();
            List<AlkValue> array = new ArrayList<>();

            for (AlkInt val : value)
                array.add(val);

            for (AlkValue val : array)
            {
                iterableValue.push(generator.generate(val.toRValue().clone(generator)));
            }
            return iterableValue;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public ExecutionState clone(Payload payload) {
        IterableWithIntervalState copy = new IterableWithIntervalState(tree, payload, null, clazz);

        return super.decorate(copy);
    }
}

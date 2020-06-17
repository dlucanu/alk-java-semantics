package util;

import execution.state.ExecutionState;
import parser.env.Environment;
import parser.env.EnvironmentImpl;
import parser.env.LocationMapper;
import parser.env.Store;
import util.exception.InternalException;
import util.types.Value;

import java.util.*;


// Worker responsible for environment management
public class EnvironmentManager {

    private Map<ExecutionState, Environment> state2env = new HashMap<>();
    private Map<Environment, List<ExecutionState> > env2state = new HashMap<>();

    public void unlink(ExecutionState<? extends Value,? extends Value> state)
    {
        /*if (!state2env.containsKey(state))
            throw new InternalException("Unsync in the envManager! Can't find state which should be deleted");*/
        state2env.remove(state);
    }

    public void link(ExecutionState state, Environment env)
    {
        if (env == null)
            throw new InternalException("Can't link a state to a null environment.");

        state2env.put(state, env);

        if (env2state.get(env) == null)
        {
            List<ExecutionState> list = new LinkedList<>();
            env2state.put(env, list);
        }

        env2state.get(env).add(state);
    }

    public Environment getEnv(ExecutionState state) {
        if (!state2env.containsKey(state))
            throw new InternalException("The environment manager is broken. No environment could be found for a state");

        return state2env.get(state);
    }

    public EnvironmentMapper cloneEnvironments(LocationMapper locMapping, Store store) {
        EnvironmentMapper mapping = new EnvironmentMapper();
        for (Environment env : env2state.keySet())
        {
            Environment copy = env.makeClone(locMapping, store);
            mapping.put(env, copy);
        }
        return mapping;
    }

    public EnvironmentManager makeClone(StateMapper stateMapping,
                                        EnvironmentMapper envMapping) {

        EnvironmentManager clone = new EnvironmentManager();

        for (Map.Entry<ExecutionState, Environment> entry : state2env.entrySet())
        {
            ExecutionState key = stateMapping.get(entry.getKey());

            Environment value = envMapping.get(entry.getValue());

            clone.state2env.put(key, value);
        }

        for (Map.Entry<Environment, List<ExecutionState> > entry : env2state.entrySet())
        {
            Environment key = envMapping.get(entry.getKey());

            List<ExecutionState> value = entry.getValue();
            List<ExecutionState> copyValue = new LinkedList<>();

            for (ExecutionState state : value)
            {
                ExecutionState copyState = stateMapping.get(state);
                copyValue.add(copyState);
            }

            clone.env2state.put(key, copyValue);
        }

        return clone;

    }
}

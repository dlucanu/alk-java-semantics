package symbolic;

import ast.AST;
import ast.ASTStack;
import execution.*;
import execution.interpreter.BaseStatefulExpressionInterpreter;
import execution.interpreter.BaseStatefulStmtInterpreter;
import execution.interpreter.SymbolicStatefulExpressionInterpreter;
import execution.parser.AlkParser;
import execution.parser.env.Environment;
import execution.parser.env.EnvironmentImpl;
import execution.parser.env.Store;
import execution.parser.env.StoreImpl;
import execution.parser.exceptions.AlkException;
import execution.state.ExecutionState;
import grammar.alkParser;
import main.Master;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.ParseTreeExprVisitor;
import state.Result;
import symbolic.SymbolicValueIface;
import util.Configuration;
import visitor.SmallStepExpressionVisitor;
import visitor.SymbolicExpressionInterpreter;
import visitor.interpreter.SmallStepExpressionInterpreter;

import java.io.File;

public class SymbolicExpressionEvaluator
implements Master
{
    Configuration config;
    public SymbolicExpressionEvaluator(Configuration config)
    {
        this.config = config;
        StoreImpl store = new StoreImpl();
    }

    public void run()
    {
        File file = config.getAlkFile();
        ParseTree parseTreeRoot = AlkParser.execute(file, true);
        if (!(parseTreeRoot instanceof alkParser.ExpressionContext))
        {
            throw new AlkException("Invalid expression!");
        }

        AST root = new ParseTreeExprVisitor().visit(parseTreeRoot);

        BaseStatefulInterpreterManager<ExecutionPayload, ExecutionResult, ExecutionState> manager =
                new BaseStatefulInterpreterManager<>(new SymbolicStatefulExpressionInterpreter(), new BaseStatefulStmtInterpreter());


        Execution exec = new Execution(config);
        ExecutionPayload payload = new ExecutionPayload(exec, exec.getGlobal());
        ExecutionState state = manager.interpret(root, payload);
        ASTStack<ExecutionState> stack = new ASTStack<>(config);
        stack.push(state);
        Result<?> result = stack.run();
        System.out.println(result.getValue().toString());
    }

}
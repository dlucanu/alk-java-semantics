package execution;

import execution.state.ExecutionState;
import parser.AlkParser;
import parser.env.Environment;
import util.EnvironmentManager;
import util.exception.InternalException;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import util.Configuration;
import util.ErrorManager;
import util.OptionProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The main class responsible for one alk file execution. It is implemented
 * under a thread paradigm to parallelize multiple file interpreting if needed.
 * It also wraps the data necessary for one execution only.
 */
public class Execution extends Thread
{

    /** The main configuration delivery instance.*/
    private Configuration config;

    private ExecutionStack stack;

    private EnvironmentManager envManager;

    /* The global environment initially empty */
    private Environment global;

    /**
     * Constructor with specific configuration
     *
     * @param config
     * The configuration meant to be used for this execution
     */
    public Execution(Configuration config) {
        this.config = config;
        envManager = new EnvironmentManager();
        global = new Environment();
    }



    /**
     * Constructor with specific configuration
     *
     * @param config
     * The configuration meant to be used for this execution
     */
    private Execution(Configuration config, ExecutionStack stack, EnvironmentManager envManager, Environment global) {
        this.config = config;
        this.stack = stack;
        this.envManager = envManager;
        this.global = global;
    }

    /**
     * The main method which fires the parsing
     *
     * @throws InternalException
     * An AlkException provided by the execution, or a wrapped non-runtime exception
     */
    private void execute() throws InternalException
    {
        ErrorManager em = config.getErrorManager();

        // get the main algorithm file and obtain the CharStream in order to be parsed
        File file = config.getAlkFile();
        CharStream alkFile = null;
        try
        {
            InputStream alkInStr = new FileInputStream(file);
            alkFile = CharStreams.fromStream(alkInStr);
        }
        catch (IOException e)
        {
            em.handleError(e, file.getPath());
        }

        /*
            execute preprocessing part
            TODO: rework preprocessing, move it to the parsing stage
         */
        /*PreProcessing pre = null;
        try
        {
            pre = new PreProcessing(file, new ArrayList<>());
        }
        catch (AlkException ex)
        {
            em.handleError(ex);
        }
        pre.execute(e, true);*/

        /*
            start parsing
            TODO: call different constructor without predefined environment
         */
        AlkParser parser = new AlkParser(alkFile, global, this);
        if (stack == null)
        {
            ExecutionState state = parser.execute(config);
            stack = new ExecutionStack(config);
            stack.push(state);
        }
        stack.run();

        // debugging
        // System.out.println("Result: " + stack.getResult());

        /* AlkParser parser = new AlkParser(alkFile, e);
        parser.execute(config); */



        // if the metadata flag is set, print the global environment
        if (config.hasMetadata())
        {
            config.getIOManager().write(parser.getGlobalEnvironment().toString());
        }
    }

    /**
     * The entry point for the thread
     */
    @Override
    public void run()
    {
        try
        {
            execute();
        }
        catch (InternalException e)
        {
            OptionProvider op = config;
            if (op.hasDebugMode())
            {
                e.printStackTrace();
            }
        }
    }

    public EnvironmentManager getEnvManager() {
        return envManager;
    }

    public Configuration getConfiguration() {
        return config;
    }

    public Execution clone(boolean nullifyLast)
    {
        Execution copy = new Execution(config.clone(), stack.clone(this), envManager.clone(), global.clone());
        if (nullifyLast)
        {
            copy.stack.nullifyLast();
        }
        return copy;
    }
}

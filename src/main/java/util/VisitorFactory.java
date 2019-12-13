package util;

import grammar.alkBaseVisitor;
import parser.env.Environment;

import java.lang.reflect.InvocationTargetException;

public class VisitorFactory {

    public static alkBaseVisitor create(Class<? extends alkBaseVisitor> clazz, Environment env, Payload payload)
    {
        // TODO: Refine exception handling
        try {
            return clazz.getDeclaredConstructor(Environment.class, Payload.class).newInstance(env, payload);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            e.printStackTrace();
        }

        return null;
    }

}
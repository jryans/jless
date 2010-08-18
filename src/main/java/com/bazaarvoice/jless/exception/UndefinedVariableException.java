package com.bazaarvoice.jless.exception;

import org.parboiled.errors.ActionException;

/**
 *
 */
public class UndefinedVariableException extends ActionException {

    public UndefinedVariableException(String name) {
        super("The variable " + name + " has not been defined.");
    }
}

package com.bazaarvoice.jless.exception;

import org.parboiled.errors.ActionException;

/**
 *
 */
public class IllegalMixinArgumentException extends ActionException {

    public IllegalMixinArgumentException(String name, int parameterCount) {
        super("The mixin " + name + " only accepts up to " + parameterCount + " argument(s).");
    }
}

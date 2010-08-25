package com.bazaarvoice.jless.exception;

import org.parboiled.errors.ActionException;

public class UndefinedMixinException extends ActionException {

    public UndefinedMixinException(String name) {
        super("The mixin " + name + " has not been defined.");
    }
}

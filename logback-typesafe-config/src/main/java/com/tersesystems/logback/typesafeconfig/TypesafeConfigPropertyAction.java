/*
 *
 *  * SPDX-License-Identifier: CC0-1.0
 *  *
 *  * Copyright 2018-2019 Will Sargent.
 *  *
 *  * Licensed under the CC0 Public Domain Dedication;
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://creativecommons.org/publicdomain/zero/1.0/
 *
 */

package com.tersesystems.logback.typesafeconfig;

import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import org.xml.sax.Attributes;

/**
 * Lets you put objects into the context's object map, as the correct type,
 * using typesafe config paths as the source.
 */
public class TypesafeConfigPropertyAction extends Action {

    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {

    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {

    }
}

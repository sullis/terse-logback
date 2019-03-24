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
import ch.qos.logback.core.joran.spi.InterpretationContext;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigValue;
import org.xml.sax.Attributes;

/**
 * Lets you put objects into the context's object map, as the correct type,
 * using typesafe config paths as the source.
 */
public class TypesafeConfigObjectMapAction extends Action {
    /**
     * Set a new property for the execution context by name, value pair, or adds
     * all the properties found in the given file.
     *
     */
    public void begin(InterpretationContext ec, String localName, Attributes attributes) {
        String name = attributes.getValue(NAME_ATTRIBUTE);
        String path = attributes.getValue("path");

        if (isValid()) {
            Config config = (Config) context.getObject(ConfigConstants.TYPESAFE_CONFIG_CTX_KEY);
            if (config == null) {
                addError("No config object found in context's object map!");
                return;
            }

            ConfigValue configValue = null;
            Object contextValue = null;
            try {
                configValue = config.getValue(path);
                if (configValue != null) {
                    contextValue = configValue.unwrapped();
                }
                context.putObject(name, contextValue);
            } catch (ConfigException e) {
                addError(String.format("Cannot set value %s typesafe config path %s to name %s", contextValue, path, name), e);
            }
        } else {
            addError("Cannot set property, it is invalid!");
        }
    }

    boolean isValid() {
        return true;
    }

    public void end(InterpretationContext ec, String name) {
    }

    public void finish(InterpretationContext ec) {
    }
}

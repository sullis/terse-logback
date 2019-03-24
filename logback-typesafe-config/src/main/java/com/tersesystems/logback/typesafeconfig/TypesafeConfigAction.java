/*
 * SPDX-License-Identifier: CC0-1.0
 *
 * Copyright 2018-2019 Will Sargent.
 *
 * Licensed under the CC0 Public Domain Dedication;
 * You may obtain a copy of the License at
 *
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */
package com.tersesystems.logback.typesafeconfig;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.joran.action.Action;
import ch.qos.logback.core.joran.spi.ActionException;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import com.typesafe.config.*;
import org.xml.sax.Attributes;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static com.tersesystems.logback.typesafeconfig.ConfigConstants.*;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Configures properties from a typesafe config object, and places them in local scope, or in the context
 * if "scope"="context" attribute is set.
 */
public class TypesafeConfigAction extends Action {

    protected String scope = LOCAL_SCOPE;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public void begin(InterpretationContext ic, String name, Attributes attributes) throws ActionException {
        String scope = attributes.getValue(SCOPE_ATTRIBUTE);
        if (scope != null) {
            setScope(scope);
        }
    }

    private Map<String, String> levelsToMap(ConfigObject levels) {
        return levels.keySet().stream().collect(
            toMap(k -> levels.get(k).unwrapped().toString(), identity())
        );
    }

    @Override
    public void end(InterpretationContext ic, String name) throws ActionException {
        Config config = generateConfig(ic.getClass().getClassLoader());
        Context context = ic.getContext();

        // Try to set up the levels as they're important...
        try {
            context.putObject(TYPESAFE_CONFIG_CTX_KEY, config);
        } catch (ConfigException e) {
            addWarn("Cannot set levels in context!", e);
        }

        // Try to set up the levels as they're important...
        try {
            Map<String, String> levelsMap = levelsToMap(config.getObject(LEVELS_KEY));
            context.putObject(LEVELS_KEY, levelsMap);
        } catch (ConfigException e) {
            addWarn("Cannot set levels in context!", e);
        }

        Set<Map.Entry<String, ConfigValue>> properties = config.getConfig(PROPERTIES_KEY).entrySet();
        if (isContextScope()) {
            configureContextScope(config, context, properties);
        } else {
            configureLocalScope(config, ic, properties);
        }
    }

    public Object findMappedValue(Config config, String mapping) {
        try {
            ConfigObject configObject = config.getObject(mapping);
            if (configObject == null) {
                addWarn("A config object exists for " + mapping + " but is null!");
                return null;
            }
            return configObject.unwrapped();
        } catch (ConfigException e) {
            addError("Cannot find mapped value using " + mapping, e);
            return null;
        }
    }

    private boolean isContextScope() {
       return CONTEXT_SCOPE.equalsIgnoreCase(scope);
    }

    public void configureContextScope(Config config, Context lc, Set<Map.Entry<String, ConfigValue>> properties) {
        addInfo("Configuring with context scope");
        lc.putObject(TYPESAFE_CONFIG_CTX_KEY, config);
        for (Map.Entry<String, ConfigValue> propertyEntry : properties) {
            String key = propertyEntry.getKey();
            String value = propertyEntry.getValue().unwrapped().toString();
            lc.putProperty(key, value);
        }
    }

    public void configureLocalScope(Config config, InterpretationContext ic,  Set<Map.Entry<String, ConfigValue>> properties) {
        addInfo("Configuring with local scope");
        ic.getObjectMap().put(TYPESAFE_CONFIG_CTX_KEY, config);

        for (Map.Entry<String, ConfigValue> propertyEntry : properties) {
            String key = propertyEntry.getKey();
            String value = propertyEntry.getValue().unwrapped().toString();
            ic.addSubstitutionProperty(key, value);
        }
    }

    public Config generateConfig(ClassLoader classLoader) {
        // Look for logback.json, logback.conf, logback.properties
        Config systemProperties = ConfigFactory.systemProperties();
        String fileName = System.getProperty(CONFIG_FILE_PROPERTY);
        Config file = ConfigFactory.empty();
        if (fileName != null) {
            file = ConfigFactory.parseFile(new File(fileName));
        }

        Config testResources = ConfigFactory.parseResourcesAnySyntax(classLoader, LOGBACK_TEST);
        Config resources = ConfigFactory.parseResourcesAnySyntax(classLoader, LOGBACK);
        Config reference = ConfigFactory.parseResources(classLoader, LOGBACK_REFERENCE_CONF);

        Config config = systemProperties        // Look for a property from system properties first...
                .withFallback(file)          // if we don't find it, then look in an explicitly defined file...
                .withFallback(testResources) // if not, then if logback-test.conf exists, look for it there...
                .withFallback(resources)     // then look in logback.conf...
                .withFallback(reference)     // and then finally in logback-reference.conf.
                .resolve();                  // Tell config that we want to use ${?ENV_VAR} type stuff.

        // Add a check to show the config value if nothing is working...
        if (Boolean.getBoolean(LOGBACK_DEBUG_PROPERTY)) {
            String configString = config.root().render(ConfigRenderOptions.defaults());
            addInfo(configString);
        }
        return config;
    }

}

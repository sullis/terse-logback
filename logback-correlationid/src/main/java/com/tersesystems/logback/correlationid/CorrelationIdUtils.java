/*
 * SPDX-License-Identifier: CC0-1.0
 *
 * Copyright 2018-2020 Will Sargent.
 *
 * Licensed under the CC0 Public Domain Dedication;
 * You may obtain a copy of the License at
 *
 *  http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.tersesystems.logback.correlationid;

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.spi.MDCAdapter;

public class CorrelationIdUtils {

  private final String mdcKey;

  public CorrelationIdUtils(String mdcKey) {
    this.mdcKey = mdcKey;
  }

  public Optional<String> get(Map<String, String> mdcPropertyMap, Marker marker) {
    Optional<String> first = fromMarker(marker);
    if (first.isPresent()) {
      return first;
    } else {
      return get(mdcPropertyMap);
    }
  }

  /** Pulls the correlation id from a marker which is a CorrelationIdProvider. */
  public Optional<String> fromMarker(Marker m) {
    if (m instanceof CorrelationIdProvider) {
      CorrelationIdProvider provider = ((CorrelationIdProvider) m);
      return Optional.of(provider.getCorrelationId());
    }
    if (m != null && m.hasReferences()) {
      for (Iterator<Marker> iter = m.iterator(); iter.hasNext(); ) {
        Marker child = iter.next();
        if (child instanceof CorrelationIdProvider) {
          CorrelationIdProvider provider = ((CorrelationIdProvider) child);
          return Optional.of(provider.getCorrelationId());
        }
        if (child.hasReferences()) {
          return fromMarker(child);
        }
      }
    }
    return Optional.empty();
  }

  public Optional<String> get(Map<String, String> mdcPropertyMap) {
    // Look in MDC for a correlation id as well...
    if (mdcKey != null) {
      String s = mdcPropertyMap.get(mdcKey);
      if (s != null) {
        return Optional.of(s);
      }
    }
    return Optional.empty();
  }

  public Map<String, String> getMDCPropertyMap() {
    MDCAdapter mdc = MDC.getMDCAdapter();

    Map<String, String> mdcPropertyMap;
    if (mdc instanceof LogbackMDCAdapter)
      mdcPropertyMap = ((LogbackMDCAdapter) mdc).getPropertyMap();
    else mdcPropertyMap = mdc.getCopyOfContextMap();

    // mdcPropertyMap still null, use emptyMap()
    if (mdcPropertyMap == null) mdcPropertyMap = Collections.emptyMap();

    return mdcPropertyMap;
  }
}

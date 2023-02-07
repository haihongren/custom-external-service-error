package com.nr.agent.instrumentation.httpclient40;
/*
 *
 *  * Copyright 2020 New Relic Corporation. All rights reserved.
 *  * SPDX-License-Identifier: Apache-2.0
 *
 */



import org.apache.http.HttpRequest;

import com.newrelic.api.agent.HeaderType;
import com.newrelic.api.agent.OutboundHeaders;

public class OutboundWrapper implements OutboundHeaders {

    private final HttpRequest delegate;

    public OutboundWrapper(HttpRequest request) {
        this.delegate = request;
    }

    @Override
    public void setHeader(String name, String value) {
        delegate.setHeader(name, value);
    }

    @Override
    public HeaderType getHeaderType() {
        return HeaderType.HTTP;
    }
}

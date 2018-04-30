package com.consort.security;

import org.pac4j.core.authorization.authorizer.RequireAnyAttributeAuthorizer;

public class JenkinsAdapterAttributeAuthorizer extends RequireAnyAttributeAuthorizer {

    public JenkinsAdapterAttributeAuthorizer(final String attribute, final String valueToMatch) {
        super(valueToMatch);
        setElements(attribute);
    }
}

package com.prodyna.pac.voting.web.rest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

/**
 * Utility class for HTTP headers creation.
 *
 */
public class HeaderUtil
{
    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);

    private static final String VOTING_APP = "VotingApp.";

    public static HttpHeaders createAlert(final String message, final String param)
    {
        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-VotingApp-alert", message);
        headers.add("X-VotingApp-params", param);
        return headers;
    }

    public static HttpHeaders createEntityCreationAlert(final String entityName, final String param)
    {
        return createAlert(VOTING_APP + entityName + ".created", param);
    }

    public static HttpHeaders createEntityUpdateAlert(final String entityName, final String param)
    {
        return createAlert(VOTING_APP + entityName + ".updated", param);
    }

    public static HttpHeaders createEntityDeletionAlert(final String entityName, final String param)
    {
        return createAlert(VOTING_APP + entityName + ".deleted", param);
    }

    public static HttpHeaders createFailureAlert(final String entityName, final String errorKey, final String defaultMessage)
    {
        log.error("Entity creation failed, {}", defaultMessage);

        final HttpHeaders headers = new HttpHeaders();
        headers.add("X-VotingApp-error", "error." + errorKey);
        headers.add("X-VotingApp-params", entityName);
        headers.add("X-VotingApp-msg", defaultMessage);
        return headers;
    }
}

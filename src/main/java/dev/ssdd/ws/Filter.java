package dev.ssdd.ws;

/**
 * Created by Per Wendel on 2014-05-10.
 */
@FunctionalInterface
public interface Filter {

    /**
     * Invoked when a request is made on this filter's corresponding path e.g. '/hello'
     *
     * @param request  The request object providing information about the HTTP request
     * @param response The response object providing functionality for modifying the response
     * @throws Exception when handle fails
     */
    void handle(Request request, Response response) throws Exception;

}

/*
 * Copyright 2016 - Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ssdd.ws.matching;

import dev.ssdd.ws.FilterImpl;
import dev.ssdd.ws.Request;
import dev.ssdd.ws.RequestResponseFactory;
import dev.ssdd.ws.route.HttpMethod;
import dev.ssdd.ws.routematch.RouteMatch;

import java.util.List;

/**
 * Executes the Before filters matching an HTTP request.
 */
final class BeforeFilters {

    static void execute(RouteContext context) throws Exception {
        Object content = context.body().get();

        List<RouteMatch> matchSet = context.routeMatcher().findMultiple(HttpMethod.before, context.uri(), context.acceptType());

        for (RouteMatch filterMatch : matchSet) {
            Object filterTarget = filterMatch.getTarget();

            if (filterTarget instanceof FilterImpl) {
                Request request = RequestResponseFactory.create(filterMatch, context.httpRequest());

                FilterImpl filter = (FilterImpl) filterTarget;

                context.requestWrapper().setDelegate(request);
                context.responseWrapper().setDelegate(context.response());

                filter.handle(context.requestWrapper(), context.responseWrapper());

                String bodyAfterFilter = context.response().body();

                if (bodyAfterFilter != null) {
                    content = bodyAfterFilter;
                }
            }
        }

        context.body().set(content);
    }

}
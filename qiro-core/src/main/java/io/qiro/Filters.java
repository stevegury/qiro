package io.qiro;

import org.reactivestreams.Publisher;

import static io.qiro.util.Publishers.map;

public class Filters {

    public static <FilterRequest, Request, Response>
    Filter<FilterRequest, Request, Response, Response> fromInputFunction(
        ThrowableFunction<FilterRequest, Request> fnIn
    ) {
        return fromFunction(fnIn, x -> x);
    }

    public static <Request, Response, FilterResponse>
    Filter<Request, Request, Response, FilterResponse> fromOutputFunction(
        ThrowableFunction<Response, FilterResponse> fnOut
    ) {
        return fromFunction(x -> x, fnOut);
    }

    public static <FilterReq, Req, Resp, FilterResp>
    Filter<FilterReq, Req, Resp, FilterResp> fromFunction(
        ThrowableFunction<FilterReq, Req> fnIn,
        ThrowableFunction<Resp, FilterResp> fnOut
    ) {
        return new Filter<FilterReq, Req, Resp, FilterResp>() {
            @Override
            public Publisher<FilterResp> requestSubscription(FilterReq input, Service<Req, Resp> service) {
                return s -> {
                    try {
                        fnIn.apply(input);
                        s.onComplete();
                    } catch (Throwable throwable) {
                        s.onError(throwable);
                    }
                };
            }

            @Override
            public Publisher<FilterResp> requestChannel(
                Publisher<FilterReq> inputs,
                Service<Req, Resp> service
            ) {
                Publisher<Req> requests = map(inputs, fnIn);
                Publisher<Resp> outputs = service.requestChannel(requests);
                return map(outputs, fnOut);
            }
        };
    }
}

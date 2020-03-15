package com.felfel.basket.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface BasketService extends Service {

    ServiceCall<NotUsed, BasketResponse> get(String uuid);

    ServiceCall<BasketItem, Done> addItem(String uuid);

    @Override
    default Descriptor descriptor() {
        return named("basket")
                .withCalls(
                        restCall(Method.GET, "/api/basket/:id", this::get),
                        restCall(Method.PUT, "/api/basket/:id", this::addItem)
                )
                .withAutoAcl(true);
    }
}

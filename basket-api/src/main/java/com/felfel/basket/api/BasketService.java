package com.felfel.basket.api;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

public interface BasketService extends Service {
    /**
     * Get a basket.
     * <p>
     * Example: curl http://localhost:9000/api/basket/c78383b8-208d-4a3b-a709-1cbc463dd541
     */
    ServiceCall<NotUsed, BasketResponse> get(String uuid);

    /**
     * Add a new item to user basket.
     * <p>
     * Example: curl -H "Content-Type: application/json" -X PUT -d '{"uuid": "c9f3c98b-e680-4090-bfac-c60aca3d1db7", "quantity": 2, "price": 10}' http://localhost:9000/api/basket/c78383b8-208d-4a3b-a709-1cbc463dd541
     */
    ServiceCall<BasketItem, BasketResponse> addItem(String uuid);

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

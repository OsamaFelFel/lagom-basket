package com.felfel.basket.impl;

import akka.NotUsed;
import akka.cluster.sharding.typed.javadsl.ClusterSharding;
import akka.cluster.sharding.typed.javadsl.Entity;
import com.felfel.basket.api.BasketItem;
import com.felfel.basket.api.BasketResponse;
import com.felfel.basket.api.BasketService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Duration;

@Slf4j
public class BasketServiceImpl implements BasketService {
    private final Duration askTimeout = Duration.ofSeconds(10);
    private final ClusterSharding clusterSharding;

    @Inject
    public BasketServiceImpl(ClusterSharding clusterSharing) {
        this.clusterSharding = clusterSharing;
        this.clusterSharding.init(Entity.of(BasketAggregate.ENTITY_TYPE_KEY, BasketAggregate::create));
    }

    @Override
    public ServiceCall<NotUsed, BasketResponse> get(String uuid) {
        log.info("Received a request to get basket [{}]", uuid);
        return request ->
                clusterSharding.entityRefFor(BasketAggregate.ENTITY_TYPE_KEY, uuid)
                        .ask(BasketCommand.GetBasket::new, askTimeout)
                        .thenApply(basket -> constructBasketResponseFromBasketReply(uuid, basket));
    }

    @Override
    public ServiceCall<BasketItem, BasketResponse> addItem(String uuid) {
        log.info("Received a request to add an item to basket [{}]", uuid);
        return item ->
                clusterSharding.entityRefFor(BasketAggregate.ENTITY_TYPE_KEY, uuid)
                        .<BasketCommand.Confirmation>ask(replyTo ->
                                new BasketCommand.AddItem(item.getUuid(), item.getQuantity(), item.getPrice(), replyTo), askTimeout)
                        .thenApply(this::handleConfirmation)
                        .thenApply(accepted -> constructBasketResponseFromBasketReply(uuid, accepted.getBasket()));
    }

    private BasketCommand.Accepted handleConfirmation(BasketCommand.Confirmation confirmation) {
        if (confirmation instanceof BasketCommand.Accepted) {
            return (BasketCommand.Accepted) confirmation;
        }

        BasketCommand.Rejected rejected = (BasketCommand.Rejected) confirmation;
        throw new BadRequest(rejected.getReason());
    }

    private BasketResponse constructBasketResponseFromBasketReply(String id, BasketCommand.Basket basket) {
        return new BasketResponse(id, basket.getUserUuid(), basket.getItems(), basket.getSubTotal(), basket.getTax(), basket.getTotal());
    }
}

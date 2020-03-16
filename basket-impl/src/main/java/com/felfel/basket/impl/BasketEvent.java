package com.felfel.basket.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

public interface BasketEvent extends Jsonable, AggregateEvent<BasketEvent> {
    int NUM_OF_SHARDS = 1;
    AggregateEventShards<BasketEvent> TAG = AggregateEventTag.sharded(BasketEvent.class, NUM_OF_SHARDS);

    @Override
    default AggregateEventTagger<BasketEvent> aggregateTag() {
        return TAG;
    }

    @Value
    @JsonDeserialize
    class ItemAdded implements BasketEvent {
        String basketUuid;
        String itemUuid;
        Integer itemQuantity;
        BigDecimal itemPrice;
        BigDecimal basketSubTotal;
        BigDecimal basketTax;
        BigDecimal basketTotal;
        Instant eventTime;

        @JsonCreator
        ItemAdded(String basketUuid, String itemUuid, Integer itemQuantity, BigDecimal itemPrice, BigDecimal basketSubTotal, BigDecimal basketTax, BigDecimal basketTotal, Instant eventTime) {
            this.basketUuid = Preconditions.checkNotNull(basketUuid, "basketUuid");
            this.itemUuid = Preconditions.checkNotNull(itemUuid, "itemUuid");
            this.itemQuantity = Preconditions.checkNotNull(itemQuantity, "quantity");
            this.itemPrice = Preconditions.checkNotNull(itemPrice, "price");
            this.basketSubTotal = Preconditions.checkNotNull(basketSubTotal, "subTotal");
            this.basketTax = Preconditions.checkNotNull(basketTax, "tax");
            this.basketTotal = Preconditions.checkNotNull(basketTotal, "total");
            this.eventTime = eventTime;
        }
    }
}

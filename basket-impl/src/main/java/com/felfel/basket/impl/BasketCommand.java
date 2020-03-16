package com.felfel.basket.impl;

import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.felfel.basket.api.BasketItem;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;
import org.pcollections.PVector;

import java.math.BigDecimal;

public interface BasketCommand extends Jsonable {

    @Value
    class GetBasket implements BasketCommand {
        ActorRef<Basket> replyTo;

        @JsonCreator
        GetBasket(ActorRef<Basket> replyTo) {
            this.replyTo = replyTo;
        }
    }

    @Value
    @JsonDeserialize
    class AddItem implements BasketCommand {
        String itemUuid;
        Integer quantity;
        BigDecimal price;
        ActorRef<Confirmation> replyTo;

        @JsonCreator
        AddItem(String itemUuid, Integer quantity, BigDecimal price, ActorRef<Confirmation> replyTo) {
            this.itemUuid = Preconditions.checkNotNull(itemUuid, "itemUuid");
            this.quantity = quantity;
            this.price = price;
            this.replyTo = replyTo;
        }
    }

    interface Reply extends Jsonable {
    }

    interface Confirmation extends Reply {
    }

    @Value
    @JsonDeserialize
    class Basket implements Reply {
        String userUuid;
        PVector<BasketItem> items;
        BigDecimal subTotal;
        BigDecimal tax;
        BigDecimal total;

        @JsonCreator
        Basket(String userUuid, PVector<BasketItem> items, BigDecimal subTotal, BigDecimal tax, BigDecimal total) {
            this.userUuid = userUuid;
            this.items = items;
            this.subTotal = subTotal;
            this.tax = tax;
            this.total = total;
        }
    }

    @Value
    @JsonDeserialize
    class Accepted implements Confirmation {
        Basket basket;

        @JsonCreator
        Accepted(Basket basket) {
            this.basket = basket;
        }
    }

    @Value
    @JsonDeserialize
    class Rejected implements Confirmation {
        String reason;

        @JsonCreator
        Rejected(String reason) {
            this.reason = reason;
        }
    }
}

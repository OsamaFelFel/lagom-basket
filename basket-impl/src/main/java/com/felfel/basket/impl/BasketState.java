package com.felfel.basket.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.felfel.basket.api.BasketItem;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.math.BigDecimal;
import java.util.UUID;

@Value
public class BasketState implements CompressedJsonable {
    public static final BasketState EMPTY = new BasketState("", TreePVector.empty(), new BigDecimal(0), new BigDecimal(0), new BigDecimal(0));

    String userUuid;
    PVector<BasketItem> items;
    BigDecimal subTotal;
    BigDecimal tax;
    BigDecimal total;

    @JsonCreator
    public BasketState(String userUuid, PVector<BasketItem> items, BigDecimal subTotal, BigDecimal tax, BigDecimal total) {
        this.userUuid = Preconditions.checkNotNull(userUuid, "userUuid");
        this.items = Preconditions.checkNotNull(items, "items");
        this.subTotal = Preconditions.checkNotNull(subTotal, "subTotal");
        this.tax = Preconditions.checkNotNull(tax, "tax");
        this.total = Preconditions.checkNotNull(total, "total");
    }

    public BasketState addItem(BasketItem item, BigDecimal subTotal, BigDecimal tax, BigDecimal total) {
        // TODO: User's UUID logic needs to be refactored after introducing a user service
        return new BasketState(userUuid.isEmpty() ? UUID.randomUUID().toString() : userUuid, items.plus(item), subTotal, tax, total);
    }
}

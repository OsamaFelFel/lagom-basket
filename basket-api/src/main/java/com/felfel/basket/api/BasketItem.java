package com.felfel.basket.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.math.BigDecimal;

@Value
@JsonDeserialize
public class BasketItem {
    String uuid;
    Integer quantity;
    BigDecimal price;

    @JsonCreator
    public BasketItem(String uuid, Integer quantity, BigDecimal price) {
        this.uuid = Preconditions.checkNotNull(uuid, "uuid");
        this.quantity = Preconditions.checkNotNull(quantity, "quantity");
        this.price = Preconditions.checkNotNull(price, "price");
    }
}

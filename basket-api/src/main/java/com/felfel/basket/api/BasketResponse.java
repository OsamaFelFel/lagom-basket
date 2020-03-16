package com.felfel.basket.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import lombok.Value;
import org.pcollections.PVector;

import java.math.BigDecimal;

@Value
@JsonDeserialize
public class BasketResponse {
    String uuid;
    String userUuid;
    PVector<BasketItem> items;
    BigDecimal subTotal;
    BigDecimal tax;
    BigDecimal total;

    @JsonCreator
    public BasketResponse(String uuid, String userUuid, PVector<BasketItem> items, BigDecimal subTotal, BigDecimal tax, BigDecimal total) {
        this.uuid = Preconditions.checkNotNull(uuid, "uuid");
        this.userUuid = Preconditions.checkNotNull(userUuid, "userUuid");
        this.items = Preconditions.checkNotNull(items, "items");
        this.subTotal = Preconditions.checkNotNull(subTotal, "subTotal");
        this.tax = Preconditions.checkNotNull(tax, "tax");
        this.total = Preconditions.checkNotNull(total, "total");
    }
}

package com.felfel.basket.impl;

import akka.Done;
import akka.NotUsed;
import com.felfel.basket.api.BasketItem;
import com.felfel.basket.api.BasketResponse;
import com.felfel.basket.api.BasketService;
import com.lightbend.lagom.javadsl.api.ServiceCall;

public class BasketServiceImpl implements BasketService {

    @Override
    public ServiceCall<NotUsed, BasketResponse> get(String uuid) {
        return null;
    }

    @Override
    public ServiceCall<BasketItem, Done> addItem(String uuid) {
        return null;
    }
}

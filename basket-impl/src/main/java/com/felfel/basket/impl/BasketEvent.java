package com.felfel.basket.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.serialization.Jsonable;

public interface BasketEvent extends Jsonable, AggregateEvent<BasketEvent> {

}

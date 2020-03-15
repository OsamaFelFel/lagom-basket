package com.felfel.basket.impl;

import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.CommandHandlerWithReply;
import akka.persistence.typed.javadsl.EventHandler;
import akka.persistence.typed.javadsl.EventSourcedBehaviorWithEnforcedReplies;

public class BasketAggregate extends EventSourcedBehaviorWithEnforcedReplies<BasketCommand, BasketEvent, BasketState> {

    private BasketAggregate(EntityContext<BasketCommand> entityContext) {
        super(PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()));
    }

    @Override
    public BasketState emptyState() {
        return null;
    }

    @Override
    public CommandHandlerWithReply<BasketCommand, BasketEvent, BasketState> commandHandler() {
        return null;
    }

    @Override
    public EventHandler<BasketState, BasketEvent> eventHandler() {
        return null;
    }
}

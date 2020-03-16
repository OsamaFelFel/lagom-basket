package com.felfel.basket.impl;

import akka.cluster.sharding.typed.javadsl.EntityContext;
import akka.cluster.sharding.typed.javadsl.EntityTypeKey;
import akka.persistence.typed.PersistenceId;
import akka.persistence.typed.javadsl.*;
import com.felfel.basket.api.BasketItem;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
public class BasketAggregate extends EventSourcedBehaviorWithEnforcedReplies<BasketCommand, BasketEvent, BasketState> {
    public static EntityTypeKey<BasketCommand> ENTITY_TYPE_KEY = EntityTypeKey.create(BasketCommand.class, "BasketAggregate");

    private final BigDecimal taxPercentage = new BigDecimal(14);
    private final String entityId;

    private BasketAggregate(EntityContext<BasketCommand> entityContext) {
        super(PersistenceId.of(entityContext.getEntityTypeKey().name(), entityContext.getEntityId()));
        this.entityId = entityContext.getEntityId();
    }

    public static BasketAggregate create(EntityContext<BasketCommand> entityContext) {
        return new BasketAggregate(entityContext);
    }

    @Override
    public BasketState emptyState() {
        return BasketState.EMPTY;
    }

    @Override
    public CommandHandlerWithReply<BasketCommand, BasketEvent, BasketState> commandHandler() {
        log.debug("A call has been made to commandHandler for basket [{}]", entityId);
        CommandHandlerWithReplyBuilder<BasketCommand, BasketEvent, BasketState> builder = newCommandHandlerWithReplyBuilder();

        builder.forAnyState()
                .onCommand(BasketCommand.AddItem.class, this::onAddItem);

        builder.forAnyState()
                .onCommand(BasketCommand.GetBasket.class, this::onGetBasket);

        return builder.build();
    }

    @Override
    public EventHandler<BasketState, BasketEvent> eventHandler() {
        log.debug("A call has been made to eventHandler for basket [{}]", entityId);
        return newEventHandlerBuilder()
                .forAnyState()
                .onEvent(BasketEvent.ItemAdded.class, (state, event) -> {
                    BasketItem newItem = new BasketItem(event.getItemUuid(), event.getItemQuantity(), event.getItemPrice());
                    return state.addItem(newItem, event.getBasketSubTotal(), event.getBasketTax(), event.getBasketTotal());
                }).build();
    }

    private ReplyEffect<BasketEvent, BasketState> onGetBasket(BasketState state, BasketCommand.GetBasket command) {
        log.debug("Getting basket [{}]", entityId);
        return Effect().none()
                .thenReply(command.getReplyTo(), __ -> new BasketCommand.Basket(state.getUserUuid(), state.getItems(), state.getSubTotal(), state.getTax(), state.getTotal()));
    }

    private ReplyEffect<BasketEvent, BasketState> onAddItem(BasketState state, BasketCommand.AddItem command) {
        log.debug("Adding item [{}] to basket [{}]", command.getItemUuid(), entityId);

        if (command.getItemUuid() == null || command.getItemUuid().isEmpty()) {
            return Effect().reply(command.getReplyTo(), new BasketCommand.Rejected("Item UUID must bbe provided"));
        }

        if (command.getQuantity() < 1) {
            return Effect().reply(command.getReplyTo(), new BasketCommand.Rejected("Quantity should be at least one"));
        }

        if (command.getPrice().compareTo(BigDecimal.ZERO) != 1) {
            return Effect().reply(command.getReplyTo(), new BasketCommand.Rejected("Price should be grater than zero"));
        }

        for (BasketItem item : state.getItems()) {
            if (item.getUuid().equals(command.getItemUuid())) {
                return Effect().reply(command.getReplyTo(), new BasketCommand.Rejected("Item already exists in the basket"));
            }
        }

        BigDecimal newItemSubTotal = command.getPrice().multiply(new BigDecimal(command.getQuantity()));
        BigDecimal newSubTotal = state.getSubTotal().add(newItemSubTotal);
        BigDecimal newTax = newSubTotal.multiply(taxPercentage).divide(new BigDecimal(100), RoundingMode.UP);
        BigDecimal newTotal = state.getTotal().add(newSubTotal).add(newTax);

        return Effect()
                .persist(new BasketEvent.ItemAdded(entityId, command.getItemUuid(), command.getQuantity(), command.getPrice(), newSubTotal, newTax, newTotal, Instant.now()))
                .thenReply(command.getReplyTo(), s -> new BasketCommand.Accepted(new BasketCommand.Basket(s.getUserUuid(), s.getItems(), s.getSubTotal(), s.getTax(), s.getTotal())));
    }
}

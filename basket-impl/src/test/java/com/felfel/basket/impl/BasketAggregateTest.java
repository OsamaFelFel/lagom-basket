package com.felfel.basket.impl;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.cluster.sharding.typed.javadsl.EntityContext;
import com.felfel.basket.api.BasketItem;
import org.junit.ClassRule;
import org.junit.Test;
import org.pcollections.PVector;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class BasketAggregateTest {
    private static final String IN_MEM_CONFIG =
            "akka.persistence.journal.plugin = \"akka.persistence.journal.inmem\" \n";

    private static final String SNAPSHOT_CONFIG =
            "akka.persistence.snapshot-store.plugin = \"akka.persistence.snapshot-store.local\" \n"
                    + "akka.persistence.snapshot-store.local.dir = \"target/snapshot-"
                    + UUID.randomUUID().toString()
                    + "\" \n";

    private static final String CONFIG = IN_MEM_CONFIG + SNAPSHOT_CONFIG;

    @ClassRule
    public static final TestKitJunitResource TEST_KIT = new TestKitJunitResource(CONFIG);

    @Test
    public void givenValidItemWhenAddingToBasketThenReturnBasketContainingThatItem() {
        // Arrange
        String basketUuid = UUID.randomUUID().toString();
        ActorRef<BasketCommand> basketAggregate = TEST_KIT.spawn(BasketAggregate.create(new EntityContext<>(BasketAggregate.ENTITY_TYPE_KEY, basketUuid, null)));
        TestProbe<BasketCommand.Confirmation> probe = TEST_KIT.createTestProbe(BasketCommand.Confirmation.class);
        BasketItem testItem = createTestItem(null, null);
        BigDecimal expectedSubTotal = testItem.getPrice().multiply(new BigDecimal(testItem.getQuantity()));

        // Act
        basketAggregate.tell(new BasketCommand.AddItem(testItem.getUuid(), testItem.getQuantity(), testItem.getPrice(), probe.ref()));

        // Assert
        BasketCommand.Confirmation reply = probe.receiveMessage();
        assertThat(reply).isInstanceOf(BasketCommand.Accepted.class);

        PVector<BasketItem> returnedItems = ((BasketCommand.Accepted) reply).getBasket().getItems();
        assertThat(returnedItems).isNotNull().hasSize(1).contains(testItem);

        BigDecimal resultedSubTotal = ((BasketCommand.Accepted) reply).getBasket().getSubTotal();
        assertThat(resultedSubTotal).isEqualTo(expectedSubTotal);
    }

    @Test
    public void givenTwoItemsWhenAddingToBasketThenReturnBasketWithRightCalculations() {
        // Arrange
        String basketUuid = UUID.randomUUID().toString();
        ActorRef<BasketCommand> basketAggregate = TEST_KIT.spawn(BasketAggregate.create(new EntityContext<>(BasketAggregate.ENTITY_TYPE_KEY, basketUuid, null)));
        TestProbe<BasketCommand.Confirmation> probe = TEST_KIT.createTestProbe(BasketCommand.Confirmation.class);
        BasketItem firstTestItem = createTestItem(null, null);
        BasketItem secondTestItem = createTestItem(null, null);
        BigDecimal firstItemSubTotal = firstTestItem.getPrice().multiply(new BigDecimal(firstTestItem.getQuantity()));
        BigDecimal secondItemSubTotal = secondTestItem.getPrice().multiply(new BigDecimal(secondTestItem.getQuantity()));
        BigDecimal expectedSubTotal = firstItemSubTotal.add(secondItemSubTotal);

        // Act
        basketAggregate.tell(new BasketCommand.AddItem(firstTestItem.getUuid(), firstTestItem.getQuantity(), firstTestItem.getPrice(), probe.ref()));
        probe.receiveMessage();
        basketAggregate.tell(new BasketCommand.AddItem(secondTestItem.getUuid(), secondTestItem.getQuantity(), secondTestItem.getPrice(), probe.ref()));

        // Assert
        BasketCommand.Confirmation reply = probe.receiveMessage();
        assertThat(reply).isInstanceOf(BasketCommand.Accepted.class);

        PVector<BasketItem> returnedItems = ((BasketCommand.Accepted) reply).getBasket().getItems();
        assertThat(returnedItems).isNotNull().hasSize(2).contains(firstTestItem);

        BigDecimal resultedSubTotal = ((BasketCommand.Accepted) reply).getBasket().getSubTotal();
        assertThat(resultedSubTotal).isEqualTo(expectedSubTotal);
    }

    @Test
    public void givenNegativeQuantityWhenAddingToBasketThenReturnRejectWithReason() {
        // Arrange
        String basketUuid = UUID.randomUUID().toString();
        ActorRef<BasketCommand> basketAggregate = TEST_KIT.spawn(BasketAggregate.create(new EntityContext<>(BasketAggregate.ENTITY_TYPE_KEY, basketUuid, null)));
        TestProbe<BasketCommand.Confirmation> probe = TEST_KIT.createTestProbe(BasketCommand.Confirmation.class);
        BasketItem testItem = createTestItem(-1, null);

        // Act
        basketAggregate.tell(new BasketCommand.AddItem(testItem.getUuid(), testItem.getQuantity(), testItem.getPrice(), probe.ref()));

        // Assert
        BasketCommand.Confirmation reply = probe.receiveMessage();
        assertThat(reply).isInstanceOf(BasketCommand.Rejected.class);
        assertThat(((BasketCommand.Rejected) reply).getReason()).isNotEmpty();
    }

    @Test
    public void givenZeroQuantityWhenAddingToBasketThenReturnRejectWithReason() {
        // Arrange
        String basketUuid = UUID.randomUUID().toString();
        ActorRef<BasketCommand> basketAggregate = TEST_KIT.spawn(BasketAggregate.create(new EntityContext<>(BasketAggregate.ENTITY_TYPE_KEY, basketUuid, null)));
        TestProbe<BasketCommand.Confirmation> probe = TEST_KIT.createTestProbe(BasketCommand.Confirmation.class);
        BasketItem testItem = createTestItem(0, null);

        // Act
        basketAggregate.tell(new BasketCommand.AddItem(testItem.getUuid(), testItem.getQuantity(), testItem.getPrice(), probe.ref()));

        // Assert
        BasketCommand.Confirmation reply = probe.receiveMessage();
        assertThat(reply).isInstanceOf(BasketCommand.Rejected.class);
        assertThat(((BasketCommand.Rejected) reply).getReason()).isNotEmpty();
    }

    @Test
    public void givenNegativePriceWhenAddingToBasketThenReturnRejectWithReason() {
        // Arrange
        String basketUuid = UUID.randomUUID().toString();
        ActorRef<BasketCommand> basketAggregate = TEST_KIT.spawn(BasketAggregate.create(new EntityContext<>(BasketAggregate.ENTITY_TYPE_KEY, basketUuid, null)));
        TestProbe<BasketCommand.Confirmation> probe = TEST_KIT.createTestProbe(BasketCommand.Confirmation.class);
        BasketItem testItem = createTestItem(null, new BigDecimal(-12));

        // Act
        basketAggregate.tell(new BasketCommand.AddItem(testItem.getUuid(), testItem.getQuantity(), testItem.getPrice(), probe.ref()));

        // Assert
        BasketCommand.Confirmation reply = probe.receiveMessage();
        assertThat(reply).isInstanceOf(BasketCommand.Rejected.class);
        assertThat(((BasketCommand.Rejected) reply).getReason()).isNotEmpty();
    }

    @Test
    public void givenZeroPriceWhenAddingToBasketThenReturnRejectWithReason() {
        // Arrange
        String basketUuid = UUID.randomUUID().toString();
        ActorRef<BasketCommand> basketAggregate = TEST_KIT.spawn(BasketAggregate.create(new EntityContext<>(BasketAggregate.ENTITY_TYPE_KEY, basketUuid, null)));
        TestProbe<BasketCommand.Confirmation> probe = TEST_KIT.createTestProbe(BasketCommand.Confirmation.class);
        BasketItem testItem = createTestItem(null, new BigDecimal(0));

        // Act
        basketAggregate.tell(new BasketCommand.AddItem(testItem.getUuid(), testItem.getQuantity(), testItem.getPrice(), probe.ref()));

        // Assert
        BasketCommand.Confirmation reply = probe.receiveMessage();
        assertThat(reply).isInstanceOf(BasketCommand.Rejected.class);
        assertThat(((BasketCommand.Rejected) reply).getReason()).isNotEmpty();
    }

    @Test
    public void givenExistingItemWhenAddingToBasketThenReturnRejectWithReason() {
        // Arrange
        String basketUuid = UUID.randomUUID().toString();
        ActorRef<BasketCommand> basketAggregate = TEST_KIT.spawn(BasketAggregate.create(new EntityContext<>(BasketAggregate.ENTITY_TYPE_KEY, basketUuid, null)));
        TestProbe<BasketCommand.Confirmation> probe = TEST_KIT.createTestProbe(BasketCommand.Confirmation.class);
        BasketItem testItem = createTestItem(null, null);
        basketAggregate.tell(new BasketCommand.AddItem(testItem.getUuid(), testItem.getQuantity(), testItem.getPrice(), probe.ref()));
        probe.receiveMessage();

        // Act
        basketAggregate.tell(new BasketCommand.AddItem(testItem.getUuid(), testItem.getQuantity(), testItem.getPrice(), probe.ref()));

        // Assert
        BasketCommand.Confirmation reply = probe.receiveMessage();
        assertThat(reply).isInstanceOf(BasketCommand.Rejected.class);
        assertThat(((BasketCommand.Rejected) reply).getReason()).isNotEmpty();
    }

    @Test
    public void givenNonExistingUuidWhenGettingBasketThenReturnAnEmptyBasket() {
        // Arrange
        String basketUuid = UUID.randomUUID().toString();
        ActorRef<BasketCommand> basketAggregate = TEST_KIT.spawn(BasketAggregate.create(new EntityContext<>(BasketAggregate.ENTITY_TYPE_KEY, basketUuid, null)));
        TestProbe<BasketCommand.Basket> probe = TEST_KIT.createTestProbe(BasketCommand.Basket.class);

        // Act
        basketAggregate.tell(new BasketCommand.GetBasket(probe.ref()));

        // Assert
        BasketCommand.Basket resultedBasket = probe.receiveMessage();
        assertThat(resultedBasket).isNotNull();
        assertThat(resultedBasket.getUserUuid()).isEmpty();
        assertThat(resultedBasket.getItems()).isNotNull().hasSize(0);
    }

    private BasketItem createTestItem(Integer quantity, BigDecimal price) {
        Integer itemQuantity = quantity == null ? 3 : quantity;
        BigDecimal ItemPrice = price == null ? new BigDecimal("12.5") : price;
        return new BasketItem(UUID.randomUUID().toString(), itemQuantity, ItemPrice);
    }
}

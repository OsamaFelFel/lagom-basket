package com.felfel.basket.impl;

import com.lightbend.lagom.serialization.Jsonable;

public interface BasketCommand extends Jsonable {

    final class GetBasket implements BasketCommand {
    }

    final class AddItem implements BasketCommand {
    }

    interface Reply extends Jsonable {
    }

    interface Confirmation extends Reply {
    }

    final class Accepted implements Confirmation {
    }

    final class Rejected implements Confirmation {
    }
}

package com.github.evgenykuzin.core.orders;

import com.github.evgenykuzin.core.entities.Order;

import java.util.Collection;

public interface OrdersManager {
    void sendOrder(Collection<Order> orders);
}

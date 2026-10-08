package com.smart.manufacturing.service;

import com.smart.manufacturing.entity.ManufacturingOrder;
import java.util.*;

/**
 * CollectionsDemoService — Demonstrates all required Java Collections meaningfully.
 */
public interface CollectionsDemoService {
    ArrayList<ManufacturingOrder> getOrdersAsArrayList();
    LinkedList<ManufacturingOrder> getProductionQueueAsLinkedList();
    HashSet<String> getUniqueProductSkus();
    HashMap<Long, Integer> getInventoryMap();
    TreeSet<String> getSortedCategories();
    PriorityQueue<ManufacturingOrder> getUrgentOrdersQueue();
    List<String> processOrdersWithIterator();
}

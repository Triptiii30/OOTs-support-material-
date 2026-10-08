package com.smart.manufacturing.service.impl;

import com.smart.manufacturing.entity.ManufacturingOrder;
import com.smart.manufacturing.entity.Product;
import com.smart.manufacturing.enums.OrderStatus;
import com.smart.manufacturing.enums.Priority;
import com.smart.manufacturing.repository.InventoryRepository;
import com.smart.manufacturing.repository.ManufacturingOrderRepository;
import com.smart.manufacturing.repository.ProductRepository;
import com.smart.manufacturing.service.CollectionsDemoService;
import com.smart.manufacturing.strategy.OrderSchedulingComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CollectionsDemoServiceImpl implements CollectionsDemoService {

    private final ManufacturingOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    @Autowired
    public CollectionsDemoServiceImpl(ManufacturingOrderRepository orderRepository,
                                      ProductRepository productRepository,
                                      InventoryRepository inventoryRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public ArrayList<ManufacturingOrder> getOrdersAsArrayList() {
        return new ArrayList<>(orderRepository.findAll());
    }

    @Override
    public LinkedList<ManufacturingOrder> getProductionQueueAsLinkedList() {
        List<ManufacturingOrder> inProgress = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.SCHEDULED || o.getStatus() == OrderStatus.IN_PROGRESS)
                .collect(Collectors.toList());
        return new LinkedList<>(inProgress);
    }

    @Override
    public HashSet<String> getUniqueProductSkus() {
        HashSet<String> skus = new HashSet<>();
        productRepository.findAll().forEach(p -> skus.add(p.getSku()));
        return skus;
    }

    @Override
    public HashMap<Long, Integer> getInventoryMap() {
        HashMap<Long, Integer> map = new HashMap<>();
        inventoryRepository.findAll().forEach(inv ->
                map.put(inv.getProduct().getId(), inv.getCurrentStock()));
        return map;
    }

    @Override
    public TreeSet<String> getSortedCategories() {
        TreeSet<String> categories = new TreeSet<>();
        productRepository.findAll().stream()
                .map(Product::getCategory)
                .filter(c -> c != null && !c.isBlank())
                .forEach(categories::add);
        return categories;
    }

    @Override
    public PriorityQueue<ManufacturingOrder> getUrgentOrdersQueue() {
        PriorityQueue<ManufacturingOrder> queue = new PriorityQueue<>(new OrderSchedulingComparator());
        orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.COMPLETED && o.getStatus() != OrderStatus.CANCELLED)
                .filter(o -> o.getPriority() == Priority.URGENT || o.getPriority() == Priority.HIGH)
                .forEach(queue::add);
        return queue;
    }

    @Override
    public List<String> processOrdersWithIterator() {
        List<ManufacturingOrder> orders = new ArrayList<>(orderRepository.findAll());
        List<String> summaries = new ArrayList<>();
        Iterator<ManufacturingOrder> iterator = orders.iterator();
        while (iterator.hasNext()) {
            ManufacturingOrder order = iterator.next();
            StringBuilder sb = new StringBuilder()
                    .append(order.getOrderNumber()).append(" | ")
                    .append(order.getPriority()).append(" | ")
                    .append(order.getStatus());
            summaries.add(sb.toString());
        }
        return summaries;
    }
}

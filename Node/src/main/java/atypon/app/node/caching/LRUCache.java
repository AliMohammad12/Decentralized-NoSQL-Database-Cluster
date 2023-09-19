package atypon.app.node.caching;

import atypon.app.node.controller.DatabaseController;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


// Reading ID => put(ID, document) to cache
// Delete Document => delete
// Update Document => Delete key, add key with new value!
@Service
public class LRUCache<K, V> {
    private static final Logger logger = LoggerFactory.getLogger(LRUCache.class);
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> previous;
        Node<K, V> next;
    }
    private Map<K, Node<K, V>> cache;
    private int capacity;
    private int count;
    private Node<K, V> head;
    private Node<K, V> tail;
    private final int CACHE_SIZE = 200;
    @PostConstruct
    public void init() {
        this.capacity = CACHE_SIZE;
        this.cache = new ConcurrentHashMap<>();
        this.count = 0;
        this.head = new Node<>();
        this.tail = new Node<>();
        this.head.next = tail;
        this.tail.previous = head;
    }
    public synchronized V get(K key) {
        logger.info("Retrieving the document with key '{}' from cache!", key);
        Node<K, V> node = cache.get(key);
        if (node == null) {
            return null;
        }
        moveToHead(node);
        return node.value;
    }
    public synchronized void put(K key, V value) {
        logger.info("Adding the document with key '{}' to cache!", key);
        Node<K, V> node = cache.get(key);
        if (node == null) {
            Node<K, V> newNode = new Node<>();
            newNode.key = key;
            newNode.value = value;

            cache.put(key, newNode);
            addNode(newNode);
            count++;

            if (count > capacity) {
                Node<K, V> tailNode = removeFromTail();
                cache.remove(tailNode.key);
                count--;
            }
        } else {
            node.value = value;
            moveToHead(node);
        }
    }
    public synchronized boolean containsKey(K key) {
        return cache.containsKey(key);
    }
    public synchronized void remove(K key) {
        logger.info("Removing the document with key '{}' from cache!", key);
        if (cache.containsKey(key)) {
            Node<K, V> currentNode = cache.get(key);
            cache.remove(key);
            removeNode(currentNode);
        }
    }
    private void addNode(Node<K, V> node) {
        node.previous = head;
        node.next = head.next;
        head.next.previous = node;
        head.next = node;
    }
    private void removeNode(Node<K, V> node) {
        Node<K, V> previous = node.previous;
        Node<K, V> next = node.next;
        previous.next = next;
        next.previous = previous;
    }
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addNode(node);
    }
    private Node<K, V> removeFromTail() {
        Node<K, V> res = tail.previous;
        removeNode(res);
        return res;
    }
}



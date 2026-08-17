package datastructures;

// Custom HashMap implementation using separate chaining
public class CustomHashMap<K, V> {
    private HashNode<K, V>[] buckets;
    private int capacity = 16;

    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        buckets = new HashNode[capacity];
    }

    private int getBucketIndex(K key) {
        return Math.abs(key.hashCode()) % capacity;
    }

    public void put(K key, V value) {
        int index = getBucketIndex(key);
        HashNode<K, V> head = buckets[index];

        while (head != null) {
            if (head.key.equals(key)) {
                head.value = value; // update existing key
                return;
            }
            head = head.next;
        }

        HashNode<K, V> newNode = new HashNode<>(key, value);
        newNode.next = buckets[index];
        buckets[index] = newNode;
    }

    public V get(K key) {
        int index = getBucketIndex(key);
        HashNode<K, V> head = buckets[index];

        while (head != null) {
            if (head.key.equals(key)) return head.value;
            head = head.next;
        }
        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }
}

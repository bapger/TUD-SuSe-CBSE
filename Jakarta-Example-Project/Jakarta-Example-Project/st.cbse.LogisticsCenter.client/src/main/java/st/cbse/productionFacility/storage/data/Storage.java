package st.cbse.productionFacility.storage.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Storage {
    
    private UUID id = UUID.randomUUID();
    
    private String name;
    
    private int capacity;
    
    private List<ItemData> items = new ArrayList<>();
    
    protected Storage() {}
    
    public Storage(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }
    
    public UUID getId() { return id; }
    public String getName() { return name; }
    public int getCapacity() { return capacity; }
    public List<ItemData> getItems() { return new ArrayList<>(items); }
    
    public int getCurrentOccupancy() {
        return items.size();
    }
    
    public boolean hasSpace() {
        return getCurrentOccupancy() < capacity;
    }
    
    public boolean addItem(ItemData item) {
        if (!hasSpace()) return false;
        items.add(item);
        return true;
    }
    
    public boolean removeItem(ItemData item) {
        return items.remove(item);
    }
    
    public ItemData findItemById(UUID itemId) {
        return items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }
}
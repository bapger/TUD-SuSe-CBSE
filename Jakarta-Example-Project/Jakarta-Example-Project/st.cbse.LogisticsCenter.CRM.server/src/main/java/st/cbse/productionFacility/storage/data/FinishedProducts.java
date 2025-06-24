package st.cbse.productionFacility.storage.data;

import java.util.Collections;
import java.util.List;

public class FinishedProducts {

    private final List<ItemData> items;

    public FinishedProducts(List<ItemData> items) {
        this.items = items;
    }

    public List<ItemData> getItems() {
        return Collections.unmodifiableList(items);
    }
}
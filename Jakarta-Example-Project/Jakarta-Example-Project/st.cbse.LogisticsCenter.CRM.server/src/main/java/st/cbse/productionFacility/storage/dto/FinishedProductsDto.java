package st.cbse.productionFacility.storage.dto;

import java.util.List;
import java.util.stream.Collectors;

import st.cbse.productionFacility.storage.data.FinishedProducts;

public class FinishedProductsDto {

    private List<ItemInfo> items;

    public FinishedProductsDto() {
    }

    public FinishedProductsDto(List<ItemInfo> items) {
        this.items = items;
    }

    public static FinishedProductsDto fromModel(FinishedProducts model) {
        return new FinishedProductsDto(
                model.getItems()
                     .stream()
                     .map(ItemInfo::fromEntity)
                     .collect(Collectors.toList())
        );
    }

    public List<ItemInfo> getItems() {
        return items;
    }

    public void setItems(List<ItemInfo> items) {
        this.items = items;
    }
}
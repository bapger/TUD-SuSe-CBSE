package st.cbse.crm.orderComponent.data;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PrintingRequest {

    private UUID id = UUID.randomUUID();

    private String description;

    private String stlPath;           // chemin/URI du fichier STL

    private String note;              // note libre du client


    private Order order;

    private List<Option> options = new ArrayList<>();

    /* -------------------------------------------------- */
    public PrintingRequest() { }

    /* ================= setters ======================== */

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setStlPath(String stlPath) {
        this.stlPath = stlPath;
    }

    public void setNote(String note) {
        this.note = note;
    }

    /* ================= getters ======================== */

    public UUID getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public String getStlPath() {
        return stlPath;
    }

    public String getNote() {
        return note;
    }

    /**
     * Retourne le total des prix de toutes les options de cette demande.
     */
    public BigDecimal getOptions() {
        BigDecimal total = BigDecimal.ZERO;
        for (Option o : options) {
            if (o.getPrice() != null) {
                total = total.add(o.getPrice());
            }
        }
        return total;
    }

    /* ================= helpers ======================== */

    /**
     * Ajoute une option en maintenant la cohérence bidirectionnelle.
     */
    public void addOption(Option option) {
        option.setPrintingRequest(this);
        options.add(option);
    }

    /* alias utilisé ailleurs dans le code */
    public void add(Option option) {
        addOption(option);
    }
}
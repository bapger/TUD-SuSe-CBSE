package st.cbse.crm.orderComponent.data;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class PrintingRequest {

	@Id
	private UUID id = UUID.randomUUID();

	private UUID orderId;
	private String description;
	private PrintingRequestStatus status;

	private String stlPath;
	@Column(length = 1024)
	private String note;

	/* --- relations --- */
	@ManyToOne
	private Order order;

	@OneToMany(mappedBy = "printingRequest", fetch = FetchType.LAZY,
			cascade = CascadeType.ALL)
	@OrderColumn(name = "opt_idx")          // extra INT column
	private List<Option> options = new ArrayList<>();

	/* -------------------------------------------------- */
	public PrintingRequest() { }

	/* ================= setters ======================== */

	public void setDescription(String description) {
		this.description = description;
	}

	public PrintingRequestStatus getStatus() {
		return status;
	}

	public void setStatus(PrintingRequestStatus status) {
		this.status = status;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}

	public String getDescription() {
		return description;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public void setOptions(List<Option> options) {
		this.options = options;
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
	/*public BigDecimal getOptions() {
        BigDecimal total = BigDecimal.ZERO;
        for (Option o : options) {
            if (o.getPrice() != null) {
                total = total.add(o.getPrice());
            }
        }
        return total;
    }*/
	public List<Option> getOptions() {
		return options;
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
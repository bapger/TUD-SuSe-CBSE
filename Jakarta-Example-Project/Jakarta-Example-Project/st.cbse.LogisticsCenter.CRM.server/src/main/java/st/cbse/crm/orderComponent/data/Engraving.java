package st.cbse.crm.orderComponent.data;

import jakarta.persistence.Entity;

@Entity
public class Engraving extends Option {

	private String text;
	private String font;
	private String imagePath;
	
	protected Engraving() { }

	public Engraving(String text, String font, String imagePath) {
		this.text = text;
		this.font = font;
		this.imagePath = imagePath;
	}

}

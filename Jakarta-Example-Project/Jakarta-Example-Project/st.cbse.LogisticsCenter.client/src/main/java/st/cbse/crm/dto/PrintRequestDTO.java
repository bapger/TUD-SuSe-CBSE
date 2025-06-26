package st.cbse.crm.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import st.cbse.crm.orderComponent.data.PrintingRequest;

public class PrintRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID            id;
    private final String          stlPath;
    private final String          note;
    private final List<OptionDTO> options;
    private final BigDecimal      price;      // ← new

    public PrintRequestDTO(UUID id,
                           String stlPath,
                           String note,
                           List<OptionDTO> options) {

        this.id       = id;
        this.stlPath  = stlPath;
        this.note     = note;
        this.options  = List.copyOf(options);

        this.price = this.options.stream()
                                 .map(OptionDTO::getPrice)
                                 .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId()                 { return id; }
    public String getStlPath()          { return stlPath; }
    public String getNote()             { return note; }
    public List<OptionDTO> getOptions() { return options; }


    public BigDecimal getPrice()        { return price; }
    
    public static PrintRequestDTO of(PrintingRequest pr) {
        List<OptionDTO> optionDtos = pr.getOptions()
                                       .stream()
                                       .map(OptionDTO::of)   
                                       .collect(Collectors.toList());

        return new PrintRequestDTO(pr.getId(),
                                   pr.getStlPath(),
                                   pr.getNote(),
                                   optionDtos);
    }
}
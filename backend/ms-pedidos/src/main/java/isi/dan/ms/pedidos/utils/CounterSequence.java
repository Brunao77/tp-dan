package isi.dan.ms.pedidos.utils;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "counters")
public class CounterSequence {

    @Id
    private String id;
    private long sequence; //pedidoSeq para pedidos
}

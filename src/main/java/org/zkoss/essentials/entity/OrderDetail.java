package org.zkoss.essentials.entity;

/**
 * Created by Sam on 19-Jul-17.
 */

import lombok.Data;
import org.zkoss.essentials.util.CurrencyUtil;

import java.io.Serializable;
import java.math.BigDecimal;

//@Table(name = "Order_Detail")
//@Entity
@Data
public class OrderDetail implements Serializable {

    private static final long serialVersionUID = 7550745928843183535L;

    //    @Id
    //    @GeneratedValue(strategy=GenerationType.AUTO)
    //    @Column(unique=true, nullable=false)
    private Long orderDetailId;

    //    @ManyToOne(fetch = FetchType.LAZY)
    //    @JoinColumn(name = "OrderId", nullable = false)
    //    @NotNull(message="Order is mandatory")
    private Order order;

    //    @ManyToOne(fetch = FetchType.LAZY)
    //    @JoinColumn(name = "OrderId", nullable = false)
    //    @NotNull(message="Product is mandatory")
    private Product product;

    //    @Column(name = "Quantity", nullable = false)
    private int quantity;

    //    @Column(name = "Price", nullable = false)
    private BigDecimal price;

    private String currencyPrice;


    public String getCurrencyPrice() {
        return "NZ$ " + CurrencyUtil.getCurrencyFormat( price );
    }
}
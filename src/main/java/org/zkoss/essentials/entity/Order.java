package org.zkoss.essentials.entity;

/**
 * Created by Sam on 19-Jul-17.
 */

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


// @Table(name = "Order" )
// @Entity
@Data
public class Order implements Serializable, Cloneable {

    private static final long serialVersionUID = -2576670215015463100L;

    private List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();

    // @Id
    // @GeneratedValue(strategy=GenerationType.AUTO)
    // @Column(unique=true, nullable=false)
    private Long orderId;

    // @Column(name = "Order_Date", nullable = false)
    // @Temporal(TemporalType.TIMESTAMP)
    private Date orderDate;

    // @Column(name = "Order_Number", nullable = false)
    private int orderNumber;

    // Total checkout price of an order
    // @Column(name = "TotalPrice", nullable = false)
    private BigDecimal totalPrice;

    private String totalPriceLabel;

}
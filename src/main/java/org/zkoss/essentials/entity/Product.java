package org.zkoss.essentials.entity;

/**
 * Created by Sam on 19-Jul-17.
 */

import lombok.Data;
import org.zkoss.essentials.util.CurrencyUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

// @Table(name = "Product", uniqueConstraints = {
//    @UniqueConstraint(columnNames = {"Name", "Code"})
// })
// @Entity
@Data
public class Product implements Serializable, Cloneable {

    private static final long serialVersionUID = -1000119078147252957L;

    // @Id
    // @GeneratedValue(strategy=GenerationType.AUTO)
    // @Column(nullable=false)
    private Long productId;

    // @Column(name = "Code", length = 20, nullable = false)
    private String code;

    // @Column(name = "Name", length = 255, nullable = false)
    private String name;

    // @Column(name = "Price", nullable = false)
    private BigDecimal price;

    private String description;

    // private byte[] image;

    // For sort.
    //    @Temporal(TemporalType.TIMESTAMP)
    //    @Column(name = "Create_Date", nullable = false)
    private Date createDate;

    private Integer stock;

    private String currencyPrice;

    public String getCurrencyPrice() {
        return "NZ$ " + CurrencyUtil.getCurrencyFormat( price );
    }

   /* @Lob
    @Column(name = "Image", length = Integer.MAX_VALUE, nullable = true)
    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }*/

}
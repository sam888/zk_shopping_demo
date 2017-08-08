package org.zkoss.essentials.vm;

/**
 * Created by Sam on 22-Jul-17.
 */

import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.essentials.entity.Product;

import java.io.Serializable;


public class ProductVM implements Serializable {


    private Product product;

    @Init
    public void init(@ExecutionArgParam("product") Product product) {
        this.product = product;
    }



    @GlobalCommand("updateProductView")
    @NotifyChange("product")
    public void updateUI() {
        // Update shopping cart
        System.out.println("update product UI....");
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

}

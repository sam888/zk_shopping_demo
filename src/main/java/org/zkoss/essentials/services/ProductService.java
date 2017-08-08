package org.zkoss.essentials.services;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.zkoss.essentials.entity.Product;

import java.math.BigDecimal;

/**
 * Created by Sam on 20-Jul-17.
 */
@Service("productService")
@Scope(value="singleton",proxyMode= ScopedProxyMode.TARGET_CLASS)
public class ProductService {

    private static final long serialVersionUID = 1L;

    private static Product productdOne = new Product();
    private static Product productTwo = new Product();

    static {
        // Init Product One data
        productdOne.setCode("45743Promo");
        productdOne.setName("Lifestreram V-Omega3 Capsules 90");
        productdOne.setDescription("An Algae derived Omega-3 product. 90 Capsules. Suitable for Vegetarians and Vegans. " +
                        "High Strength EPA, DHA with plant derived Vitamin D3 ");
        productdOne.setPrice(new BigDecimal(55.94));
        productdOne.setStock(new Integer(20));


        // Init Product Two data
        productTwo.setCode("2151073");
        productTwo.setName("Chlorella 500mg Tablets ");
        productTwo.setDescription("Lifestream Chlorella provides 500mg chlorella per tablet. It may help provide protection " +
                "against toxins, pollutants and free radicals.");
        productTwo.setPrice(new BigDecimal(31.43) );
        productTwo.setStock( new Integer(10));
    }

    public Product getProductdOne() {
        return productdOne;
    }

    public Product getProductdTwo() {
        return productTwo;
    }

    public void restockProducts() {
        productdOne.setStock( new Integer(20) );
        productTwo.setStock( new Integer(10) );
    }
}

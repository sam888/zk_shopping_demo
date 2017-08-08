package org.zkoss.essentials.vm;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.*;
import org.zkoss.essentials.entity.Order;
import org.zkoss.essentials.entity.OrderDetail;
import org.zkoss.essentials.entity.Product;
import org.zkoss.essentials.services.ProductService;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 25-Jul-17.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class OrderProductViewModel implements Serializable {

    @WireVariable
    private ProductService productService;

    @Getter private Product productOne;
    @Getter private Product productTwo;

    @Getter @Setter
    private List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();

    @Getter
    private Order order;


    @Init
    public void init() {
        order = new Order();
        productOne = productService.getProductdOne();
        productTwo = productService.getProductdTwo();
    }

    @GlobalCommand
    @NotifyChange( {"orderDetailList", "order"}  )
    public void clickToOrderProduct( @BindingParam("product") Product product, @BindingParam("orderQuantity") Integer orderQuantity ) {

        if ( product == null ) return;

        int stock = product.getStock();

        if ( !isValidOrder( stock, orderQuantity)) {
            return;
        }

        OrderDetail orderDetail = getOrderDetailByProductCode( product.getCode() );
        updateOrderDetailListAfterOrder(orderDetail, product, orderQuantity);

        // Update UI of product view/form
        BindUtils.postGlobalCommand( null, EventQueues.DESKTOP, "updateProductView", null);

        computeCheckoutPrice();
    }

    @Command
    public void restockAction() {
        productOne.setStock( productOne.getStock() + 10);
        productTwo.setStock( productOne.getStock() + 10);

        BindUtils.postGlobalCommand( null, EventQueues.DESKTOP, "updateProductView", null);
    }


    @Command
    public void buyButtonAction() {

        if ( orderDetailList.size() == 0 ) return;

        //create a window programmatically and use it as a modal dialog.
        Window window = (Window) Executions.createComponents("/shop/buy_confirm.zul", null, null);
        window.doModal();

        order.setOrderDetailList( new ArrayList<OrderDetail>());
        orderDetailList.clear();
    }

    @Command @NotifyChange( "orderDetailList"  )
    public void clearOrders() {

        if ( orderDetailList.size() == 0 ) return;

        for ( OrderDetail orderDetail: order.getOrderDetailList() ) {
            Product product = orderDetail.getProduct();
            product.setStock(product.getStock() + orderDetail.getQuantity());

            // One way of sending data to MVVM. Works but needs to declare queue name in zul file
            // Map<String, Object> argsMap = Collections.<String, Object>singletonMap("product", product);
            // BindUtils.postGlobalCommand("productMvvmQueue", null, "updateProductView", argsMap);
        }

        order.setOrderDetailList( new ArrayList<OrderDetail>());
        orderDetailList.clear();

        BindUtils.postGlobalCommand( null, EventQueues.DESKTOP, "updateProductView", null);
    }


    private void updateOrderDetailListAfterOrder(OrderDetail orderDetail, Product product, Integer quantityToOrder) {
        if ( orderDetail == null ) {
            orderDetail = new OrderDetail();
            orderDetail.setProduct(product);
            orderDetail.setQuantity( quantityToOrder );

            order.getOrderDetailList().add( orderDetail );
            orderDetailList.add(orderDetail);

        } else {

            int newQuantity = orderDetail.getQuantity() + quantityToOrder;
            orderDetail.setQuantity( newQuantity );
        }

        Integer stockIntbox = product.getStock();

        Integer stockLeft = stockIntbox - quantityToOrder;
        product.setStock(stockLeft);

        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(orderDetail.getQuantity()  ));
        orderDetail.setPrice( roundBigDecimal(totalPrice) );
    }

    private void computeCheckoutPrice() {
        BigDecimal checkoutPrice = BigDecimal.ZERO;
        List<OrderDetail> orderDetailList = order.getOrderDetailList();
        for ( OrderDetail orderDetail: orderDetailList ) {
            checkoutPrice = checkoutPrice.add( orderDetail.getPrice() );
        }
        order.setTotalPrice( checkoutPrice );
        order.setTotalPriceLabel( "NZ$ " + checkoutPrice );
    }

    private boolean isValidOrder(int stock, Integer quantityToOrder) {

        if ( quantityToOrder == null) {
            return false;
        }

        if ( stock == 0 )  {
            Messagebox.show("Sorry, this product is out of stock", "Error", Messagebox.OK, Messagebox.ERROR);
            return false;
        }

        if ( quantityToOrder > stock && ( stock > 0)) {
            Messagebox.show("Sorry, your order exceeds stock available", "Error", Messagebox.OK, Messagebox.ERROR);
            return false;
        }
        return true;
    }


    public OrderDetail getOrderDetailByProductCode(String productCode) {
        if ( productCode == null )return null;

        for ( OrderDetail orderDetail: orderDetailList ) {
            Product product = orderDetail.getProduct();
            if ( product != null ) {
                String testProductCode = product.getCode();
                if ( ( testProductCode != null ) && StringUtils.equals(productCode, testProductCode) ) {
                    return orderDetail;
                }
            }
        }
        return null;
    }

    private BigDecimal roundBigDecimal(BigDecimal bigDecimal){
        if ( bigDecimal == null ) return null;
        return bigDecimal.setScale(2, RoundingMode.HALF_UP);
    }
}

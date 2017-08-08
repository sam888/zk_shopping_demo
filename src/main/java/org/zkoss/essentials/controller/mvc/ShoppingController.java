package org.zkoss.essentials.controller.mvc;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.zkoss.essentials.entity.Order;
import org.zkoss.essentials.entity.OrderDetail;
import org.zkoss.essentials.entity.Product;
import org.zkoss.essentials.services.ProductService;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sam on 20-Jul-17.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class ShoppingController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;


    @Getter private Product productOne;
    @Getter private Product productTwo;

    // Product One UI components
    @Wire
    private Textbox productOne_codeTextbox;
    @Wire
    private Textbox productOne_nameTextbox;
    @Wire
    private Textbox productOne_descriptionTextbox;
    @Wire
    private Intbox productOne_stockIntbox;
    @Wire
    private Intbox productOne_orderQuantityIntbox;
    @Wire
    private Button productOne_addToOrderButton;
    @Wire
    private Caption productOne_caption;
    @Wire
    private Decimalbox productOne_priceDecimalbox;

    // Product Two UI components
    @Wire
    private Textbox productTwo_codeTextbox;
    @Wire
    private Textbox productTwo_nameTextbox;
    @Wire
    private Textbox productTwo_descriptionTextbox;
    @Wire
    private Intbox productTwo_stockIntbox;
    @Wire
    private Intbox productTwo_orderQuantityIntbox;
    @Wire
    private Button productTwo_addToOrderButton;
    @Wire
    private Caption productTwo_caption;
    @Wire
    private Decimalbox productTwo_priceDecimalbox;

    @Wire
    private Label totalOrderPriceLabel;

    @Wire
    private Button restockButton;
    @Wire
    private Button buyButton;
    @Wire
    private Button clearOrderButton;


    @Wire
    private Grid orderDetailsGrid;
    @Wire
    private Grid totalOrderPriceGrid;


    @WireVariable
    private ProductService productService;

    // Data for the view
    private ListModelList<OrderDetail> orderDetailListModel;

    private Order order;

    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        ComponentInfo componnentInfo = super.doBeforeCompose(page, parent, compInfo);

        productOne = productService.getProductdOne();
        productOne.setPrice( roundBigDecimal( productOne.getPrice() ) );
        productTwo = productService.getProductdTwo();
        productTwo.setPrice( roundBigDecimal( productTwo.getPrice() ) );

        return compInfo;
    }

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();
        orderDetailListModel = new ListModelList<OrderDetail>( orderDetailList );
        orderDetailsGrid.setModel( orderDetailListModel );

        // Initialize order
        order = new Order();
        order.setOrderDetailList( orderDetailList );
    }

    @Listen("onClick = #restockButton")
    public void clickRestockButton() {
        productOne.setStock( productOne.getStock() + new Integer(10));
        productOne_stockIntbox.setValue( productOne.getStock() );

        productTwo.setStock( productTwo.getStock() + new Integer(10));
        productTwo_stockIntbox.setValue( productTwo.getStock());
    }

    @Listen("onClick = #productOne_addToOrderButton")
    public void click_productOne_addToOrderButton(){

        Integer quantityToOrder = productOne_orderQuantityIntbox.getValue();
        if ( quantityToOrder == null ) return;

        int stock = productOne.getStock();

        if ( !isValidOrder( stock, quantityToOrder)) {
            return;
        }

        OrderDetail orderDetail = getOrderDetailByProductCode( productOne.getCode() );
        updateOrderDetailListModelAfterOrder(orderDetail, productOne, quantityToOrder, productOne_stockIntbox );
    }


    @Listen("onClick = #productTwo_addToOrderButton")
    public void click_productTwo_addToOrderButton(){
        Integer quantityToOrder = productTwo_orderQuantityIntbox.getValue();
        if ( quantityToOrder == null ) return;

        int stock = productTwo.getStock();

        if ( !isValidOrder( stock, quantityToOrder)) {
            return;
        }

        OrderDetail orderDetail = getOrderDetailByProductCode( productTwo.getCode() );
        updateOrderDetailListModelAfterOrder(orderDetail, productTwo, quantityToOrder, productTwo_stockIntbox );
    }

    @Listen("onClick = #buyButton")
    public void showModal(Event e) {

        if ( orderDetailListModel.getSize() == 0 ) return;

        //create a window programmatically and use it as a modal dialog.
        Window window = (Window) Executions.createComponents("/shop/buy_confirm.zul", null, null);
        window.doModal();

        order.setOrderDetailList( new ArrayList<OrderDetail>());
        orderDetailListModel.clear();
        totalOrderPriceGrid.setVisible( false );
    }

    @Listen("onClick = #clearOrderButton")
    public void clearOrders(Event e) {

        if ( orderDetailListModel.getSize() == 0 ) return;

        for ( OrderDetail orderDetail: order.getOrderDetailList() ) {
            Product product = orderDetail.getProduct();
            product.setStock(product.getStock() + orderDetail.getQuantity());
        }
        productOne_stockIntbox.setValue( productOne.getStock() );
        productTwo_stockIntbox.setValue( productTwo.getStock() );

        order.setOrderDetailList( new ArrayList<OrderDetail>());
        orderDetailListModel.clear();
        totalOrderPriceGrid.setVisible( false );
    }

    private void updateOrderDetailListModelAfterOrder(OrderDetail orderDetail, Product product, Integer quantityToOrder, Intbox stockIntbox) {
        if ( orderDetail == null ) {
            orderDetail = new OrderDetail();
            orderDetail.setProduct(product);
            orderDetail.setQuantity( quantityToOrder );

            order.getOrderDetailList().add( orderDetail );
            orderDetailListModel.add( orderDetail );

        } else {

            int newQuantity = orderDetail.getQuantity() + quantityToOrder;
            orderDetail.setQuantity( newQuantity );
        }

        Integer stockLeft = stockIntbox.getValue() - quantityToOrder;
        product.setStock(stockLeft);
        stockIntbox.setValue(stockLeft);

        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal( orderDetail.getQuantity()  ));
        orderDetail.setPrice( roundBigDecimal(totalPrice) );

        // This will update UI
        orderDetailListModel.set(orderDetailListModel.indexOf( orderDetail ), orderDetail);

        totalOrderPriceGrid.setVisible( true );
        totalOrderPriceLabel.setValue( "NZ$ " + dFormat.format( getCheckoutPrice() ));
    }

    private BigDecimal getCheckoutPrice() {
        BigDecimal checkoutPrice = BigDecimal.ZERO;
        List<OrderDetail> orderDetailList = order.getOrderDetailList();
        for ( OrderDetail orderDetail: orderDetailList ) {
            checkoutPrice = checkoutPrice.add( orderDetail.getPrice() );
        }
        return checkoutPrice;
    }

    private boolean isValidOrder(int stock, Integer quantityToOrder) {
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

        List<OrderDetail> orderDetailList = orderDetailListModel.getInnerList();
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

    private static DecimalFormat dFormat = new DecimalFormat("####,###,###.00");
}

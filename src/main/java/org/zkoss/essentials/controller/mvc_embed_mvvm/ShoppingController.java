package org.zkoss.essentials.controller.mvc_embed_mvvm;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.GlobalCommandEvent;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.essentials.entity.Order;
import org.zkoss.essentials.entity.OrderDetail;
import org.zkoss.essentials.entity.Product;
import org.zkoss.essentials.services.ProductService;
import org.zkoss.essentials.util.CurrencyUtil;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zkmax.ui.select.annotation.Subscribe;
import org.zkoss.zkplus.spring.DelegatingVariableResolver;
import org.zkoss.zul.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Sam on 20-Jul-17.
 */
@VariableResolver(DelegatingVariableResolver.class)
public class ShoppingController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;


    @Getter private Product productOne;
    @Getter private Product productTwo;



    // Product Two UI components
    @Wire private Textbox productTwo_codeTextbox;
    @Wire private Textbox productTwo_nameTextbox;
    @Wire private Textbox productTwo_descriptionTextbox;
    @Wire private Intbox productTwo_stockIntbox;
    @Wire private Intbox productTwo_orderQuantityIntbox;
    @Wire private Button productTwo_addToOrderButton;
    @Wire private Caption productTwo_caption;
    @Wire private Decimalbox productTwo_priceDecimalbox;

    @Wire private Label totalOrderPriceLabel;

    @Wire private Button restockButton;
    @Wire private Button buyButton;
    @Wire private Button clearOrderButton;


    @Wire private Grid orderDetailsGrid;
    @Wire private Grid totalOrderPriceGrid;


    @WireVariable
    private ProductService productService;

    // Data for the view
    private ListModelList<OrderDetail> orderDetailListModel;

    private Order order;

    @Override
    public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
        ComponentInfo componnentInfo = super.doBeforeCompose(page, parent, compInfo);

        productOne = productService.getProductdOne();
        productTwo = productService.getProductdTwo();
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


    // The @Subscribe annotation can't be removed else can't receive event sent from product.zul
    @Subscribe( "productMvvmQueue" )
    @GlobalCommand( "clickToOrderProduct" )
    public void clickToOrderProduct(Event event) {


        if ( !(event instanceof GlobalCommandEvent) ) return;
        GlobalCommandEvent globalCommandEvent = (GlobalCommandEvent)event;

        if ( !"clickToOrderProduct".equals( globalCommandEvent.getCommand() ) ) {
            return;
        }

        System.out.println("ShoppingController.clickToOrderProduct(..)...");

        Map<String, Object> args = globalCommandEvent.getArgs();
        if ( args == null ) return;

        Product product = (Product)args.get( "product");
        if ( product == null ) return;

        Integer orderQuantityInteger = (Integer)args.get( "orderQuantity" );
        if ( orderQuantityInteger == null ) return;

        int stock = product.getStock();
        int orderQuantity = orderQuantityInteger.intValue();

        if ( !isValidOrder( stock, orderQuantity)) {
            return;
        }

        OrderDetail orderDetail = getOrderDetailByProductCode(product.getCode());
        updateOrderDetailListModelAfterOrder(orderDetail, product, orderQuantity);

        // Update UI of product view/form
        BindUtils.postGlobalCommand("productMvvmQueue", EventQueues.DESKTOP, "updateProductView", null);
    }


    @Listen("onClick = #restockButton")
    public void clickRestockButton() {
        productOne.setStock( productOne.getStock() + 10);
        productTwo.setStock( productOne.getStock() + 10);

        BindUtils.postGlobalCommand("productMvvmQueue", EventQueues.DESKTOP, "updateProductView", null);

        // This doesn't work
        // BindUtils.postGlobalCommand(null, EventQueues.DESKTOP, "updateProductView", null);
    }


    @Listen("onClick = #buyButton")
    public void buyButtonAction(Event e) {

        if ( orderDetailListModel.getSize() == 0 ) return;

        //create a window programmatically and use it as a modal dialog.
        Window window = (Window) Executions.createComponents( "/shop/buy_confirm.zul", null, null);
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

            // One way of sending data to MVVM. Works but needs to declare queue name in zul file
            // Map<String, Object> argsMap = Collections.<String, Object>singletonMap("product", product);
            // BindUtils.postGlobalCommand("productMvvmQueue", null, "updateProductView", argsMap);
        }

        order.setOrderDetailList( new ArrayList<OrderDetail>());
        orderDetailListModel.clear();
        totalOrderPriceGrid.setVisible( false );

        BindUtils.postGlobalCommand("productMvvmQueue", EventQueues.DESKTOP, "updateProductView", null);
    }

    private void updateOrderDetailListModelAfterOrder(OrderDetail orderDetail, Product product, Integer quantityToOrder) {
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

        Integer productStock = product.getStock();

        Integer stockLeft = productStock - quantityToOrder;
        product.setStock(stockLeft);

        BigDecimal totalPrice = product.getPrice().multiply(new BigDecimal(orderDetail.getQuantity()  ));
        orderDetail.setPrice( roundBigDecimal(totalPrice) );

        // This will update UI of orderDetailsGrid
        orderDetailListModel.set(orderDetailListModel.indexOf( orderDetail ), orderDetail);

        totalOrderPriceGrid.setVisible( true );
        totalOrderPriceLabel.setValue( "NZ$ " + CurrencyUtil.getCurrencyFormat(getCheckoutPrice()));
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

}

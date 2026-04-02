package com.ecommerce.api.dto.response;

public class OrderDetailResponse {
    private String orderInfo;
    private Integer shippingFee;
    private Boolean inStock;

    public OrderDetailResponse(String orderInfo, Integer shippingFee, Boolean inStock) {
        this.orderInfo = orderInfo;
        this.shippingFee = shippingFee;
        this.inStock = inStock;
    }

    public String getOrderInfo() {
        return orderInfo;
    }

    public void setOrderInfo(String orderInfo) {
        this.orderInfo = orderInfo;
    }

    public Integer getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Integer shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Boolean getInStock() {
        return inStock;
    }

    public void setInStock(Boolean inStock) {
        this.inStock = inStock;
    }
}

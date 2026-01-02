package com.apex.core.dto;

import java.math.BigDecimal; /**
 * 商品更新请求DTO
 */
public class UpdateProductRequest {
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String image;

    // Getter和Setter
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}

package com.example.mcpserver;

import org.springframework.stereotype.Service;

@Service
public class DemoProductService implements ProductService {

  @Override
  public ProductDetails getProductDetails(String sku) {
    if (sku.equals("sku-99999")) {
      return new ProductDetails(
          "sku-99999",
          "Ultra-rare, solid gold, coffee mug",
          99,
          19999.99f,
          true);
    } else {
      return new ProductDetails(
          sku,
          "Red 16oz Solo Cup",
          1000,
          0.99f,
          false);
    }
  }
}

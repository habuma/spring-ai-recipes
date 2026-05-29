package com.example.mcpserver;

import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.ai.mcp.annotation.context.StructuredElicitResult;
import org.springframework.stereotype.Service;

@Service
public class ProductTools {

  private static final Logger logger = LoggerFactory.getLogger(ProductTools.class);

  private ProductService productService;

  public ProductTools(ProductService productService) {
    this.productService = productService;
  }

  @McpTool(name = "get-product-details",
           description = "Get details for a given product.")
  public ProductDetails getProductDetails(
      @McpToolParam(description = "The product sku") String sku,
      McpSyncRequestContext requestContext) {

    logger.info("Getting product details for sku {}", sku);

    var productDetails = productService.getProductDetails(sku);
    if (!productDetails.exclusive()) {
      logger.info("Returning product details for non-exclusive sku {}", sku);
      return productDetails;
    }

    logger.info("Product is exclusive...checking customer status.");

    if (requestContext.elicitEnabled()) {
      logger.info("Sending elicitation for customer info");
      StructuredElicitResult<CustomerInfo> elicitResult = requestContext.elicit(
          e -> e.message("isVip"),
          CustomerInfo.class
      );

      if (elicitResult.action() == McpSchema.ElicitResult.Action.ACCEPT) {
        CustomerInfo customerInfo = elicitResult.structuredContent();
        if (customerInfo.vip()) {
          logger.info("Customer is VIP. Returning product details for sku {}", sku);
          return productDetails;
        }
      }
    }

    logger.info("Customer is not VIP. Throwing exception.");
    throw new IllegalArgumentException("The product sku is invalid");
  }


}

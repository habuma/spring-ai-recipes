package com.example.streaminghttpmcpserver;

import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.context.MetaProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Service
public class McpApps {

  @Value("classpath:/app/weather.html")
  Resource appsHtmlResource;

  @McpResource(name = "Weather Resource",
      uri = "ui://weather/weather.html",
      mimeType = "text/html;profile=mcp-app",
      metaProvider = CspMetaProvider.class)
  public String getWeatherHtmlResource() throws IOException {
    return appsHtmlResource.getContentAsString(Charset.defaultCharset());
  }

  public static final class CspMetaProvider implements MetaProvider {
    @Override
    public Map<String, Object> getMeta() {
      return Map.of("ui",
          Map.of("csp",Map.of("resourceDomains",
              List.of("https://unpkg.com"))));
    }
  }

}

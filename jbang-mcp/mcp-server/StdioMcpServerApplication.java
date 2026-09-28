///usr/bin/env jbang --quiet "$0" "$@" --spring.config.location=file:///Users/habuma/Projects/spring-ai-recipes/jbang-mcp/mcp-server/ ; exit $?
//JAVA 25
//BOM org.springframework.boot:spring-boot-dependencies:4.1.1
//BOM org.springframework.ai:spring-ai-bom:2.0.1
//DEPS org.springframework.ai:spring-ai-starter-mcp-server:2.0.1
//DEPS com.fasterxml.jackson.core:jackson-annotations:2.21
//SOURCES WeatherTools.java
//SOURCES Weather.java

package com.example.stdiomcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StdioMcpServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(StdioMcpServerApplication.class, args);
  }

}

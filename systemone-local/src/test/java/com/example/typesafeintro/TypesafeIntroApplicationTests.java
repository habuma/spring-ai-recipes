package com.example.typesafeintro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class TypesafeIntroApplicationTests {

  // the real runner calls the TypeSafe service for every game at startup
  @MockitoBean(name = "go")
  ApplicationRunner runner;

  @Test
  void contextLoads() {
  }

}

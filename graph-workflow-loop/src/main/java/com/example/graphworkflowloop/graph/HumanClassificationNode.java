package com.example.graphworkflowloop.graph;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;

import java.util.Map;

public class HumanClassificationNode implements NodeAction {

  @Override
  public Map<String, Object> apply(OverAllState state) {
    return Map.of();
  }

}

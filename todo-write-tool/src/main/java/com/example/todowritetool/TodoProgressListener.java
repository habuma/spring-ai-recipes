package com.example.todowritetool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agent.tools.TodoWriteTool;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TodoProgressListener {

  private static final Logger logger = LoggerFactory.getLogger(TodoProgressListener.class);

  @EventListener
  public void onTodoUpdate(TodoUpdateEvent event) {
    var todos = event.getTodos();
    var completeCount = todos.stream().filter(todo -> todo.status().equals(TodoWriteTool.Todos.Status.completed)).count();

    // calculate percentage complete
    var percentageComplete = Math.round((completeCount * 100.0) / todos.size());

    logger.info("Event ({}/{} : {}%):", completeCount, event.getTodos().size(), percentageComplete);
    event.getTodos().forEach(todoItem -> {
      logger.info("   -- TODO Item: {} - {}", todoItem.status(), todoItem.content());
    });
  }
}
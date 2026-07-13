# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Safeguard Advisor** is a Spring Boot application demonstrating Spring AI's chat client with content filtering. It runs as an interactive REPL that answers questions about Disney's Encanto movie, using a SafeGuardAdvisor to filter responses containing sensitive words.

**Key Technologies:**
- Spring Boot 4.1.0 with Spring AI 2.0.0
- OpenAI ChatClient for LLM interactions
- Java 25

## Build and Development Commands

### Build
```bash
./gradlew build                    # Full build (compile + test)
./gradlew clean build              # Clean rebuild
```

### Run
```bash
./gradlew bootRun                  # Start the interactive chat application (reads from stdin)
```

### Tests
```bash
./gradlew test                     # Run all tests
./gradlew test --tests SafeguardAdvisorApplicationTests  # Run specific test class
```

### Other
```bash
./gradlew tasks                    # Show all available Gradle tasks
./gradlew dependencies             # Display dependency tree
```

## Architecture

### Core Components

**SafeguardAdvisorApplication** (`src/main/java/.../SafeguardAdvisorApplication.java`)
- Entry point and application runner for the REPL interface
- Creates an infinite loop reading user input from stdin and calling the ChatClient
- Passes each prompt through configured advisors with a conversation ID for memory
- Applies SaferGuardAdvisor to filter LLM responses for forbidden content
- Input validation: skips blank lines, exits on EOF

**ChatClientConfig** (`src/main/java/.../ChatClientConfig.java`)
- Configures the ChatClient with customizers applied in order
- **MessageChatMemoryAdvisor**: Maintains conversation history with a window of 500 messages
- **System Message**: Constrains responses to Encanto-related questions
- **SafeGuardAdvisor**: Filters user input for sensitive words (Bruno, vision, prophecy) — prevents questions about forbidden topics

**SaferGuardAdvisor** (`src/main/java/.../SaferGuardAdvisor.java`)
- Implements both `CallAdvisor` and `StreamAdvisor` interfaces for response filtering
- Checks LLM output for forbidden words after the ChatClient generates responses
- Replaces entire response with failure message if sensitive words are detected
- Builder pattern with configurable sensitive words, failure response, and order
- Can be configured as a default advisor in ChatClientConfig, just like SafeGuardAdvisor
- Uses reflection to work with the Spring AI ChatResponse API

### Request Flow

1. User enters text via stdin → SafeguardAdvisorApplication reads line
2. ChatClient invokes advisors in precedence order:
   - **Request phase:**
     - SafeGuardAdvisor: checks input for sensitive words (HIGHEST_PRECEDENCE)
     - MessageChatMemoryAdvisor: adds conversation history
     - System message: constrains LLM behavior
   - OpenAI ChatClient processes the request
   - **Response phase:**
     - SaferGuardAdvisor: checks response for sensitive words (LOWEST_PRECEDENCE)
     - If forbidden content found, replaces entire response with failure message
3. Final response is printed with " - " prefix

### Dependency Injection Notes

- Spring Boot auto-configures OpenAI ChatClient if `OPENAI_API_KEY` env var is set
- ChatClientBuilderCustomizer beans are automatically applied to the ChatClient.Builder
- Advisor order matters: HIGHEST_PRECEDENCE runs first

## Configuration

- **Max Chat Memory**: 500 messages (MessageWindowChatMemory in ChatClientConfig)
- **Sensitive Words**: "Bruno", "bruno", "BRUNO", "vision", "prophecy"
- **Failure Response**: "We don't talk about Bruno."

### Dual-Layer Content Filtering

The application uses two complementary advisors in ChatClientConfig:

1. **SafeGuardAdvisor (Request-side, HIGHEST_PRECEDENCE)**: Blocks user input containing sensitive words
2. **SaferGuardAdvisor (Response-side, LOWEST_PRECEDENCE)**: Filters responses that mention forbidden content

This catches forbidden topics in two places:
- Direct questions about forbidden topics (SafeGuardAdvisor blocks the input)
- Indirect questions that might cause LLM to mention forbidden topics (SaferGuardAdvisor blocks the output)

To modify sensitive words or responses, update the builder calls in:
- `addSafeguardAdvisor()` bean (request filtering)
- `addSaferGuardAdvisor()` bean (response filtering)

## Dependencies

Core dependencies from build.gradle:
- `spring-ai-starter-model-openai`: ChatClient + OpenAI integration
- `spring-boot-starter-test`: JUnit 5, Mockito, Spring Boot test utilities

## Testing Notes

The SafeGuardAdvisorApplicationTests contains only a context load test. Interactive behavior is manual. The application handles EOF gracefully in tests (checks `scanner.hasNextLine()` to avoid infinite loops).

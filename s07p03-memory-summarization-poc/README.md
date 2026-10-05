# s07p03-memory-summarization-poc

This proof-of-concept demonstrates how to implement an overflow-triggered chat memory summarization strategy using Spring AI.
When the conversation exceeds a certain number of turns, a secondary `ChatClient` call summarizes older messages,
compressing them into a single `SystemMessage` to save context window space.

## Requirements
- Overflow-triggered summarization using a secondary `ChatClient` call
- Custom `ChatMemory` wrapper compressing old turns into a system-message summary
- Compares Claude's vs. Gemini's summarization fidelity using Spring profiles

## Run
Use `SPRING_PROFILES_ACTIVE=claude` or `SPRING_PROFILES_ACTIVE=gemini` to run with different providers.

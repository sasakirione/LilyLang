# LilyLang Improvement Tasks

This document contains a comprehensive list of actionable improvement tasks for the LilyLang project. Tasks are organized by category and include both architectural and code-level improvements.

## Code Organisation and Architecture

[x] Refactor codebase to follow a clearer compiler architecture with separate phases:
   - Lexical analysis (tokenization)
   - Syntax analysis (parsing)
   - Semantic analysis
   - Code generation

[x] Create a dedicated Lexer class to handle tokenization instead of regex-based splitting

[x] Implement a proper Symbol Table for tracking variables and their scopes

[x] Extract bytecode generation logic from Main.kt into a dedicated CodeGenerator class

[x] Implement a proper error reporting system with line/column information

[x] Add a configuration system to allow customisation of compiler behaviour

[x] Create a proper CLI interface with command-line arguments parsing

## Language Features

[x] Add support for boolean data types and logical operations (AND, OR, NOT)

[x] Implement string data type and string operations

[x] Add control flow structures:
   - If/else statements
   - While loops
   - For loops

[x] Implement function declarations and calls

[x] Add support for user-defined data structures/classes

[ ] Implement proper list operations (add, remove, access elements)

[ ] Add type checking and type inference

[ ] Implement scoping rules for variables

[ ] Add support for importing code from other files

## Error Handling and Reporting

[ ] Implement comprehensive error messages with line and column information

[ ] Add error recovery to allow parsing to continue after errors

[ ] Implement warnings for potentially problematic code

[ ] Add validation for variable usage (undefined variables, type mismatches)

[ ] Create a dedicated ErrorReporter class to standardise error formatting

[ ] Add runtime error handling for division by zero and other runtime exceptions

## Testing and Quality Assurance

[ ] Add unit tests for code generation functionality

[ ] Create integration tests for end-to-end compilation and execution

[ ] Implement property-based testing for parser robustness

[ ] Add tests for error conditions and edge cases

[ ] Create a test suite with example programs showcasing language features

[ ] Implement code coverage reporting and set minimum coverage thresholds

[ ] Add performance benchmarks for compilation and execution

## Documentation

[ ] Create comprehensive language specification document

[ ] Add KDoc comments to all public functions and classes

[ ] Create a user guide with examples for language features

[ ] Add architecture documentation explaining compiler design

[ ] Create contributor guidelines for the project

[ ] Add inline comments explaining complex algorithms and design decisions

[ ] Create a README with quick start guide and project overview

## Build and Project Setup

[ ] Add Detekt for static code analysis

[ ] Configure Dokka for API documentation generation

[ ] Set up GitHub Actions for CI/CD

[ ] Add versioning strategy and release process

[ ] Configure Gradle for publishing artefacts

[ ] Add dependency management for handling version updates

[ ] Create Docker configuration for containerised builds

## Performance Improvements

[ ] Optimise parsing algorithm for better performance with large files

[ ] Implement bytecode optimization passes

[ ] Add caching for repeated expressions

[ ] Optimise memory usage during compilation

[ ] Implement parallel processing where applicable

## Modern Kotlin Features

[ ] Utilise coroutines for potential async operations

[ ] Implement extension functions for better code organisation

[ ] Use sealed interfaces where appropriate

[ ] Leverage Kotlin DSL for more expressive code

[ ] Utilize context receivers for compiler phases (Kotlin 1.6.20+)

[ ] Implement flow for streaming compilation results

## User Experience

[ ] Add REPL (Read-Eval-Print Loop) for interactive code execution

[ ] Implement syntax highlighting definitions for common editors

[ ] Create better error visualization in compiler output

[ ] Add progress reporting for long compilations

[ ] Implement auto-completion suggestions for language server protocol

[ ] Create a simple IDE plugin for IntelliJ IDEA

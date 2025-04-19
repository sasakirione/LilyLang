import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * Variable details for bytecode generation
 */
data class VariableDetail(val type: String, val index: Int, val name: String)

/* ==============================
 *  Main function for LilyLang compiler
 * ============================== */

fun main(args: Array<String>) {
    // Parse command-line arguments and create configuration
    val (config, sourceFile) = parseCommandLineArgs(args)
    
    // Create error reporter
    val errorReporter = ErrorReporter()
    
    // Read source code from a file or use sample if no file provided
    val sourceCode = if (sourceFile != null) {
        try {
            String(Files.readAllBytes(Paths.get(sourceFile)))
        } catch (e: Exception) {
            println("Error reading source file: ${e.message}")
            return
        }
    } else {
        // Sample small program for testing
        """
        var x = 10
        var y = 20
        var z = x + y - 10 + 100
        var exp1 = x * y
        var exp2 = 10 + x * y - 5
        var exp3 = 32 + x / y * z
        var exp4 = exp1 % 3
        z = 3
        print z
        print y + x
        print y - x
        """.trimIndent()
    }
    
    if (config.verbose) {
        println("Compiling ${sourceFile ?: "sample code"}...")
    }

    try {
        // Compiler phases
        // 1. Lexical Analysis: Convert source code to tokens
        if (config.verbose) println("Phase 1: Lexical Analysis")
        val lexer = Lexer(sourceCode, errorReporter)
        val tokens = lexer.tokenize()
        
        // Check for lexical errors
        if (errorReporter.hasErrors() && !config.continueOnError) {
            errorReporter.printErrors()
            return
        }

        // 2. Syntax Analysis: Parse tokens into an AST
        if (config.verbose) println("Phase 2: Syntax Analysis")
        val parser = Parser(tokens, errorReporter)
        val ast = parser.parseProgram()
        
        // Check for syntax errors
        if (errorReporter.hasErrors() && !config.continueOnError) {
            errorReporter.printErrors()
            return
        }

        // 3. Semantic Analysis: Check for semantic errors
        if (config.verbose) println("Phase 3: Semantic Analysis")
        val semanticAnalyzer = SemanticAnalyzer(errorReporter)
        val checkedAst = semanticAnalyzer.analyze(ast)
        
        // Check for semantic errors
        if (errorReporter.hasErrors() && !config.continueOnError) {
            errorReporter.printErrors()
            return
        }

        // 4. Code Generation: Generate bytecode from the AST
        if (config.verbose) println("Phase 4: Code Generation")
        val codeGenerator = CodeGenerator()
        
        // Determine the class name from a source file or use default
        val className = if (sourceFile != null) {
            File(sourceFile).nameWithoutExtension
        } else {
            config.outputFileName
        }
        
        val bytecode = codeGenerator.generate(checkedAst, className)

        // 5. Write bytecode to a .class file
        if (config.verbose) println("Writing bytecode to $className.class")
        FileOutputStream("$className.class").use { it.write(bytecode) }

        println("Compilation complete: $className.class has been generated.")
        println("Run with: java $className")
    } catch (e: CompilationException) {
        println(e.message)
    } catch (e: Exception) {
        println("Unexpected error: ${e.message}")
        if (config.debug) {
            e.printStackTrace()
        }
    }
}

/**
 * Parse command-line arguments and create a compiler configuration
 *
 * @param args Command-line arguments
 * @return Pair of compiler configuration and source file path (or null if not provided)
 */
private fun parseCommandLineArgs(args: Array<String>): Pair<CompilerConfig, String?> {
    val config = CompilerConfig.default()
    var sourceFile: String? = null
    
    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "-o", "--optimize" -> config.optimize = true
            "-d", "--debug" -> config.debug = true
            "-v", "--verbose" -> config.verbose = true
            "-c", "--continue-on-error" -> config.continueOnError = true
            "-h", "--help" -> {
                printUsage()
                exitProcess(0)
            }
            "--output" -> {
                if (i + 1 < args.size) {
                    config.outputFileName = args[i + 1]
                    i++
                } else {
                    println("Error: Missing argument for --output")
                    printUsage()
                    exitProcess(1)
                }
            }
            "--max-errors" -> {
                if (i + 1 < args.size) {
                    try {
                        config.maxErrors = args[i + 1].toInt()
                        i++
                    } catch (_: NumberFormatException) {
                        println("Error: Invalid argument for --max-errors")
                        printUsage()
                        exitProcess(1)
                    }
                } else {
                    println("Error: Missing argument for --max-errors")
                    printUsage()
                    exitProcess(1)
                }
            }
            "--dev" -> {
                // Preset for development
                val devConfig = CompilerConfig.development()
                config.debug = devConfig.debug
                config.verbose = devConfig.verbose
                config.continueOnError = devConfig.continueOnError
            }
            "--prod" -> {
                // Preset for production
                val prodConfig = CompilerConfig.production()
                config.optimize = prodConfig.optimize
                config.debug = prodConfig.debug
                config.verbose = prodConfig.verbose
                config.continueOnError = prodConfig.continueOnError
            }
            else -> {
                if (!args[i].startsWith("-")) {
                    sourceFile = args[i]
                } else {
                    println("Unknown option: ${args[i]}")
                    printUsage()
                    exitProcess(1)
                }
            }
        }
        i++
    }
    
    return Pair(config, sourceFile)
}

/**
 * Print usage information
 */
private fun printUsage() {
    println("Usage: lilylang [options] [source_file]")
    println("Options:")
    println("  -o, --optimize           Enable bytecode optimization")
    println("  -d, --debug              Generate debug information")
    println("  -v, --verbose            Print verbose output during compilation")
    println("  -c, --continue-on-error  Continue compilation after errors")
    println("  --output <name>          Set output file name (without extension)")
    println("  --max-errors <number>    Maximum number of errors to report")
    println("  --dev                    Use development configuration preset")
    println("  --prod                   Use production configuration preset")
    println("  -h, --help               Print this help message")
}
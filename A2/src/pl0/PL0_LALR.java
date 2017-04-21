package pl0;
import java.io.IOException;
import java.io.PrintStream;

import java_cup.runtime.*;

import parser.CUPParser;
import source.ErrorHandler;
import source.Errors;
import source.Source;
import tree.CodeGenerator;
import tree.DeclNode;
import tree.Procedures;
import tree.StaticChecker;
import machine.StackMachine;

/** 
 * class PL0_LALR - PL0 Compiler with JavaCUP generated parser.
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $ 
 * Parses the command line arguments, and then compiles and/or executes the
 * code.
 */
public class PL0_LALR {

    /** Output stream for compiler */
    public static PrintStream outStream = System.out;
    /** Print usage information */
    public static void usage() {
        outStream.println(
            "PL0 Compiler\n" +
            "Usage: java pl0.PL0_LALR [-cdhptv] <filename>\n"+
            "  -d  =  debug messages on\n" +
            "  -p  =  parse only (no static check or code gen)\n" +
            "  -c  =  parse and static check only (no code gen)\n" +
            "  -v  =  verbose output of generated code\n" +
            "  -t  =  trace execution of resulting code\n" +
            "  -h  =  output this usage information\n" +
            " <filename> is compiled, and if no errors the generated code" +
            " is executed unless -c or -p is given." );
    }
    public static String SourceSuffix = ".pl0";

    /** PL0 main procedure */
    public static void main( String args[] ) throws java.lang.Exception {
        /** Name of the input source program file. */
        String srcFile = null;
        /** Error handler for reporting error messages */
        Errors errors;
        /** Input source stream */
        Source src;
        /** Generated code for the procedures */
        Procedures code = null;
        /** Perform a static check */
        boolean staticCheck = true;
        /** Generate code after a successful parse and static check */
        boolean genCode = true;
        /** Detailed trace of execution */
        boolean tracing = false;
        /** Verbose output of code generation */
        boolean verbose = false;
        /** Debug mode for parser - quite verbose */
        boolean debug = false;

        /* Parse command line */
        for( String arg : args ) {
            if( arg.charAt(0) == '-' ) { /* Option */
                switch( arg.charAt(1) ) {
                case 'd': /* Debug parse */
                    debug = true;
                    break;
                case 'p': /* Parse only */
                    staticCheck = false;
                    genCode = false;
                    break;
                case 'c': /* Parse and static check only */
                    genCode = false;
                    break;
                case 'v': /* Verbose output (of generated code) */
                    verbose = true;
                    break;
                case 't': /* Trace program at runtime. */
                    tracing = true;
                    break;
                case 'h': /* Help */
                default:
                    usage();
                    System.exit(0);
                    break;
                }
            } else { /* ( arg.charAt(0) != '-' ) Not Option */
                srcFile = arg;
            }
        }
        try {
            /* Set up the input source stream for the source file */
            if( srcFile == null ) {
                outStream.println( "No source file specified." );
                System.exit( 1 );
            }
            src = new Source( srcFile );
            /* Set up the error handler reference */
            errors = new ErrorHandler( outStream, src, debug );
            /* Compile the program */
            code = compile( src, errors, verbose, staticCheck, genCode, debug );
            if( code != null ) { /* run it if possible */
                StackMachine machine;
                machine = new StackMachine( errors, outStream, verbose, code );
                if( genCode ) {
                    outStream.println( "Running ..." );
                    machine.setTracing( tracing ? StackMachine.TRACE_ALL 
                                     : StackMachine.TRACE_NONE );
                    machine.run();
                }
            }
        } catch( IOException e ) {
            System.out.println( "Got IOException: " + e + "... Aborting" );
            System.exit(1);
        }
    }

    /** Compile the program
     * 
     * @param src program source
     * @param errors handler for errors
     * @param verbose generate more messages during compilation
     * @param staticCheck do the static checking
     * @param genCode perform the code generation
     * @param debugParse debugging messages during parsing 
     * @return generated code for procedures
     */
    private static Procedures compile( Source src, Errors errors,
            boolean verbose, boolean staticCheck, boolean genCode, 
            boolean debugParse ) 
        throws IOException, Exception
    {
        /** Abstract syntax tree returned by parser */
        DeclNode.ProgramNode tree = null;
        /** Generated code for procedures */
        Procedures code = null;
        /** Abstract syntax tree returned by parser. Really of type 
         * StatementNode.ProgramNode but the parser generator doesn't know that. */
        Object parseResult; 
        
        outStream.println( "Compiling " + src.getFileName() );
        try {
            ComplexSymbolFactory csf = new ComplexSymbolFactory();
            /* Set up the lexical analyzer using the source program stream */
            parser.Lexer lex = new parser.Lexer(src, csf);
            /** Generated parser.
             * Set up the parser with the lexical analyzer. */
            CUPParser parser = new CUPParser(lex, csf );
            if( debugParse ) {
                parseResult = parser.debug_parse().value;
            } else {
                parseResult = parser.parse().value;
            }
            /* Flush any error messages from the parse */
            errors.flush();
            outStream.println( "Parsing complete" );
            if( staticCheck && parseResult instanceof DeclNode.ProgramNode ) {
                tree = (DeclNode.ProgramNode)parseResult;
                /* Perform the static semantics analysis */
                StaticChecker staticSemantics = 
                    new StaticChecker( ErrorHandler.getErrorHandler() );
                staticSemantics.visitProgramNode( tree );           
                /* Don't generate any code if there are any errors. */
                if( ErrorHandler.getErrorHandler().hadErrors() ) {
                    /* Skip code generation if there were errors */
                    tree = null;
                }
                errors.flush();
                outStream.println( "Static semantic analysis complete" );
            }
        } catch (IOException e) {
            System.out.println( "Exception: " + e + "... Aborting" );
            System.exit(1);
        }
        if( tree != null && genCode ) {
            /* Generate the stack machine code */
            CodeGenerator codeGen = new CodeGenerator( errors );
            code = codeGen.generateCode( tree );
            outStream.println( "Code generation complete" );
        }
        errors.flush();
        errors.errorSummary();
        return code;
    }
}

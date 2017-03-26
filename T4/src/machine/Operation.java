package machine;

/**
 * enumeration Operation - Operation codes for the instructions
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 */

public enum Operation {
    NO_OP(),           /* Do nothing */
    BR,                /* Unconditional branch */
    BR_FALSE(),        /* Branch if false (0) */
    COPY(),            /* Copy block of memory */
    /* Procedure call and return */
    CALL(),            /* Call */
    RETURN(),          /* Exit stack frame and return */
    /* Stack manipulation */
    ALLOC_STACK(),     /* Allocate space on stack frame */
    DEALLOC_STACK(),   /* Deallocate space from stack */
    POP(),             /* Discard the top of stack */
    DUP(),             /* Duplicate top of stack */
    SWAP(),            /* Swap top two stack words */
    /* Binary operators */
    ADD(),             /* Add */
    MPY(),             /* Multiply */
    DIV(),             /* Divide */
    OR(),              /* Bitwise or */ 
    AND(),             /* Bitwise and */
    XOR(),             /* Bitwise xor */
    EQUAL(),           /* Test for equality */
    LESS(),            /* Test for less-than - swap arguments for GREATER */
    LESSEQ(),          /* Test for less-than-or-equal - swap args for GEQ */
    /* Unary operators */
    NOT(),             /* Bitwise complement */
    NEGATE(),          /* Two's complement negation */
    /* Read and write - too high level to really be instructions */
    READ(),            /* Read word from stdin */
    WRITE(),           /* Write word to stdout */
    
    BOUND(),           /* Bounds check */
    /* Instructions for handling references to non-local variables */
    TO_GLOBAL(),       /* Covert local address on top of stack to global */
    TO_LOCAL(),        /* Covert global address on top of stack to local*/
    /* Load and store instructions */
    LOAD_CON( 2 ),     /* Load next word as constant */
    LOAD_ABS(),        /* Load from address in top of stack */
    STORE_FRAME(),     /* Store word in memory */
    LOAD_FRAME(),      /* Load word from memory frame relative */
    /* Load constants 0 and 1 */
    ZERO(),            /* Load zero */
    ONE(),             /* Load one */
    /* Allocating space on the heap */
    ALLOC_HEAP(),      /* Allocate memory from heap */
    /* Loading multiple words onto the stack */
    LOAD_MULTI(),      /* Load multiple words onto stack */
    STORE_MULTI(),     /* Store multiple words from stack */
    STOP();            /* Halt execution */

    /* Size of the instruction - LOAD_CON of size 2 */
    private int size;
    
    public int getSize() {
        return size;
    }
    /* Constructor for operation with explicit size */
    Operation( int size ) {
        this.size = size;
    }
    /* Constructor with default instruction size of 1 */
    Operation() {
        this( 1 );
    }
}

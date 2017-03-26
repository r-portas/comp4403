package machine;

import syms.SymEntry;

/**
 * class Instruction - represents an instruction in generated code
 * @version $Revision: 22 $  $Date: 2014-05-20 15:14:36 +1000 (Tue, 20 May 2014) $
 */
public class Instruction {
    protected Operation op;
    
    public Instruction( Operation op ) {
        this.op = op;
    }
    public Operation getOp() {
        return op;
    }
    public void loadInstruction( StackMachine machine ) {
        machine.generateWord(op.ordinal(), op.toString() );
    }
    
    /** LOAD_CON is the only instruction with a parameter */
    public static class LoadConInstruction extends Instruction {
        protected int value;
        
        public LoadConInstruction( int value ) {
            super( Operation.LOAD_CON );
            this.value = value;
        }
        public int getValue() {
            return value;
        }
        @Override
        public void loadInstruction( StackMachine machine ) {
            super.loadInstruction(machine);
            machine.generateWord( value, "" );
        }
    }
    
    /** The addresses of procedures are resolved when the program is
     * loaded and the sizes of procedures are known.
     * The LOAD_CON is for the procedure address (eventually) and
     * hence this class extends LoadConInstruction.
     */
    public static class ProcRefInstruction extends LoadConInstruction {
        private SymEntry.ProcedureEntry proc;
        
        public ProcRefInstruction( SymEntry.ProcedureEntry proc ) {
            super( StackMachine.NULL_ADDR );
            this.proc = proc;
        }
        public SymEntry.ProcedureEntry getProc() {
            return proc;
        }
        @Override
        public void loadInstruction( StackMachine machine ) {
            value = proc.getStart();
            super.loadInstruction( machine );
        }
    }
}

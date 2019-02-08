package chocopy.common.codegen;

import java.io.OutputStream;
import java.io.PrintWriter;

public class RiscVBackend {

    protected final PrintWriter out;

    /** The word size in bytes for RISC-V 32-bit */
    protected static final int WORD_SIZE = 4;

    /** A RISC-V register. */
    public enum Register {

        A0("a0"), A1("a1"), A2("a2"), A3("a3"), A4("a4"), A5("a5"), A6("a6"), A7("a7"),
        T0("t0"), T1("t1"), T2("t2"), T3("t3"), T4("t4"), T5("t5"), T6("t6"),
        S1("s1"), S2("s2"), S3("s3"), S4("s4"), S5("s5"),
        S6("s6"), S7("s7"), S8("s8"), S9("s9"), S10("s10"), S11("s11"),
        FP("fp"), SP("sp"), GP("gp"), RA("ra"), ZERO("zero");

        /** The name of the register used in assembly. */
        protected final String name;

        Register(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

    /**
     * Creates a new backend with a given outputstream
     * where the generated code will be emitted.
     *
     * @param out the output stream
     */
    public RiscVBackend(OutputStream out) {
        this.out = new PrintWriter(out, true);
    }

    /**
     * Returns the word size in bytes.
     *
     * This method is used instead of directly accessing the
     * static field {@link #WORD_SIZE}, so that this class
     * may be extended with alternate word sizes.
     *
     * @return the word size in bytes
     */
    public int getWordSize() {
        return WORD_SIZE;
    }

    /**
     * Emits a line of text to the output stream.
     *
     * @param str the string to output (without trailing newline)
     */
    protected void emit(String str) {
        out.println(str);
    }

    /**
     * Emits an instruction or directive along with a comment.
     *
     * @param insn the assembly instruction or directive
     * @param comment a single-line comment, possibly `null`
     */
    public void emitInsn(String insn, String comment) {
        if (comment != null) {
            emit(String.format("  %-40s # %s", insn, comment));
        } else {
            emitInsn(insn);
        }
    }

    /**
     * Emits an instruction or directive without a comment.
     *
     * @param insn the assembly instruction or directive
     */
    protected void emitInsn(String insn) {
        emit(String.format("  %s", insn)); // No comment
    }

    /**
     * Emits a local label marker.
     *
     * Invoke only once per unique label.
     *
     * @param label the label to emit
     * @param comment a single-line comment, possibly `null`
     */
    public void emitLocalLabel(Label label, String comment) {
        emitInsn(label+":", comment);
    }

    /**
     * Emits a global label marker.
     *
     * Invoke only once per unique label.
     *
     * @param label the label to emit
     */
    public void emitGlobalLabel(Label label) {
        emit(String.format("\n.globl %s", label));
        emit(String.format("%s:", label));
    }

    /**
     * Emits a data word as a literal integer
     *
     * @param value the word value
     * @param comment optional comment (may be `null`)
     */
    public void emitWordLiteral(Integer value, String comment) {
        emitInsn(String.format(".word %s", value), comment);
    }

    /**
     * Emits a data word as an address to a label
     *
     * @param addr the word value (may be `null`)
     * @param comment optional comment (may be `null`)
     */
    public void emitWordAddress(Label addr, String comment) {
        if (addr == null) {
            emitWordLiteral(0, comment);
        } else {
            emitInsn(String.format(".word %s", addr), comment);
        }
    }


    /**
     * Emits an ASCII null-terminated string.
     *
     * @param value the string to emit
     * @param comment optional comment (may be `null`)
     */
    public void emitString(String value, String comment) {
        String quoted = value
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");
        emitInsn(String.format(".string \"%s\"", quoted), comment);
    }

    /**
     * Marks the start of a data section.
     */
    public void startData() {
        emit("\n.data");
    }

    /**
     * Marks the start of a code/text section.
     */
    public void startCode() {
        emit("\n.text");
    }

    /**
     * Aligns the next instruction/word in memory to
     * a power-of-two number of bytes.
     *
     * @param pow the power of two to align to
     */
    public void alignNext(int pow) {
        emitInsn(String.format(".align %d", pow));
    }

    /**
     * Emits an ecall instruction
     *
     * @param comment optional comment (may be `null`)
     */
    public void emitEcall(String comment) {
        emitInsn("ecall", comment);
    }

    /**
     * Emits a load-address instruction
     *
     * @param rd the dest register
     * @param label the address to load
     * @param comment optional comment (may be `null`)
     */
    public void emitLA(Register rd, Label label, String comment) {
        emitInsn(String.format("la %s, %s", rd, label), comment);
    }

    /**
     * Emits a load-immediate instruction
     *
     * @param rd the dest register (lower 12 bits are set)
     * @param imm the immediate value (must be in range [-2048, 2047])
     * @param comment optional comment (may be `null`)
     */
    public void emitLI(Register rd, Integer imm, String comment) {
        emitInsn(String.format("li %s, %d", rd, imm), comment);
    }

    /**
     * Emits a load-upper-immediate instruction
     *
     * @param rd the dest register (higher 20 bits are set)
     * @param imm the immediate value (must be in range [0, 1048575])
     * @param comment optional comment (may be `null`)
     */
    public void emitLUI(Register rd, Integer imm, String comment) {
        emitInsn(String.format("lui %s, %d", rd, imm), comment);
    }

    /**
     * Emits a move instruction
     *
     * @param rd the dest register
     * @param rs the src register
     * @param comment optional comment (may be `null`)
     */
    public void emitMV(Register rd, Register rs, String comment) {
        emitInsn(String.format("mv %s, %s", rd, rs), comment);
    }

    /**
     * Emits a jump-register (computed jump) instruction
     *
     * @param rs the src register
     * @param comment optional comment (may be `null`)
     */
    public void emitJR(Register rs, String comment) {
        emitInsn(String.format("jr %s", rs), comment);
    }

    /**
     * Emits a jump (unconditional jump) instruction
     *
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitJ(Label label, String comment) {
        emitInsn(String.format("j %s", label), comment);
    }


    /**
     * Emits a jump-and-link instruction
     *
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitJAL(Label label, String comment) {
        emitInsn(String.format("jal %s", label), comment);
    }

    /**
     * Emits a computed-jump-and-link instruction
     *
     * @param rs the register containing jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitJALR(Register rs, String comment) {
        emitInsn(String.format("jalr %s", rs), comment);
    }

    /**
     * Emits an add-immediate instruction
     *
     * @param rd the dest register
     * @param rs the first src register
     * @param imm the immediate value
     * @param comment optional comment (may be `null`)
     */
    public void emitADDI(Register rd, Register rs, Integer imm, String comment) {
        emitInsn(String.format("addi %s, %s, %d", rd, rs, imm), comment);
    }

    /**
     * Emits an add instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitADD(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("add %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits a subtract instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitSUB(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("sub %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits a multiply instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitMUL(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("mul %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits a divide instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitDIV(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("div %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits a modulo instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitREM(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("rem %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits an xor instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitXOR(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("xor %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits an xor-immediate instruction
     *
     * @param rd the dest register
     * @param rs the first src register
     * @param imm the immediate value
     * @param comment optional comment (may be `null`)
     */
    public void emitXORI(Register rd, Register rs, Integer imm, String comment) {
        emitInsn(String.format("xori %s, %s, %d", rd, rs, imm), comment);
    }

    /**
     * Emits an and instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitAND(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("and %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits an andi-immediate instruction
     *
     * @param rd the dest register
     * @param rs the first src register
     * @param imm the immediate value
     * @param comment optional comment (may be `null`)
     */
    public void emitANDI(Register rd, Register rs, Integer imm, String comment) {
        emitInsn(String.format("andi %s, %s, %d", rd, rs, imm), comment);
    }

    /**
     * Emits an or instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitOR(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("or %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits an or-immediate instruction
     *
     * @param rd the dest register
     * @param rs the first src register
     * @param imm the immediate value
     * @param comment optional comment (may be `null`)
     */
    public void emitORI(Register rd, Register rs, Integer imm, String comment) {
        emitInsn(String.format("ori %s, %s, %d", rd, rs, imm), comment);
    }

    /**
     * Emits a load-word instruction
     *
     * @param rd the register where the value will be loaded
     * @param rs the register containing the memory address
     * @param imm the offset from `rs`
     * @param comment optional comment (may be `null`)
     */
    public void emitLW(Register rd, Register rs, Integer imm, String comment) {
        emitInsn(String.format("lw %s, %d(%s)", rd, imm, rs), comment);
    }

    /**
     * Emits a store-word instruction
     *
     * @param rs2 the register containing the value to store
     * @param rs1 the register containing the memory address
     * @param imm the offset from `rs1`
     * @param comment optional comment (may be `null`)
     */
    public void emitSW(Register rs2, Register rs1, Integer imm, String comment) {
        emitInsn(String.format("sw %s, %d(%s)", rs2, imm, rs1), comment);
    }

    /**
     * Emits a load-word instruction for globals
     *
     * @param rd the register where the value will be loaded
     * @param label the address of the global
     * @param comment optional comment (may be `null`)
     */
    public void emitLW(Register rd, Label label, String comment) {
        emitInsn(String.format("lw %s, %s", rd, label), comment);
    }

    /**
     * Emits a store-word instruction for globals
     *
     * @param rs the register containing the value to store
     * @param label the address of the global
     * @param tmp a temporary register used to hold the address
     * @param comment optional comment (may be `null`)
     */
    public void emitSW(Register rs, Label label, Register tmp, String comment) {
        emitInsn(String.format("sw %s, %s, %s", rs, label, tmp), comment);
    }

    /**
     * Emits a load-byte instruction
     *
     * @param rd the register where the value will be loaded
     * @param rs the register containing the memory address
     * @param imm the offset from `rs`
     * @param comment optional comment (may be `null`)
     */
    public void emitLB(Register rd, Register rs, Integer imm, String comment) {
        emitInsn(String.format("lb %s, %d(%s)", rd, imm, rs), comment);
    }

    /**
     * Emits a load-byte-unsigned instruction
     *
     * @param rd the register where the value will be loaded
     * @param rs the register containing the memory address
     * @param imm the offset from `rs`
     * @param comment optional comment (may be `null`)
     */
    public void emitLBU(Register rd, Register rs, Integer imm, String comment) {
        emitInsn(String.format("lbu %s, %d(%s)", rd, imm, rs), comment);
    }

    /**
     * Emits a store-byte instruction
     *
     * @param rs2 the register containing the value to store
     * @param rs1 the register containing the memory address
     * @param imm the offset from `rs1`
     * @param comment optional comment (may be `null`)
     */
    public void emitSB(Register rs2, Register rs1, Integer imm, String comment) {
        emitInsn(String.format("sb %s, %d(%s)", rs2, imm, rs1), comment);
    }

    /**
     * Emits a branch-if-equal instruction
     *
     * @param rs1 the left operand
     * @param rs2 the right operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBEQ(Register rs1, Register rs2, Label label, String comment) {
        emitInsn(String.format("beq %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emits a branch-if-unequal instruction
     *
     * @param rs1 the left operand
     * @param rs2 the right operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBNE(Register rs1, Register rs2, Label label, String comment) {
        emitInsn(String.format("bne %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emits a branch-if-greater-or-equal (unsigned) instruction
     *
     * @param rs1 the left operand
     * @param rs2 the right operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBGEU(Register rs1, Register rs2, Label label, String comment) {
        emitInsn(String.format("bgeu %s, %s, %s", rs1, rs2, label), comment);
    }

    /**
     * Emits a branch-if-zero instruction
     *
     * @param rs the operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBEQZ(Register rs, Label label, String comment) {
        emitInsn(String.format("beqz %s, %s", rs, label), comment);
    }

    /**
     * Emits a branch-if-not-zero instruction
     *
     * @param rs the operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBNEZ(Register rs, Label label, String comment) {
        emitInsn(String.format("bnez %s, %s", rs, label), comment);
    }

    /**
     * Emits a branch-if-less-than-zero instruction
     *
     * @param rs the operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBLTZ(Register rs, Label label, String comment) {
        emitInsn(String.format("bltz %s, %s", rs, label), comment);
    }

    /**
     * Emits a branch-if-greater-than-zero instruction
     *
     * @param rs the operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBGTZ(Register rs, Label label, String comment) {
        emitInsn(String.format("bgtz %s, %s", rs, label), comment);
    }

    /**
     * Emits a branch-if-less-than-equal-to-zero instruction
     *
     * @param rs the operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBLEZ(Register rs, Label label, String comment) {
        emitInsn(String.format("blez %s, %s", rs, label), comment);
    }

    /**
     * Emits a branch-if-greater-than-equal-to-zero instruction
     *
     * @param rs the operand
     * @param label the jump destination
     * @param comment optional comment (may be `null`)
     */
    public void emitBGEZ(Register rs, Label label, String comment) {
        emitInsn(String.format("bgez %s, %s", rs, label), comment);
    }

    /**
     * Emits a set-less-than instruction
     *
     * @param rd the dest register
     * @param rs1 the first src register
     * @param rs1 the second src register
     * @param comment optional comment (may be `null`)
     */
    public void emitSLT(Register rd, Register rs1, Register rs2, String comment) {
        emitInsn(String.format("slt %s, %s, %s", rd, rs1, rs2), comment);
    }

    /**
     * Emits a set-if-zero instruction
     *
     * @param rd the dest register
     * @param rs the src register
     * @param comment optional comment (may be `null`)
     */
    public void emitSEQZ(Register rd, Register rs, String comment) {
        // Note: Can also be done as `slt rd, rs2, rs1; xori rd, rd, 1`
        emitInsn(String.format("seqz %s, %s", rd, rs), comment);
    }

    /**
     * Emits a set-if-not-zero instruction
     *
     * @param rd the dest register
     * @param rs the src register
     * @param comment optional comment (may be `null`)
     */
    public void emitSNEZ(Register rd, Register rs, String comment) {
        // Note: Can also be done as `slt rd, rs2, rs1; xori rd, rd, 1`
        emitInsn(String.format("snez %s, %s", rd, rs), comment);
    }

}

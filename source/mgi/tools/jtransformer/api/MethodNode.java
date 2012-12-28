package mgi.tools.jtransformer.api;

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jtransformer.Constants;
import mgi.tools.jtransformer.api.code.CodeNode;
import mgi.tools.jtransformer.api.code.generation.CodeGenerator;
import mgi.tools.jtransformer.api.code.optimization.CodeOptimizer;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.MethodVisitor;

public class MethodNode extends MethodVisitor {
	
	/**
	 * Parent (class) of this node.
	 */
	private ClassNode parent;
	/**
	 * Accessor of this method.
	 */
	private int accessor;
	/**
	 * Name of this method.
	 */
	private String name;
	/**
	 * Descriptor of this method.
	 */
	private String descriptor;
	/**
	 * Generic signature of this method.
	 */
	private String signature;
	/**
	 * Contains exceptions that this method
	 * throws.
	 */
	private String[] exceptions;
	
	/**
	 * Contains method code if it has body or 
	 * at least debugging information about it's arguments.
	 */
	private CodeNode code;
	
	/**
	 * Code generator, get's called by 
	 * calling visitCode() and outputs it's code when visitMaxs() is
	 * called to our CodeNode.
	 */
	private CodeGenerator generator;
	
	/**
	 * Contains unknown method attributes.
	 */
	private List<Attribute> unknownAttributes = new ArrayList<Attribute>();
	
	public MethodNode(int accessor,String name,String descriptor,String signature, String[] exceptions,ClassNode parent) {
		super(Constants.ASM_API_VERSION);
		this.accessor = accessor;
		this.name = name;
		this.descriptor = descriptor;
		this.signature = signature;
		this.exceptions = exceptions;
		this.parent = parent;
	}

	@Override
	public void visitCode() {
		if (generator != null)
			throw new RuntimeException("Unfinished generation!");
		if (code == null)
			code = new CodeNode();
		generator = new CodeGenerator(this);
		mv = generator; // setup calls forwarding
		super.visitCode(); // forward call to generator
	}

	
	@Override
	public void visitMaxs(int maxs, int maxl) {
		if (generator == null)
			throw new RuntimeException("Not generating code!");
		super.visitMaxs(maxs, maxl); // forward call to generator
		int dumpsStart = generator.getStackDumpsStartAddress();
		mv = generator = null;
		if ((parent.getOptions() & ClassNode.OPT_OPTIMIZATION_SKIP) == 0) {
			CodeOptimizer optimizer = new CodeOptimizer(this, code, dumpsStart);
			optimizer.optimize();
		}
	}
	
	
	
	/**
	 * Strips any debugging information.
	 */
	public void stripDebugInformation() {
		if (code != null)
			code.stripDebugInformation();
	}
	
	/**
	 * Forward's information about this method contents to given
	 * visitor.
	 */
	public void accept(MethodVisitor visitor) {
		for (Attribute attribute : unknownAttributes)
			visitor.visitAttribute(attribute);
		if (code != null)
			code.accept(visitor);
		visitor.visitEnd();
	}


	
	public void setParent(ClassNode parent) {
		this.parent = parent;
	}

	public ClassNode getParent() {
		return parent;
	}

	public void setAccessor(int accessor) {
		this.accessor = accessor;
	}

	public int getAccessor() {
		return accessor;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setDescriptor(String descriptor) {
		this.descriptor = descriptor;
	}

	public String getDescriptor() {
		return descriptor;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getSignature() {
		return signature;
	}

	public void setExceptions(String[] exceptions) {
		this.exceptions = exceptions;
	}

	public String[] getExceptions() {
		return exceptions;
	}

	public List<Attribute> getUnknownAttributes() {
		return unknownAttributes;
	}

	public void setCode(CodeNode code) {
		this.code = code;
	}

	public CodeNode getCode() {
		return code;
	}

	@Override
	public String toString() {
		return "MethodNode[" + name + "," + descriptor + "," + signature + "]";
	}
	
	
	
	
}

package mgi.tools.jtransformer.api;

import java.util.ArrayList;
import java.util.List;

import mgi.tools.jtransformer.Constants;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

public class ClassNode extends ClassVisitor {

	public static final int OPT_GENERATION_SKIP_DEBUG_INFORMATION = 0x1;
	public static final int OPT_GENERATION_PRINT_STACK_DUMPS = 0x2;
	public static final int OPT_OPTIMIZATION_SKIP = 0x4;
	public static final int OPT_OPTIMIZATION_SKIP_NONSYNTH_LOCALS = 0x8;
	
	/**
	 * Contains options of this node.
	 */
	private int options;
	/**
	 * Contains version of this class.
	 */
	private int version;
	/**
	 * Contains access flag of this class.
	 */
	private int accessor;
	/**
	 * Contains name of this class.
	 */
	private String name;
	/**
	 * Contains generics signature of this 
	 * class.
	 */
	private String signature;
	/**
	 * Contains name of superclass or null if 
	 * this class is java/lang/Object
	 */
	private String superClass;
	/**
	 * Contains names of all interfaces that 
	 * this class implements.
	 */
	private String[] interfaces;
	
	/**
	 * All fields that this class node contains.
	 */
	private List<FieldNode> fields = new ArrayList<FieldNode>();
	/**
	 * All methods that this class node contains.
	 */
	private List<MethodNode> methods = new ArrayList<MethodNode>();

	
	// DEBUG INFORMATION
	/**
	 * Contains all inner classes information.
	 */
	private List<InnerClassInformation> innerClasses = new ArrayList<InnerClassInformation>();
	/**
	 * Contains outer class information.
	 */
	private OuterClassInformation outerClass;
	/**
	 * Contains information about source file.
	 */
	private SourceInformation source;
	// ------------------
	
	/**
	 * Contains list of attributes that aren't 
	 * understood.
	 */
	private List<Attribute> unknownAttributes = new ArrayList<Attribute>();
	

	
	
	/**
	 * Create's new class using given data.
	 */
	public ClassNode(int version, int accessor, String name, String signature, String superClass, String[] interfaces, int options) {
		super(Constants.ASM_API_VERSION);
		this.options = options;
		this.version = version;
		this.accessor = accessor;
		this.name = name;
		this.signature = signature;
		this.superClass = superClass;
		this.interfaces = interfaces;
	}
	
	/**
	 * Initialize's class from given buffer.
	 */
	public ClassNode(byte[] buffer,int offset,int length,int options) throws RuntimeException {
		super(Constants.ASM_API_VERSION);
		this.options = options;
		try {
			ClassReader reader = new ClassReader(buffer,offset,length);
			reader.accept(this, ClassReader.SKIP_FRAMES | ((options & OPT_GENERATION_SKIP_DEBUG_INFORMATION) != 0 ? ClassReader.SKIP_DEBUG : 0));
		}
		catch (Throwable t) {
			if (Constants.CORE_DEBUG)
				t.printStackTrace();
			throw new RuntimeException("Invalid data provided.");
		}
	}
	
	
	@Override
	public void visit(int version, int accessor, String name, String signature, String superClass, String[] interfaces) {
		this.version = version;
		this.accessor = accessor;
		this.name = name;
		this.signature = signature;
		this.superClass = superClass;
		this.interfaces = interfaces != null ? interfaces : new String[0];
	}
	
	@Override
	public void visitSource(String sourceFile, String sourceDebug) {
		setSource(new SourceInformation(sourceFile, sourceDebug));
	}
	
	@Override
	public void visitOuterClass(String owner, String name, String descriptor) {
		setOuterClass(new OuterClassInformation(owner,name,descriptor));
	}
	
	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int accessor) {
		innerClasses.add(new InnerClassInformation(accessor, name,innerName, outerName));
	}
	
	@Override
	public void visitAttribute(Attribute attribute) {
		unknownAttributes.add(attribute);
	}
	
	@Override
	public FieldVisitor visitField(int accessor, String name, String descriptor, String signature, Object value) {
		FieldNode field = new FieldNode(accessor, name, descriptor, signature, value, this);
		fields.add(field);
		return field;
	}
	
	@Override
	public MethodVisitor visitMethod(int accessor, String name, String descriptor, String signature, String[] exceptions) {
		MethodNode method = new MethodNode(accessor, name, descriptor, signature, exceptions != null ? exceptions : new String[0], this);
		methods.add(method);
		return method;
	}
	
	@Override
	public void visitEnd() {
	}
	
	
	/**
	 * Strip's all debugging information inside this class.
	 */
	public void stripDebugInformation() {
		this.innerClasses.clear();
		this.outerClass = null;
		this.source = null;
		
		for (FieldNode field : fields)
			field.stripDebugInformation();
		for (MethodNode method : methods)
			method.stripDebugInformation();
	}
	
	/**
	 * Forward's information about this class contents to given
	 * visitor.
	 */
	public void accept(ClassVisitor visitor) {
		visitor.visit(version, accessor, name, signature, superClass, interfaces.length != 0 ? interfaces : null);
		if (source != null)
			visitor.visitSource(source.sourceFile, source.sourceDebug);
		if (outerClass != null)
			visitor.visitOuterClass(outerClass.owner, outerClass.name, outerClass.descriptor);
		for (Attribute attribute : unknownAttributes)
			visitor.visitAttribute(attribute);
		for (InnerClassInformation inner : innerClasses)
			visitor.visitInnerClass(inner.name, inner.outerName, inner.innerName, inner.accessor);
		for (FieldNode field : fields) {
			FieldVisitor fv = visitor.visitField(field.getAccessor(), field.getName(), field.getDescriptor(), field.getSignature(), field.getValue());
			if (fv != null)
				field.accept(fv);
		}
		for (MethodNode method : methods) {
			MethodVisitor mv = visitor.visitMethod(method.getAccessor(), method.getName(), method.getDescriptor(), method.getSignature(), method.getExceptions().length != 0 ? method.getExceptions() : null);
			if (mv != null)
				method.accept(mv);
		}
		visitor.visitEnd();
	}
	
	
	/**
	 * Build's this class.
	 */
	public byte[] toByteArray() throws RuntimeException {
		try {
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			accept(writer);
			return writer.toByteArray();
		}
		catch (Throwable t) {
			if (Constants.CORE_DEBUG)
				t.printStackTrace();
			throw new RuntimeException("Error while building class.");
		}
	}
	
	
	public int getVersion() {
		return version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public int getAccessor() {
		return accessor;
	}
	
	public void setAccessor(int accessor) {
		this.accessor = accessor;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getSignature() {
		return signature;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String getSuperClass() {
		return superClass;
	}
	
	public void setSuperClass(String superClass) {
		this.superClass = superClass;
	}
	
	public String[] getInterfaces() {
		return this.interfaces;
	}
	
	public void setInterfaces(String[] interfaces) {
		this.interfaces = interfaces;
	}
	
	public List<Attribute> getUnknownAttributes() {
		return unknownAttributes;
	}
	
	public SourceInformation getSource() {
		return source;
	}
	
	public void setSource(SourceInformation sourceInformation) {
		this.source = sourceInformation;
	}
	
	public List<InnerClassInformation> getInnerClasses() {
		return innerClasses;
	}

	public void setOuterClass(OuterClassInformation outerClass) {
		this.outerClass = outerClass;
	}

	public OuterClassInformation getOuterClass() {
		return outerClass;
	}
	
	public List<FieldNode> getFields() {
		return fields;
	}

	public List<MethodNode> getMethods() {
		return methods;
	}
	
	@Override
	public String toString() {
		return "ClassNode[" + name + "," + superClass + "]";
	}

	public void setOptions(int options) {
		this.options = options;
	}

	public int getOptions() {
		return options;
	}

	public static class InnerClassInformation {
		private int accessor;
		private String name;
		private String innerName;
		private String outerName;
		
		public InnerClassInformation(int accessor,String name,String inner,String outer) {
			this.accessor = accessor;
			this.name = name;
			this.innerName = inner;
			this.outerName = outer;
		}

		public int getAccessor() {
			return accessor;
		}

		public String getName() {
			return name;
		}

		public String getInnerName() {
			return innerName;
		}

		public String getOuterName() {
			return outerName;
		}
	}
	
	public static class OuterClassInformation {
		private String owner;
		private String name;
		private String descriptor;
		
		public OuterClassInformation(String owner,String name,String descriptor) {
			this.owner = owner;
			this.name = name;
			this.descriptor = descriptor;
		}

		public String getOwner() {
			return owner;
		}

		public String getName() {
			return name;
		}

		public String getDescriptor() {
			return descriptor;
		}		
	}

	public static class SourceInformation {
		private String sourceFile;
		private String sourceDebug;
		
		public SourceInformation(String srcFile,String srcDebug) {
			sourceFile = srcFile;
			sourceDebug = srcDebug;
		}
		
		public String getSourceFile() {
			return sourceFile;
		}
		
		public String getSourceDebug() {
			return sourceDebug;
		}
	}
	
	
	
}

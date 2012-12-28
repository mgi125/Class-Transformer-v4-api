package mgi.tools.jtransformer.api.code.memory;

import mgi.tools.jtransformer.api.code.AbstractCodeNode;
import mgi.tools.jtransformer.api.code.CodePrinter;
import mgi.tools.jtransformer.api.code.ExpressionNode;
import mgi.tools.jtransformer.api.code.tools.Utilities;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

public class NewUninitializedArrayExpression extends ExpressionNode {

	/**
	 * Contains array type.
	 */
	private Type type;
	/**
	 * Contains lengths of initialized dimensions.
	 */
	private ExpressionNode[] lengths;
	
	public NewUninitializedArrayExpression(Type type, ExpressionNode[] lengths) {
		this.type = type;
		this.lengths = lengths;
		
		for (int i = 0; i < lengths.length; i++)
			overwrite(lengths[i], i);
		
		if (type.getSort() != Type.ARRAY || type.getDimensions() < lengths.length || lengths.length <= 0)
			throw new RuntimeException("WT");
	}
	
	@Override
	public boolean canTrim() {
		for (int i = 0; i < lengths.length; i++)
			if (!lengths[i].canTrim())
				return false;
		return true;
	}
	
	@Override
	public boolean altersLogic() {
		for (int i = 0; i < lengths.length; i++)
			if (lengths[i].altersLogic())
				return true;
		return false;
	}

	@Override
	public boolean altersFlow() {
		return false;
	}

	@Override
	public boolean affectedBy(AbstractCodeNode n) {
		for (int i = 0; i < lengths.length; i++)
			if (lengths[i].affectedBy(n))
				return true;
		return false;
	}
	
	@Override
	public int getPriority() {
		return ExpressionNode.PRIORITY_ARRAY_INDEX;
	}
	
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public void onChildUpdated(int addr) {
		updateLength(addr, (ExpressionNode)read(addr));
	}

	@Override
	public ExpressionNode copy() {
		ExpressionNode[] lengths = new ExpressionNode[this.lengths.length];
		for (int i = 0; i < lengths.length; i++)
			lengths[i] = this.lengths[i].copy();
		return new NewUninitializedArrayExpression(type, lengths);
	}

	@Override
	public void print(CodePrinter printer) {
		printer.print("new " + type.getElementType().getClassName());
		for (int dim = 0; dim < type.getDimensions(); dim++) {
			printer.print('[');
			if (dim < lengths.length) {
				lengths[dim].print(printer);
			}
			printer.print(']');
		}
	}

	@Override
	public void accept(MethodVisitor visitor) {
		for (int i = 0; i < lengths.length; i++) {
			lengths[i].accept(visitor);
			int[] cast = Utilities.primitiveCastOpcodes(lengths[i].getType(), Type.INT_TYPE);
			for (int a = 0; a < cast.length; a++)
				visitor.visitInsn(cast[a]);
		}
		
		if (type.getDimensions() != 1) {
			visitor.visitMultiANewArrayInsn(type.getDescriptor(), lengths.length);
		}
		else {
			Type element = type.getElementType();
			if (element.getSort() == Type.OBJECT || element.getSort() == Type.METHOD) {
				visitor.visitTypeInsn(Opcodes.ANEWARRAY, element.getInternalName());
			}
			else {
				visitor.visitIntInsn(Opcodes.NEWARRAY, Utilities.primitiveArrayOpcode(type));
			}
		}
	}
	
	public ExpressionNode[] getLengths() {
		return lengths;
	}
	
	public void setLengths(ExpressionNode[] lengths) {
		if (type.getDimensions() < lengths.length || lengths.length <= 0)
			throw new RuntimeException("WT");
		if (lengths.length < this.lengths.length) {
			setCodeAddress(0);
			while (read(0) != null)
				delete();
		}
		this.lengths = lengths;
		for (int i = 0; i < lengths.length; i++)
			overwrite(lengths[i], i);
	}
	
	public void updateLength(int dimension, ExpressionNode length) {
		if (dimension < 0 || dimension >= lengths.length)
			throw new RuntimeException("WT");
		lengths[dimension] = length;
		overwrite(length, dimension);
	}

	public Type getArrayType() {
		return type;
	}

	public void setArrayType(Type type) {
		if (type.getSort() != Type.ARRAY || lengths.length > type.getDimensions())
			throw new RuntimeException("WT");
		this.type = type;
	}

}

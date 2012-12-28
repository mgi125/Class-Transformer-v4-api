package mgi.tools.jtransformer.api.serialization;

import mgi.tools.jtransformer.utilities.ByteBuffer;

public interface ISerializer {
	Object readObject(ByteBuffer buffer);
	ByteBuffer writeObject(Object object);
}

package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;

import fr.ni240sx.ucgt.geometryFile.io.ZModelerZ3D;

public class NodeBinds extends ZModBlock {

	// these indices come even after the ones in renderdata in a zmod saved file
	public LinkedHashMap<Integer, String> bindingData = new LinkedHashMap<>();
	
	public NodeBinds(int ID) {
		super(ID);
	}

	public NodeBinds() {
	}

	@Override
	public void readData(ByteBuffer in) {
		int numBindings = in.getInt();
		for (int i=0; i<numBindings; i++) {
			bindingData.put(in.getInt(), ZModBlock.readString(in));
		}
		in.getInt(); //?=0
	}

	@Override
	public String getName() {
		return "services::CNodesBindService";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		/* header : 16B
		 * some length : 4B
		 * nameLength+5B
		 * blank : 4B
		 */
		final var length = 29+getName().length();
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);

		block.put(new byte[] {0x44, 0x45, 0x43, 0x4C}); //DECL
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);

		block.putInt(33);
		ZModBlock.putString(block, getName());
		block.putInt(0);

		fos.write(block.array());
		
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		/* header : 16B
		 * num bindings : 4B
		 * then per data
		 * string1length+5B
		 * ID : 4B
		 * then blank : 4B
		 */
		//precompute length
		var length = 24; //header + blank + num datas
		for (var i : bindingData.entrySet()) {
			length += i.getValue().length()+9; //5B for string length, 4B for UID
		}
		
		var block = ByteBuffer.wrap(new byte[length]);
		block.order(ByteOrder.LITTLE_ENDIAN);
		
		block.put(new byte[] {0x44, 0x41, 0x54, 0x41}); //DATA
		block.putInt(UID); //UID
		block.putInt(0); //version
		block.putInt(length-16);
		
		block.putInt(bindingData.entrySet().size());
		for (var i : bindingData.entrySet()) {
			block.putInt(i.getKey());
			ZModBlock.putString(block, i.getValue());
		}
		block.putInt(0);
		
		fos.write(block.array());
	}


	public void addNodes1() {
		bindingData.put(ZModelerZ3D.createUID(), "core::bind::CGenericNodeBindCondition");
		bindingData.put(ZModelerZ3D.createUID(), "core::bind::CAutoSourceDeleteCondition");
	}
	
	public void addNodes2() {
		bindingData.put(ZModelerZ3D.createUID(), "core::bind::CGenericNodeBindCondition");
		bindingData.put(ZModelerZ3D.createUID(), "core::bind::CAutoSourceDeleteCondition");
		bindingData.put(ZModelerZ3D.createUID(), "core::bind::CSkeletonBind");
	}
}

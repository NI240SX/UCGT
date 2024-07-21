package fr.ni240sx.ucgt.geometryFile.io.zModBlocks;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SkipBlock extends ZModBlock {
	
	@Override
	public void readData(ByteBuffer in) {
		//no need to load it
	}

	@Override
	public String getName() {
		return "";
	}

	@Override
	public void writeDeclaration(OutputStream fos) throws IOException {
		//nothing
	}

	@Override
	public void writeData(OutputStream fos) throws IOException {
		//nothing
	}
}

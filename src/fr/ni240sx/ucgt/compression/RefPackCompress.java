package fr.ni240sx.ucgt.compression;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import javafx.util.Pair;

// Original code by Rick Gibbed https://github.com/gibbed/Gibbed.RefPack/tree/master
// Ported to Java by NI240SX
// TODO look for possible performance improvements
public class RefPackCompress {

	public int sequenceStart = 0;
    public int sequenceLength = 0;
    public int sequenceIndex = 0;
    
	public byte[] compress(ByteBuffer in) {
		return compress(in, CompressionLevel.Maximum);
	}

	public byte[] compress(ByteBuffer input, CompressionLevel level) {
    	byte[] output;

    	if (input.capacity() >= Integer.toUnsignedLong(0xFFFFFFFF)) { //workaround because the latter is -1, could use compareUsigned
        	System.out.println("[RFPKComp] Error : Input data is too large.");
        	return null;
//            throw new InvalidOperationException("input data is too large");
        }

        var endIsValid = false;
        var compressedChunks = new ArrayList<byte[]>();
        var compressedIndex = 0;
        var compressedLength = 0;
        output = null;

        if (input.capacity() < 16) {
        	System.out.println("[RFPKComp] Error : Input data is too small to be compressed.");
            return null;
        }

        
        var blockTrackingQueue = new LinkedList<Pair<Integer, Integer>>();
        var blockPretrackingQueue = new LinkedList<Pair<Integer, Integer>>();

        // So lists aren't being freed and allocated so much
        var unusedLists = new LinkedList<ArrayList<Integer>>();
        var latestBlocks = new HashMap<Integer, ArrayList<Integer>>();
        var lastBlockStored = 0;
        //TODO check whether this is necessary in Java or is a C# thing that degrades performance

        while (compressedIndex < input.capacity()) { // loop on input data
        	
        	//TODO what's compressedIndex for ? can it be replaced with like a ByteBuffer
        	
        	
            while (compressedIndex > lastBlockStored + level.BlockInterval && input.capacity() - compressedIndex > 16) {
        		
                if (blockPretrackingQueue.size() >= level.PrequeueLength) {
                    var tmppair = blockPretrackingQueue.poll();
                    blockTrackingQueue.add(tmppair);

                    ArrayList<Integer> valueList = new ArrayList<>();
                    
                    if (latestBlocks.getOrDefault(tmppair.getKey(), valueList).equals(valueList))  {
                        valueList = unusedLists.size() > 0 ? unusedLists.poll() : new ArrayList<>();
                        latestBlocks.put(tmppair.getKey(), valueList);
                    }

                    if (valueList.size() >= level.SameValToTrack) {
                        var earliestIndex = 0;
                        var earliestValue = valueList.get(0);

                        for (int loop = 1; loop < valueList.size(); loop++) {
                            if (valueList.get(loop) < earliestValue) {
                                earliestIndex = loop;
                                earliestValue = valueList.get(loop);
                            }
                        }

                        valueList.set(earliestIndex, tmppair.getValue());
                    }
                    else valueList.add(tmppair.getValue());

                    if (blockTrackingQueue.size() > level.QueueLength) {
                        var tmppair2 = blockTrackingQueue.poll();
                        valueList = latestBlocks.get(tmppair2.getKey());

                        if (valueList != null) {
	                        for (int loop = 0; loop < valueList.size(); loop++) {
	                            if (valueList.get(loop) == tmppair2.getValue()) {
	                                valueList.remove(loop);
	                                break;
	                            }
	                        }
	
	                        if (valueList.size() == 0) {
	                            latestBlocks.remove(tmppair2.getKey());
	                            unusedLists.add(valueList);
	                        }
                    	}
                    }
                }

                var newBlock = new Pair<>(input.getInt(lastBlockStored), lastBlockStored);
                lastBlockStored += level.BlockInterval;
                blockPretrackingQueue.add(newBlock);
            }

            if (input.capacity() - compressedIndex < 4) {
                // Just copy the rest
                var chunk = new byte[input.capacity() - compressedIndex + 1];
                chunk[0] = (byte)(0xFC | (input.capacity() - compressedIndex));
                
                input.get(compressedIndex, chunk, 1, input.capacity() - compressedIndex);         
//                Array.Copy(input, compressedIndex, chunk, 1, input.capacity() - compressedIndex);

                compressedChunks.add(chunk);
                compressedIndex += chunk.length - 1;
                compressedLength += chunk.length;

                endIsValid = true;
                continue;
            }

            // Search ahead the next 3 bytes for the "best" sequence to copy
            sequenceStart = 0;
            sequenceLength = 0;
            sequenceIndex = 0;
            var isSequence = false;

            if (FindSequence(input, compressedIndex, latestBlocks, level)) { //also depends on sequenceStart, sequenceLength and sequenceIndex
                isSequence = true;
            } else {
                // Find the next sequence
                for (int loop = compressedIndex + 4;
                     isSequence == false && loop + 3 < input.capacity();
                     loop += 4) {
                    if (FindSequence(input, loop, latestBlocks, level)) { //also depends on sequenceStart, sequenceLength and sequenceIndex
                        sequenceIndex += loop - compressedIndex;
                        isSequence = true;
                    }
                }

                if (sequenceIndex == Integer.MAX_VALUE) sequenceIndex = input.capacity() - compressedIndex;

                // Copy all the data skipped over
                while (sequenceIndex >= 4) {
                    int toCopy = (sequenceIndex & ~3);
                    if (toCopy > 112) toCopy = 112;

                    var chunk = new byte[toCopy + 1];
                    chunk[0] = (byte)(0xE0 | ((toCopy >>> 2) - 1));
                    input.get(compressedIndex, chunk, 1, toCopy);
//                    Array.Copy(input, compressedIndex, chunk, 1, toCopy);
                    compressedChunks.add(chunk);
                    compressedIndex += toCopy;
                    compressedLength += chunk.length;
                    sequenceIndex -= toCopy;

                    // int toRead = 0;
                    // int toCopy2 = 0;
                    // int copyOffset = 0;
                }
            }

            if (isSequence) {
                /*
                 * 00-7F  0oocccpp oooooooo
                 *   Read 0-3
                 *   Copy 3-10
                 *   Offset 0-1023
                 *   
                 * 80-BF  10cccccc ppoooooo oooooooo
                 *   Read 0-3
                 *   Copy 4-67
                 *   Offset 0-16383
                 *   
                 * C0-DF  110cccpp oooooooo oooooooo cccccccc
                 *   Read 0-3
                 *   Copy 5-1028
                 *   Offset 0-131071
                 *   
                 * E0-FC  111ppppp
                 *   Read 4-128 (Multiples of 4)
                 *   
                 * FD-FF  111111pp
                 *   Read 0-3
                 */
                if (FindRunLength(input, sequenceStart, compressedIndex + sequenceIndex) < sequenceLength) break;

                while (sequenceLength > 0) {
                    int thisLength = sequenceLength;
                    if (thisLength > 1028)thisLength = 1028;

                    sequenceLength -= thisLength;
                    int offset = compressedIndex - sequenceStart + sequenceIndex - 1;

                    byte[] chunk;
                    
                    if (thisLength > 67 || offset > 16383) {
                        
                    	chunk = new byte[sequenceIndex + 4];
                        chunk[0] =
                            (byte)
                            (0xC0 | sequenceIndex | (((thisLength - 5) >>> 6) & 0x0C) | ((offset >>> 12) & 0x10));
                        chunk[1] = (byte)((offset >>> 8) & 0xFF);
                        chunk[2] = (byte)(offset & 0xFF);
                        chunk[3] = (byte)((thisLength - 5) & 0xFF);
                        
                    } else if (thisLength > 10 || offset > 1023) {
                        chunk = new byte[sequenceIndex + 3];
                        chunk[0] = (byte)(0x80 | ((thisLength - 4) & 0x3F));
                        chunk[1] = (byte)(((sequenceIndex << 6) & 0xC0) | ((offset >>> 8) & 0x3F));
                        chunk[2] = (byte)(offset & 0xFF);
                    } else {
                        chunk = new byte[sequenceIndex + 2];
                        chunk[0] = (byte)((sequenceIndex & 0x3) | (((thisLength - 3) << 2) & 0x1C) | ((offset >>> 3) & 0x60));
                        chunk[1] = (byte)(offset & 0xFF);
                    }

                    if (sequenceIndex > 0) {
                        input.get(compressedIndex, chunk, chunk.length - sequenceIndex, sequenceIndex);
//                        Array.Copy(input, compressedIndex, chunk, chunk.length - sequenceIndex, sequenceIndex);
                    }

                    compressedChunks.add(chunk);
                    compressedIndex += thisLength + sequenceIndex;
                    compressedLength += chunk.length;

                    // int toRead = 0;
                    // int toCopy = 0;
                    // int copyOffset = 0;

                    sequenceStart += thisLength;
                    sequenceIndex = 0;
                }
            }
        } // loop on input data

        //END
        if (compressedLength + 6 < input.capacity()) {
//            int chunkPosition;
            
          //adding the header used in UC : RFPK flags decompSize compSize
            output = new byte[compressedLength + 5 + (endIsValid ? 0 : 1) + 16];
            var bb = ByteBuffer.wrap(output);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.put("RFPK".getBytes());
            bb.putInt(4097);
            bb.putInt(input.capacity());
            bb.putInt(compressedLength + 5 + (endIsValid ? 0 : 1));

            if (input.capacity() > Integer.toUnsignedLong(0xFFFFFF)) {
                bb.put((byte) (0x10 | 0x80)); // 0x80 = length is 4 bytes
                bb.put((byte) 0xFB);
                bb.put((byte)(input.capacity() >>> 24));
                bb.put((byte)(input.capacity() >>> 16));
                bb.put((byte)(input.capacity() >>> 8));
                bb.put((byte)(input.capacity()));
//                chunkPosition = 22;
            } else {
            	bb.put((byte) 0x10);
            	bb.put((byte) 0xFB);
            	bb.put((byte)(input.capacity() >>> 16));
            	bb.put((byte)(input.capacity() >>> 8));
            	bb.put((byte)(input.capacity()));
//                chunkPosition = 21;
            }

            for (byte[] t : compressedChunks) bb.put(t);

            if (!endIsValid) output[output.length - 1] = (byte) 0xFC;
            
            return output;
        }

        System.out.println("[RFPKCompress] Something didn't go well");
        return null;
    }

    private boolean FindSequence(ByteBuffer input,
                                     int offset,
//                                     int bestStart, //ref
//                                     int bestLength, //ref
//                                     int bestIndex, //ref
                                     HashMap<Integer, ArrayList<Integer>> latestBlocks,
                                     CompressionLevel level) {
    	
        int end = (offset < level.BruteForceLength) ? -offset : -level.BruteForceLength;
        int start = (offset > 4) ? -3 : offset - 3;

        boolean foundRun = false;
        if (sequenceLength < 3) {
            sequenceLength = 3;
            sequenceIndex = Integer.MAX_VALUE;
        }

        var search = new byte[(input.capacity()-offset > 4) ? 4 : input.capacity() - offset];

        input.get(offset, search, 0, search.length);

        while (start >= end && sequenceLength < 1028) {
            byte currentByte = input.get(start + offset);

            for (int loop = 0; loop < search.length; loop++) {
                if (currentByte != search[loop] || start >= loop || start - loop < -131072) continue;

                int len = FindRunLength(input, offset + start, offset + loop);

                if ((len > sequenceLength || len == sequenceLength && loop < sequenceIndex) &&
                    (len >= 5 ||
                     len >= 4 && start - loop > -16384 ||
                     len >= 3 && start - loop > -1024)) {
                    foundRun = true;
                    sequenceStart = offset + start;
                    sequenceLength = len;
                    sequenceIndex = loop;
                }
            }

            start--;
        }

        if (latestBlocks.size() > 0 && input.capacity() - offset > 16 && sequenceLength < 1028) {
            for (int loop = 0; loop < 4; loop++) {
                var thisPosition = offset + 3 - loop;
                var adjust = loop > 3 ? loop - 3 : 0;
                var value = input.getInt(thisPosition); // BitConverter.ToInt32(data, thisPosition);
                ArrayList<Integer> positions = new ArrayList<>();

                if (!latestBlocks.getOrDefault(value, positions).equals(positions)) //  latestBlocks.TryGetValue(value, positions)
                {
                    for (var trypos : positions) {
                        int localadjust = adjust;

                        if (trypos + 131072 < offset + 8) continue;

                        int length = FindRunLength(input, trypos + localadjust, thisPosition + localadjust);

                        if (length >= 5 && length > sequenceLength){
                            foundRun = true;
                            sequenceStart = trypos + localadjust;
                            sequenceLength = length;
                            sequenceIndex = (loop < 3) ? 3 - loop : 0;
                        }
                        if (sequenceLength > 1028) break;
                    }
                }
                if (sequenceLength > 1028) break;
            }
        }

        return foundRun;
    }

    private static int FindRunLength(ByteBuffer input, int source, int destination) {
        int endSource = source + 1;
        int endDestination = destination + 1;

        while (endDestination < input.capacity() && input.get(endSource) == input.get(endDestination) &&
               endDestination - destination < 1028) {
            endSource++;
            endDestination++;
        }

        return endDestination - destination;
    }
	
}
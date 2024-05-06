package fr.ni240sx.ucgt.compression;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

import javafx.util.Pair;

public class RefPackCompress {

	static int sequenceStart = 0;
    static int sequenceLength = 0;
    static int sequenceIndex = 0;
    
	public static byte[] compress(ByteBuffer in) {
		// TODO Auto-generated method stub
		return compress(in, CompressionLevel.Max);
	}

    public static byte[] compress(ByteBuffer input, CompressionLevel level)
    {
    	byte[] output;

		System.out.println("RefPackCompress");
    			
    	if (input.capacity() >= Integer.toUnsignedLong(0xFFFFFFFF)) //workaround because the latter is -1
        {
        	System.out.println("[RFPKComp] Error : Input data is too large.");
        	return null;
//            throw new InvalidOperationException("input data is too large");
        }

        var endIsValid = false;
        var compressedChunks = new ArrayList<byte[]>();
        var compressedIndex = 0;
        var compressedLength = 0;
        output = null;

        if (input.capacity() < 16)
        {
        	System.out.println("[RFPKComp] Error : Input data is too small to be compressed.");
            return null;
        }

        
        var blockTrackingQueue = new LinkedList<Pair<Integer, Integer>>();
        var blockPretrackingQueue = new LinkedList<Pair<Integer, Integer>>();

        // So lists aren't being freed and allocated so much
        var unusedLists = new LinkedList<ArrayList<Integer>>();
        var latestBlocks = new HashMap<Integer, ArrayList<Integer>>();
        var lastBlockStored = 0;

        while (compressedIndex < input.capacity())
        {
        	
            while (compressedIndex > lastBlockStored + level.BlockInterval && input.capacity() - compressedIndex > 16)
            {
        		
                if (blockPretrackingQueue.size() >= level.PrequeueLength)
                {
                    var tmppair = blockPretrackingQueue.poll();
                    blockTrackingQueue.add(tmppair);

                    ArrayList<Integer> valueList = new ArrayList<Integer>();
                    
                    if (latestBlocks.getOrDefault(tmppair.getKey(), valueList).equals(valueList)) //TODO out valueList
                    {
                        valueList = unusedLists.size() > 0 ? unusedLists.poll() : new ArrayList<Integer>();
                        latestBlocks.put(tmppair.getKey(), valueList);
                    }

                    if (valueList.size() >= level.SameValToTrack)
                    {
                        var earliestIndex = 0;
                        var earliestValue = valueList.get(0);

                        for (int loop = 1; loop < valueList.size(); loop++)
                        {
                            if (valueList.get(loop) < earliestValue)
                            {
                                earliestIndex = loop;
                                earliestValue = valueList.get(loop);
                            }
                        }

                        valueList.set(earliestIndex, tmppair.getValue());
                    }
                    else
                    {
                        valueList.add(tmppair.getValue());
                    }

                    if (blockTrackingQueue.size() > level.QueueLength)
                    {
                        var tmppair2 = blockTrackingQueue.poll();
                        valueList = latestBlocks.get(tmppair2.getKey());

                        for (int loop = 0; loop < valueList.size(); loop++)
                        {
                            if (valueList.get(loop) == tmppair2.getValue())
                            {
                                valueList.remove(loop);
                                break;
                            }
                        }

                        if (valueList.size() == 0)
                        {
                            latestBlocks.remove(tmppair2.getKey());
                            unusedLists.add(valueList);
                        }
                    }
                }

                var newBlock = new Pair<Integer, Integer>(input.getInt(lastBlockStored), lastBlockStored);
                lastBlockStored += level.BlockInterval;
                blockPretrackingQueue.add(newBlock);
            }

            if (input.capacity() - compressedIndex < 4)
            {
                // Just copy the rest
                var chunk = new byte[input.capacity() - compressedIndex + 1];
                chunk[0] = (byte)(0xFC | (input.capacity() - compressedIndex));
                
                input.get(compressedIndex, chunk, 1, input.capacity() - compressedIndex);         
//                Array.Copy(input, compressedIndex, chunk, 1, input.capacity() - compressedIndex);

                compressedChunks.add(chunk);
                compressedIndex += chunk.length - 1;
                compressedLength += chunk.length;

                // int toRead = 0;
                // int toCopy2 = 0;
                // int copyOffset = 0;

                endIsValid = true;
                continue;
            }

            // Search ahead the next 3 bytes for the "best" sequence to copy
            sequenceStart = 0;
            sequenceLength = 0;
            sequenceIndex = 0;
            var isSequence = false;

            if (FindSequence(input,
                             compressedIndex,
//                             sequenceStart, //ref
//                             sequenceLength, //ref
//                             sequenceIndex, //ref
                             latestBlocks,
                             level))
            {
                isSequence = true;
            }
            else
            {
                // Find the next sequence
                for (int loop = compressedIndex + 4;
                     isSequence == false && loop + 3 < input.capacity();
                     loop += 4)
                {
                    if (FindSequence(input,
                                     loop,
//                                     sequenceStart, //ref
//                                     sequenceLength, //ref
//                                     sequenceIndex, //ref
                                     latestBlocks,
                                     level))
                    {
                        sequenceIndex += loop - compressedIndex;
                        isSequence = true;
                    }
                }

                if (sequenceIndex == Integer.MAX_VALUE)
                {
                    sequenceIndex = input.capacity() - compressedIndex;
                }

                // Copy all the data skipped over
                while (sequenceIndex >= 4)
                {
                    int toCopy = (sequenceIndex & ~3);
                    if (toCopy > 112)
                    {
                        toCopy = 112;
                    }

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

            if (isSequence)
            {
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
                if (FindRunLength(input, sequenceStart, compressedIndex + sequenceIndex) < sequenceLength)
                {
                    break;
                }

                while (sequenceLength > 0)
                {
                    int thisLength = sequenceLength;
                    if (thisLength > 1028)
                    {
                        thisLength = 1028;
                    }

                    sequenceLength -= thisLength;
                    int offset = compressedIndex - sequenceStart + sequenceIndex - 1;

                    byte[] chunk;
                    if (thisLength > 67 || offset > 16383)
                    {
                        chunk = new byte[sequenceIndex + 4];
                        chunk[0] =
                            (byte)
                            (0xC0 | sequenceIndex | (((thisLength - 5) >>> 6) & 0x0C) | ((offset >>> 12) & 0x10));
                        chunk[1] = (byte)((offset >>> 8) & 0xFF);
                        chunk[2] = (byte)(offset & 0xFF);
                        chunk[3] = (byte)((thisLength - 5) & 0xFF);
                    }
                    else if (thisLength > 10 || offset > 1023)
                    {
                        chunk = new byte[sequenceIndex + 3];
                        chunk[0] = (byte)(0x80 | ((thisLength - 4) & 0x3F));
                        chunk[1] = (byte)(((sequenceIndex << 6) & 0xC0) | ((offset >>> 8) & 0x3F));
                        chunk[2] = (byte)(offset & 0xFF);
                    }
                    else
                    {
                        chunk = new byte[sequenceIndex + 2];
                        chunk[0] =
                            (byte)
                            ((sequenceIndex & 0x3) | (((thisLength - 3) << 2) & 0x1C) | ((offset >>> 3) & 0x60));
                        chunk[1] = (byte)(offset & 0xFF);
                    }

                    if (sequenceIndex > 0)
                    {
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
        }

        if (compressedLength + 6 < input.capacity())
        {
        	//END
            int chunkPosition;

            if (input.capacity() > Integer.toUnsignedLong(0xFFFFFF))
            {
                output = new byte[compressedLength + 5 + (endIsValid ? 0 : 1)];
                output[0] = (byte) (0x10 | 0x80); // 0x80 = length is 4 bytes
                output[1] = (byte) 0xFB;
                output[2] = (byte)(input.capacity() >>> 24);
                output[3] = (byte)(input.capacity() >>> 16);
                output[4] = (byte)(input.capacity() >>> 8);
                output[5] = (byte)(input.capacity());
                chunkPosition = 6;
            }
            else
            {
                output = new byte[compressedLength + 5 + (endIsValid ? 0 : 1)];
                output[0] = 0x10;
                output[1] = (byte) 0xFB;
                output[2] = (byte)(input.capacity() >>> 16);
                output[3] = (byte)(input.capacity() >>> 8);
                output[4] = (byte)(input.capacity());
                chunkPosition = 5;
            }

            for (byte[] t : compressedChunks)
            {
            	var T = ByteBuffer.wrap(t);
                T.get(0, output, chunkPosition, t.length);
//                Array.Copy(t, 0, output, chunkPosition, t.Length);
                chunkPosition += t.length;
            }

            if (!endIsValid)
            {
                output[output.length - 1] = (byte) 0xFC;
            }

            return output;
        }

        System.out.println("Something didn't go well");
        return output;
    }

    private static boolean FindSequence(ByteBuffer input,
                                     int offset,
//                                     int bestStart, //ref
//                                     int bestLength, //ref
//                                     int bestIndex, //ref
                                     HashMap<Integer, ArrayList<Integer>> latestBlocks,
                                     CompressionLevel level)
    {
        int start;
        int end = -level.BruteForceLength;

        if (offset < level.BruteForceLength)
        {
            end = -offset;
        }

        if (offset > 4)
        {
            start = -3;
        }
        else
        {
            start = offset - 3;
        }

        boolean foundRun = false;
        if (sequenceLength < 3)
        {
            sequenceLength = 3;
            sequenceIndex = Integer.MAX_VALUE;
        }

        var search = new byte[input.capacity() - offset > 4 ? 4 : input.capacity() - offset];

        for (int loop = 0; loop < search.length; loop++)
        {
            search[loop] = input.get(offset + loop);
        }

        while (start >= end && sequenceLength < 1028)
        {
            byte currentByte = input.get(start + offset);

            for (int loop = 0; loop < search.length; loop++)
            {
                if (currentByte != search[loop] || start >= loop || start - loop < -131072)
                {
                    continue;
                }

                int len = FindRunLength(input, offset + start, offset + loop);

                if ((len > sequenceLength || len == sequenceLength && loop < sequenceIndex) &&
                    (len >= 5 ||
                     len >= 4 && start - loop > -16384 ||
                     len >= 3 && start - loop > -1024))
                {
                    foundRun = true;
                    sequenceStart = offset + start;
                    sequenceLength = len;
                    sequenceIndex = loop;
                }
            }

            start--;
        }

        if (latestBlocks.size() > 0 && input.capacity() - offset > 16 && sequenceLength < 1028)
        {
            for (int loop = 0; loop < 4; loop++)
            {
                var thisPosition = offset + 3 - loop;
                var adjust = loop > 3 ? loop - 3 : 0;
                var value = input.getInt(thisPosition); // BitConverter.ToInt32(data, thisPosition);
                ArrayList<Integer> positions = new ArrayList<Integer>();

                if (!latestBlocks.getOrDefault(value, positions).equals(positions)) //  latestBlocks.TryGetValue(value, positions)
                {
                    for (var trypos : positions)
                    {
                        int localadjust = adjust;

                        if (trypos + 131072 < offset + 8)
                        {
                            continue;
                        }

                        int length = FindRunLength(input, trypos + localadjust, thisPosition + localadjust);

                        if (length >= 5 && length > sequenceLength)
                        {
                            foundRun = true;
                            sequenceStart = trypos + localadjust;
                            sequenceLength = length;
                            if (loop < 3)
                            {
                                sequenceIndex = 3 - loop;
                            }
                            else
                            {
                                sequenceIndex = 0;
                            }
                        }

                        if (sequenceLength > 1028)
                        {
                            break;
                        }
                    }
                }

                if (sequenceLength > 1028)
                {
                    break;
                }
            }
        }

        return foundRun;
    }

    private static int FindRunLength(ByteBuffer input, int source, int destination)
    {
        int endSource = source + 1;
        int endDestination = destination + 1;

        while (endDestination < input.capacity() && input.get(endSource) == input.get(endDestination) &&
               endDestination - destination < 1028)
        {
            endSource++;
            endDestination++;
        }

        return endDestination - destination;
    }
	
}

class CompressionLevel
{
    public static CompressionLevel Max = new CompressionLevel(1, 1, 10, 65536);
    public static CompressionLevel Ultra = new CompressionLevel(1, 1, 10, 32768);
    public static CompressionLevel VeryHigh = new CompressionLevel(1, 1, 10, 16384);
    public static CompressionLevel High = new CompressionLevel(1, 1, 10, 8192);
    public static CompressionLevel Medium = new CompressionLevel(1, 1, 10, 4096);
    public static CompressionLevel Low = new CompressionLevel(1, 1, 10, 2048);

    public int BlockInterval;
    public int SearchLength;
    public int PrequeueLength;
    public int QueueLength;
    public int SameValToTrack;
    public int BruteForceLength;

    public CompressionLevel(int blockInterval, 
    						int searchLength,
                            int prequeueLength,
                            int queueLength,
                            int sameValToTrack,
                            int bruteForceLength)
    {
        this.BlockInterval = blockInterval;
        this.SearchLength = searchLength;
        this.PrequeueLength = prequeueLength;
        this.QueueLength = queueLength;
        this.SameValToTrack = sameValToTrack;
        this.BruteForceLength = bruteForceLength;
    }

    public CompressionLevel(int blockInterval, int searchLength, int sameValToTrack, int bruteForceLength)
    {
        this.BlockInterval = blockInterval;
        this.SearchLength = searchLength;
        this.PrequeueLength = this.SearchLength / this.BlockInterval;
        this.QueueLength = 131000 / this.BlockInterval - this.PrequeueLength;
        this.SameValToTrack = sameValToTrack;
        this.BruteForceLength = bruteForceLength;
    }
}
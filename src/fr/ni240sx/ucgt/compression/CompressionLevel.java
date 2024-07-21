package fr.ni240sx.ucgt.compression;

public class CompressionLevel
{
    public static CompressionLevel Maximum = 	new CompressionLevel(8, 64, 10, 65536, "Maximum");
    public static CompressionLevel Ultra = 		new CompressionLevel(8, 64, 10, 32768, "Ultra");
    public static CompressionLevel VeryHigh = 	new CompressionLevel(8, 64, 10, 16384, "VeryHigh");
    public static CompressionLevel High = 		new CompressionLevel(8, 64, 10, 8192, "High");
    public static CompressionLevel Medium = 	new CompressionLevel(1, 1, 10, 2048, "Medium");
    public static CompressionLevel Low = 		new CompressionLevel(1, 1, 10, 512, "Low");
    public static CompressionLevel Minimum = 	new CompressionLevel(1, 1, 10, 64, "Minimum");

    public int BlockInterval;
    public int SearchLength;
    public int PrequeueLength;
    public int QueueLength;			// increasing may be faster
    public int SameValToTrack;		// very high can be faster after a certain thresold
    public int BruteForceLength;	// high is slower and compresses better
    public String name;

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
    public CompressionLevel(int blockInterval, int searchLength, int sameValToTrack, int bruteForceLength) {
    	this(blockInterval, searchLength, sameValToTrack, bruteForceLength, "CompressionLevel");
    }

    public CompressionLevel(int blockInterval, int searchLength, int sameValToTrack, int bruteForceLength, String name)
    {
    	this.name = name;
        this.BlockInterval = blockInterval;
        this.SearchLength = searchLength;
        this.PrequeueLength = this.SearchLength / this.BlockInterval;
        this.QueueLength = 131000 / this.BlockInterval - this.PrequeueLength;
        this.SameValToTrack = sameValToTrack;
        this.BruteForceLength = bruteForceLength;
    }
    
    public String getName() {
    	return name;
    }
    public static CompressionLevel fromName(String name) {
    	switch (name) {
    	case "Maximum": return Maximum;
    	case "Ultra": return Ultra;
    	case "VeryHigh": return VeryHigh;
    	case "High": return High;
    	case "Medium": return Medium;
    	case "Low": return Low;
    	default: return Minimum;
    	}
    }
}
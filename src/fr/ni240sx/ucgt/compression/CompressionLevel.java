package fr.ni240sx.ucgt.compression;

public class CompressionLevel
{
    public static CompressionLevel Max = new CompressionLevel(1, 1, 10, 65536);
    public static CompressionLevel Ultra = new CompressionLevel(1, 1, 10, 32768);
    public static CompressionLevel VeryHigh = new CompressionLevel(1, 1, 10, 16384);
    public static CompressionLevel High = new CompressionLevel(1, 1, 10, 8192);
    public static CompressionLevel Medium = new CompressionLevel(1, 1, 10, 2048);
    public static CompressionLevel Low = new CompressionLevel(1, 1, 10, 512);
    public static CompressionLevel Minimum = new CompressionLevel(1, 1, 10, 64);

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
/**
 * Mahout XML Input format modified to return all xml elements that the input split can contain
 */

package org.almouro.hadoop.utils;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.log4j.Logger;

/**
 * Reads records that are delimited by a specifc begin/end tag.
 */
public class XmlInputFormat extends TextInputFormat {

    private static final transient Logger logger = Logger
	    .getLogger(XmlInputFormat.class);

    public static final String START_TAG_KEY = "xmlinput.start";
    public static final String END_TAG_KEY = "xmlinput.end";
    public static final String START_ROOT_TAG_KEY = "xmlinput.rootstart";
    public static final String END_ROOT_TAG_KEY = "xmlinput.rootend";
    public static final String FILE_LENGTH_KEY = "xmlinput.bytes";

    @Override
    public RecordReader<LongWritable, Text> createRecordReader(InputSplit is,
	    TaskAttemptContext tac) {
	return new XmlRecordReader();
    }

    /**
     * XMLRecordReader class to read through a given xml document to output xml
     * blocks as records as specified by the start tag and end tag
     * 
     */
    public static class XmlRecordReader extends
	    RecordReader<LongWritable, Text> {
	// Should be static final
	private byte[] startTag;
	private byte[] endTag;
	private byte[] startRootTag;
	private byte[] endRootTag;
	private float fileLength;
	// ---------------------

	private long start;
	private long end;
	private FSDataInputStream fsin;
	private final DataOutputBuffer buffer = new DataOutputBuffer();
	private LongWritable key = new LongWritable();
	private Text value = new Text();

	@Override
	public void initialize(InputSplit is, TaskAttemptContext tac)
		throws IOException, InterruptedException {
	    startTag = tac.getConfiguration().get(START_TAG_KEY)
		    .getBytes("utf-8");
	    endTag = tac.getConfiguration().get(END_TAG_KEY).getBytes("utf-8");
	    startRootTag = tac.getConfiguration().get(START_ROOT_TAG_KEY)
		    .getBytes("utf-8");
	    endRootTag = tac.getConfiguration().get(END_ROOT_TAG_KEY)
		    .getBytes("utf-8");
	    fileLength = tac.getConfiguration().getLong(FILE_LENGTH_KEY, 0);

	    // open the file and seek to the start of the split
	    final FileSplit split = (FileSplit) is;
	    start = split.getStart();
	    end = start + split.getLength();
	    final Path file = split.getPath();
	    FileSystem fs = file.getFileSystem(tac.getConfiguration());
	    fsin = fs.open(split.getPath());
	    fsin.seek(start);
	}

	@Override
	public boolean nextKeyValue() throws IOException, InterruptedException {
	    buffer.write(startRootTag);

	    while (fsin.getPos() < end) {
		if (readUntilMatch(startTag, false)) {
		    buffer.write(startTag);
		    readUntilMatch(endTag, true);
		}
	    }

	    if (buffer.getLength() > startRootTag.length) {
		buffer.write(endRootTag);
		key.set(fsin.getPos());
		value.set(buffer.getData(), 0, buffer.getLength());
		buffer.reset();
		return true;
	    }

	    buffer.reset();
	    return false;
	}

	@Override
	public LongWritable getCurrentKey() throws IOException,
		InterruptedException {
	    return key;
	}

	@Override
	public Text getCurrentValue() throws IOException, InterruptedException {
	    return value;
	}

	@Override
	public void close() throws IOException {
	    fsin.close();
	}

	@Override
	public float getProgress() throws IOException {

	    return fsin.getPos() / fileLength;
	}

	private boolean readUntilMatch(byte[] match, boolean withinBlock)
		throws IOException {
	    int i = 0;
	    while (true) {
		int b = fsin.read();
		// end of file:
		if (b == -1)
		    return false;
		// save to buffer:
		if (withinBlock)
		    buffer.write(b);

		// check if we're matching:
		if (b == match[i]) {
		    i++;
		    if (i >= match.length)
			return true;
		} else
		    i = 0;
		// see if we've passed the stop point:
		if (!withinBlock && i == 0 && fsin.getPos() >= end)
		    return false;
	    }
	}
    }
}

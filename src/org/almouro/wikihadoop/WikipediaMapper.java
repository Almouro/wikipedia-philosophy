package org.almouro.wikihadoop;

import java.io.IOException;

import org.almouro.hadoop.utils.TextArrayWritable;
import org.almouro.wiki.PageInfo;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

public class WikipediaMapper extends Mapper<Object, Text, Text, TextArrayWritable> {

    private static final transient Logger logger = Logger.getLogger(WikipediaMapper.class);

    private Text nextPageText = new Text();
    private Text titleText = new Text();
    private TextArrayWritable outputValues = new TextArrayWritable();

    private WikipediaMapper() {
        super();
        outputValues.set(new Writable[] { titleText });
    }

    public void map(Object key, Text value, Context context) throws IOException,
            InterruptedException {
        long startTime = System.currentTimeMillis();

        String document = value.toString();

        for (PageInfo page : PagesXmlReader.readPagesFromXml(document)) {
            titleText.set(page.getTitle());
            nextPageText.set(page.getNextPage());
            context.write(nextPageText, outputValues);
        }

        logger.info("Mapper task ended after " + (System.currentTimeMillis() - startTime) + "ms");
    }
}
package org.almouro.wikihadoop;

import java.io.IOException;

import org.almouro.hadoop.utils.TextArrayWritable;
import org.almouro.wiki.PageInfo;
import org.almouro.wiki.PagesXmlReader;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

public class WikipediaMapper extends Mapper<Object, Text, Text, TextArrayWritable> {

    private Text nextPageText = new Text();
    private Text titleText = new Text();
    private TextArrayWritable outputValues = new TextArrayWritable();

    private WikipediaMapper() {
        super();
        outputValues.set(new Writable[] { titleText });
    }

    public void map(Object key, Text value, Context context) throws IOException,
            InterruptedException {
        
        String document = value.toString();
        
        for (PageInfo page : PagesXmlReader.readPagesFromXml(document)) {
            titleText.set(page.getTitle());
            nextPageText.set(page.getNextPage());
            context.write(nextPageText, outputValues);
        }
    }
}
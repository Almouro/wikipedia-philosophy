package org.almouro.wikihadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.almouro.hadoop.utils.TextArrayWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Reducer;

public class WikipediaReducer extends Reducer<Text, TextArrayWritable, Text, TextArrayWritable>{
	
	private TextArrayWritable result = new TextArrayWritable();
	
	public void reduce(Text key, Iterable<TextArrayWritable> values, Context context) throws IOException, InterruptedException {
		List<Writable> valuesText = new ArrayList<>();
		
        for(TextArrayWritable value : values){
        	for (Writable text : value.get()) {
				valuesText.add(text);
			}
        }
        
        Writable[] texts = new Writable[valuesText.size()];
        texts = valuesText.toArray(texts);
        
        result.set(texts);
        context.write(key, result);
	}
}

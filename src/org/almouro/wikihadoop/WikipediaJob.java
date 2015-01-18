package org.almouro.wikihadoop;

import java.util.Date;

import org.almouro.hadoop.utils.TextArrayWritable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.mahout.classifier.bayes.XmlInputFormat;

public class WikipediaJob {

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
	Configuration conf = new Configuration();
	conf.set("xmlinput.start", "<page>");
	conf.set("xmlinput.end", "</page>");
	conf.set("xmlinput.rootstart", "<pages>");
	conf.set("xmlinput.rootend", "</pages>");

	Job job = Job.getInstance(conf, "word count");

	job.setJarByClass(WikipediaJob.class);
	job.setMapperClass(WikipediaMapper.class);
	job.setCombinerClass(WikipediaReducer.class);
	job.setReducerClass(WikipediaReducer.class);

	job.setInputFormatClass(XmlInputFormat.class);

	job.setOutputKeyClass(Text.class);
	job.setOutputValueClass(TextArrayWritable.class);

	FileInputFormat.addInputPath(job, new Path(args[0]));
	FileOutputFormat.setOutputPath(job,
		new Path(args[1] + new Date().getTime()));

	System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}

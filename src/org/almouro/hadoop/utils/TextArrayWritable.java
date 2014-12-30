package org.almouro.hadoop.utils;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class TextArrayWritable extends ArrayWritable {
    public TextArrayWritable() {
        super(Text.class);
    }

    public TextArrayWritable(String[] strings) {
        super(Text.class);
        Text[] texts = new Text[strings.length];
        for (int i = 0; i < strings.length; i++) {
            texts[i] = new Text(strings[i]);
        }
        set(texts);
    }

    
    public String toString() {
        Writable[] values = get();

        StringBuilder sb = new StringBuilder();
        for (Writable value : values) {
            sb.append(value.toString()).append("\t");
        }
        
        //trim the trailing space
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
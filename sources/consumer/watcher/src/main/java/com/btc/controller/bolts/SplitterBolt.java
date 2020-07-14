/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.btc.controller.bolts;

import java.util.Map;
import org.apache.storm.shade.org.apache.commons.lang.StringEscapeUtils;
import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.shade.org.json.simple.parser.JSONParser;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

/**
 *
 * @author ?
 */
public class SplitterBolt extends BaseRichBolt {

    /**
     * 
     */
    private static final JSONParser PARSER = new JSONParser();
    
    /**
     *
     */
    private OutputCollector collector;

    /**
     *
     * @param declarer
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declareStream("blocks", new Fields("source"));
        declarer.declareStream("transactions", new Fields("source"));
    }

    /**
     * 
     * @param topoConf
     * @param context
     * @param collector 
     */
    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    /**
     * 
     * @param input 
     */
    @Override
    public void execute(Tuple input) {
        try {
            SplitterBolt.PARSER.reset();
            
            String string = StringEscapeUtils.unescapeJava(input.getString(4));
    
            JSONObject object = (JSONObject) SplitterBolt.PARSER.parse(string.substring(1, string.length() -1));
            
            if (object.containsKey("total_amount")) {
                this.collector.emit("transactions", new Values(object));
            } else {
                if (object.containsKey("found_by")) {
                    this.collector.emit("blocks", new Values(object));
                } else {
                    System.out.println(input);
                }
            }
            
            collector.ack(input);
        } catch (Exception e) {
            collector.reportError(e);
            collector.fail(input);
        }
    }

}

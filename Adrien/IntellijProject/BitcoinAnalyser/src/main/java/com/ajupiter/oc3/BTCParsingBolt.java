package com.ajupiter.oc3;

import org.apache.storm.shade.org.json.simple.JSONObject;
import org.apache.storm.shade.org.json.simple.parser.JSONParser;
import org.apache.storm.shade.org.json.simple.parser.ParseException;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class BTCParsingBolt extends BaseRichBolt {
    private OutputCollector outputCollector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {
        this.outputCollector = outputCollector;
    }

    @Override
    public void execute(Tuple tuple) {
        try {
            process(tuple);
        } catch (ParseException e){
            e.printStackTrace();
            outputCollector.fail(tuple);
        }
    }


    public void process(Tuple input) throws ParseException {

        JSONParser jsonParser = new JSONParser();
        String btc_info;
        try {
            btc_info = input.getString(4);
            JSONObject obj = (JSONObject)jsonParser.parse(btc_info);

            JSONObject time_obj = (JSONObject) obj.get("time");
            String time_uk = (String) time_obj.get("updateduk");

            JSONObject bpi_obj = (JSONObject) obj.get("bpi");
            JSONObject eur_obj = (JSONObject) bpi_obj.get("EUR");
            Double euro_rate = (Double) eur_obj.get("rate_float");

            // System.out.println(time_uk + ": " + euro_rate + " euro");

            outputCollector.emit(new Values(time_uk, euro_rate));
            outputCollector.ack(input);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("time_uk", "euro_rate"));
    }
}

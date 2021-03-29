package com.ashish.nse.data;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataCache {

    private static final String COMMA = ",";
    private static DataCache dataCache = DataCache.getDataCache();
    private ConcurrentMap<String, List<TickData>> tickDataMap;

    private DataCache() {
        this.tickDataMap = new ConcurrentHashMap<>();
    }

    public void loadDataOf(final String stock, final String inputFilePath) {
        List<TickData> inputList;
        try {
            System.out.println("Loading data of "+stock);
            File inputF = new File(inputFilePath);
            InputStream inputFS = new FileInputStream(inputF);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
            inputList = br.lines().skip(1).map(mapToItem).collect(Collectors.toList());
            br.close();
            tickDataMap.put(stock,inputList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Function<String, TickData> mapToItem = (line) -> {
        String[] dataV = line.split(COMMA);// a CSV has comma separated lines
        TickData item = new TickData(dataV[0], Double.parseDouble(dataV[1]), Double.parseDouble(dataV[2]), Double.parseDouble(dataV[3]), Double.parseDouble(dataV[4]));
        return item;
    };

    public static DataCache getDataCache() {
        return new DataCache();
    }

    public void loadMultipleStockData(final Map<String , String > stockToFilePathMap){
        stockToFilePathMap.forEach((k,v) -> loadDataOf(k,v));
    }

    public List<TickData> getDataFor(final String stock){
        return  tickDataMap.get(stock);
    }
}

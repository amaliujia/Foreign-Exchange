import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by amaliujia on 15-9-19.
 */
public class Label {

    private ArrayList<Record> records = new ArrayList<Record>();
    private ArrayList<Float> avgsFroTenMins = new ArrayList<Float>();

    /**
     * Read all records into arraylist
     * @param path
     *          File handler to csv file.
     * @throws IOException
     *          throws when IO error curr.
     * @throws ParseException
     *          throws when parsing date format wrong.
     */
    public void processData(File path) throws IOException, ParseException {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        String line;
        while ((line = reader.readLine()) != null){
            String[] components = line.split(",");
            if(components.length != 4){
                continue;
            }else{
                Record record = new Record(components);
                records.add(record);
            }
        }
    }

    /**
     * Start label data, print labeled records line by line.
     */
    public void analyze(){
        Date base = records.get(0).date;
        float count = 0;
        float asksum = 0;

        // compute bid avg for every ten minutes
        for(Record r : records){
            if((r.date.getTime() - base.getTime()) < 600000){
                count++;
                asksum += r.askPrice;
            }else{
                avgsFroTenMins.add(asksum/count);
                count = 1;
                asksum = r.bidPrice;
                base = r.date;
            }
        }

        if(count != 1){
            avgsFroTenMins.add(asksum/count);
        }

        // compare current bid price with future avg bid price
        base = records.get(0).date;
        int index = 1;

        int label = 0;
        for(Record r : records){

            if((r.date.getTime() - base.getTime()) > 600000){
                index++;
                base = r.date;
            }

            if(r.bidPrice < avgsFroTenMins.get(index)){
                label = 1;
            }else{
                label = 0;
            }

            System.out.println(r.currency + "," + r.date + "," + r.bidPrice + "," + r.askPrice + "," + label);
        }

    }

    public static void main(String[] args) throws IOException, ParseException {
        File folder = new File(args[0]);
        Label label = new Label();
        for (File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                label.processData(fileEntry);
            }
        }

        label.analyze();
    }

    private class Record{
        public String currency;
        public Date date;
        public float askPrice;
        public float bidPrice;

        public DateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS");
        public Record(String[] r) throws ParseException {
            currency = r[0];
            date = format.parse(r[1]);
            askPrice = Float.parseFloat(r[2]);
            bidPrice = Float.parseFloat(r[3]);
        }
    }
}

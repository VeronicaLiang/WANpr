/**
 * Created by Xiaoyu on 3/10/2016.
 * Reading the configuration file, and setup the initial parameters.
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;

public class Config {
    public static void configuration (String inputFile){
        String line = null;
        Hashtable<String, String> arg = new Hashtable<String, String>();
        try{
            FileReader filereader = new FileReader(inputFile);
            BufferedReader bufferedreader = new BufferedReader(filereader);
            while ((line = bufferedreader.readLine()) != null){
                if(!line.contains("=")){
                    continue;
                }else{
                    String[] tmp = line.split("=");
                    String para = tmp[0].trim();
                    String val = tmp[1].trim();
                    if(para.equals("NEIGHBOR")){

                    }else{
                        arg.put(para, val);
                    }
                }
            }
            bufferedreader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(arg);
    }
}


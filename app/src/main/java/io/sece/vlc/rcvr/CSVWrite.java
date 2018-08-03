package io.sece.vlc.rcvr;

import com.google.common.eventbus.Subscribe;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by alex on 8/1/18.
 */

public class CSVWrite {

    String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    String fileName;
    CSVWriter writer;
    FileWriter fileWriter;

    String fileType;
    boolean isActive = false;
    public CSVWrite(String fileType){
        Bus.subscribe(this);
        this.fileType = fileType;
    }

    public void initWriteSession() throws IOException {
        this.fileName = fileType + System.nanoTime() + ".csv";

        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath );
        if(f.exists() && !f.isDirectory()){
            fileWriter = new FileWriter(filePath , true);
            writer = new CSVWriter(fileWriter);
        }
        else {
            writer = new CSVWriter(new FileWriter(filePath));
        }

        String[] tableHead = {};
        if(fileType.equals("params_")){
            tableHead = new String[]{"TimeStamp","ExposureTime", "ExposureComp", "ISO", "AWB", "FocusDist", "Zoom"};
        }else if(fileType.equals("values_")){
            tableHead = new String[]{"TimeStamp", "Hue", "Brightness"};
        }

        writer.writeNext(tableHead);
        System.out.println("CSV Writing started, filename: " + fileName);
    }

    public void write(String[] data) throws IOException{
        if(writer != null){
            if(isActive){
                writer.writeNext(data);
            }else{
                writer.close();
            }
        }

    }

    private void close() throws IOException{
        writer.close();
    }

    @Subscribe
    private void onWriteEvent(Bus.WriteEvent ev) throws IOException {
        this.isActive = ev.writingActive;
        if(isActive){
            initWriteSession();
        }else{
            close();
            writer = null;
        }
    }
}

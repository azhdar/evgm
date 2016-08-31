package com.evgm;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Recorder {

    private static final String RECORD_DIRECTORY_NAME = "Records";
    private static final String RECORD_FILENAME = "record_";
    private static final String KML_FILE_EXTENSION = ".kml";

    private static final long MAX_FILE_SIZE_IN_BYTE = 1024*1024;

    private static final String STYLE_NAME = "evg-style";

    private Context context;
    private FileOutputStream fileOutputStream;
    private File currentFile;
    private long startTimestamp;


    public Recorder(Context context) throws IOException {
        this.context = context;
        createNewFile();
    }

    public String record(GpsLocation location) throws IOException {
        String placeMarkXmlString = null;
        if(location != null && location.isValid()) {
            placeMarkXmlString = "<Placemark>";
            placeMarkXmlString += "<description><![CDATA[" +
                    "Device time: " + getFormattedDate(System.currentTimeMillis(), TimeZone.getDefault()) + "</li>";
            placeMarkXmlString += "<ul><b>Location</b>" +
                    "<li>time: " + getFormattedDate(location.getTimeStamp(), TimeZone.getDefault()) + "</li>" +
                    "<li>longitude: " + location.getLongitude() + "</li>"+
                    "<li>latitude: " + location.getLatitude() + "</li>" +
                    "<li>altitude: " + location.getAltitude() + "</li>" +
                    "<li>accuracy: " + location.getAccuracy() + "</li></ul>";
            placeMarkXmlString += "]]></description>";
            placeMarkXmlString += "<styleUrl>#"+STYLE_NAME+"</styleUrl>";
            placeMarkXmlString += "<TimeStamp>";
            placeMarkXmlString += "<when>"+getFormattedDate(location.getTimeStamp(), TimeZone.getDefault())+"</when>";
            placeMarkXmlString += "</TimeStamp>";
            placeMarkXmlString += "<Point>";
            placeMarkXmlString += "<altitudeMode>clampToGround</altitudeMode>";
            placeMarkXmlString += "<coordinates>"+String.valueOf(location.getLongitude())+","+String.valueOf(location.getLatitude())+","+String.valueOf(location.getAltitude())+"</coordinates>";
            placeMarkXmlString += "</Point>";
            placeMarkXmlString += "</Placemark>";

            writePlaceMark(placeMarkXmlString);
        }
        return getFormattedDate(location.getTimeStamp(), TimeZone.getDefault()) + "," + String.valueOf(location.getLongitude())+ "," +String.valueOf(location.getLongitude()) + "," + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getAltitude());
    }

    public void close() throws IOException {
        closeCurrentFile();
    }

    private void createNewFile() throws IOException {
        startTimestamp = System.currentTimeMillis();
        currentFile = new File(getProductionDirectory(), RECORD_FILENAME + getFormattedDateForFilename(startTimestamp, TimeZone.getDefault()) + KML_FILE_EXTENSION);
        fileOutputStream = new FileOutputStream(currentFile);
        writeHeader();
    }

    private void closeCurrentFile() throws IOException {
        writeFooter();
        currentFile = null;
        if(fileOutputStream != null) fileOutputStream.close();
        fileOutputStream = null;
        startTimestamp = -1L;
    }

    private void writePlaceMark(String placeMark) throws IOException {
        if(currentFile != null && (currentFile.length() + placeMark.length()) > MAX_FILE_SIZE_IN_BYTE) {
            closeCurrentFile();
        }
        if(currentFile == null && fileOutputStream == null) {
            createNewFile();
        }
        fileOutputStream.write(placeMark.getBytes());
        fileOutputStream.flush();
    }

    private void writeHeader() throws IOException {
        String xmlDataHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        xmlDataHeader += "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\">";
        xmlDataHeader += "<Document>";
        xmlDataHeader += "<name>"+currentFile.getName()+"</name>";

        // styles
        xmlDataHeader += "<Style id=\""+STYLE_NAME+"\">";
        xmlDataHeader += "<IconStyle>";
        xmlDataHeader += "<scale>0.5</scale>";
        xmlDataHeader += "<heading>0</heading>";
        xmlDataHeader += "<Icon>";
        xmlDataHeader += "<href>http://maps.google.com/mapfiles/kml/paddle/blu-circle.png</href>";
        xmlDataHeader += "</Icon>";
        xmlDataHeader += "<hotSpot x=\"0.5\" xunits=\"fraction\" y=\"0\" yunits=\"fraction\" />";
        xmlDataHeader += "</IconStyle>";
        xmlDataHeader += "</Style>";

        fileOutputStream.write(xmlDataHeader.getBytes());
        fileOutputStream.flush();
    }

    private void writeFooter() throws IOException {
        String xmlDataFooter = "</Document>";
        xmlDataFooter += "</kml>";

        fileOutputStream.write(xmlDataFooter.getBytes());
        fileOutputStream.flush();
    }

    private File getProductionDirectory() throws IOException {
        File productionDirectory = new File(/*context.getFilesDir().getAbsolutePath()*/Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + RECORD_DIRECTORY_NAME);
        if(!productionDirectory.exists()) {
            if(!productionDirectory.mkdirs()) {
                throw new IOException("Directories cannot be created");
            }
        }
        return productionDirectory;
    }

    private String getHeader(){
        String xmlDataHeader = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
        xmlDataHeader += "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\">";
        xmlDataHeader += "<Document>";
        xmlDataHeader += "<name>"+currentFile.getName()+"</name>";
        // styles
        xmlDataHeader += "<Style id=\""+STYLE_NAME+"\">";
        xmlDataHeader += "<IconStyle>";
        xmlDataHeader += "<scale>0.5</scale>";
        xmlDataHeader += "<heading>0</heading>";
        xmlDataHeader += "<Icon>";
        xmlDataHeader += "<href>http://maps.google.com/mapfiles/kml/paddle/blu-circle.png</href>";
        xmlDataHeader += "</Icon>";
        xmlDataHeader += "<hotSpot x=\"0.5\" xunits=\"fraction\" y=\"0\" yunits=\"fraction\" />";
        xmlDataHeader += "</IconStyle>";
        xmlDataHeader += "</Style>";

        return xmlDataHeader;
    }

    private String getFooter(){
        return "</Document></kml>";
    }

    private String getFormattedDate(long timestamp, TimeZone timezone) {
        //TimeZone.getTimeZone("UTC");
        // TimeZone.getDefault();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (ZZZZ)");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat.format(new Date(timestamp));
    }

    private String getFormattedDateForFilename(long timestamp, TimeZone timezone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_S");
        simpleDateFormat.setTimeZone(timezone);
        return simpleDateFormat.format(new Date(timestamp));
    }
}

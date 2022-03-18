package com.johnc.remotesupportssh;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ClockCode {

    private String sshCode;
    private String vncCode;
    private String readerName;
    private Date date;
    private Path path;
    public String getSshCode() {
        return sshCode;
    }
    public void setSshCode(String sshCode) {
        this.sshCode = sshCode;
    }
    public String getVncCode() {
        return vncCode;
    }
    public void setVncCode(String vncCode) {
        this.vncCode = vncCode;
    }
    public String getReaderName() {
        return readerName;
    }
    public void setReaderName(String readerName) {
//		this.readerName = new String(readerName.getBytes(), StandardCharsets.ISO_8859_1);
        this.readerName = readerName;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public Path getPath() {
        return path;
    }
    public void setPath(Path path) {
        this.path = path;
    }
    private int getDayTimesMonth()
    {

        SimpleDateFormat utcDayFormat = new SimpleDateFormat("dd");
        SimpleDateFormat utcMonthFormat = new SimpleDateFormat("MM");
        TimeZone utc = TimeZone.getTimeZone("UTC");
        utcDayFormat.setTimeZone(utc);
        utcMonthFormat.setTimeZone(utc);

        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");

        int day = Integer.valueOf(utcDayFormat.format(this.date));
        int month = Integer.valueOf(utcMonthFormat.format(this.date));

        if (day < Integer.valueOf(dayFormat.format(new Date())))
        {
            day = Integer.valueOf(dayFormat.format(new Date()));
            month = Integer.valueOf(monthFormat.format(new Date()));
        }
        return day*month;
    }

    public String getPassword()
    {

        String lastFour="";
        if (readerName.length() > 4) {
            lastFour= this.readerName.substring(this.readerName.length()-4);
        } else {
            lastFour=this.readerName;
        }
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");

        int day = Integer.valueOf(dayFormat.format(new Date()));
        int month = Integer.valueOf(monthFormat.format(new Date()));

        String pwd ="$ynEL"+(day*month)+lastFour;
//		String pwd ="$ynEL"+getDayTimesMonth()+lastFour;
//		return new String(pwd.getBytes(), StandardCharsets.ISO_8859_1);
        return pwd;
    }

}

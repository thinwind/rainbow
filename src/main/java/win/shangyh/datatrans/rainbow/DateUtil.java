/*
 * Copyright 1999-2004 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package win.shangyh.datatrans.rainbow;


import java.util.Date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对日期进行处理的工具类
 *
 * 标准的日期格式是 yyyy-MM-dd HH:mm:ss
 */
public class DateUtil {

    private final static Logger logger = LoggerFactory.getLogger(DateUtil.class);
    
    private String dateFormat;
    
    private String dateTimeFormat;
    
    private String timeFormat;
    
    private final DateTimeFormatter dateFormatter;
    
    private final DateTimeFormatter dateTimeFormatter;
    
    private final DateTimeFormatter timeFormatter;
    
    //全参构造器
    public DateUtil(String dateFormat, String dateTimeFormat, String timeFormat) {
        this.dateFormat = dateFormat;
        this.dateTimeFormat = dateTimeFormat;
        this.timeFormat = timeFormat;
        //默认日期格式
        if(this.dateFormat==null){
            this.dateFormat="yyyy-MM-dd";
        }
        //默认日期时间格式
        if(this.dateTimeFormat==null){
            this.dateTimeFormat="yyyy-MM-dd HH:mm:ss";
        }
        //默认时间格式
        if(this.timeFormat==null){
            this.timeFormat="HH:mm:ss";
        }
        logger.debug("DateUtil init with dateFormat:{},dateTimeFormat:{},timeFormat:{}",dateFormat,dateTimeFormat,timeFormat);
        dateFormatter = DateTimeFormatter.ofPattern(this.dateFormat).withZone(ZoneId.of("Asia/Shanghai"));
        dateTimeFormatter = DateTimeFormatter.ofPattern(this.dateTimeFormat).withZone(ZoneId.of("Asia/Shanghai"));
        timeFormatter = DateTimeFormatter.ofPattern(this.timeFormat).withZone(ZoneId.of("Asia/Shanghai"));
    }
    
    public LocalDate parseDate(String dateStr){
        if(dateStr==null || dateStr.trim().isEmpty()){
            return null;
        }
       return LocalDate.parse(dateStr, dateFormatter);
    }
    
    public LocalDateTime parseDateTime(String dateStr){
        if(dateStr==null || dateStr.trim().isEmpty()){
            return null;
        }
        return LocalDateTime.parse(dateStr, dateTimeFormatter);
    }
    
    public LocalTime parseTime(String dateStr){
        if(dateStr==null || dateStr.trim().isEmpty()){
            return null;
        }
        return LocalTime.parse(dateStr, timeFormatter);
    }
    
    public String formatDateTime(Date date){
        if(date==null){
            return null;
        }
        return dateTimeFormatter.format(date.toInstant());
    }
    
    public String formatDate(LocalDate date){
        if(date==null){
            return null;
        }
        return dateFormatter.format(date);
    }
    
    public String formatTime(LocalTime time){
        if(time==null){
            return null;
        }
        return timeFormatter.format(time);
    }
    
    public String formatDateTime(LocalDateTime date){
        if(date==null){
            return null;
        }
        return dateTimeFormatter.format(date);
    }
    
}
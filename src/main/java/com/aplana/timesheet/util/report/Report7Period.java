package com.aplana.timesheet.util.report;

import com.aplana.timesheet.enums.Report07PeriodTypeEnum;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.aplana.timesheet.enums.Report07PeriodTypeEnum.*;

public class Report7Period implements Comparable {

    private Date start;
    private Date end;
    private Report07PeriodTypeEnum type;
    private Integer number;
    private String name;

    public Report7Period(Integer number, Date start, Date end, Report07PeriodTypeEnum type) throws Exception {
        this.start = start;
        this.end = end;
        this.type = type;
        this.number = number;
        this.name = this.generateName();
    }

    public Report7Period(String name) {
        this.name = name;
        this.number = Integer.MAX_VALUE;
    }

    public String getName() throws Exception {
        if (this.name == null) {
            setName(generateName());
        }
        return this.name;
    }

    @Override
    public String toString() {
        try {
            return getName();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return super.toString();
    }

    private void setName(String name) {
        this.name = name;
    }

    private String generateName() throws Exception {
        if (type.equals(PERIOD_TYPE_MONTH)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM.yyyy");
            return sdf.format(this.start);
        } else if (type.equals(PERIOD_TYPE_KVARTAL)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM");
            Integer number = Integer.valueOf(sdf.format(this.start));
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
            if (number > 0 && number < 4) {
                return "1-ый квартал " + sdf2.format(this.start);
            } else if (number > 3 && number < 7) {
                return "2-ой квартал" + sdf2.format(this.start);
            } else if (number > 6 && number < 10) {
                return "3-ий квартал " + sdf2.format(this.start);
            } else if (number > 9) {
                return "4-ый квартал " + sdf2.format(this.start);
            }
        } else if (type.equals(PERIOD_TYPE_HALF_YEAR)) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy");
            Integer number = new Integer(sdf.format(this.start));
            if (number > 0 && number < 7) {
                return "1-ое полугодие " + sdf2.format(this.start);
            } else {
                return "2-ое полугодие" + sdf2.format(this.start);
            }
        } else if (type.equals(PERIOD_TYPE_YEAR)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            return sdf.format(this.start) + "г.";
        }
        throw new Exception("Can't get period name not valid type");
    }

    public Integer getNumber() {
        return this.number;
    }

    public int compareTo(Object value) {
        Report7Period o = (Report7Period)value;
        if (o.getNumber().equals(this.getNumber())) {
            return 0;
        } else if(o.getNumber() > this.getNumber()) {
            return 1;
        } else {
            return -1;
        }
    }

}

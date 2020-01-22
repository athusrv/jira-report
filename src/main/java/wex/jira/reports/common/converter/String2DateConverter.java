package wex.jira.reports.common.converter;

import picocli.CommandLine;

import java.text.SimpleDateFormat;
import java.util.Date;

public class String2DateConverter implements CommandLine.ITypeConverter<Date> {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    private SimpleDateFormat formatter = new SimpleDateFormat(String2DateConverter.DATE_FORMAT);

    @Override
    public Date convert(String date) throws Exception {
        return formatter.parse(date);
    }
}

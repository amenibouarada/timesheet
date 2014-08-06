package com.aplana.timesheet.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by abayanov
 * Date: 05.08.14
 */
public class LoggingFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        //Фильтрация того что выводится в консоль
        List<String> excludeList = new ArrayList<String>(Arrays.asList(
                "AntPathRequestMatcher",
                "FilterChainProxy",
                "JpaTransactionManager",
                "OpenEntityManagerInViewFilter",
                "EntityManagerFactoryUtils"
        ));

        String[] loggerName = event.getLoggerName().split("\\.");

        if (!excludeList.contains(loggerName[loggerName.length - 1])) {
            return FilterReply.ACCEPT;
        } else {
            return FilterReply.DENY;
        }
    }
}

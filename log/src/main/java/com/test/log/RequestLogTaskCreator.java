package com.test.log;

import com.test.log.core.JoinPointExtractor;
import com.test.log.core.LogRepository;
import com.test.log.core.LogTask;
import com.test.log.core.RecordBuilder;
import org.aspectj.lang.JoinPoint;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Shoven
 * @date 2019-07-26 11:13
 */
public class RequestLogTaskCreator extends DefaultLogTaskCreator {

    public RequestLogTaskCreator(JoinPointExtractor extractor,
                                 List<LogRepository> repositories,
                                 RecordBuilder recordBuilder) {
        super(extractor, repositories, recordBuilder);
    }

    @Override
    public LogTask create(JoinPoint jp, Object value, Throwable cause, long cost) {
        HttpServletRequest request = UserContext.getRequest();
        RequestInfo requestInfo = new RequestInfo(request);
        RequestLogTask requestLogTask = new RequestLogTask(getExtractor(), getRepositories(), getRecordBuilder(),
                jp, requestInfo);
        requestLogTask.setCause(cause);
        requestLogTask.setValue(value);
        requestLogTask.setCost(cost);
        return requestLogTask;
    }
}

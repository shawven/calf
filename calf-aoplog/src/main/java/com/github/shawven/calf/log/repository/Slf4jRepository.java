package com.github.shawven.calf.log.repository;

import com.github.shawven.calf.log.core.JoinPointInfo;
import com.github.shawven.calf.log.core.LogRepository;
import com.github.shawven.calf.log.core.Recordable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shoven
 * @date 2019-07-26 15:04
 */
public class Slf4jRepository implements LogRepository {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jRepository.class);

    @Override
    public boolean isSupport(JoinPointInfo joinPointInfo) {
        return true;
    }

    @Override
    public void write(Recordable record) {
        logger.info(record.toString());
    }
}

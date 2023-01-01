package com.github.shawven.calf.oplog.client.annotation;



import com.github.shawven.calf.oplog.base.Const;
import com.github.shawven.calf.oplog.base.EventAction;

import java.lang.annotation.*;

/**
 * @author zhenhui
 * @Ddate Created in 2018/18/01/2018/7:02 PM
 * @modified by
 */
@Documented
@Target(ElementType.TYPE)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface HandleDatabaseEvent {

    String namespace() default Const.DEFAULT_NAMESPACE;

    String database();

    String table();

    EventAction[] events();
}

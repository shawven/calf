package com.starter.oplog.client;


import com.starter.oplog.parm.DatabaseEvent;
import com.starter.oplog.parm.LockLevel;

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

    String namespace() default "default";

    String database();

    String table();

    DatabaseEvent[] events();

    LockLevel lockLevel() default LockLevel.NONE;

    String columnName() default "";
}

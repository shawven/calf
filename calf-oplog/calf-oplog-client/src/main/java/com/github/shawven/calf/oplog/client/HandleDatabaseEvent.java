package com.github.shawven.calf.oplog.client;



import com.github.shawven.calf.base.DatabaseEvent;
import com.github.shawven.calf.base.LockLevel;

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

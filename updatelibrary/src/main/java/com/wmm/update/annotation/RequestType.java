package com.wmm.update.annotation;
import android.support.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.wmm.update.constants.Const.GET;
import static com.wmm.update.constants.Const.POST;

@StringDef({GET, POST})
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE)
public @interface RequestType {
}

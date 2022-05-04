package com.tuling.serialize.util;

/**
 * 该枚举定义了某些值可能出现的三种情况：一定是，一定不是，可能是;
 * 例如对于一个对象的类型是否是基本类型的判定
 * Created by ljg on 2022/5/2.
 */
public enum SituationEnum {
    MUST_BE,
    MUST_NOT,
    POSSIBLE_BE
}

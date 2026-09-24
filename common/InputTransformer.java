package com.ing.bankguarantees.remote.common;

public interface InputTransformer<T, E, OUT> {

    OUT transform(T input1, E input2);
}
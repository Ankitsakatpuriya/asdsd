package com.ing.bankguarantees.remote.kafka.engagementsuite.producer.callback;

public interface NotificationFutureCallback {
    void onFailure(Throwable throwable);

    void onSuccess();
}

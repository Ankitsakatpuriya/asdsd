package com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler;


import com.ing.bankguarantees.remote.kafka.engagementsuite.model.FeedbackEvent;

public interface FeedbackHandler {

    void handleFailure(FeedbackEvent feedbackEvent);

    void handleSuccess(FeedbackEvent feedbackEvent);
}

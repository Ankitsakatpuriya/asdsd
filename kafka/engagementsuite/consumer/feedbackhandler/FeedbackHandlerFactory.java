package com.ing.bankguarantees.remote.kafka.engagementsuite.consumer.feedbackhandler;

import com.ing.bankguarantees.remote.kafka.engagementsuite.model.SupportedCommunication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedbackHandlerFactory {


    public FeedbackHandler getFeedbackHandler(SupportedCommunication supportedCommunication) {

        return switch (supportedCommunication) {
            case NON_STP_EMAIL_NOTIFICATION -> new NonStpEmailNotificationFeedbackHandler();
            case CUSTOMER_EMAIL_NOTIFICATION -> new CustomerEmailNotificationFeedbackHandler();
            case BENEFICIARY_EMAIL_NOTIFICATION -> new BeneficiaryEmailNotificationFeedbackHandler();
            case FULFILMENT_FAILURE_EMAIL_NOTIFICATION -> new FulfilmentFailureEmailNotificationFeedbackHandler();
            case STP_RESULTS_EMAIL_NOTIFICATION -> new StpResultsEmailNotificationFeedbackHandler();
            case SIGN_COMPLETION_EMAIL_NOTIFICATION -> new SignCompletionEmailNotificationFeedbackHandler();
            case SIGNATURE_REMINDER_EMAIL_NOTIFICATION -> new SignatureReminderEmailNotificationFeedbackHandler();
            case REQUESTER_PARTIALLY_SIGN_EMAIL_NOTIFICATION ->
                    new RequesterPartiallySignedEmailNotificationFeedbackHandler();
        };
    }
}

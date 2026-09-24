package com.ing.bankguarantees.remote.rest.ccaas.dossier;

import com.ing.apisdk.toolkit.connectivity.api.JavaService;
import com.ing.apisdk.toolkit.connectivity.filter.RequestTransformingFilter;
import com.ing.bankguarantees.error.config.ErrorSource;
import com.ing.bankguarantees.remote.common.ClientGateway;
import com.ing.bankguarantees.remote.common.ResponseValidator;
import com.ing.bankguarantees.remote.factory.RestServiceFactory;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.AgreementDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.DocumentPlaceHolderIn;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.request.RequestDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.AgreementDossierDataResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.model.response.DossierDataResponse;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer.AgreementDossierReqTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer.DossierResponseTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer.PlaceholderDossierReqTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.create.transformer.RequestDossierReqTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.model.request.UpdateDossierDataInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.transformer.UpdateAgreementDossierRequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.transformer.UpdateDocumentDossierRequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.update.transformer.UpdateRequestDossierRequestTransformer;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.model.request.UploadDocumentInput;
import com.ing.bankguarantees.remote.rest.ccaas.dossier.upload.transform.UploadDocumentRequestTransformer;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.concurrent.ExecutorService;


@Validated
@Configuration
@RequiredArgsConstructor
public class DossierApiConfig {


    @Bean("requestDossierRestClient")
    public JavaService<Request, DossierDataResponse> requestDossierRestClient(
            @Qualifier("routingResilientHttpClient")  Service<Request, Response> httpClient,
             RestServiceFactory
                    restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                DossierDataResponse.class,
                ErrorSource.CCA);
    }

    @Bean("agreementDossierRestClient")
    public JavaService<Request, AgreementDossierDataResponse> agreementDossierRestClient(
            @Qualifier("routingResilientHttpClient")  Service<Request, Response> httpClient,
             RestServiceFactory
                    restServiceFactory) {

        return restServiceFactory.getRestService(
                httpClient,
                new RequestTransformingFilter<>(input -> input),
                AgreementDossierDataResponse.class,
                ErrorSource.CCA);
    }


    @Bean("requestDossierClientGateway")
    public ClientGateway<RequestDossierDataInput, String, DossierDataResponse> requestDossierClientGateway(
             JavaService<Request, DossierDataResponse> requestDossierRestClient,
             RequestDossierReqTransformer requestDossierReqTransformer,
             DossierResponseTransformer dossierResponseTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService
    ) {
        return new ClientGateway<>(requestDossierRestClient, requestDossierReqTransformer::transform, (res, in) -> dossierResponseTransformer.transform(res),
                new ResponseValidator<DossierDataResponse>(validator)::validate, executorService);
    }


    @Bean("agreementDossierClientGateway")
    public ClientGateway<AgreementDossierDataInput, AgreementDossierDataResponse, AgreementDossierDataResponse> agreementDossierClientGateway(
             JavaService<Request, AgreementDossierDataResponse> agreementDossierRestClient,
             AgreementDossierReqTransformer agreementDossierReqTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService
    ) {
        return new ClientGateway<>(agreementDossierRestClient, agreementDossierReqTransformer::transform,
                new ResponseValidator<AgreementDossierDataResponse>(validator)::validate, executorService);
    }

    @Bean("placeholderDossierClientGateway")
    public ClientGateway<DocumentPlaceHolderIn, String, DossierDataResponse> placeholderDossierClientGateway(
             JavaService<Request, DossierDataResponse> requestDossierRestClient,
             PlaceholderDossierReqTransformer placeholderDossierReqTransformer,
             DossierResponseTransformer dossierResponseTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService
    ) {
        return new ClientGateway<>(requestDossierRestClient, placeholderDossierReqTransformer::transform, (res, in) -> dossierResponseTransformer.transform(res),
                new ResponseValidator<DossierDataResponse>(validator)::validate, executorService);
    }


    @Bean(name = "updateRequestDossierDataGateway")
    public ClientGateway<UpdateDossierDataInput, String, DossierDataResponse> updateRequestDossierDataGateway(
             JavaService<Request, DossierDataResponse> requestDossierRestClient,
             UpdateRequestDossierRequestTransformer requestTransformer,
             DossierResponseTransformer responseTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService) {

        return new ClientGateway<>(requestDossierRestClient, requestTransformer::transform, (res, in) -> responseTransformer.transform(res),
                res -> new ResponseValidator<DossierDataResponse>(validator).validate(res), executorService);
    }


    @Bean(name = "updateAgreementDossierDataGateway")
    public ClientGateway<UpdateDossierDataInput, String, DossierDataResponse> updateAgreementDossierDataGateway(
             JavaService<Request, DossierDataResponse> requestDossierRestClient,
             UpdateAgreementDossierRequestTransformer requestTransformer,
             DossierResponseTransformer responseTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService) {

        return new ClientGateway<>(requestDossierRestClient, requestTransformer::transform, (res, in) -> responseTransformer.transform(res),
                res -> new ResponseValidator<DossierDataResponse>(validator).validate(res), executorService);
    }


    @Bean(name = "updateDocumentDossierDataGateway")
    public ClientGateway<UpdateDossierDataInput, String, DossierDataResponse> updateDocumentDossierDataGateway(
             JavaService<Request, DossierDataResponse> requestDossierRestClient,
             UpdateDocumentDossierRequestTransformer requestTransformer,
             DossierResponseTransformer responseTransformer,
             Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService) {

        return new ClientGateway<>(requestDossierRestClient, requestTransformer::transform, (res, in) -> responseTransformer.transform(res),
                res -> new ResponseValidator<DossierDataResponse>(validator).validate(res), executorService);

    }

    @Bean("uploadDocumentClientGateway")
    public ClientGateway<UploadDocumentInput, String, DossierDataResponse> uploadDocumentClientGateway(
            JavaService<Request, DossierDataResponse> requestDossierRestClient,
            UploadDocumentRequestTransformer uploadDocumentRequestTransformer,
            DossierResponseTransformer dossierResponseTransformer,
            Validator validator,
            @Qualifier("workStealingPool")  ExecutorService executorService
    ) {
        return new ClientGateway<>(requestDossierRestClient, uploadDocumentRequestTransformer::transform, (res, in) -> dossierResponseTransformer.transform(res),
                new ResponseValidator<DossierDataResponse>(validator)::validate, executorService);
    }
}

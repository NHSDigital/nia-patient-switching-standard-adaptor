package uk.nhs.adaptors.pss.gpc.controller.handler;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity.ERROR;
import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueSeverity.INFORMATION;
import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueType.EXCEPTION;
import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueType.NOTFOUND;
import static org.hl7.fhir.dstu3.model.OperationOutcome.IssueType.NOTSUPPORTED;
import static org.springframework.http.HttpHeaders.ALLOW;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import static uk.nhs.adaptors.pss.gpc.controller.handler.FhirMediaTypes.APPLICATION_FHIR_JSON_VALUE;
import static uk.nhs.adaptors.pss.gpc.util.CodeableConceptUtils.createCodeableConcept;
import static uk.nhs.adaptors.pss.gpc.util.OperationOutcomeUtils.createOperationOutcome;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.pss.gpc.service.FhirParser;

@ControllerAdvice
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class OperationOutcomeExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String ISSUE_SYSTEM = "https://fhir.nhs.uk/STU3/ValueSet/Spine-ErrorOrWarningCode-1";
    private static final Map<String, List<HttpMethod>> ALLOWED_METHODS = Map.of(
        "/Patient/$gpc.migratestructuredrecord", List.of(POST)
    );

    private FhirParser fhirParser;

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
        String errorMessage = servletReq.getRequestURI() + " not found";
        CodeableConcept details = createCodeableConcept("RESOURCE_NOT_FOUND", ISSUE_SYSTEM, "Resource not found", errorMessage);
        OperationOutcome operationOutcome = createOperationOutcome(NOTFOUND, INFORMATION, details, null); // czy "information" jak w przykladzie?
        return errorResponse(requestHeaders, status, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.put(ALLOW, List.of(getAllowedMethods(request)));
        CodeableConcept details = createCodeableConcept("METHOD_NOT_SUPPORTED", ISSUE_SYSTEM, "Method not supported", ex.getMessage()); // czy tak moze byc?
        OperationOutcome operationOutcome = createOperationOutcome(NOTSUPPORTED, ERROR, details, null);
        return errorResponse(headers, status, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        HttpMediaTypeNotSupportedException ex, HttpHeaders requestHeaders, HttpStatus status, WebRequest request) {
        CodeableConcept details = createCodeableConcept("UNSUPPORTED_MEDIA_TYPE", ISSUE_SYSTEM, "Unsupported media type", null);
        OperationOutcome operationOutcome = createOperationOutcome(NOTSUPPORTED, ERROR, details, ex.getMessage());
        return errorResponse(requestHeaders, status, operationOutcome);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleAllExceptions(ex);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<Object> handleAllExceptions(Exception ex) {
        LOGGER.error("Error occurred: {}", ex.getMessage());
//        OperationOutcome operationOutcome;
//        HttpStatus httpStatus;
//        if (ex instanceof OperationOutcomeError) {
//            OperationOutcomeError error = (OperationOutcomeError) ex;
//            operationOutcome = error.getOperationOutcome();
//            httpStatus = error.getStatusCode();
//        } else {
        CodeableConcept details = createCodeableConcept("INTERNAL_SERVER_ERROR", ISSUE_SYSTEM, "Internal server error", null);
        OperationOutcome operationOutcome = createOperationOutcome(EXCEPTION, ERROR, details, ex.getMessage());
//        HttpStatus httpStatus = INTERNAL_SERVER_ERROR;
//        }
        return errorResponse(new HttpHeaders(), INTERNAL_SERVER_ERROR, operationOutcome);
    }

    private ResponseEntity<Object> errorResponse(HttpHeaders headers, HttpStatus status, OperationOutcome operationOutcome) {
        headers.put(CONTENT_TYPE, singletonList(APPLICATION_FHIR_JSON_VALUE));
        String content = fhirParser.encodeToJson(operationOutcome);
        return new ResponseEntity<>(content, headers, status);
    }

    private String getAllowedMethods(WebRequest request) {
        HttpServletRequest servletReq = ((ServletWebRequest) request).getRequest();
        String requestURI = servletReq.getRequestURI();
        return ALLOWED_METHODS.get(requestURI)
            .stream()
            .map(Enum::name)
            .collect(joining(","));
    }
}

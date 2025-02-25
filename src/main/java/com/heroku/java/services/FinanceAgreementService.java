package com.heroku.java.services;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.ws.ConnectionException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "Finance Agreement Calculation", description = "Calculates finance agreements for car purchases based on valuation, credit status, and business margins.")
@RestController
@RequestMapping("/api/")
public class FinanceAgreementService {

    @Operation(
        summary = "Calculate a Finance Agreement",
        description = "Processes a finance agreement based on car valuation, customer credit profile, business margin constraints, and competitor pricing.",
        responses = { @ApiResponse(responseCode = "200", description = "Response containing the calculated finance agreement. Describe the results in natural language text to the user.")})
    @PostMapping("/calculateFinanceAgreement")
    public FinanceCalculationResponse calculateFinanceAgreement(
            @RequestBody(
                description = "Request to compute a finance agreement for a car purchase, including the Salesforce record ID of both the customer applying for financing and the vehicle being financed.", 
                content = @Content(schema = @Schema(implementation = FinanceCalculationRequest.class)))
            FinanceCalculationRequest request,
            HttpServletRequest httpServletRequest) throws ConnectionException {
            
        // Query Vehicle information from Salesforce
        PartnerConnection connection = (PartnerConnection) httpServletRequest.getAttribute("salesforcePartnerConnection");
        String soql = String.format(
            "SELECT Id, Color__c, Fuel_Type__c, Make__c, Mileage__c, Model__c, Price__c, Status__c, Year__c " +
            "FROM Vehicle_Model__c " +
            "WHERE Id = '%s' ", 
            request.vehicleId);
        QueryResult queryResult = connection.query(soql);    

        // Mocked Response Data
        FinanceCalculationResponse response = new FinanceCalculationResponse();
        response.recommendedFinanceOffer = new FinanceOffer();
        response.recommendedFinanceOffer.finalCarPrice = 41800;
        response.recommendedFinanceOffer.adjustedInterestRate = 3.4;
        response.recommendedFinanceOffer.monthlyPayment = 690.50;
        response.recommendedFinanceOffer.loanTermMonths = 60;
        response.recommendedFinanceOffer.totalFinancingCost = 41430.00;
        return response;
    }

    @Schema(description = "Request to compute a finance agreement for a car purchase, including the Salesforce record ID of both the customer applying for financing and the vehicle being financed.")
    public static class FinanceCalculationRequest {
        @Schema(example = "0035g00000XyZbHAZ", description = "The Salesforce record ID of the customer applying for financing.")
        public String customerId;
        @Schema(example = "a0B5g00000LkVnWEAV", description = "The Salesforce record ID of the car being financed.")
        public String vehicleId;
        @Schema(example = "3.5%", description = "The maximum interest rate the user is prepared to go to")
        public double maxInterestRate;
        @Schema(example = "1000", description = "The down payment the user is prepared to give")
        public double downPayment;
        @Schema(example = "3", description = "The number years to pay the finance the user is reuqesting")
        public int years;
    }

    @Schema(description = "Response containing the calculated finance agreement. Describe the results in natural language text to the user.")
    public static class FinanceCalculationResponse {
        public FinanceOffer recommendedFinanceOffer;
    }

    @Schema(description = "Recommended finance offer based on business rules and customer affordability.")
    public static class FinanceOffer {
        public double finalCarPrice;
        public double adjustedInterestRate;
        public double monthlyPayment;
        public int loanTermMonths;
        public double totalFinancingCost;
    }
}

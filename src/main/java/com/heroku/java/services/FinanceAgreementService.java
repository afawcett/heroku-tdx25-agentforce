package com.heroku.java.services;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.sobject.SObject;
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
        responses = { 
            @ApiResponse(responseCode = "200", description = "Response containing the calculated finance agreement."),
            @ApiResponse(responseCode = "404", description = "Vehicle not found in Salesforce."),
            @ApiResponse(responseCode = "503", description = "Salesforce connection error."),
            @ApiResponse(responseCode = "500", description = "Unexpected server error.")
        })
    @PostMapping("/calculateFinanceAgreement")
    public FinanceCalculationResponse calculateFinanceAgreement(
            @org.springframework.web.bind.annotation.RequestBody
            @RequestBody(
                description = "Request to compute a finance agreement for a car purchase, including the Salesforce record ID of both the customer applying for financing and the vehicle being financed.", 
                content = @Content(schema = @Schema(implementation = FinanceCalculationRequest.class)))
            FinanceCalculationRequest request,
            HttpServletRequest httpServletRequest) {
            
        // Obtain Salesforce connection for Heroku Integration add-on
        PartnerConnection connection = (PartnerConnection) httpServletRequest.getAttribute("salesforcePartnerConnection");
        if (connection == null) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Salesforce connection is not available.");
        }

        try {
            // Query Vehicle information from Salesforce
            String soql = String.format(
                "SELECT Id, Price__c FROM Vehicle_Model__c WHERE Id = '%s' ", 
                request.vehicleId);
            QueryResult queryResult = connection.query(soql);

            // If no vehicle is found, return 404 error
            if (queryResult.getSize() == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found in Salesforce.");
            }

            // Retrieve vehicle price
            SObject vehicle = queryResult.getRecords()[0];
            double vehiclePrice = Double.parseDouble(vehicle.getField("Price__c").toString());

            // Simple finance calculations
            double loanAmount = vehiclePrice - request.downPayment;
            double annualInterestRate = Math.min(3.5, request.maxInterestRate); // Cap interest rate at 3.5% for demo
            int loanTermMonths = request.years * 12;

            // Monthly Payment Calculation (Basic Loan Formula)
            double monthlyInterestRate = (annualInterestRate / 100) / 12;
            double monthlyPayment = (loanAmount * monthlyInterestRate) / (1 - Math.pow(1 + monthlyInterestRate, -loanTermMonths));            
            double totalFinancingCost = monthlyPayment * loanTermMonths;

            // Build response
            FinanceCalculationResponse response = new FinanceCalculationResponse();
            response.recommendedFinanceOffer = new FinanceOffer();
            response.recommendedFinanceOffer.finalCarPrice = vehiclePrice;
            response.recommendedFinanceOffer.adjustedInterestRate = annualInterestRate;
            response.recommendedFinanceOffer.monthlyPayment = monthlyPayment;
            response.recommendedFinanceOffer.loanTermMonths = loanTermMonths;
            response.recommendedFinanceOffer.totalFinancingCost = totalFinancingCost;

            return response;

        } catch (ConnectionException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to connect to Salesforce.", e);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", e);
        }
    }

    @Schema(description = "Request to compute a finance agreement for a car purchase, including the Salesforce record ID of both the customer applying for financing and the vehicle being financed.")
    public static class FinanceCalculationRequest {
        @Schema(example = "0035g00000XyZbHAZ", description = "The Salesforce record ID of the customer applying for financing.")
        public String customerId;
        @Schema(example = "a0B5g00000LkVnWEAV", description = "The Salesforce record ID of the car being financed.")
        public String vehicleId;
        @Schema(example = "3.5", description = "The maximum interest rate the user is prepared to go to (percentage).")
        public double maxInterestRate;
        @Schema(example = "1000", description = "The down payment the user is prepared to give.")
        public double downPayment;
        @Schema(example = "3", description = "The number of years to pay the finance the user is requesting.")
        public int years;
    }

    @Schema(description = "Response containing the calculated finance agreement.")
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

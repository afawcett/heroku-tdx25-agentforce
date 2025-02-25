package com.heroku.java.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Carbon Footprint Calculation", description = "Calculates the carbon footprint for a given flight based on external data.")
@RestController
@RequestMapping("/api/")
public class CarbonFootprintService {

    private static final Logger logger = LoggerFactory.getLogger(CarbonFootprintService.class);

    @Operation(summary = "Calculate Carbon Footprint", description = "Calculates the carbon footprint for a given flight based on external data.")
    @PostMapping("/calculateCarbonFootprint")
    public CarbonFootprintResponse calculateCarbonFootprint(
            @RequestBody CarbonFootprintRequest request, HttpServletRequest httpServletRequest) {
        logger.info("Processing carbon calculation for Flight ID: {}", request.flightId);
        // Mocked Response Data
        CarbonFootprintResponse response = new CarbonFootprintResponse();
        response.flight = new FlightInfo();
        response.flight.flightNumber = "AA100";
        response.flight.departureAirport = "JFK";
        response.flight.arrivalAirport = "LHR";
        response.flight.aircraftType = "Boeing 777-300ER";
        response.flight.distanceKm = 5540;
        response.flight.passengerCount = 250;
        response.emissions = new EmissionsData();
        response.emissions.totalCo2Kg = 52000.5;
        response.emissions.co2PerPassengerKg = 208.0;
        response.emissions.co2PerKmKg = 9.39;
        response.emissions.fuelBurnedLiters = 16600.0;
        response.methodology = new Methodology();
        response.methodology.calculationBasis = "DEFRA 2023 emission factors";
        response.methodology.fuelToCo2Ratio = 3.16;
        response.methodology.radiativeForcingMultiplier = 1.9;
        response.methodology.dataSource = "ICAO Carbon Emissions Calculator";
        response.timestamp = Instant.now().toString();
        response.units = new HashMap<>();
        response.units.put("distance", "km");
        response.units.put("emissions", "kg CO2e");
        response.units.put("fuel", "liters");
        return response;
    }

    @Schema(description = "Request to calculate the carbon footprint of a flight, including the Salesforce record ID of the flight being analyzed.")
    public static class CarbonFootprintRequest {
        @Schema(example = "0035g00000XyZbHAZ", description = "The Salesforce record ID of the flight.")
        public String flightId;
    }

    @Schema(description = "Response containing the calculated carbon footprint for the flight. Describe the results in natural language text to the user.")
    public static class CarbonFootprintResponse {
        public FlightInfo flight;
        public EmissionsData emissions;
        public Methodology methodology;
        public String timestamp;
        public Map<String, String> units;
    }

    @Schema(description = "Flight information details.")
    public static class FlightInfo {
        public String flightNumber;
        public String departureAirport;
        public String arrivalAirport;
        public String aircraftType;
        public int distanceKm;
        public int passengerCount;
    }

    @Schema(description = "Carbon emissions data for the flight.")
    public static class EmissionsData {
        public double totalCo2Kg;
        public double co2PerPassengerKg;
        public double co2PerKmKg;
        public double fuelBurnedLiters;
    }

    @Schema(description = "Details about the methodology used for emissions calculation.")
    public static class Methodology {
        public String calculationBasis;
        public double fuelToCo2Ratio;
        public double radiativeForcingMultiplier;
        public String dataSource;
    }
}

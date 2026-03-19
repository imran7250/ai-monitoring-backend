//
//
//package com.imran.aimonitoring.dto;
//
//import lombok.Data;
//
//@Data
//public class CreateServiceRequest {
//
//    private Long projectId;   // REQUIRED
//
//    private String name;
//    private String baseUrl;
//    private String type;      // API, WEBSITE, MICROSERVICE
//}

package com.imran.aimonitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ✅ IMPROVEMENT #9 — Added @NotBlank, @NotNull, @Pattern validation
//    Before: No validation — null name, null baseUrl, invalid type
//            could silently reach the database.
//    After:  All fields validated before reaching the service layer.

@Data
public class CreateServiceRequest {

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "Service name is required")
    @Size(min = 2, max = 100, message = "Service name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Base URL is required")
    @Size(max = 500, message = "Base URL cannot exceed 500 characters")
    private String baseUrl;

    // ✅ Validates that type is one of the allowed values
    @NotBlank(message = "Service type is required")
    @Pattern(
        regexp = "API|WEBSITE|MICROSERVICE|SERVER|DATABASE",
        message = "Type must be one of: API, WEBSITE, MICROSERVICE, SERVER, DATABASE"
    )
    private String type;
}
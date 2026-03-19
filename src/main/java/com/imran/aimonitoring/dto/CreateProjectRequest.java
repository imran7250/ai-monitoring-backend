//package com.imran.aimonitoring.dto;
//
//public class CreateProjectRequest {
//
//    private String name;
//    private String description;
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//}

package com.imran.aimonitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// ✅ IMPROVEMENT #9 — Added @NotBlank and @Size validation
//    Before: No validation — null or blank project name could reach
//            the database causing cryptic constraint violations.
//    After:  Spring validates automatically when @Valid is used
//            in ProjectController. Clean error message returned.

public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
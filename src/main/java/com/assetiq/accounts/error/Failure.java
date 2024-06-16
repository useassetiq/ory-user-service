package com.assetiq.accounts.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Failure(String message, String code, @JsonIgnore Throwable cause) {}

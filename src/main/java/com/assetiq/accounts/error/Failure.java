package com.assetiq.accounts.error;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Failure {
    private final String message;
   private final String code;

   private final Throwable cause;
}

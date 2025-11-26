package com.se114p12.backend.annotations;

import com.se114p12.backend.vo.ErrorVO;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.TYPE})
@ApiResponse(
    responseCode = "400",
    description = "Error response",
    content =
        @Content(
            schema = @Schema(implementation = ErrorVO.class),
            examples = {
              @ExampleObject(
                  name = "Bad Request Error Response",
                  value =
                      """
                            {
                                "type": "BAD_REQUEST",
                                "details": {
                                  "message": "Invalid request"
                                }
                            }
                      """)
            }))
public @interface ErrorResponse {}

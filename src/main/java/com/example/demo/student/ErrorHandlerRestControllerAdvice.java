package com.example.demo.student;

import com.example.demo.model.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@Slf4j
@RestControllerAdvice // used for Rest APIs
public class ErrorHandlerRestControllerAdvice {

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void processNoData(NoSuchElementException ex,
                              HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("Data not found: " + request.getRequestURL(), ex);
        response.sendError(HttpStatus.BAD_REQUEST.value(), "Requested data not found");
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponseDTO> processRuntimeException(RuntimeException ex,
                                                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("Internal Server Exception: " + request.getRequestURL(), ex);
        response.sendError(HttpStatus.BAD_REQUEST.value(), "Requested data not found");
        ErrorResponseDTO errorResponseDTO =  new ErrorResponseDTO(OffsetDateTime.now().toString(), HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error" , ex.getMessage());
        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
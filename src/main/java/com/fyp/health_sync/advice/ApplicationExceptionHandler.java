package com.fyp.health_sync.advice;

import com.fyp.health_sync.exception.BadRequestException;
import com.fyp.health_sync.exception.ForbiddenException;
import com.fyp.health_sync.exception.InternalServerErrorException;
import com.fyp.health_sync.exception.UserNotFoundException;
import com.fyp.health_sync.utils.ErrorResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleInvalidArgument(MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());

        List<String> errorMessages = new ArrayList<>();
        e.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errorMessages.add(fieldError.getDefaultMessage());
        });
        errorResponse.setMessage(errorMessages);

        return errorResponse;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UserNotFoundException.class})
    public ErrorResponse handleUserNotFound(UserNotFoundException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(404);
        errorResponse.setError("Not Found");
        List<String> message = new ArrayList<>();
        message.add(e.getMessage());
        errorResponse.setMessage(message);
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UsernameNotFoundException.class})
    public ErrorResponse handleUserNameNotFound(UsernameNotFoundException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(404);
        errorResponse.setError("Not Found");
        List<String> message = new ArrayList<>();
        message.add(e.getMessage());
        errorResponse.setMessage(message);
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ErrorResponse handleUserExist(DataIntegrityViolationException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(400);
        errorResponse.setError("Bad Request");
        List<String> message = new ArrayList<>();
        message.add(e.getMessage());
        errorResponse.setMessage(message);
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class})
    public ErrorResponse handleBadRequest(BadRequestException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(400);
        errorResponse.setError("Bad Request");
        List<String> message = new ArrayList<>();
        message.add(e.getMessage());
        errorResponse.setMessage(message);
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({ForbiddenException.class})
    public ErrorResponse handleForbidden(ForbiddenException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(403);
        errorResponse.setError("Forbidden");
        List<String> message = new ArrayList<>();
        message.add(e.getMessage());
        errorResponse.setMessage(message);
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({InternalServerErrorException.class})
    public ErrorResponse handleInternalServerError(InternalServerErrorException e){
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(500);
        errorResponse.setError("Internal Server Error");
        List<String> message = new ArrayList<>();
        message.add(e.getMessage());
        errorResponse.setMessage(message);
        return errorResponse;
    }
}

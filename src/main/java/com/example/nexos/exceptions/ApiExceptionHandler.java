package com.example.nexos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFoundException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problemDetail.setTitle("Recurso não encontrado");

        return problemDetail;
    }

    @ExceptionHandler(InvalidServiceOrderStatusException.class)
    public ProblemDetail handleInvalidServiceOrderStatus(InvalidServiceOrderStatusException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Transição de status inválida");

        return problemDetail;
    }

    @ExceptionHandler(InvalidServiceOrderDeletionException.class)
    public ProblemDetail handleInvalidServiceOrderDeletion(InvalidServiceOrderDeletionException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Exclusão de ordem não permitida");

        return problemDetail;
    }

    @ExceptionHandler(InvalidServiceOrderFilterException.class)
    public ProblemDetail handleInvalidServiceOrderFilter(InvalidServiceOrderFilterException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setTitle("Filtro de ordem inválido");

        return problemDetail;
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problemDetail.setTitle("Credenciais inválidas");

        return problemDetail;
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public ProblemDetail handleEmailAlreadyInUse(EmailAlreadyInUseException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("E-mail já cadastrado");

        return problemDetail;
    }

    @ExceptionHandler(InvalidUserOperationException.class)
    public ProblemDetail handleInvalidUserOperation(InvalidUserOperationException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problemDetail.setTitle("Operação de usuário não permitida");

        return problemDetail;
    }

}

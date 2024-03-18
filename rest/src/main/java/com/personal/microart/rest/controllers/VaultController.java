package com.personal.microart.rest.controllers;

import com.personal.microart.api.base.ProcessorInput;
import com.personal.microart.api.errors.ApiError;
import com.personal.microart.api.operations.user.login.LoginInput;
import com.personal.microart.api.operations.user.login.LoginOperation;
import com.personal.microart.api.operations.user.login.LoginResult;
import com.personal.microart.api.operations.user.logout.LogoutInput;
import com.personal.microart.api.operations.user.logout.LogoutOperation;
import com.personal.microart.api.operations.user.register.RegisterInput;
import com.personal.microart.api.operations.user.register.RegisterOperation;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordInput;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordOperation;
import com.personal.microart.api.operations.user.resetpassword.ResetPasswordInput;
import com.personal.microart.api.operations.user.resetpassword.ResetPasswordOperation;
import com.personal.microart.api.operations.user.verifypassordresettoken.VerifyPasswordResetTokenInput;
import com.personal.microart.api.operations.user.verifypassordresettoken.VerifyPasswordResetTokenOperation;
import com.personal.microart.api.operations.vault.adduser.AddUserInput;
import com.personal.microart.api.operations.vault.adduser.AddUserOperation;
import com.personal.microart.api.operations.vault.removeuser.RemoveUserInput;
import com.personal.microart.api.operations.vault.removeuser.RemoveUserOperation;
import com.personal.microart.core.processor.ProcessorInputValidator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * A controller that is responsible for handling requests related to vaults and authorized vault users -
 * creating, updating, deleting vaults, adding and removing users from vaults, and managing user rights to vaults.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/vault")
public class VaultController extends BaseController {

    private final ProcessorInputValidator inputValidator;
    private final ExchangeAccessor exchangeAccessor;
    private final AddUserOperation addUser;
    private final RemoveUserOperation removeUser;

    @PostConstruct
    private void setExchangeAccessor() {
        super.setExchangeAccessor(exchangeAccessor);
    }

    /**
     * Adds a user to the list of authorized users of a vault
     */
    @PostMapping(path = "/{vaultName}/user")
    @ResponseBody
    public ResponseEntity<?> addAuthorizedUser(@RequestBody AddUserInput userInput,
                                               @PathVariable String vaultName,
                                               HttpServletResponse response) {

        AddUserInput input = AddUserInput
                .builder()
                .vaultName(vaultName)
                .username(userInput.getUsername())
                .build();

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.addUser.process(input), response, HttpStatus.CREATED);
    }


    /**
     * Removes a user from the list of authorized users of a vault
     */
    @DeleteMapping(path = "/{vaultName}/user")
    @ResponseBody
    public ResponseEntity<?> removeAuthorizedUser(@RequestBody RemoveUserInput userInput,
                                                  @PathVariable String vaultName,
                                                  HttpServletResponse response) {

        RemoveUserInput input = RemoveUserInput
                .builder()
                .vaultName(vaultName)
                .username(userInput.getUsername())
                .build();

        Either<ApiError, ProcessorInput> validationResult = this.inputValidator.validateInput(input);

        return validationResult.isLeft()
                ? this.handle(validationResult, response)
                : this.handle(this.removeUser.process(input), response, HttpStatus.NO_CONTENT);
    }


}

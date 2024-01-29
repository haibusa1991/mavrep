package com.personal.microart.api.base;

import com.personal.microart.api.errors.ApiError;
import io.vavr.control.Either;

public interface  Processor<R extends ProcessorResult, I extends ProcessorInput> {

    Either<ApiError, R> process(I input);
}

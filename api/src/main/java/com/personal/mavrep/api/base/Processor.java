package com.personal.mavrep.api.base;

import com.personal.mavrep.api.errors.ApiError;
import com.personal.mavrep.api.errors.BaseApiError;
import io.vavr.control.Either;

public interface  Processor<R extends ProcessorResult, I extends ProcessorInput> {

    Either<ApiError, R> process(I input);
}

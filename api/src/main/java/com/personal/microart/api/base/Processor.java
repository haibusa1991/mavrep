package com.personal.microart.api.base;

import com.personal.microart.api.errors.ApiError;
import io.vavr.control.Either;

/**
 * The processor is the basic building block of the application. Every operation should be described as a processor,
 * and consequently implemented as a Core in the core module. The processing might fail, in which case an appropriate
 * ApiError is returned.
 *
 * @param <R> the type of the result produced by the processor. It must extend/implement ProcessorResult.
 * @param <I> the type of the input consumed by the processor. It must extend/implement ProcessorInput.
 */
public interface Processor<R extends ProcessorResult, I extends ProcessorInput> {

    /**
     * Process the given input and produce a result.
     * If the processing fails, an ApiError is returned.
     *
     * @param input the input to be processed
     * @return a result of the processing, wrapped in an Either.
     * If the processing was successful, the result is a Right of the Either.
     * If the processing failed, an appropriate implementation of the ApiError is returned.
     */
    Either<ApiError, R> process(I input);
}
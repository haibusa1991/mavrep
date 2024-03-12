package com.personal.microart.api.base;

import com.personal.microart.api.errors.ApiError;
import io.vavr.control.Either;

/**
 * The processor is the basic building block of the application. Every operation should be described as a processor,
 * and consequently implemented as a Core in the core module. The processing might fail, in which case an appropriate
 * ApiError is returned.
 *
 * @param <R> the type of the result produced by the processor. It must implement {@link ProcessorResult}.
 * @param <I> the type of the input consumed by the processor. It must implement {@link ProcessorInput}.
 */
public interface Processor<R extends ProcessorResult, I extends ProcessorInput> {

    /**
     * Process the given input and produce a result.
     * If the processing fails, an {@link ApiError} is returned.
     *
     * @param input the input to be processed
     * @return a result of the processing, wrapped in an {@link io.vavr.control.Either Either}.
     */
    Either<ApiError, R> process(I input);
}
package com.personal.mavrep.api.base;

import com.personal.mavrep.api.exceptions.MavrepException;
import io.vavr.control.Either;

public interface  Processor<R extends ProcessorResult, I extends ProcessorInput> {

    Either<? extends MavrepException, R> process(I input);
}

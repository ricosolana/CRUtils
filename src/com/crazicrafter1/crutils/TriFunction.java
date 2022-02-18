package com.crazicrafter1.crutils;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface TriFunction<AA, BB, CC, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param aa the first function argument
     * @param bb the second function argument
     * @param cc the third function argument
     * @return the function result
     */
    R apply(AA aa, BB bb, CC cc);

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     */
    default <V> TriFunction<AA, BB, CC, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (AA aa, BB bb, CC cc) -> after.apply(apply(aa, bb, cc));
    }
}
package Functional;

@FunctionalInterface
public interface CheckedBiFunction<T, U, R, E extends Exception> {
    R apply(T t, U u) throws E;
}
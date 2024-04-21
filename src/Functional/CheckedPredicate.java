package Functional;

@FunctionalInterface
public interface CheckedPredicate<T, E extends Exception> {
    Boolean test(T t) throws E;
}
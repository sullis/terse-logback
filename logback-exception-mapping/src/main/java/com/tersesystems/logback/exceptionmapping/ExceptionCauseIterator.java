package com.tersesystems.logback.exceptionmapping;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ExceptionCauseIterator implements Iterator<Throwable> {
    private Throwable throwable;

    ExceptionCauseIterator(Throwable throwable) {
        this.throwable = throwable;
    }

    @Override
    public boolean hasNext() {
        return throwable != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Throwable next() {
        Throwable oldThrowable = throwable;
        if (throwable != null) {
            throwable = throwable.getCause();
        }
        return oldThrowable;
    }

    @SuppressWarnings("unchecked")
    public Stream<Throwable> stream() {
        Spliterator spliterator = Spliterators.spliteratorUnknownSize(this, 0);
        return (Stream<Throwable>) StreamSupport.stream(spliterator, false);
    }

    public static ExceptionCauseIterator create(Throwable throwable) {
        return new ExceptionCauseIterator(throwable);
    }
}

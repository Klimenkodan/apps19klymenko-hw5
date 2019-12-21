package ua.edu.ucu.stream;

import ua.edu.ucu.function.*;
import java.util.ArrayList;
import java.util.Arrays;


public class AsIntStream implements IntStream {
    private ArrayList<Integer> stream;
    private ArrayList<Object> operations = new ArrayList<>();
    private boolean closed = false;

    private AsIntStream() {
        stream = new ArrayList<>();

    }

    public AsIntStream(int... values) {
        stream = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            stream.add(values[i]);
        }
    }

    private void throwExceptionIfEmpty() {
        if (stream.size() == 0) {
            throw new IllegalArgumentException();
        }
    }

    private void throwExceptionClosedStream() {
        if (closed) {
            throw new UnsupportedOperationException();
        }
    }

    public static IntStream of(int... values) {
        return new AsIntStream(values);
    }

    private AsIntStream callAllFunctions() {
        AsIntStream newStream = new AsIntStream();
        newStream.stream = (ArrayList<Integer>) stream.clone();
        for (int i = 0; i < operations.size(); i++) {
            if (operations.get(i) instanceof IntToIntStreamFunction) {
                newStream = (AsIntStream) newStream.doFlatMap((IntToIntStreamFunction) operations.get(i));
            } else if (operations.get(i) instanceof IntPredicate) {
                newStream = (AsIntStream) newStream.doFilter((IntPredicate) operations.get(i));
            } else if (operations.get(i) instanceof IntUnaryOperator) {
                newStream = (AsIntStream) newStream.doMap((IntUnaryOperator) operations.get(i));
            }

        }
        newStream.closed = true;
        return newStream;
    }

    @Override
    public Double average() {
        return (double) reduce(0, (sum, x) -> sum += x / stream.size());
    }

    @Override
    public Integer max() {
        return reduce(Integer.MIN_VALUE, Math::max);
    }

    @Override
    public Integer min() {
        return reduce(Integer.MAX_VALUE, Math::min);
    }

    @Override
    public long count() {
        AsIntStream newStream = callAllFunctions();
        return newStream.stream.size();
    }

    @Override
    public Integer sum() {
        return reduce(0, (sum, x) -> sum += x);
    }



    @Override
    public IntStream filter(IntPredicate predicate) {
        throwExceptionClosedStream();
        operations.add(predicate);
        return this;
    }

    private IntStream doFilter(IntPredicate predicate) {
        throwExceptionClosedStream();
        ArrayList<Integer> newStream = new ArrayList<>();
        for (int i = 0; i < stream.size(); i++) {
            if (predicate.test(stream.get(i))) {
                newStream.add(stream.get(i));
            }
        }
        AsIntStream AsNewStream = new AsIntStream();
        AsNewStream.stream = newStream;
        return AsNewStream;
   }


    @Override
    public void forEach(IntConsumer action) {
        throwExceptionClosedStream();
        AsIntStream newStream = callAllFunctions();
        for (int i = 0; i < newStream.stream.size(); i++) {
            action.accept(newStream.stream.get(i));
        }
    }

    @Override
    public IntStream map(IntUnaryOperator mapper) {
        throwExceptionClosedStream();
        operations.add(mapper);
        return this;
    }

    private IntStream doMap(IntUnaryOperator mapper) {
        throwExceptionClosedStream();
        int[] newStream = new int[stream.size()];
        for (int i = 0; i < stream.size(); i++) {
            newStream[i] = mapper.apply(stream.get(i));
        }
        return new AsIntStream(newStream);
    }

    @Override
    public IntStream flatMap(IntToIntStreamFunction func) {
        throwExceptionClosedStream();
        operations.add(func);
        return this;
    }

    private IntStream doFlatMap(IntToIntStreamFunction func) {
        ArrayList<Integer> newStream = new ArrayList<>();
        for (int i = 0; i < stream.size(); i++) {
            AsIntStream intStream = (AsIntStream) func.applyAsIntStream(stream.get(i));
            for (int j = 0; j < intStream.count(); j++) {
                newStream.add(intStream.stream.get(j));
            }
        }
        AsIntStream newIntStream =  new AsIntStream();
        newIntStream.stream = newStream;
        return newIntStream;
    }

    @Override
    public int reduce(int identity, IntBinaryOperator op) {
        throwExceptionClosedStream();
        throwExceptionIfEmpty();
        AsIntStream newStream = callAllFunctions();
        for (int i = 0; i < newStream.stream.size(); i++) {
            identity = op.apply(identity, newStream.stream.get(i));
        }
        return identity;
    }

    @Override
    public int[] toArray() {
        throwExceptionClosedStream();
        AsIntStream newStream = callAllFunctions();
        int[] array = new int[(int)newStream.stream.size()];
        for (int i = 0; i < newStream.stream.size(); i++) {
            array[i] = newStream.stream.get(i);
        }
        return array;
    }

}

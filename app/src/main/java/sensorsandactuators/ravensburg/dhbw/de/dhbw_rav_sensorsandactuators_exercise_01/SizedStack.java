package sensorsandactuators.ravensburg.dhbw.de.dhbw_rav_sensorsandactuators_exercise_01;

import java.util.Stack;

public class SizedStack<T> extends Stack<T> {
    private final int maxSize;

    public SizedStack(final int size) {
        super();
        this.maxSize = size;
    }

    @Override
    public T push(T item) {
        while (this.size() >= maxSize)
            this.remove(0);
        return super.push(item);
    }
}

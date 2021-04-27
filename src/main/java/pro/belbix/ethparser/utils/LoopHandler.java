package pro.belbix.ethparser.utils;

import java.util.function.BiConsumer;

public class LoopHandler {

    private final int loopStep;
    private final BiConsumer<Integer, Integer> handler;

    public LoopHandler(int loopStep,
        BiConsumer<Integer, Integer> handler) {
        this.loopStep = loopStep;
        this.handler = handler;
    }

    public void start(Integer from, Integer to) {
        while (true) {
            Integer end = null;
            if (to != null) {
                end = from + loopStep;
            }
            handler.accept(from, end);
            from = end;
            if (to != null) {
                if (end > to) {
                    break;
                }
            } else {
                break;
            }
        }
    }

}

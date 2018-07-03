package io.sece.vlc.rcvr.processing;

import java.util.function.UnaryOperator;

@FunctionalInterface
public interface ProcessingBlock extends UnaryOperator<Frame> { }

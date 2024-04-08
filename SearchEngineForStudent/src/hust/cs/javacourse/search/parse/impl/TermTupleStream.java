package hust.cs.javacourse.search.parse.impl;

import hust.cs.javacourse.search.index.AbstractTermTuple;
import hust.cs.javacourse.search.parse.AbstractTermTupleStream;

public class TermTupleStream extends AbstractTermTupleStream {
    /**
     * 获得下一个三元组
     *
     * @return: 下一个三元组；如果到了流的末尾，返回null
     */
    @Override
    public AbstractTermTuple next() {
        return null;
    }

    /**
     * 关闭流
     */
    @Override
    public void close() {

    }
}

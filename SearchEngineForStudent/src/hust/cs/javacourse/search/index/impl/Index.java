package hust.cs.javacourse.search.index.impl;

import com.google.gson.Gson;
import hust.cs.javacourse.search.index.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Index extends AbstractIndex {
    /**
     * 返回索引的字符串表示
     *
     * @return 索引的字符串表示
     */
    @Override
    public String toString() {
        String str = "";
        for (AbstractTerm term : termToPostingListMapping.keySet()) {
            str += term.toString() + ":\n" + termToPostingListMapping.get(term).toString() + "\n";
        }
        return str;
    }

    /**
     * 添加文档到索引，更新索引内部的HashMap
     *
     * @param document ：文档的AbstractDocument子类型表示
     */
    @Override
    public void addDocument(AbstractDocument document) {
        if (!docIdToDocPathMapping.containsKey(document.getDocId())) {
            docIdToDocPathMapping.put(document.getDocId(), document.getDocPath());
        }
        for (AbstractTermTuple tuple : document.getTuples()) {
            if (!termToPostingListMapping.containsKey(tuple.term)) {
                termToPostingListMapping.put(tuple.term, new PostingList());
            }
            AbstractPostingList postingList = termToPostingListMapping.get(tuple.term);
            if (postingList.indexOf(document.getDocId()) == -1) {
                List<Integer> positions = new ArrayList<>();
                positions.add(tuple.curPos);
                postingList.add(new Posting(document.getDocId(), tuple.freq, positions));
            }
        }
    }

    /**
     * <pre>
     * 从索引文件里加载已经构建好的索引.内部调用FileSerializable接口方法readObject即可
     * @param file ：索引文件
     * </pre>
     */
    @Override
    public void load(File file) {
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            String json = new String(bytes);
            Gson gson = new Gson();
            Index index = gson.fromJson(json, Index.class);
            fileInputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <pre>
     * 将在内存里构建好的索引写入到文件. 内部调用FileSerializable接口方法writeObject即可
     * @param file ：写入的目标索引文件
     * </pre>
     */
    @Override
    public void save(File file) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            Gson gson = new Gson();
            String json = gson.toJson(this);
            fileOutputStream.write(json.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 返回指定单词的PostingList
     *
     * @param term : 指定的单词
     * @return ：指定单词的PostingList;如果索引字典没有该单词，则返回null
     */
    @Override
    public AbstractPostingList search(AbstractTerm term) {
        return termToPostingListMapping.get(term);
    }

    /**
     * 返回索引的字典.字典为索引里所有单词的并集
     *
     * @return ：索引中Term列表
     */
    @Override
    public Set<AbstractTerm> getDictionary() {
        return termToPostingListMapping.keySet();
    }

    /**
     * <pre>
     * 对索引进行优化，包括：
     *      对索引里每个单词的PostingList按docId从小到大排序
     *      同时对每个Posting里的positions从小到大排序
     * 在内存中把索引构建完后执行该方法
     * </pre>
     */
    @Override
    public void optimize() {
        for (AbstractTerm term : termToPostingListMapping.keySet()) {
            termToPostingListMapping.get(term).sort();
            for (int i = 0; i < termToPostingListMapping.get(term).size(); i++) {
                termToPostingListMapping.get(term).get(i).sort();
            }
        }
    }

    /**
     * 根据docId获得对应文档的完全路径名
     *
     * @param docId ：文档id
     * @return : 对应文档的完全路径名
     */
    @Override
    public String getDocName(int docId) {
        return docIdToDocPathMapping.get(docId);
    }

    /**
     * 写到二进制文件
     *
     * @param out :输出流对象
     */
    @Override
    public void writeObject(ObjectOutputStream out) {
        try {
            out.writeObject(docIdToDocPathMapping);
            out.writeObject(termToPostingListMapping);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从二进制文件读
     *
     * @param in ：输入流对象
     */
    @Override
    public void readObject(ObjectInputStream in) {
        try {
            docIdToDocPathMapping = (Map<Integer, String>) in.readObject();
            termToPostingListMapping = (Map<AbstractTerm, AbstractPostingList>) in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}

package Moudle;

import java.util.List;

public class PageResponse<T> {
    private List<T> content;  // 必须与后端返回的字段名完全一致
    private boolean last;     // 对应后端返回的 last 字段

    public List<T> getContent() { return content; }
    public boolean isLast() { return last; }
}
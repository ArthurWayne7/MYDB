package top.guoziyang.mydb.backend.common;

//共享数组
//subArray方法可以使用同一片内存
public class SubArray {
    public byte[] raw;
    public int start;
    public int end;
    //该方法返回的数组是数据共享的
    public SubArray(byte[] raw, int start, int end) {
        this.raw = raw;
        this.start = start;
        this.end = end;
    }
}

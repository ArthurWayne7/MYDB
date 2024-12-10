package top.guoziyang.mydb.backend.dm.page;

import java.util.Arrays;

import top.guoziyang.mydb.backend.dm.pageCache.PageCache;
import top.guoziyang.mydb.backend.utils.Parser;

/**
 * PageX管理普通页
 * 普通页结构
 * [FreeSpaceOffset] [Data]
 * FreeSpaceOffset: 2字节 空闲位置开始偏移
 * 空闲位置偏移量是为了方便插入时直接找到被插入页面的空闲位置
 */
public class PageX {

    private static final short OF_FREE = 0;
    private static final short OF_DATA = 2;
    public static final int MAX_FREE_SPACE = PageCache.PAGE_SIZE - OF_DATA;

    public static byte[] initRaw() {
        byte[] raw = new byte[PageCache.PAGE_SIZE];
        setFSO(raw, OF_DATA);
        return raw;
    }

    //setFSO 方法将新的偏移量（即插入位置加上插入数据的长度）写入页面数据的开头的两个字节
    private static void setFSO(byte[] raw, short ofData) {
        System.arraycopy(Parser.short2Byte(ofData), 0, raw, OF_FREE, OF_DATA);
    }

    // 获取pg的FSO
    public static short getFSO(Page pg) {
        return getFSO(pg.getData());
    }

    //页面数据（raw 数组）的开头提取2个字节
    //这2个字节代表了页面中空闲空间的起始位置偏移量，并将其解析为 short 类型的值返回。
    // 这个值指示了页面中空闲空间的起始位置，从而可以知道页面中还有多少空闲空间以及空闲空间在哪里。
    private static short getFSO(byte[] raw) {
        return Parser.parseShort(Arrays.copyOfRange(raw, 0, 2));
    }

    // 将raw插入pg中，返回插入位置

    public static short insert(Page pg, byte[] raw) {
        pg.setDirty(true);
        //获取当前页面数据中空闲空间的起始位置偏移量。这个偏移量指示了在页面中插入新数据的位置。
        short offset = getFSO(pg.getData());
        //从raw（插入的数据）从下标0开始，复制数据到pg.getData()数组中，在pg.getData()数组中以offset开始复制raw.length的长度
        System.arraycopy(raw, 0, pg.getData(), offset, raw.length);
        //插入数据后，更新页面的空闲空间偏移量。
        // setFSO 方法将新的偏移量（即插入位置加上插入数据的长度）写入页面数据的开头。
        setFSO(pg.getData(), (short)(offset + raw.length));
        return offset;
    }

    // 获取页面的空闲空间大小
    public static int getFreeSpace(Page pg) {
        return PageCache.PAGE_SIZE - (int)getFSO(pg.getData());
    }

    // 将raw插入pg中的offset位置，并将pg的offset设置为较大的offset
    public static void recoverInsert(Page pg, byte[] raw, short offset) {
        pg.setDirty(true);
        System.arraycopy(raw, 0, pg.getData(), offset, raw.length);

        short rawFSO = getFSO(pg.getData());
        if(rawFSO < offset + raw.length) {
            setFSO(pg.getData(), (short)(offset+raw.length));
        }
    }

    // 将raw插入pg中的offset位置，不更新update
    public static void recoverUpdate(Page pg, byte[] raw, short offset) {
        pg.setDirty(true);
        System.arraycopy(raw, 0, pg.getData(), offset, raw.length);
    }
}

package net.iioss.memory.core.constant;

import cn.hutool.core.util.ObjectUtil;

/**
 * @Title 缓存级别
 * @auther huangyinqiang
 * @create 2019-06-05 上午11:06
 */
public enum MemoryLevel {

    ONE(1,"一级缓存"),

    TWO(2,"二级缓存"),

    THREE(3,"三级缓存");


    private final Integer levelNumber;
    private final String chineseName;

    MemoryLevel(Integer levelNumber, String chineseName) {
        this.levelNumber=levelNumber;
        this.chineseName=chineseName;
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public String getChineseName() {
        return chineseName;
    }

    public static MemoryLevel getCacheLevelByLevelNumber(Integer levelNumber){
        for(MemoryLevel cacheLevel:MemoryLevel.values()){
            if(levelNumber.equals(cacheLevel.getLevelNumber())){
                return cacheLevel;
            }
        }
        return  null;
    }

    public static boolean existLevelNumber(Integer levelNumber){
        return !ObjectUtil.isNull(getCacheLevelByLevelNumber(levelNumber));
    }
}

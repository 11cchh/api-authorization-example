package com.hangzhou.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 测试实体类
 * @Author linchenghui
 * @Date 2021/4/13
 */
@Data
@AllArgsConstructor
public class Node {
    /**
     * 存放数据
     */
    private String item;
    /**
     * 下一个节点
     */
    private Node next;
}

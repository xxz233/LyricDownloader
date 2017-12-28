/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zhang.lyricdownloader.pojo;

import javafx.scene.control.CheckBox;

/**
 * 表格模型类
 * @author Administrator
 */
public class ItemModel {
    private CheckBox ckeckBox; //复选框
    private String fileName;//文件名
    private String filePath;//路径

    /**
     * @return the ckeckBox
     */
    public CheckBox getCkeckBox() {
        return ckeckBox;
    }

    /**
     * @param ckeckBox the ckeckBox to set
     */
    public void setCkeckBox(CheckBox ckeckBox) {
        this.ckeckBox = ckeckBox;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

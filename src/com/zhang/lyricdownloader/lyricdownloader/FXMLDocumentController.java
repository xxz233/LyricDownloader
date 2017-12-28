/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zhang.lyricdownloader.lyricdownloader;

import com.zhang.lyricdownloader.util.Base64Util;
import com.zhang.lyricdownloader.pojo.ItemModel;
import com.zhang.lyricdownloader.util.HttpUtil;
import com.zhang.lyricdownloader.util.Transcoding;
import com.sun.javafx.robot.impl.FXRobotHelper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.imageio.ImageIO;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3AudioHeader;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.datatype.Artwork;

/**
 *
 * @author Administrator
 */
public class FXMLDocumentController implements Initializable {

    private final ObservableList<Stage> stage = FXRobotHelper.getStages();//获取所有舞台
    private List<File> files;
    private final ObservableList<ItemModel> itemModels = FXCollections.observableArrayList();
    private String path = "";
    private Image defImage = new Image("resource/pic/album.png");
    @FXML
    private TableView table;
    @FXML
    private TableColumn ckbox;
    @FXML
    private TableColumn fileName;
    @FXML
    private TableColumn filePath;

    @FXML
    private ImageView imageView;
    @FXML
    private TextField title;
    @FXML
    private TextField artist;
    @FXML
    private TextField album;
    @FXML
    private TextField year;
    @FXML
    private ProgressBar progressBar;

    private double count = 0;
    private double finish = 0;

    /**
     * 选择目录
     *
     * @param event
     */
    @FXML
    private void open(ActionEvent event) {
        itemModels.clear();
        count = 0;
        finish = 0;
        progressBar.setProgress(0);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择MP3音频文件");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MP3音频文件", "*.mp3"),
                new FileChooser.ExtensionFilter("MP3音频文件", "*.MP3")
        );
        files = fileChooser.showOpenMultipleDialog(stage.get(0));
        if (files == null) {
            return;
        }
        //构造数据模型
        ItemModel itemModel;
        CheckBox ck;
        for (File file : files) {
            if ("".equals(path)) {
                path = file.getParent();
            }
            itemModel = new ItemModel();
            ck = new CheckBox();
            itemModel.setCkeckBox(ck);
            itemModel.setFileName(file.getName());
            itemModel.setFilePath(file.getAbsolutePath());
            itemModels.add(itemModel);
        }
        //设置数据工厂
        ckbox.setCellValueFactory(new PropertyValueFactory<>("ckeckBox"));
        fileName.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        filePath.setCellValueFactory(new PropertyValueFactory<>("filePath"));
        table.setItems(itemModels);
        //设置回调
        fileName.setCellFactory(new TaskCellFactory());
        filePath.setCellFactory(new TaskCellFactory());
    }

    @FXML
    private void checkAll(ActionEvent event) {
        itemModels.stream().forEach((item) -> {
            item.getCkeckBox().setSelected(true);
        });
    }

    @FXML
    private void clearCheck(ActionEvent event) {
        itemModels.stream().forEach((item) -> {
            item.getCkeckBox().setSelected(false);
        });
    }

    @FXML
    private void downloadLrc(ActionEvent event) {
        new Thread() {
            @Override
            public void run() {
                count = 0;
                finish = 0;
                progressBar.setProgress(0);
                itemModels.stream().forEach((item) -> {
                    if (item.getCkeckBox().isSelected()) {
                        count++;
                    }
                });
                itemModels.stream().forEach((item) -> {
                    if (item.getCkeckBox().isSelected()) {
                        System.out.println("选中的文件路径是：" + item.getFilePath());
                        analysis(item.getFilePath());
                        finish++;
                        double p = finish / count;
                        progressBar.setProgress(p);
                    }
                });
            }
        }.start();
    }

    @FXML
    private void utf8Togbk(ActionEvent event) {
        new Thread() {
            @Override
            public void run() {
                count = 0;
                finish = 0;
                progressBar.setProgress(0);
                itemModels.stream().forEach((item) -> {
                    if (item.getCkeckBox().isSelected()) {
                        count++;
                    }
                });
                itemModels.stream().forEach((item) -> {
                    if (item.getCkeckBox().isSelected()) {
                        Transcoding t = new Transcoding();
                        String filePathStr = item.getFilePath();
                        String lrcPath = filePathStr.substring(0, filePathStr.lastIndexOf(".")) + ".lrc";
                        try {
                            t.encoding("GB2312", lrcPath);
                        } catch (Exception ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        finish++;
                        double p = finish / count;
                        progressBar.setProgress(p);
                    }
                });
            }
        }.start();

    }

    @FXML
    private void gbkToUtf8(ActionEvent event) {
        new Thread() {
            @Override
            public void run() {
                count = 0;
                finish = 0;
                progressBar.setProgress(0);
                itemModels.stream().forEach((item) -> {
                    if (item.getCkeckBox().isSelected()) {
                        count++;
                    }
                });
                itemModels.stream().forEach((item) -> {
                    if (item.getCkeckBox().isSelected()) {
                        Transcoding t = new Transcoding();
                        String filePathStr = item.getFilePath();
                        String lrcPath = filePathStr.substring(0, filePathStr.lastIndexOf(".")) + ".lrc";
                        try {
                            t.encoding("UTF-8", lrcPath);
                        } catch (Exception ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        finish++;
                        double p = finish / count;
                        progressBar.setProgress(p);
                    }
                });
            }
        }.start();

    }

    @FXML
    private void deleteFile(ActionEvent event) {
        if (!f_alert_confirmDialog("提示", "选中的文件将会被删除，您确定？")) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                count = 0;
                finish = 0;
                progressBar.setProgress(0);
                itemModels.stream().forEach((item) -> {
                    if (item.getCkeckBox().isSelected()) {
                        count++;
                    }
                });
                Iterator it = itemModels.iterator();
                ItemModel item;
                while (it.hasNext()) {
                    item = (ItemModel) it.next();
                    if (item.getCkeckBox().isSelected()) {
                        File file = new File(item.getFilePath());
                        if (file.exists()) {
                            file.delete();
                        }
                        finish++;
                        double p = finish / count;
                        progressBar.setProgress(p);
                        it.remove();
                    }
                }
            }
        }.start();
    }

    @FXML
    private void exit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void about(ActionEvent event) {
        f_alert_informationDialog("关于", "本程序作者是：\n吉利2016全新金刚群(491179159)的\n疯狂的程序员\n有问题请在群内咨询");
    }

    public void detail(String filePath) {
        File mp3 = new File(filePath);
        try {
            MP3File f = (MP3File) AudioFileIO.read(mp3);
            Tag tag = f.getTag();
            if (tag != null) {
                Artwork a = tag.getFirstArtwork();
                if (a != null) {
                    BufferedImage b = a.getImage();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    ImageIO.write(b, "jpeg", os);
                    InputStream is = new ByteArrayInputStream(os.toByteArray());
                    Image image = new Image(is);
                    imageView.setImage(image);
                    title.setText(tag.getFirst(FieldKey.TITLE));
                    artist.setText(tag.getFirst(FieldKey.ARTIST));
                    album.setText(tag.getFirst(FieldKey.ALBUM));
                    year.setText(tag.getFirst(FieldKey.YEAR));
                } else {
                    imageView.setImage(defImage);
                    String str = filePath.substring(filePath.lastIndexOf("\\") + 1, filePath.length() - 4);
                    title.setText(str);
                }
            } else {
                imageView.setImage(defImage);
                String str = filePath.substring(filePath.lastIndexOf("\\"), filePath.length() - 1);
                System.out.print(str);
            }
        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | TagException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void analysis(String filePath) {
        File mp3 = new File(filePath);
        try {
            MP3File f = (MP3File) AudioFileIO.read(mp3);
            MP3AudioHeader audioHeader = (MP3AudioHeader) f.getAudioHeader();
            String length = String.valueOf(audioHeader.getTrackLength() * 1000);
            Tag tag = f.getTag();
            String songName = "";
            System.out.print("-------------" + songName + "--------------------");
            if (tag != null) {
                songName = tag.getFirst(FieldKey.TITLE);
            }
            if ("".equals(songName)) {
                songName = filePath.substring(filePath.lastIndexOf("\\"), filePath.length() - 1);
            }
            this.getLrcUrl(songName, length, filePath);

        } catch (IOException | CannotReadException | InvalidAudioFrameException | ReadOnlyFileException | TagException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 获取歌词地址
     *
     * @param key 关键字
     * @param length 播放时长
     */
    private void getLrcUrl(String key, String length, String filePath) {
        System.out.println("mp3文件名是：" + key + "mp3文件的长度是：" + length);
        try {
            key = URLEncoder.encode(key, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
        String url = "http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword=" + key + "&duration=" + length + "&hash=";

        String jsonString = HttpUtil.doGet(url);
        //将Json字符串转为java对象
        JSONObject obj = JSONObject.fromObject(jsonString);
        if (obj.has("candidates")) {
            JSONArray candidates = obj.getJSONArray("candidates");
            if (candidates.isEmpty()) {
                return;
            }
            JSONObject one = candidates.getJSONObject(0);
            String lrcUrl = "http://lyrics.kugou.com/download?ver=1&client=pc&id=" + one.getString("id") + "&accesskey=" + one.getString("accesskey") + "&fmt=lrc&charset=utf8";
            System.out.println("lrc下载地址是：" + lrcUrl);
            this.getLrc(lrcUrl, filePath);
        }
    }

    private void getLrc(String url, String filePath) {
        String jsonString = HttpUtil.doGet(url);
        //将Json字符串转为java对象
        JSONObject obj = JSONObject.fromObject(jsonString);
        try {
            byte[] b = Base64Util.decodeBase64(obj.getString("content"));
            String content = new String(b);

            String lrcPath = filePath.substring(0, filePath.lastIndexOf(".")) + ".lrc";
            System.out.println("lrc：" + lrcPath);
            File file = new File(lrcPath);
            if (!file.exists()) {
                OutputStream out = new FileOutputStream(file);
                OutputStreamWriter writer = new OutputStreamWriter(out, "GBK");
                writer.write(content);
                writer.close();
            }
        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    /**
     * 弹出一个通用的确定对话框
     *
     * @param p_header 对话框的信息标题
     * @param p_message 对话框的信息
     * @return 用户点击了是或否
     */
    public boolean f_alert_confirmDialog(String p_header, String p_message) {
//        按钮部分可以使用预设的也可以像这样自己 new 一个
        Alert _alert = new Alert(Alert.AlertType.CONFIRMATION, p_message, new ButtonType("取消", ButtonBar.ButtonData.NO),
                new ButtonType("确定", ButtonBar.ButtonData.YES));
//        设置窗口的标题
        _alert.setTitle("确认");
        _alert.setHeaderText(p_header);
//        设置对话框的 icon 图标，参数是主窗口的 stage
        _alert.initOwner(stage.get(0));
//        showAndWait() 将在对话框消失以前不会执行之后的代码
        Optional<ButtonType> _buttonType = _alert.showAndWait();
//        根据点击结果返回
        if (_buttonType.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {
            return true;
        } else {
            return false;
        }
    }

//    弹出一个信息对话框
    public void f_alert_informationDialog(String p_header, String p_message) {
        Alert _alert = new Alert(Alert.AlertType.INFORMATION);
        _alert.setTitle("信息");
        _alert.setHeaderText(p_header);
        _alert.setContentText(p_message);
        _alert.initOwner(stage.get(0));
        _alert.show();
    }

    private class TaskCellFactory implements Callback<TableColumn<Task, String>, TableCell<Task, String>> {

        @Override
        public TableCell<Task, String> call(TableColumn<Task, String> param) {
            TextFieldTableCell<Task, String> cell = new TextFieldTableCell<>();
            cell.setOnMouseClicked((MouseEvent t) -> {
                if (t.getClickCount() == 1) {
                    if ("filePath".equals(cell.getId())) {
                        detail(cell.getItem());
                    } else {
                        detail(path + "\\" + cell.getItem());
                    }
                }
            });
            //cell.setContextMenu(taskContextMenu);
            return cell;
        }
    }
}

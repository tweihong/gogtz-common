/*
 * Copyright(c) 2012-2017 JD Pharma.Ltd. All Rights Reserved.
 * 
 */
package com.gogtz.common.file;

import com.gogtz.common.contants.ConstantsUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.PicturesManager;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.hwpf.usermodel.PictureType;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * word文件转换html
 *
 * @author renxingchen
 * @version zxpt 1.0
 * @since zxpt 1.0 2017年5月24日
 */
public class WordToHtmlUtils {

    /**
     * Word 转换成 Html
     *
     * @param wordFilePath Word文件路径
     * @return html代码
     */
    public static String wordToHtml(String wordFilePath) {
        ByteArrayOutputStream outStream = null;
        String content = null;
        try {
            if (StringUtils.isNotBlank(wordFilePath)) {
                InputStream input = new FileInputStream(wordFilePath);
                String suffix = FileUtils.getSuffix(wordFilePath);
                if (ConstantsUtil.FILE_DOC.equals(suffix)) {
                    HWPFDocument wordDocument = new HWPFDocument(input);
                    WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(DocumentBuilderFactory
                            .newInstance().newDocumentBuilder().newDocument());
                    wordToHtmlConverter.setPicturesManager(new PicturesManager() {
                        public String savePicture(byte[] content, PictureType pictureType, String suggestedName,
                                                  float widthInches, float heightInches) {
                            return suggestedName;
                        }
                    });
                    wordToHtmlConverter.processDocument(wordDocument);
                    // List pics =
                    // wordDocument.getPicturesTable().getAllPictures();
                    // if (pics != null) {
                    // for (int i = 0; i < pics.size(); i++) {
                    // Picture pic = (Picture) pics.get(i);
                    // try {
                    // pic.writeImageContent(new FileOutputStream(path +
                    // pic.suggestFullFileName()));
                    // } catch (FileNotFoundException e) {
                    // e.printStackTrace();
                    // }
                    // }
                    // }
                    Document htmlDocument = wordToHtmlConverter.getDocument();
                    outStream = new ByteArrayOutputStream();
                    DOMSource domSource = new DOMSource(htmlDocument);
                    StreamResult streamResult = new StreamResult(outStream);
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer serializer = tf.newTransformer();
                    serializer.setOutputProperty(OutputKeys.ENCODING, Charset.defaultCharset().name());
                    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                    serializer.setOutputProperty(OutputKeys.METHOD, "html");
                    serializer.transform(domSource, streamResult);
                    // 获取转义出来的html内容
                    content = new String(outStream.toByteArray());
                    // FileUtils.write(new File(path, "1.html"), content,
                    // "utf-8");
                } else {
                    XWPFDocument document = new XWPFDocument(input);
                    XHTMLOptions options = XHTMLOptions.create();
                    outStream = new ByteArrayOutputStream();
                    XHTMLConverter xhtmlConverter = (XHTMLConverter) XHTMLConverter.getInstance();
                    xhtmlConverter.convert(document, outStream, options);
                    content = new String(outStream.toByteArray());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != outStream) {
                    outStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    /**
     * 分隔打印的html
     *
     * @param content
     * @return
     */
    public static HashMap<String, String> getContentHtml(String content) {
        int startBodyIndex = content.indexOf("<body class=\"b1 b2\">");
        int startBodyLength = "<body class=\"b1 b2\">".length();
        int endBodyIndex = content.indexOf("</body>");

        String headHtml = content.substring(0, startBodyIndex + startBodyLength);
        String resultContent = content.substring(startBodyIndex + startBodyLength, endBodyIndex);
        String footHtml = content.substring(endBodyIndex, content.length());

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put("headHtml", headHtml);
        hashMap.put("content", resultContent);
        hashMap.put("footHtml", footHtml);

        return hashMap;
    }


    /**
     * 分隔打印的html
     *
     * @return
     */
    public static String splitPage() {
        return "<div style='page-break-after: always;'></div><div class='b1 b2'>&nbsp;</div>";
    }
}

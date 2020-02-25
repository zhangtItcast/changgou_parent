package com.changgou.file.controller;

import com.changgou.file.utils.FastDFSClient;
import com.changgou.file.utils.FastDFSFile;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author Steven
 * @version 1.0
 * @description com.changgou.file.controller
 * @date 2020-1-15
 */
@RestController
@CrossOrigin
public class FileController {

    @RequestMapping("/upload")
    public Result<String> uploda(MultipartFile file) {
        try {
            //1、包装文件上传需要的参数
            FastDFSFile dfsFile = new FastDFSFile(
                    file.getOriginalFilename(),
                    file.getBytes(),
                    StringUtils.getFilenameExtension(file.getOriginalFilename())
            );
            //2、调用FastDFS的api完成文件上传操作
            String[] upload = FastDFSClient.upload(dfsFile);
            //3、拼接文件直址url,并返回
            //http://192.168.211.132:8080/group1/M00/00/00/wKjThF4eyAiALJSHAA832942OCg734.jpg
            //String url = "http://192.168.211.132:8080/" + upload[0] + "/" + upload[1];
            String url = FastDFSClient.getTrackerUrl() + upload[0] + "/" + upload[1];
            return new Result<String>(true, StatusCode.OK, "文件上传成功", url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result<String>(false, StatusCode.ERROR, "文件上传失败");
    }
}

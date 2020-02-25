package com.changgou.file.utils;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.*;

/**
 * FastDFS文件操作相关工具类
 * 文件上传、下载、删除，读取服务器信息、文件信息等等操作
 * @author Steven
 * @version 1.0
 * @description com.changgou.file.utils
 * @date 2020-1-15
 */
public class FastDFSClient {

    //初始化加载配置文件
    static{
        try {
            //1、获取配置文件路径-filePath = new ClassPathResource("fdfs_client.conf").getPath()
            String path = new ClassPathResource("fdfs_client.conf").getPath();
            //2、加载配置文件-ClientGlobal.init(配置文件路径)
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取TrackerServer对象
     * @return
     */
    public static TrackerServer getTrackerServer(){
        TrackerServer trackerServer = null;
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return trackerServer;
    }

    /**
     * 获取 StorageClient
     * @return
     */
    public static StorageClient getStorageClient(){
        //5、创建一个StorageClient对象，直接new一个，需要两个参数TrackerServer对象、null
        StorageClient storageClient = new StorageClient(getTrackerServer(), null);
        return storageClient;
    }

    /**
     * 文件上传
     * @param dfsFile 文件上传相关信息
     * @return file_id数组对象
     */
    public static String[]  upload(FastDFSFile dfsFile){
        String[] uploadFile = new String[0];
        try {
            //调用FastDFS文件上传api，把文件上传到FastDFS中
            //文件拓展信息
            NameValuePair[] meta_list = new NameValuePair[1];
            meta_list[0] = new NameValuePair("author",dfsFile.getAuthor());

            uploadFile = getStorageClient().upload_file(dfsFile.getContent(), dfsFile.getExt(), meta_list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //返回file_id
        return uploadFile;
    }

    /**
     * 获取文件信息
     * @param group_name  组名
     * @param remote_filename fileId
     * @return 文件信息
     */
    public static FileInfo getFileInfo(String group_name,String remote_filename) {
        FileInfo info = null;
        try {
            info = getStorageClient().get_file_info(group_name, remote_filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    /**
     * 下载文件
     * @param group_name 组名
     * @param remote_filename FileId
     * @return InputStream
     */
    public static InputStream downloadFile(String group_name, String remote_filename) {
        InputStream is = null;
        try {
            //下载文件
            byte[] bytes = getStorageClient().download_file(group_name, remote_filename);
            is = new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    /**
     * 删除文件
     * @param group_name 组名
     * @param remote_filename FileID
     * @return 返回0代表成功 非0失败
     */
    public static int deleteFile(String group_name, String remote_filename) {
        int count = 0;
        try {
            count = getStorageClient().delete_file(group_name, remote_filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 根据组名，获取存储服务器相关信息
     * @param group_name  组名
     * @return StorageServer
     */
    public static StorageServer getStorageServer(String group_name) {
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();
            //查询服务器信息
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer, group_name);
            return storageServer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据组名与文件名获取存储服务器列表相关信息
     * @param group_name  组名
     * @return ServerInfo[]
     */
    public static ServerInfo[]  getServerInfo(String group_name,String filename) {
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();

            //获取组的所有服务器
            ServerInfo[] infos = trackerClient.getFetchStorages(trackerServer, group_name, filename);

            return infos;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取TrackerServer地址与端口
     * @return
     */
    public static String getTrackerUrl() {
        try {
            //3、创建一个TrackerClient对象。直接new一个。
            TrackerClient trackerClient = new TrackerClient();
            //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
            TrackerServer trackerServer = trackerClient.getConnection();
            //http://192.168.211.132:8080/
            String url = "http://" + trackerServer.getInetSocketAddress().getHostString() + ":"
                    + ClientGlobal.getG_tracker_http_port() + "/";
            return url;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) throws Exception{
        //测试文件基本上传api
        /*//1、获取配置文件路径-filePath = new ClassPathResource("fdfs_client.conf").getPath()
        String path = new ClassPathResource("fdfs_client.conf").getPath();
        //2、加载配置文件-ClientGlobal.init(配置文件路径)
        ClientGlobal.init(path);
        //3、创建一个TrackerClient对象。直接new一个。
        TrackerClient trackerClient = new TrackerClient();
        //4、使用TrackerClient对象创建连接，getConnection获得一个TrackerServer对象。
        TrackerServer trackerServer = trackerClient.getConnection();
        //5、创建一个StorageClient对象，直接new一个，需要两个参数TrackerServer对象、null
        StorageClient storageClient = new StorageClient(trackerServer, null);

        //文件上传操作
        String[] uploadFile = storageClient.upload_file("D:/WebWork/360wallpaper (4).jpg", "jpg", null);
        for (String s : uploadFile) {
            System.out.println(s);
        }*/

        //测试获取文件信息
        /*FileInfo info = getFileInfo("group1", "M00/00/00/wKjThF4e00OAdtGiAAfNplOPbtY255.jpg");
        System.out.println(info);*/

        //测试下载文件
        /*InputStream is = downloadFile("group1", "M00/00/00/wKjThF4e00OAdtGiAAfNplOPbtY255.jpg");
        //构建输出流对象
        OutputStream out = new FileOutputStream("D:/a.jpg");
        //创建缓冲区
        byte[] buff = new byte[1024];
        //把数据读到缓冲区
        while ((is.read(buff) > 0)) {
            out.write(buff);
        }
        out.close();
        is.close();*/


        //测试文件删除
        /*int count = deleteFile("group1", "M00/00/00/wKjThF4eyAiALJSHAA832942OCg734.jpg");
        System.out.println("删除了文件结果为：" + (count == 0 ? "成功" : "失败"));*/

        //测试获取服务器信息
        /*StorageServer storageServer = getStorageServer("group1");
        System.out.println("组的下标为：" + storageServer.getStorePathIndex());
        System.out.println(storageServer.getInetSocketAddress());*/

        //测试获取整个组所有服务器信息
        /*ServerInfo[] infos = getServerInfo("group1", "M00/00/00/wKjThF4e4pKAOrUdAAfNplOPbtY974.jpg");
        for (ServerInfo info : infos) {
            System.out.println(info.getIpAddr() + ":" + info.getPort());
        }*/

        //测试获取Tracker服务地址与端口
        System.out.println(getTrackerUrl());
    }
}

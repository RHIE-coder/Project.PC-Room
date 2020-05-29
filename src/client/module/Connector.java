package client.module;

import server.config.ServerConfiguration;
import server.model.request.Ping;
import server.model.request.RequestModel;
import server.model.response.ResponseModel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;

public class Connector {

    private Socket sock;
    private InetSocketAddress ipep;
    private ObjectOutputStream requestData;
    private ObjectInputStream responseData;
    private GuiManager guiManager;

    public void createGuiManager(){
        this.guiManager = new GuiManager();
    }

    public void guiManager(GuiProfile profile){
        try{
            guiManager.GuiGenerator(profile, this);
        }catch (Exception e) {
            destoryAll();
            System.out.println("Connector-guiManager():" + e);
            e.printStackTrace();
        }
    }

    public void connectToServer() {
        try {
            this.sock = new Socket();
            this.ipep = new InetSocketAddress(ServerConfiguration.IP, ServerConfiguration.PORT);
            this.sock.connect(ipep);

            this.requestData = new ObjectOutputStream(sock.getOutputStream());
            this.responseData = new ObjectInputStream(sock.getInputStream());

            if(sock.isConnected()) {
                System.out.println("서버와 연결 성공 [Client :"+sock.getInetAddress()+"--->"+sock.getLocalAddress()+": Server]");
            }
        }catch (Exception e) {
            System.out.println("Connector-connectToServer():" + e);
            e.printStackTrace();
        }
    }

    private void reconnect(){
        try {
            this.sock = new Socket();
            this.ipep = new InetSocketAddress(ServerConfiguration.IP, ServerConfiguration.PORT);
            this.sock.connect(ipep);

            this.requestData = new ObjectOutputStream(sock.getOutputStream());
            this.responseData = new ObjectInputStream(sock.getInputStream());

            if(sock.isConnected()) {
                JOptionPane.showMessageDialog(null, "서버와 연결이 끊겨져 재연결을 시도합니다.", "재연결 시도",JOptionPane.WARNING_MESSAGE);
                System.out.println("서버와 재연결 성공 [Client :"+sock.getInetAddress()+"--->"+sock.getLocalAddress()+": Server]");
                JOptionPane.showMessageDialog(null, "연결에 성공하였습니다.", "재연결 성공",JOptionPane.INFORMATION_MESSAGE);
            }
        }catch (Exception e) {
            JOptionPane.showMessageDialog(null, "연결 실패", "재연결 시도",JOptionPane.WARNING_MESSAGE);
            System.out.println("Connector-reconnect():" + e);
            e.printStackTrace();
        }
    }

    public HashMap<String,Object> communicateWithServer(RequestModel sendToServer) {

        HashMap<String,Object> result = null;
        try {
            // 서버에게 요청을 보낸다.
            requestData.writeObject(sendToServer);

            // 서버에게 요청을 받는다.
            ResponseModel recvFromServer = (ResponseModel)responseData.readObject();

            // 받은 요청을 처리한다.
            result = recvFromServer.data;

        }catch(SocketException se){
            destoryAll();
        }catch (Exception e) {
            System.out.println("Connector-communicateWithServer():" + e);
            e.printStackTrace();
        }finally {
            if(sock.isClosed()){
                reconnect();
            }
        }
        return result;
    }

    public void ping() throws Exception{
        requestData.writeObject(new Ping());
        responseData.readObject();
        System.out.println("[ping]서버에게 응답 받음");
    }

    private void destoryAll(){
        JOptionPane.showMessageDialog(null, "서버와 연결이 안되어 있습니다.", "네트워크 오류",JOptionPane.WARNING_MESSAGE);
        try{
            if(requestData != null){
                System.out.println("requestData close");
                requestData.close();
            }
        }catch (Exception e){
            System.out.println("Connector-destoryAll():" + e);
            e.printStackTrace();
        }

        try{
            if(responseData != null){
                System.out.println("responseData close");
                responseData.close();
            }
        }catch (Exception e){
            System.out.println("Connector-destoryAll():" + e);
            e.printStackTrace();
        }

        try{
            if(sock != null){
                System.out.println("sock close");
                sock.close();
            }
        }catch (Exception e){
            System.out.println("Connector-destoryAll():" + e);
            e.printStackTrace();
        }

    }
}

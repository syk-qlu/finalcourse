package service;

import model.ChatMessage;

import java.io.*;

/**
 * 消息协议 - 序列化/反序列化
 */
public class MessageProtocol {

    /**
     * 序列化消息(对象->字节数组)
     */
    public static byte[] serialize(ChatMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//输出流
        ObjectOutputStream oos = new ObjectOutputStream(baos);//对象序列化输出流
        oos.writeObject(message);//消息对象写入序列化流
        oos.close();
        return baos.toByteArray();
    }

    /**
     * 反序列化消息(字节数组—>对象)
     */
    public static ChatMessage deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);//输入流
        ObjectInputStream ois = new ObjectInputStream(bais);//对象序列化输入流
        ChatMessage message = (ChatMessage) ois.readObject();//消息对象从序列化流中读取
        ois.close();
        return message;
    }

    /**
     * 发送消息
     */
    public static void sendMessage(OutputStream out, ChatMessage message) throws IOException {
        byte[] data = serialize(message);
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeInt(data.length);
        dos.write(data);
        dos.flush();
    }

    /**
     * 接收消息
     */
    public static ChatMessage receiveMessage(InputStream in) throws IOException, ClassNotFoundException {
        DataInputStream dis = new DataInputStream(in);
        int length = dis.readInt();
        byte[] data = new byte[length];
        dis.readFully(data);
        return deserialize(data);
    }
}

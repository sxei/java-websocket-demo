package com.lxa.websocket;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/chat/{uid}")
public class Chat
{

	/**
	 * 连接对象集合
	 */
	private static final Set<Chat> connections = new CopyOnWriteArraySet<Chat>();

	private String uid;

	/**
	 * WebSocket Session
	 */
	private Session session;

	public Chat()
	{
	}

	/**
	 * 打开连接
	 * @param session
	 * @param nickName
	 */
	@OnOpen
	public void onOpen(Session session, @PathParam(value = "uid") String uid)
	{
		this.session = session;
		this.uid = uid;
		connections.add(this);
		Chat.broadCast("系统消息："+this.uid+"加入！");
	}

	/**
	 * 关闭连接
	 */
	@OnClose
	public void onClose()
	{
		connections.remove(this);
		Chat.broadCast("系统消息："+this.uid+"断开连接！");
	}

	/**
	 * 接收信息
	 * @param message
	 * @param nickName
	 */
	@OnMessage
	public void onMessage(String message, @PathParam(value = "uid") String uid)
	{
		Chat.broadCast(uid + "：" + message);
	}

	/**
	 * 错误信息响应
	 * @param throwable
	 */
	@OnError
	public void onError(Throwable throwable)
	{
		System.out.println(throwable.getMessage());
	}

	/**
	 * 发送或广播信息
	 * @param message
	 */
	private static void broadCast(String message)
	{
		System.out.println(message);
		for (Chat chat : connections)
		{
			try
			{
				synchronized (chat)
				{
					chat.session.getBasicRemote().sendText(message);
				}
			}
			catch (IOException e)
			{
				connections.remove(chat);
				try
				{
					chat.session.close();
				}
				catch (IOException e1)
				{
				}
				Chat.broadCast(String.format("System> %s %s", chat.uid, " has bean disconnection."));
			}
		}
	}
}
package com.myland.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;



/**
 * Tcp client for echo server. This client also receive message forwarded by
 * netty MQ server
 */
public class TcpClient {

	private final static String serverString = "127.0.0.1";
	private final static int servPort = 18866;

	public static void main(String[] args) {

		Socket socket = null;
		try {
			// Create socket that is connected to server on specified port
			socket = new Socket(serverString, servPort);
			System.out.println("Connected to server...send echo string (quit to end)");

			final InputStream in = socket.getInputStream();
			OutputStream out = socket.getOutputStream();

			startReceiveThread(in);

//			sendMsgToServerFromInput(out);
			sendMsgToServerFromThread(out);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static void startReceiveThread(final InputStream in) {
		Thread receiveThread = new Thread() {
			public void run() {
				while (true) {
					byte[] readBytes = new byte[2048];
					int ret = 0;
					try {
						ret = in.read(readBytes);
					} catch (IOException e) {
						break;
					}
					if (ret == -1) {
						break;
					}
					String retString = new String(readBytes);
					System.out.println("Received : " + retString);
				}
			}
		};

		receiveThread.setDaemon(true);
		receiveThread.start();
	}

	private static void sendMsgToServerFromInput(OutputStream out) {
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(
				System.in));
		while (true) {
			String msg = new String();
			try {
				msg = inFromUser.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (msg.equals("quit")) {
				break;
			}
			byte[] msgBytes = getMessageBytes(msg);
			if (msgBytes != null) {
				try {
					out.write(msgBytes);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void sendMsgToServerFromThread(final OutputStream out) {
		ClientMsgSender msgSender = new ClientMsgSender(out);
		msgSender.start();
	}

	public static byte[] getMessageBytes(String msg) {
		msg = msg.trim();
		if (!msg.isEmpty()) {
			try {
				return msg.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}			
		}

		return null;
	}
}

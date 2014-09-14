package org.dclayer.net.a2s;

import org.dclayer.listener.net.OnConnectionErrorListener;
import org.dclayer.net.filter.MessageFilterCollection;
import org.dclayer.net.socket.TCPSocketConnection;

/**
 * a connection to an application instance
 */
public class ApplicationConnection {
	/**
	 * the {@link TCPSocketConnection} instance connected to the application
	 */
	private TCPSocketConnection tcpSocketConnection;
	/**
	 * a {@link MessageFilterCollection} that applies to messages received from this {@link ApplicationConnection}
	 */
	private MessageFilterCollection messageFilterCollection;
	/**
	 * the revision to use for communication on this {@link ApplicationConnection}
	 */
	private int revision;
	/**
	 * an {@link OnConnectionErrorListener} to call on a connection termination due to an error
	 */
	private OnConnectionErrorListener onConnectionErrorListener;
	
	/**
	 * create a new {@link ApplicationConnection} for the given {@link TCPSocketConnection}
	 * @param tcpSocketConnection the {@link TCPSocketConnection} to create an {@link ApplicationConnection} for
	 */
	public ApplicationConnection(TCPSocketConnection tcpSocketConnection) {
		this.tcpSocketConnection = tcpSocketConnection;
	}
	
	/**
	 * returns the TCPSocketConnection connected to the application
	 * @return the TCPSocketConnection connected to the application
	 */
	public TCPSocketConnection getTCPSocketConnection() {
		return tcpSocketConnection;
	}
	
	/**
	 * returns the {@link MessageFilterCollection} attached to this {@link ApplicationConnection} (and creates is if it does not exist and true is passed)
	 * @param create true if the {@link MessageFilterCollection} should be created if it does not exist yet, false otherwise
	 * @return the {@link MessageFilterCollection}
	 */
	public MessageFilterCollection getMessageFilterCollection(boolean create) {
		if(create && this.messageFilterCollection == null) this.messageFilterCollection = new MessageFilterCollection();
		return this.messageFilterCollection;
	}
	
	/**
	 * returns the {@link MessageFilterCollection} attached to this {@link ApplicationConnection}
	 * @return the {@link MessageFilterCollection} attached to this {@link ApplicationConnection}
	 */
	public MessageFilterCollection getMessageFilterCollection() {
		return getMessageFilterCollection(false);
	}
	
	/**
	 * returns the revision to use for communication on this {@link ApplicationConnection}
	 * @return the revision to use for communication on this {@link ApplicationConnection}
	 */
	public int getRevision() {
		return revision;
	}
	
	/**
	 * sets the revision to use for communication on this {@link ApplicationConnection}
	 * @param revision the revision to use for communication on this {@link ApplicationConnection}
	 */
	public void setRevision(int revision) {
		this.revision = revision;
	}
	
	/**
	 * sets the {@link OnConnectionErrorListener} to call on a connection termination due to an error
	 * @param onConnectionErrorListener the {@link OnConnectionErrorListener} to call on a connection termination due to an error
	 */
	public void setOnConnectionErrorListener(OnConnectionErrorListener onConnectionErrorListener) {
		this.onConnectionErrorListener = onConnectionErrorListener;
	}
	
	/**
	 * returns the {@link OnConnectionErrorListener} to call on a connection termination due to an error
	 * @return the {@link OnConnectionErrorListener} to call on a connection termination due to an error
	 */
	public OnConnectionErrorListener getOnConnectionErrorListener() {
		return onConnectionErrorListener;
	}
	
	@Override
	public String toString() {
		return String.format("%s:%d", tcpSocketConnection.getInetAddress().toString(), tcpSocketConnection.getPort());
	}
}

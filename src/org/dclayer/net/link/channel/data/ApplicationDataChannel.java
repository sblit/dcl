package org.dclayer.net.link.channel.data;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.link.Link;
import org.dclayer.net.link.channel.component.DataChannelDataComponent;

/**
 * a {@link DataChannel} implementation for tunneling application data
 */
public class ApplicationDataChannel extends DataChannel {
	
	/**
	 * interface for receivers of the tunneled application data
	 */
	public static interface OnChannelDataListener {
		/**
		 * called upon receipt of application data
		 * @param applicationDataChannel the {@link ApplicationDataChannel} this was received on
		 * @param dataChannelDataComponent the {@link DataChannelDataComponent} that was received
		 */
		public void onChannelData(ApplicationDataChannel applicationDataChannel, DataChannelDataComponent dataChannelDataComponent);
	}
	
	/**
	 * the {@link DataChannelDataComponent} used for reading into
	 */
	private DataChannelDataComponent inDataChannelDataComponent = new DataChannelDataComponent();
	/**
	 * the {@link DataChannelDataComponent} used for writing from
	 */
	private DataChannelDataComponent outDataChannelDataComponent = new DataChannelDataComponent();
	
	/**
	 * {@link OnChannelDataListener} that is called upon receipt of application data
	 */
	private OnChannelDataListener onChannelDataListener;

	public ApplicationDataChannel(Link link, long channelId, String channelName) {
		super(link, channelId, channelName);
	}

	@Override
	public void readConstantly(ByteBuf byteBuf) {
		for(;;) {
			
			Log.debug(this, "receiving...");
			
			try {
				inDataChannelDataComponent.read(byteBuf);
			} catch (BufException e) {
				e.printStackTrace();
				return;
			} catch (ParseException e) {
				e.printStackTrace();
				return;
			}
			
			Log.debug(this, "received: %s", inDataChannelDataComponent.represent(true));
			
			onChannelDataListener.onChannelData(this, inDataChannelDataComponent);
			
		}
	}
	
	/**
	 * sends the given {@link Data} over this {@link ApplicationDataChannel}
	 * @param data the {@link Data} to send
	 */
	public synchronized void send(Data data) {
		Log.debug(this, "sending %d bytes of data", data.length());
		outDataChannelDataComponent.setData(data);
		send(outDataChannelDataComponent);
	}
	
	/**
	 * sets the {@link OnChannelDataListener} that is called upon receipt of application data
	 * @param onChannelDataListener the {@link OnChannelDataListener} that should be called upon receipt of application data
	 */
	public void setOnChannelDataListener(OnChannelDataListener onChannelDataListener) {
		this.onChannelDataListener = onChannelDataListener;
	}

}

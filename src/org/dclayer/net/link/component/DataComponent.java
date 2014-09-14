package org.dclayer.net.link.component;


import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.Data;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;

/**
 * a {@link PacketComponent} containing data
 */
public class DataComponent extends PacketComponent {
	/**
	 * the contained data
	 */
	private Data ownData;
	private Data data;
	
	/**
	 * the length of the data
	 */
	private FlexNum dataLength;
	
	/**
	 * creates an empty {@link DataComponent} that must first be read into before it can be written from
	 */
	public DataComponent() {
		this.dataLength = new FlexNum();
	}
    
    /**
     * returns the {@link Data} holding the data contained in this {@link DataComponent}
     * @return the {@link Data} holding the data contained in this {@link DataComponent}
     */
    public Data getData() {
    	return data;
    }
	
    /**
     * sets the Data that is contained in this {@link DataComponent}
     * @param data the Data that should be contained in this {@link DataComponent}
     */
	public void setData(Data data) {
		this.data = data;
		this.dataLength.setNum(data.length());
	}

	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		dataLength.read(byteBuf);

		if(ownData == null) {
			ownData = new Data((int) dataLength.getNum());
		} else {
			ownData.prepare((int) dataLength.getNum());
		}
		byteBuf.read(ownData);
		data = ownData;
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		dataLength.write(byteBuf);
		byteBuf.write(data);
	}

	@Override
	public int length() {
		return dataLength.length() + data.length();
	}

	@Override
	public String toString() {
		return String.format("DataComponent(len=%d)", data.length());
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}
}

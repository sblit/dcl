package org.dclayer.net.link.bmcp.component;
import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.net.PacketComponent;
import org.dclayer.net.buf.ByteBuf;
import org.dclayer.net.component.FlexNum;

/**
 * a part of different BMCP commands, describing a block/range of data ids
 */
public class BMCPIdBlock extends PacketComponent {
	
	/**
	 * the first data id in the block
	 */
	private FlexNum startId = new FlexNum();
	/**
	 * the amount of data ids between the first (i.e. {@link BMCPIdBlock#startId}) and the last data id inside this block.<br />
	 * (thus, startId 42 and innerSize 0 describe a range of 42 to 43, inclusive)
	 */
	private FlexNum innerSize = new FlexNum();
	
	@Override
	public void read(ByteBuf byteBuf) throws ParseException, BufException {
		startId.read(byteBuf);
		innerSize.read(byteBuf);
	}

	@Override
	public void write(ByteBuf byteBuf) throws BufException {
		startId.write(byteBuf);
		innerSize.write(byteBuf);
	}

	@Override
	public PacketComponent[] getChildren() {
		return null;
	}

	@Override
	public int length() {
		return startId.length() + innerSize.length();
	}
	
	@Override
	public String toString() {
		return String.format("IdBlock(startId=%d, innerSize=%d, range: %d - %d)", startId.getNum(), innerSize.getNum(), startId.getNum(), startId.getNum()+innerSize.getNum()+1);
	}
	
	/**
	 * sets the start data id of this data id block
	 * @param startId the first data id in the block
	 */
	public void setStartId(long startId) {
		this.startId.setNum(startId);
	}
	
	/**
	 * sets the amount of data ids between the first (i.e. {@link BMCPIdBlock#getStartId()}) and the last data id inside this block.<br />
	 * (thus, startId 42 and innerSize 0 describe a range of 42 to 43, inclusive)
	 * @param innerSize the innerSize value of this {@link BMCPIdBlock}
	 */
	public void setInnerSize(long innerSize) {
		this.innerSize.setNum(innerSize);
	}
	
	/**
	 * @return the start data id (i.e. the first data id included in this block) of this {@link BMCPIdBlock}
	 */
	public long getStartId() {
		return startId.getNum();
	}
	
	/**
	 * returns the amount of data ids between the first (i.e. {@link BMCPIdBlock#getStartId()}) and the last data id inside this block.<br />
	 * (thus, startId 42 and innerSize 0 describe a range of 42 to 43, inclusive)
	 * @return the innerSize value of this {@link BMCPIdBlock}
	 */
	public long getInnerSize() {
		return innerSize.getNum();
	}

}

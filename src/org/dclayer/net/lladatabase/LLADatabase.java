package org.dclayer.net.lladatabase;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.dclayer.exception.net.buf.BufException;
import org.dclayer.exception.net.parse.ParseException;
import org.dclayer.meta.Log;
import org.dclayer.net.Data;
import org.dclayer.net.buf.DataByteBuf;
import org.dclayer.net.llacache.LLA;

/**
 * Database for permanently storing lower-level addresses
 * @author Martin Exner
 */
public class LLADatabase {
	
	// TODO implement this properly
	
	private HashSet<Data> llaDatas = new HashSet<Data>();
	
	/**
	 * permanently stores the given LLA (having it serialized first, thus not storing the LLA object itself)
	 * @param lla the LLA to store
	 */
	public synchronized void store(LLA lla) {
		Data data = lla.getData();
		llaDatas.add(data);
	}
	
	/**
	 * permanently stores the LLAs in the given list (having them serialized first, thus not storing the LLA objects themselves)
	 * @param llas the list of LLAs to store
	 */
	public synchronized void store(List<LLA> llas) {
		for(LLA lla : llas) {
			this.store(lla);
		}
	}
	
	public synchronized List<LLA> getLLAs() {
		List<LLA> llas = new LinkedList<LLA>();
		DataByteBuf dataByteBuf = new DataByteBuf();
		for(Data data : llaDatas) {
			dataByteBuf.setData(data);
			try {
				llas.add(LLA.fromByteBuf(dataByteBuf));
			} catch (BufException e) {
				Log.exception(this, e);
			} catch (ParseException e) {
				Log.exception(this, e);
			}
		}
		return llas;
	}
	
}

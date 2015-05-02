package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;


// TODO: Auto-generated Javadoc
/**
 * The Class NIOIrankiai.
 */
public class NIOIrankiai {
	
	/**
	 * Instantiates a new NIO irankiai.
	 */
	NIOIrankiai() {}
	
	/**
	 * Tyliai uzdary rakta ir kanala.
	 *
	 * @param raktas the raktas
	 * @param kanalas the kanalas
	 */
	public static void tyliaiUzdaryRaktaIrKanala(SelectionKey raktas, Channel kanalas) {
		tyliaiUzdarykKanala(kanalas);
		tyliaiAtsaukRakta(raktas);
	}

	/**
	 * Tyliai uzdaryk kanala.
	 *
	 * @param kanalas the kanalas
	 */
	public static void tyliaiUzdarykKanala(Channel kanalas) {
		try{
			if(kanalas != null){
				kanalas.close();
			}
		} catch (IOException e){
			
		}
	}

	/**
	 * Tyliai atsauk rakta.
	 *
	 * @param raktas the raktas
	 */
	public static void tyliaiAtsaukRakta(SelectionKey raktas) {
		try{
			if(raktas != null) raktas.cancel();
		} catch (Exception e){
			
		}
	}

	/**
	 * Kopijuok.
	 *
	 * @param buferis the buferis
	 * @return the byte buffer
	 */
	public static ByteBuffer kopijuok(ByteBuffer buferis) {
		if(buferis == null)return null;
		ByteBuffer kopija = ByteBuffer.allocate(buferis.remaining());
		kopija.put(buferis);
		kopija.flip();
		return kopija;
	}

	/**
	 * Nustatyk paketo dydi buferyje.
	 *
	 * @param buferis the buferis
	 * @param antrastesDydis the antrastes dydis
	 * @param baituSkaicius the baitu skaicius
	 * @param bigEndian the big endian
	 */
	public static void nustatykPaketoDydiBuferyje(ByteBuffer buferis, int antrastesDydis, int baituSkaicius, boolean bigEndian) {
		if(baituSkaicius < 0) throw new IllegalArgumentException("Paketo dydis ma�esnis u� 0.");
		if(antrastesDydis != 4 && baituSkaicius >> (antrastesDydis * 8) > 0){
			throw new IllegalArgumentException("Paketo dyd�io negalima �ra�yti � " + antrastesDydis + "baitus");
		}
		
		for(int i = 0; i < antrastesDydis; i++){
			int indeksas = bigEndian ? (antrastesDydis - 1 -i ) : i;
			buferis.put((byte) (baituSkaicius >> (8 * indeksas) & 0xFF));
		}
		
	}
	
	/**
	 * Sumesk i viena.
	 *
	 * @param buferiai the buferiai
	 * @param buferis the buferis
	 * @return the byte buffer[]
	 */
	public static ByteBuffer[] sumeskIViena(ByteBuffer[] buferiai, ByteBuffer buferis) {
		return sumeskIViena(buferiai, new ByteBuffer[] {buferis});
	}
	
	/**
	 * Sumesk i viena.
	 *
	 * @param buferis the buferis
	 * @param buferiai the buferiai
	 * @return the byte buffer[]
	 */
	public static ByteBuffer[] sumeskIViena(ByteBuffer buferis, ByteBuffer[] buferiai) {
		return sumeskIViena(new ByteBuffer[] {buferis}, buferiai);
	}
	
	/**
	 * Sumesk i viena.
	 *
	 * @param buferiai1 the buferiai1
	 * @param buferiai2 the buferiai2
	 * @return the byte buffer[]
	 */
	public static ByteBuffer[] sumeskIViena(ByteBuffer[] buferiai1, ByteBuffer[] buferiai2) {
		if(buferiai1 == null || buferiai1.length == 0) return buferiai2;
		if(buferiai2 == null || buferiai2.length == 0) return buferiai1;
		ByteBuffer[] naujiBuferiai = new ByteBuffer[buferiai1.length + buferiai2.length];
		System.arraycopy(buferiai1, 0, naujiBuferiai, 0, buferiai1.length);
		System.arraycopy(buferiai2, 0, naujiBuferiai, buferiai1.length, buferiai1.length);
		return naujiBuferiai;
	}

	/**
	 * Like baitai.
	 *
	 * @param buferiai the buferiai
	 * @return the long
	 */
	public static long likeBaitai(ByteBuffer[] buferiai) {
		long ilgis = 0;
		for(ByteBuffer buferis : buferiai) ilgis += buferis.remaining();
		return ilgis;
	}

	/**
	 * Gauk paketo dydi buferyje.
	 *
	 * @param antraste the antraste
	 * @param antrastesDydis the antrastes dydis
	 * @param bigEndian the big endian
	 * @return the int
	 */
	public static int gaukPaketoDydiBuferyje(ByteBuffer antraste, int antrastesDydis, boolean bigEndian) {
		long paketoDydis = 0;
		if(bigEndian){
			for(int i = 0; i < antrastesDydis; i++){
				paketoDydis <<= 8;
				paketoDydis += antraste.get() & 0xFF;
			}
		} else {
			int postumis = 0;
			for(int i = 0; i < antrastesDydis; i++){
				paketoDydis += (antraste.get() & 0xFF) << postumis;
				postumis += 8;
			}
		}
		return (int) paketoDydis;
	}
}

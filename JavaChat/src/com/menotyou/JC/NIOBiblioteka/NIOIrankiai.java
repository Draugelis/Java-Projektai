package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;


public class NIOIrankiai {
	
	NIOIrankiai() {}
	
	public static void tyliaiUzdaryRaktaIrKanala(SelectionKey raktas, Channel kanalas) {
		tyliaiUzdarykKanala(kanalas);
		tyliaiAtsaukRakta(raktas);
	}

	public static void tyliaiUzdarykKanala(Channel kanalas) {
		try{
			if(kanalas != null){
				kanalas.close();
			}
		} catch (IOException e){
			
		}
	}

	public static void tyliaiAtsaukRakta(SelectionKey raktas) {
		try{
			if(raktas != null) raktas.cancel();
		} catch (Exception e){
			
		}
	}

	public static ByteBuffer kopijuok(ByteBuffer buferis) {
		if(buferis == null)return null;
		ByteBuffer kopija = ByteBuffer.allocate(buferis.remaining());
		kopija.put(buferis);
		kopija.flip();
		return kopija;
	}

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
	public static ByteBuffer[] sumeskIViena(ByteBuffer[] buferiai, ByteBuffer buferis) {
		return sumeskIViena(buferiai, new ByteBuffer[] {buferis});
	}
	public static ByteBuffer[] sumeskIViena(ByteBuffer buferis, ByteBuffer[] buferiai) {
		return sumeskIViena(new ByteBuffer[] {buferis}, buferiai);
	}
	public static ByteBuffer[] sumeskIViena(ByteBuffer[] buferiai1, ByteBuffer[] buferiai2) {
		if(buferiai1 == null || buferiai1.length == 0) return buferiai2;
		if(buferiai2 == null || buferiai2.length == 0) return buferiai1;
		ByteBuffer[] naujiBuferiai = new ByteBuffer[buferiai1.length + buferiai2.length];
		System.arraycopy(buferiai1, 0, naujiBuferiai, 0, buferiai1.length);
		System.arraycopy(buferiai2, 0, naujiBuferiai, buferiai1.length, buferiai1.length);
		return naujiBuferiai;
	}

	public static long likeBaitai(ByteBuffer[] buferiai) {
		long ilgis = 0;
		for(ByteBuffer buferis : buferiai) ilgis += buferis.remaining();
		return ilgis;
	}

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

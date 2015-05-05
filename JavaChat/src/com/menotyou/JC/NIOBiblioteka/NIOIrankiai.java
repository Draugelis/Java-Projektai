package com.menotyou.JC.NIOBiblioteka;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;

/**
 * Klasė skirta talpinti NIOAptarnavimui, bei kitoms NIOBibliotekos klasėms
 * reikalingas funkcijas.
 * 
 *  Kas yra big endian?
 *  Tai baitų rašymo formatas
 *  kur baitai pagal svarbumą(reikšmę) rašomi iš kairės į dešinę
 *  arba iš dešinės į kairę.
 *  Big Endian pvz:
 *      1000 0000 = 2^7 = 128
 * 	Little Endian pvz:
 *      1000 0000 = 2^0 = 1
 * 
 */
public class NIOIrankiai {

    NIOIrankiai() {
    }

    /**
     * Tyliai uždaromas raktas ir kanalas.
     * T.y. jei bet kokios iššimtys atsiradusios uždarymo metu yra ignoruojamos.
     * @param raktas -> uždatomas raktas.
     * @param kanalas -> uždaromas kanalas.
     */
    public static void tyliaiUzdarykRaktaIrKanala(SelectionKey raktas, Channel kanalas) {
        tyliaiUzdarykKanala(kanalas);
        tyliaiAtsaukRakta(raktas);
    }

    public static void tyliaiUzdarykKanala(Channel kanalas) {
        try {
            if (kanalas != null) {
                kanalas.close();
            }
        } catch (IOException e) {

        }
    }

    public static void tyliaiAtsaukRakta(SelectionKey raktas) {
        try {
            if (raktas != null) raktas.cancel();
        } catch (Exception e) {

        }
    }

    /**
     * Funkcija kopijuoja visą nurodyto buferio turinį ir grąžina jį kaip
     * rezultatą.
     *
     * @param buferis -> kopijuojamas buferis
     * @return kopijuojamo buferio turinys.
     */
    public static ByteBuffer kopijuok(ByteBuffer buferis) {
        if (buferis == null) return null;
        ByteBuffer kopija = ByteBuffer.allocate(buferis.remaining());
        kopija.put(buferis);
        kopija.flip();
        return kopija;
    }

    /**
     * Šis metodas į pirmus 1,2,3 ar 4 masyvo baitus įrašo siunčiamo paketo
     * baitų skaičių.
     * Jei naudojamas 1 baitas, maximalus paketo dydis yra 255 baitai.
     * Jei naudojami 2 baitai, maximalus paketo dydis yra 65533 baitai.
     * @param buferis -> Buferis į kurį surašomas paketo dydis.
     * @param antrastesDydis -> skaičius nurodantis keik baitu naudojama antraštei.
     * @param baituSkaicius -> skaičius kurį reikia paveristi į kelis ar vieną baitą.
     * @param bigEndian -> nurodymas algoritmui kokia sistema yra reika koduoti baitus.
     * 
     */
    public static void nustatykPaketoDydiBuferyje(ByteBuffer buferis, int antrastesDydis, int baituSkaicius, boolean bigEndian) {
        if (baituSkaicius < 0) throw new IllegalArgumentException("Paketo dydis mažesnis už 0.");
        if (antrastesDydis != 4 && baituSkaicius >> (antrastesDydis * 8) > 0) {
            throw new IllegalArgumentException("Paketo dydžio negalima įrašyti į " + antrastesDydis + "baitus");
        }

        for (int i = 0; i < antrastesDydis; i++) {
            int indeksas = bigEndian ? (antrastesDydis - 1 - i) : i;
            buferis.put((byte) (baituSkaicius >> (8 * indeksas) & 0xFF));
        }

    }

    /**
     * Funkcija sujungia kelis ByteBuffer objektus į vieną.
     * Jei vienas ar kitas paramteras nurodomas kaip vienas objektas,
     * tai jis pirma paverčiamas masyvu, o tuomet perduodamas masyvus sujungainčiai
     * funcijai
     */
    public static ByteBuffer[] sumeskIViena(ByteBuffer[] buferiai, ByteBuffer buferis) {
        return sumeskIViena(buferiai, new ByteBuffer[] { buferis });
    }

    public static ByteBuffer[] sumeskIViena(ByteBuffer buferis, ByteBuffer[] buferiai) {
        return sumeskIViena(new ByteBuffer[] { buferis }, buferiai);
    }

    public static ByteBuffer[] sumeskIViena(ByteBuffer[] buferiai1, ByteBuffer[] buferiai2) {
        if (buferiai1 == null || buferiai1.length == 0) return buferiai2;
        if (buferiai2 == null || buferiai2.length == 0) return buferiai1;
        ByteBuffer[] naujiBuferiai = new ByteBuffer[buferiai1.length + buferiai2.length];
        System.arraycopy(buferiai1, 0, naujiBuferiai, 0, buferiai1.length);
        System.arraycopy(buferiai2, 0, naujiBuferiai, buferiai1.length, buferiai1.length);
        return naujiBuferiai;
    }

    /**
     * Funkicija gražina buferyje likusiuų baitų skaičių.
     *
     * @param buferiai -> buferiai kurių baitus reikia skaičiuoti.
     * @return baitų skaičius.
     */
    public static long likeBaitai(ByteBuffer[] buferiai) {
        long ilgis = 0;
        for (ByteBuffer buferis : buferiai)
            ilgis += buferis.remaining();
        return ilgis;
    }

    /**
     * Atvirkštinė funkcija funcijai nustatykPaketoDydiBuferyje().
     * Ji iš nurodytų pirmų baitų nustato kiokio ilgio paketo reikai tikėtis.
     *
     * @param antraste -> Buferis į kurį surašomas paketo dydis.
     * @param antrastesDydis -> skaičius nurodantis keik baitu naudojama antraštei.
     * @param bigEndian -> nurodymas algoritmui kokia sistema yra reika koduoti baitus.
     * @return paketo dydis baitais.
     */
    public static int gaukPaketoDydiBuferyje(ByteBuffer antraste, int antrastesDydis, boolean bigEndian) {
        long paketoDydis = 0;
        if (bigEndian) {
            for (int i = 0; i < antrastesDydis; i++) {
                paketoDydis <<= 8;
                paketoDydis += antraste.get() & 0xFF;
            }
        } else {
            int postumis = 0;
            for (int i = 0; i < antrastesDydis; i++) {
                paketoDydis += (antraste.get() & 0xFF) << postumis;
                postumis += 8;
            }
        }
        return (int) paketoDydis;
    }
}

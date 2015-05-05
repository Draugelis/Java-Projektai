package com.menotyou.JC.Serveris;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

/**
 * Klasė kuri talpina pagrindinius slaptažodžių hash'avimo algoritmus.
 */
public final class VartotojoAutentifikacija {

    private static VartotojoAutentifikacija VA;
    private final static int ITERACIJU_SKAICIUS = 1000;

    /**
     * Sukuriamas naujas VA objektas.
     * Parametrų nera nes šios klasės paskirtis yra slaptažodžių
     * hash'avimas.
     */
    private VartotojoAutentifikacija() {

    }

    /**
     * Numatyta funkcija, kurios iteracijų skaičius yra iš anksto apibrėžtas.
     *
     * @param slaptazodis -> tekstas kurį norima už'hash'uoti.
     * @param druska -> nurodyta vartotojo druska arba random bitu seka.
     * @param algoritmas -> hash'avimo algoritmas.
     * @return galutinis algoritmo rezultatas paverstas į šešioliktainį formatą.
     * @throws NoSuchAlgorithmException -> Išimtis nutinka neradus nurodyto algoritmo.
     * @throws DecoderException -> Išimtis nutinka jei dekoduojant įvyksta klaida.
     */
    public String UzkoduokSlaptazodi(String slaptazodis, String druska, String algoritmas) throws NoSuchAlgorithmException, DecoderException {
        return UzkoduokSlaptazodi(slaptazodis, druska, algoritmas, ITERACIJU_SKAICIUS);
    }

    /**
     * Funkcija naudoja nurodyta hash'avimo algoritmą ir užkoduoja duotą tekstą.
     * Naudojamasi MessageDigest klase kuri yra java.security paketo dalis.
     *
     * @param slaptazodis -> tekstas kurį norima už'hash'uoti.
     * @param druska -> nurodyta vartotojo druska arba random bitu seka.
     * @param algoritmas -> hash'avimo algoritmas.
     * @param It_skaicius -> siūlomas iteracijų skaičius.
     * @return galutinis algoritmo rezultatas paverstas į šešioliktainį formatą.
     * @throws NoSuchAlgorithmException -> Išimtis nutinka neradus nurodyto algoritmo.
     * @throws DecoderException -> Išimtis nutinka jei dekoduojant įvyksta klaida.
     */
    public String UzkoduokSlaptazodi(String slaptazodis, String druska, String algoritmas, int It_skaicius) throws NoSuchAlgorithmException, DecoderException {
        MessageDigest digest = MessageDigest.getInstance(algoritmas);
        digest.reset();
        byte[] ivestis = null;
        try {
            ivestis = digest.digest((slaptazodis + druska).getBytes("UTF-8"));
            for (int i = 0; i < It_skaicius; i++) {
                digest.reset();
                ivestis = digest.digest(ivestis);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return Hex.encodeHexString(ivestis);
    }

    /** 
     * Gražinamas VartotojoAutentifikacija objektas, taip užtikrinama
     * kad bet kuriuo momentu toks bus tik vienas, todėl taupomi
     * kompiuterio resursai.
     * @return VartotojoAutentifikacija objektas.
     */
    public static VartotojoAutentifikacija gaukVAValdikli() {
        if (VA == null) VA = new VartotojoAutentifikacija();
        return VA;
    }
}

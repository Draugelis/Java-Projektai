package com.menotyou.JC.Serveris;

import java.io.IOException;

import com.menotyou.JC.NIOBiblioteka.NIOServerioSasaja;
import com.menotyou.JC.NIOBiblioteka.PrisijungimuFiltras;
import com.menotyou.JC.NIOBiblioteka.EventuValdiklis.EventuValdiklis;

/**
 * Klasė ataskinga už serverio ar kelių serverių paleidimą.
 * Šiai klasei per konsolę galima nurodyti per kurį porta kurti serverį.
 */
public class PagrindinisServeris {

    private final static int NUMATYTASIS_PORTAS = 8192;

    public static void main(String[] args) {
        int port;
        if (args.length > 1) {
            System.out.println("Naudojimas: java -jar ChatServer.jar [port] [t/f]- (Komndų konsolė įjungta)");
            return;
        } else if (args.length == 1) {
            port = Integer.parseInt(args[0]);
            paleiskServeri(port, false);
        } else {
            System.out.println("Portas nenurodytas, paleidžiama per numatytajį portą:" + NUMATYTASIS_PORTAS);
            paleiskServeri(NUMATYTASIS_PORTAS, false);
        }
    }

    /**
     * Paleisk serveri.
     *
     * @param portas -> Portas ties kurio reikia sukurti serverį.
     * @param konsoleIjungta -> kintamasis nurodantis ar severio konsolė bus įjungta.
     * Deja, šiuo metu konsolės rėžimas nėra įdiegtas.
     */
    private static void paleiskServeri(int portas, boolean konsoleIjungta) {
        try {
            EventuValdiklis eventuValdiklis = new EventuValdiklis();
            NIOServerioSasaja sasaja = eventuValdiklis.gaukNIOAptarnavima().sukurkServerioSasaja(portas);
            sasaja.stebek(new JCServeris(eventuValdiklis));
            sasaja.nustatykPrisijungimuFiltra(PrisijungimuFiltras.LEISK_VISUS);
            eventuValdiklis.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
